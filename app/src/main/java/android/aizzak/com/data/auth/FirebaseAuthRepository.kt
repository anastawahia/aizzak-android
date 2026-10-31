package android.aizzak.com.data.auth

import android.aizzak.com.core.network.IdTokenProvider
import android.aizzak.com.feature.auth.AuthError
import android.aizzak.com.feature.auth.AuthFailure
import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthRepository
import android.aizzak.com.feature.auth.AuthSession
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.EmailAuthProvider
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Firebase owns persisted credentials; this adapter never stores ID tokens or passwords. */
class FirebaseAuthRepository internal constructor(private val gateway: FirebaseAuthGateway) : AuthRepository, IdTokenProvider {
    constructor(auth: FirebaseAuth) : this(SdkFirebaseAuthGateway(auth))

    private val credentialMutex = Mutex()
    private val tokenMutex = Mutex()
    private val stateLock = Any()
    private var generation = 0L
    private var credentialInFlight = false
    private var reauthentication: Reauthentication? = null
    private var tokenRefreshVersion = 0L
    private val changes = MutableStateFlow(0L)

    override val session: Flow<AuthSession> = callbackFlow {
        trySend(AuthSession.Restoring)
        val restored = AtomicBoolean(false)
        val registration = gateway.observe {
            restored.set(true)
            trySend(snapshot())
        }
        val updates = launch {
            changes.collect { if (restored.get()) trySend(snapshot()) }
        }
        awaitClose {
            updates.cancel()
            registration.close()
        }
    }

    override suspend fun signIn(email: String, password: String) = credentialMutation {
        try {
            gateway.signIn(email, password)
        } catch (failure: AuthFailure) {
            if (failure.error == AuthError.SESSION_EXPIRED) throw AuthFailure(AuthError.INVALID_CREDENTIALS)
            throw failure
        }
    }

    override suspend fun signUp(email: String, password: String) = credentialMutation { isCurrent ->
        gateway.signUp(email, password)
        if (isCurrent()) gateway.resendVerification()
    }

    override suspend fun sendPasswordReset(email: String) = gateway.sendPasswordReset(email)

    override suspend fun resendVerification() {
        if (gateway.identity == null) throw AuthFailure(AuthError.SESSION_EXPIRED)
        gateway.resendVerification()
    }

    override suspend fun refreshIdentity(): AuthIdentity? {
        val expected = synchronized(stateLock) { generation }
        val uid = gateway.identity?.uid ?: return null
        gateway.reload()
        val identity = gateway.identity ?: throw AuthFailure(AuthError.SESSION_EXPIRED)
        if (expected != synchronized(stateLock) { generation } || identity.uid != uid) {
            throw CancellationException("Identity changed")
        }
        if (identity.emailVerified) getIdToken(uid, forceRefresh = true)
        currentCoroutineContext().ensureActive()
        if (expected != synchronized(stateLock) { generation } || gateway.identity?.uid != uid) {
            throw CancellationException("Identity changed")
        }
        changes.update { it + 1 }
        return identity
    }

    override suspend fun getIdToken(expectedUid: String, forceRefresh: Boolean): String {
        val expected = captureAccount(expectedUid)
        val refreshVersion = synchronized(stateLock) { tokenRefreshVersion }
        return tokenMutex.withLock {
            checkAccount(expectedUid, expected)
            // Concurrent forced callers share a refresh completed while waiting.
            val refresh = forceRefresh && refreshVersion == synchronized(stateLock) { tokenRefreshVersion }
            val token = gateway.getIdToken(refresh)
            currentCoroutineContext().ensureActive()
            checkAccount(expectedUid, expected)
            if (token.isBlank()) throw AuthFailure(AuthError.SESSION_EXPIRED)
            if (refresh) synchronized(stateLock) { tokenRefreshVersion++ }
            token
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val caller = currentCoroutineContext()
        credentialMutex.withLock {
            caller.ensureActive()
            val uid = gateway.identity?.uid ?: throw AuthFailure(AuthError.SESSION_EXPIRED)
            val expected = captureAccount(uid)
            synchronized(stateLock) { reauthentication = Reauthentication(uid, expected) }
            // Reauthentication can complete in the SDK after its caller is
            // cancelled. Keep the credential lane until it settles.
            withContext(NonCancellable) {
                try {
                    gateway.reauthenticate(currentPassword)
                    if (caller.isActive) {
                        checkAccount(uid, expected)
                        gateway.updatePassword(newPassword)
                    }
                } finally {
                    val stale = expected != synchronized(stateLock) { generation }
                    try {
                        if (stale && gateway.identity?.uid == uid) gateway.signOut()
                    } finally {
                        synchronized(stateLock) { reauthentication = null }
                        changes.update { it + 1 }
                    }
                }
            }
            caller.ensureActive()
            checkAccount(uid, expected)
        }
    }

    private fun captureAccount(expectedUid: String): Long = synchronized(stateLock) {
        val identity = gateway.identity ?: throw AuthFailure(AuthError.SESSION_EXPIRED)
        if (credentialInFlight || identity.uid != expectedUid) throw CancellationException("Identity changed")
        if (!identity.emailVerified) throw AuthFailure(AuthError.SESSION_EXPIRED)
        generation
    }

    private fun checkAccount(expectedUid: String, expectedGeneration: Long) {
        val actual = captureAccount(expectedUid)
        if (actual != expectedGeneration) throw CancellationException("Identity changed")
    }

    override suspend fun signOut() {
        synchronized(stateLock) { generation++ }
        gateway.signOut()
        changes.update { it + 1 }
    }

    private fun snapshot(): AuthSession = synchronized(stateLock) {
        val identity = gateway.identity
        val lateReauthentication = reauthentication?.let {
            it.generation != generation && it.uid == identity?.uid
        } == true
        // An SDK reauthentication may restore and notify the old identity before
        // its await returns to our cleanup. Suppress that superseded identity at
        // the publication boundary, while keeping normal password changes signed in.
        if (credentialInFlight || lateReauthentication) AuthSession.SignedOut
        else identity?.let { AuthSession.SignedIn(it) } ?: AuthSession.SignedOut
    }

    private data class Reauthentication(val uid: String, val generation: Long)

    private suspend fun credentialMutation(block: suspend (() -> Boolean) -> Unit) {
        val caller = currentCoroutineContext()
        credentialMutex.withLock {
            caller.ensureActive()
            val expected = synchronized(stateLock) {
                credentialInFlight = true
                ++generation
            }
            changes.update { it + 1 }
            // Cancelling Task.await does not cancel Firebase's credential mutation.
            // Keep this serialized section until the SDK task settles, then remove a
            // late identity before exposing state or admitting another credential task.
            withContext(NonCancellable) {
                try {
                    block { expected == synchronized(stateLock) { generation } && caller.isActive }
                } finally {
                    val stale = expected != synchronized(stateLock) { generation } || !caller.isActive
                    try {
                        if (stale) gateway.signOut()
                    } finally {
                        synchronized(stateLock) { credentialInFlight = false }
                        changes.update { it + 1 }
                    }
                }
            }
            caller.ensureActive()
            if (expected != synchronized(stateLock) { generation }) {
                throw CancellationException("Credential operation superseded")
            }
        }
    }
}

/** Narrow SDK seam for deterministic tests of late, non-cancellable credential completion. */
internal interface FirebaseAuthGateway {
    val identity: AuthIdentity?
    fun observe(onChanged: () -> Unit): AutoCloseable
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun resendVerification()
    suspend fun reload()
    suspend fun getIdToken(forceRefresh: Boolean): String
    suspend fun reauthenticate(currentPassword: String)
    suspend fun updatePassword(newPassword: String)
    fun signOut()
}

private class SdkFirebaseAuthGateway(private val auth: FirebaseAuth) : FirebaseAuthGateway {
    override val identity: AuthIdentity?
        get() = auth.currentUser?.toIdentity()

