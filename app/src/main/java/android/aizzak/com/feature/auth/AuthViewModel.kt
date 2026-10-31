package android.aizzak.com.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class AuthStage {
    RESTORING, SIGNED_OUT, NEEDS_VERIFICATION,
    LOADING_CONTEXT, CONTEXT_UNAVAILABLE, ACCESS_DENIED, READY,
}
enum class AuthAction {
    SIGN_IN, SIGN_UP, RESET_PASSWORD, RESEND_VERIFICATION, REFRESH_IDENTITY, CHANGE_PASSWORD, SIGN_OUT,
}
enum class AuthMessage { RESET_REQUESTED, VERIFICATION_SENT, EMAIL_NOT_VERIFIED, PASSWORD_CHANGED }

data class AuthUiState(
    val stage: AuthStage = AuthStage.RESTORING,
    val identity: AuthIdentity? = null,
    val busy: Boolean = false,
    val action: AuthAction? = null,
    val error: AuthError? = null,
    val message: AuthMessage? = null,
    val context: AccountContext? = null,
    val contextError: AccountContextError? = null,
    val correlationId: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val contexts: AccountContextRepository = UnavailableAccountContextRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()
    private var operation: Job? = null
    private var contextJob: Job? = null
    private var generation = 0L
    private var contextGeneration = 0L
    private var observedUid: String? = null
    private var operationBusy = false
    private var signingOut = false

    init {
        viewModelScope.launch {
            try {
                repository.session.collect { if (!signingOut) acceptSession(it) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                invalidateContext()
                generation++
                operation?.cancel()
                operationBusy = false
                observedUid = null
                mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT, error = AuthError.SERVICE_UNAVAILABLE)
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (!validateCredentials(email, password)) return
        submit(AuthAction.SIGN_IN) {
            repository.signIn(email.trim(), password)
            AuthOutcome()
        }
    }

    fun signUp(email: String, password: String, confirmation: String) {
        if (!validateCredentials(email, password)) return
        if (password != confirmation) { showError(AuthError.PASSWORD_MISMATCH); return }
        submit(AuthAction.SIGN_UP) {
            repository.signUp(email.trim(), password)
            AuthOutcome(message = AuthMessage.VERIFICATION_SENT)
        }
    }

    fun resetPassword(email: String) {
        if (!validEmail(email)) { showError(AuthError.INVALID_EMAIL); return }
        submit(AuthAction.RESET_PASSWORD) {
            repository.sendPasswordReset(email.trim())
            AuthOutcome(message = AuthMessage.RESET_REQUESTED)
        }
    }

    fun resendVerification() = submit(AuthAction.RESEND_VERIFICATION) {
        repository.resendVerification()
        AuthOutcome(message = AuthMessage.VERIFICATION_SENT)
    }

    fun refreshIdentity() = submit(AuthAction.REFRESH_IDENTITY) {
        val contextBeforeRefresh = contextGeneration
        val identity = repository.refreshIdentity()
        AuthOutcome(
            identity = identity, replaceIdentity = true, contextBeforeRefresh = contextBeforeRefresh,
            message = if (identity != null && !identity.emailVerified) AuthMessage.EMAIL_NOT_VERIFIED else null,
        )
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmation: String) {
        val state = mutableUiState.value
        if (state.busy || state.stage != AuthStage.READY || state.context == null) return
        if (currentPassword.isEmpty() || newPassword.isEmpty()) { showError(AuthError.EMPTY_PASSWORD); return }
        if (newPassword != confirmation) { showError(AuthError.PASSWORD_MISMATCH); return }
        submit(AuthAction.CHANGE_PASSWORD) {
            repository.changePassword(currentPassword, newPassword)
            AuthOutcome(message = AuthMessage.PASSWORD_CHANGED)
        }
    }

    // The current backend logout blocks the Firebase subject across devices and fresh logins.
    fun signOut() = startSignOut(remote = false)

    fun clearFeedback() {
        mutableUiState.update { it.copy(error = null, message = null) }
    }

    private fun acceptSession(session: AuthSession) {
        val nextUid = (session as? AuthSession.SignedIn)?.identity?.uid
        if (observedUid != null && observedUid != nextUid) {
            generation++
            operation?.cancel()
            operationBusy = false
            mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT)
            invalidateContext()
        }
        observedUid = nextUid
        when (session) {
            AuthSession.Restoring, AuthSession.SignedOut -> {
                invalidateContext()
                mutableUiState.update {
                    it.copy(
                        stage = if (session == AuthSession.Restoring) AuthStage.RESTORING else AuthStage.SIGNED_OUT,
                        identity = null, context = null, contextError = null, correlationId = null,
                        busy = operationBusy, action = if (operationBusy) it.action else null,
                    )
                }
            }
            is AuthSession.SignedIn -> acceptIdentity(session.identity)
        }
    }

    private fun acceptIdentity(identity: AuthIdentity) {
        val previous = mutableUiState.value
        val alreadyVerified = previous.identity?.let { it.uid == identity.uid && it.emailVerified } == true
        mutableUiState.update { it.copy(identity = identity) }
        if (!identity.emailVerified) {
            invalidateContext()
            mutableUiState.update {
                it.copy(stage = AuthStage.NEEDS_VERIFICATION, context = null, contextError = null,
                    correlationId = null, busy = operationBusy)
            }
        } else if (!alreadyVerified) {
            loadContext(identity.uid)
        }
    }

    private fun loadContext(uid: String) {
        invalidateContext()
        val expectedContext = contextGeneration
        val expectedAccount = generation
        mutableUiState.update {
            it.copy(stage = AuthStage.LOADING_CONTEXT, context = null, contextError = null,
                correlationId = null, error = null, busy = true)
        }
        contextJob = viewModelScope.launch {
            try {
                val context = contexts.load(uid)
                currentCoroutineContext().ensureActive()
                if (isCurrentContext(uid, expectedAccount, expectedContext)) {
                    mutableUiState.update {
                        it.copy(stage = AuthStage.READY, context = context, contextError = null,
                            correlationId = null, busy = operationBusy)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: AccountContextFailure) {
                if (isCurrentContext(uid, expectedAccount, expectedContext)) {
                    if (failure.error == AccountContextError.UNAUTHENTICATED) {
                        startSignOut(remote = false, feedback = AuthError.SESSION_EXPIRED)
                    } else {
                        failContext(failure.error, failure.correlationId)
                    }
                }
            } catch (_: Exception) {
                if (isCurrentContext(uid, expectedAccount, expectedContext)) {
                    failContext(AccountContextError.UNAVAILABLE, null)
                }
            }
        }
    }

    private fun failContext(error: AccountContextError, correlationId: String?) {
        mutableUiState.update {
            it.copy(stage = if (error == AccountContextError.FORBIDDEN) AuthStage.ACCESS_DENIED else AuthStage.CONTEXT_UNAVAILABLE,
                context = null, contextError = error, correlationId = correlationId, busy = operationBusy)
        }
    }

    private fun isCurrentContext(uid: String, account: Long, context: Long): Boolean =
        !signingOut && generation == account && contextGeneration == context &&
            mutableUiState.value.identity?.let { it.uid == uid && it.emailVerified } == true

    private fun invalidateContext() {
        contextGeneration++
        contextJob?.cancel()
        contextJob = null
    }

    private fun startSignOut(remote: Boolean, feedback: AuthError? = null) {
        if (signingOut) return
        val identity = mutableUiState.value.identity
        generation++
        operation?.cancel()
        invalidateContext()
        operationBusy = false
        signingOut = true
        mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT, busy = true,
            action = AuthAction.SIGN_OUT, error = feedback)
        // Register cleanup immediately, before a queued launch could be cancelled.
        operation = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            var localFailure: AuthError? = null
            try {
                if (remote && identity?.emailVerified == true) {
                    try {
                        withTimeoutOrNull(LOGOUT_TIMEOUT_MS) { contexts.logout(identity.uid) }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        // A failed server request must never retain the local account.
                    }
                }
            } finally {
                try {
                    withContext(NonCancellable) { repository.signOut() }
                } catch (_: Exception) {
                    localFailure = AuthError.SERVICE_UNAVAILABLE
                } finally {
                    observedUid = null
                    signingOut = false
                    mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT, error = localFailure ?: feedback)
                }
            }
        }
    }

    private fun submit(action: AuthAction, block: suspend () -> AuthOutcome) {
        if (mutableUiState.value.busy || signingOut) return
        val expected = generation
        operationBusy = true
        mutableUiState.update { it.copy(busy = true, action = action, error = null, message = null) }
        operation = viewModelScope.launch {
            try {
                val outcome = block()
                if (expected == generation && !signingOut) {
                    if (outcome.replaceIdentity) {
                        val identity = outcome.identity
                        if (identity == null) acceptSession(AuthSession.SignedOut)
                        else {
                            observedUid = identity.uid
                            acceptIdentity(identity)
                            if (identity.emailVerified && outcome.contextBeforeRefresh == contextGeneration) {
                                loadContext(identity.uid)
                            }
                        }
                    }
                    if (expected == generation) mutableUiState.update { it.copy(message = outcome.message) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: AuthFailure) {
                if (expected == generation) showError(failure.error)
            } catch (_: Exception) {
                if (expected == generation) showError(AuthError.UNKNOWN)
            } finally {
                if (expected == generation && !signingOut) {
                    operationBusy = false
                    mutableUiState.update { it.copy(busy = it.stage == AuthStage.LOADING_CONTEXT, action = null) }
                }
            }
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (mutableUiState.value.busy || signingOut) return false
        if (!validEmail(email)) { showError(AuthError.INVALID_EMAIL); return false }
        if (password.isEmpty()) { showError(AuthError.EMPTY_PASSWORD); return false }
        return true
    }

    private fun showError(error: AuthError) {
        mutableUiState.update { it.copy(error = error, message = null) }
    }

    private fun validEmail(email: String): Boolean = EMAIL.matches(email.trim())

    private data class AuthOutcome(
        val message: AuthMessage? = null,
        val replaceIdentity: Boolean = false,
        val identity: AuthIdentity? = null,
        val contextBeforeRefresh: Long? = null,
    )

    private companion object {
        const val LOGOUT_TIMEOUT_MS = 5_000L
        val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