    override fun observe(onChanged: () -> Unit): AutoCloseable {
        val listener = FirebaseAuth.AuthStateListener { onChanged() }
        auth.addAuthStateListener(listener)
        return AutoCloseable { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String) = mapped {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun signUp(email: String, password: String) = mapped {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun sendPasswordReset(email: String) {
        mapped {
            try {
                auth.sendPasswordResetEmail(email).await()
            } catch (failure: FirebaseAuthException) {
                // Avoid revealing account existence even where enumeration protection is disabled.
                if (failure.errorCode != "ERROR_USER_NOT_FOUND") throw failure
            }
        }
    }

    override suspend fun resendVerification() = mapped { currentUser().sendEmailVerification().await(); Unit }
    override suspend fun reload() = mapped { currentUser().reload().await(); Unit }
    override suspend fun getIdToken(forceRefresh: Boolean): String = mapped {
        currentUser().getIdToken(forceRefresh).await().token
            ?.takeIf { it.isNotBlank() } ?: throw AuthFailure(AuthError.SESSION_EXPIRED)
    }
    override suspend fun reauthenticate(currentPassword: String) = mapped {
        val user = currentUser()
        val email = user.email ?: throw AuthFailure(AuthError.SESSION_EXPIRED)
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
        Unit
    }
    override suspend fun updatePassword(newPassword: String) = mapped {
        currentUser().updatePassword(newPassword).await()
        Unit
    }

    override fun signOut() = auth.signOut()

    private fun currentUser(): FirebaseUser =
        auth.currentUser ?: throw AuthFailure(AuthError.SESSION_EXPIRED)

    private suspend fun <T> mapped(block: suspend () -> T): T = try {
        block()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: AuthFailure) {
        throw failure
    } catch (failure: Exception) {
        throw AuthFailure(mapFirebaseError(failure))
    }

    private fun FirebaseUser.toIdentity() = AuthIdentity(uid, email, isEmailVerified)
}

internal fun mapFirebaseError(failure: Exception): AuthError = when (failure) {
    is FirebaseNetworkException -> AuthError.NETWORK
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
    is FirebaseAuthException -> when (failure.errorCode) {
        "ERROR_INVALID_EMAIL" -> AuthError.INVALID_EMAIL
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_LOGIN_CREDENTIALS" -> AuthError.INVALID_CREDENTIALS
        "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> AuthError.EMAIL_IN_USE
        "ERROR_WEAK_PASSWORD" -> AuthError.WEAK_PASSWORD
        "ERROR_USER_DISABLED" -> AuthError.USER_DISABLED
        "ERROR_USER_NOT_FOUND", "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "ERROR_REQUIRES_RECENT_LOGIN" -> AuthError.SESSION_EXPIRED
        "ERROR_OPERATION_NOT_ALLOWED", "ERROR_APP_NOT_AUTHORIZED", "ERROR_INVALID_API_KEY" -> AuthError.SERVICE_UNAVAILABLE
        "ERROR_TOO_MANY_REQUESTS" -> AuthError.TOO_MANY_REQUESTS
        "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NETWORK
        else -> AuthError.UNKNOWN
    }
    else -> AuthError.UNKNOWN
}
