package android.aizzak.com.data.auth

import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthSession
import android.aizzak.com.feature.auth.AuthError
import android.aizzak.com.feature.auth.AuthFailure
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthViewModel
import android.aizzak.com.feature.auth.AccountContext
import android.aizzak.com.feature.auth.AccountContextRepository
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthRepositoryTest {
    @Test fun restorationWaitsForFirstSdkCallbackRatherThanReadingEarlyNull() = runTest {
        val gateway = FakeGateway().apply { notifyImmediately = false }
        val repository = FirebaseAuthRepository(gateway)
        val sessions = mutableListOf<AuthSession>()
        backgroundScope.launch { repository.session.collect { sessions += it } }
        runCurrent()
        assertEquals(listOf(AuthSession.Restoring), sessions)
        gateway.identity = AuthIdentity("restored", "person@example.com", true)
        gateway.listeners.toList().forEach { it() }
        runCurrent()
        assertTrue(sessions.last() is AuthSession.SignedIn)
        assertFalse(sessions.contains(AuthSession.SignedOut))
    }

    @Test fun listenerRemovedWhenSessionCollectionStops() = runTest {
        val gateway = FakeGateway()
        val repository = FirebaseAuthRepository(gateway)
        val job = backgroundScope.launch { repository.session.collect {} }
        runCurrent()
        assertEquals(1, gateway.listeners.size)
        job.cancel()
        runCurrent()
        assertEquals(0, gateway.listeners.size)
    }

    @Test fun signoutDuringNoncancellableSdkSigninCleansLateIdentityBeforePublishing() = runTest {
        val gateway = FakeGateway().apply { credentialGate = CompletableDeferred() }
        val repository = FirebaseAuthRepository(gateway)
        val sessions = mutableListOf<AuthSession>()
        backgroundScope.launch { repository.session.collect { sessions += it } }
        val signIn = launch { repository.signIn("person@example.com", "value") }
        runCurrent()
        repository.signOut()
        signIn.cancel()
        gateway.credentialGate!!.complete(Unit)
        advanceUntilIdle()
        assertNull(gateway.identity)
        assertTrue(sessions.none { it is AuthSession.SignedIn })
        assertTrue(gateway.signOutCalls >= 2)
    }

    @Test fun cancellingCredentialCallerAlsoClearsSdkLateSigninWithoutExplicitSignout() = runTest {
        val gateway = FakeGateway().apply { credentialGate = CompletableDeferred() }
        val repository = FirebaseAuthRepository(gateway)
        val signIn = launch { repository.signIn("person@example.com", "value") }
        runCurrent()
        signIn.cancel()
        gateway.credentialGate!!.complete(Unit)
        advanceUntilIdle()
        assertNull(gateway.identity)
    }

    @Test fun onlyVerifiedReloadRefreshesTokenAndStillReturnsIdentityOnly() = runTest {
        val gateway = FakeGateway().apply { identity = AuthIdentity("a", "person@example.com", false) }
        val repository = FirebaseAuthRepository(gateway)
        assertFalse(repository.refreshIdentity()!!.emailVerified)
        assertEquals(0, gateway.tokenRefreshes)
        gateway.identity = gateway.identity!!.copy(emailVerified = true)
        assertTrue(repository.refreshIdentity()!!.emailVerified)
        assertEquals(1, gateway.tokenRefreshes)
    }

    @Test fun replacementCallerWaitsForCancelledCredentialCleanupBeforeStarting() = runTest {
        val gateway = FakeGateway().apply { credentialGate = CompletableDeferred() }
        val repository = FirebaseAuthRepository(gateway)
        val oldCaller = launch { repository.signIn("old@example.com", "value") }
        runCurrent()
        oldCaller.cancel()
        val replacement = launch { repository.signIn("new@example.com", "value") }
        runCurrent()
        assertEquals(1, gateway.credentialCalls)
        gateway.credentialGate!!.complete(Unit)
        advanceUntilIdle()
        assertTrue(replacement.isCompleted)
        assertEquals(2, gateway.credentialCalls)
        assertEquals("new@example.com", gateway.identity!!.email)
        assertEquals(1, gateway.signOutCalls)
    }

    @Test fun staleTokenAfterAccountReplacementIsNeverReturned() = runTest {
        val gateway = FakeGateway().apply {
            identity = AuthIdentity("a", "person@example.com", true)
            tokenGate = CompletableDeferred()
        }
        val repository = FirebaseAuthRepository(gateway)
        var returned = false
        val caller = launch {
            try { repository.getIdToken("a"); returned = true } catch (_: CancellationException) { }
        }
        runCurrent()
        gateway.identity = AuthIdentity("b", "other@example.com", true)
        gateway.tokenGate!!.complete(Unit)
        caller.join()
        assertFalse(returned)
    }

    @Test fun overlappingForcedRefreshesUseOneSdkRefresh() = runTest {
        val gateway = FakeGateway().apply {
            identity = AuthIdentity("a", "person@example.com", true)
            tokenGate = CompletableDeferred()
        }
        val repository = FirebaseAuthRepository(gateway)
        val first = async { repository.getIdToken("a", true) }
        val second = async { repository.getIdToken("a", true) }
        runCurrent()
        assertEquals(1, gateway.tokenRefreshes)
        gateway.tokenGate!!.complete(Unit)
        assertEquals("fake-id-token", first.await())
        assertEquals("fake-id-token", second.await())
        assertEquals(1, gateway.tokenRefreshes)
    }

    @Test fun unverifiedIdentityCannotProvideAnApiToken() = runTest {
        val gateway = FakeGateway().apply { identity = AuthIdentity("a", "person@example.com", false) }
        val repository = FirebaseAuthRepository(gateway)
        try {
            repository.getIdToken("a")
            fail("Unverified identity returned a token")
        } catch (failure: AuthFailure) {
            assertEquals(AuthError.SESSION_EXPIRED, failure.error)
        }
        assertEquals(0, gateway.tokenReads)
    }

    @Test fun passwordChangeReauthenticatesBeforeUpdateAndDoesNotUpdateOnFailure() = runTest {
        val gateway = FakeGateway().apply { identity = AuthIdentity("a", "person@example.com", true) }
        val repository = FirebaseAuthRepository(gateway)
        repository.changePassword("current", "new")
        assertEquals(listOf("reauthenticate", "update"), gateway.passwordCalls)
        gateway.passwordCalls.clear()
        gateway.reauthenticationFailure = AuthFailure(AuthError.INVALID_CREDENTIALS)
        try {
            repository.changePassword("wrong", "new")
            fail("Reauthentication failure ignored")
        } catch (failure: AuthFailure) {
            assertEquals(AuthError.INVALID_CREDENTIALS, failure.error)
        }
        assertEquals(listOf("reauthenticate"), gateway.passwordCalls)
    }

    @Test fun signoutDuringPasswordReauthenticationCannotUpdateOrRestoreOldIdentity() = runTest {
        val gateway = FakeGateway().apply {
            identity = AuthIdentity("a", "person@example.com", true)
            reauthenticationGate = CompletableDeferred()
        }
        val repository = FirebaseAuthRepository(gateway)
        val sessions = mutableListOf<AuthSession>()
        backgroundScope.launch { repository.session.collect { sessions += it } }
        val caller = launch { repository.changePassword("current", "new") }
        runCurrent()
        repository.signOut()
        caller.cancel()
        runCurrent()
        val afterSignout = sessions.size
        gateway.reauthenticationGate!!.complete(Unit)
        caller.join()
        runCurrent()
        assertEquals(listOf("reauthenticate"), gateway.passwordCalls)
        assertNull(gateway.identity)
        assertTrue(sessions.drop(afterSignout).none { it is AuthSession.SignedIn })
    }

    @Test fun cancellingOnlyPasswordFormKeepsExistingIdentityWithoutAnUnsignedTransition() = runTest {
        val gateway = FakeGateway().apply {
            identity = AuthIdentity("a", "person@example.com", true)
            reauthenticationGate = CompletableDeferred()
        }
        val repository = FirebaseAuthRepository(gateway)
        val sessions = mutableListOf<AuthSession>()
        backgroundScope.launch { repository.session.collect { sessions += it } }
        val caller = launch { repository.changePassword("current", "new") }
        runCurrent()
        caller.cancel()
        gateway.reauthenticationGate!!.complete(Unit)
        caller.join()
        runCurrent()
        assertEquals(listOf("reauthenticate"), gateway.passwordCalls)
        assertEquals("a", gateway.identity!!.uid)
        assertEquals(0, gateway.signOutCalls)
        assertTrue(sessions.none { it is AuthSession.SignedOut })
    }

    @Test fun lateReauthenticationCallbackAfterLogoutCannotLoadAccountContextAgain() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val gateway = FakeGateway().apply {
                identity = AuthIdentity("a", "person@example.com", true)
                reauthenticationGate = CompletableDeferred()
            }
            val repository = FirebaseAuthRepository(gateway)
            var loads = 0
            val contexts = object : AccountContextRepository {
                override suspend fun load(expectedUid: String): AccountContext {
                    loads++
                    return AccountContext("backend-user", "workspace", emptySet(), emptySet())
                }
                override suspend fun logout(expectedUid: String) = Unit
            }
            val subject = AuthViewModel(repository, contexts)
            store.put("auth", subject)
            runCurrent()
            assertEquals(AuthStage.READY, subject.uiState.value.stage)
            assertEquals(1, loads)
            subject.changePassword("current", "new", "new")
            runCurrent()
            assertEquals(AuthStage.READY, subject.uiState.value.stage)
            subject.signOut()
            runCurrent()
            assertEquals(AuthStage.SIGNED_OUT, subject.uiState.value.stage)
            gateway.reauthenticationGate!!.complete(Unit)
            runCurrent()
            assertEquals(1, loads)
            assertEquals(AuthStage.SIGNED_OUT, subject.uiState.value.stage)
            assertNull(subject.uiState.value.context)
            assertNull(gateway.identity)
        } finally {
            store.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private class FakeGateway : FirebaseAuthGateway {
        override var identity: AuthIdentity? = null
        val listeners = mutableListOf<() -> Unit>()
        var credentialGate: CompletableDeferred<Unit>? = null
        var signOutCalls = 0
        var tokenRefreshes = 0
        var tokenReads = 0
        var tokenGate: CompletableDeferred<Unit>? = null
        var reauthenticationGate: CompletableDeferred<Unit>? = null
        var reauthenticationFailure: AuthFailure? = null
        val passwordCalls = mutableListOf<String>()
        var notifyImmediately = true
        var credentialCalls = 0
        override fun observe(onChanged: () -> Unit): AutoCloseable {
            listeners += onChanged
            if (notifyImmediately) onChanged()
            return AutoCloseable { listeners -= onChanged }
        }
        override suspend fun signIn(email: String, password: String) {
            credentialCalls++
            credentialGate?.await()
            identity = AuthIdentity("a", email, false)
            listeners.toList().forEach { it() }
        }
        override suspend fun signUp(email: String, password: String) = signIn(email, password)
        override suspend fun sendPasswordReset(email: String) = Unit
        override suspend fun resendVerification() = Unit
        override suspend fun reload() = Unit
        override suspend fun getIdToken(forceRefresh: Boolean): String {
            tokenReads++
            if (forceRefresh) tokenRefreshes++
            tokenGate?.await()
            return "fake-id-token"
        }
        override suspend fun reauthenticate(currentPassword: String) {
            passwordCalls += "reauthenticate"
            val original = identity
            reauthenticationGate?.await()
            reauthenticationFailure?.let { throw it }
            identity = original
            listeners.toList().forEach { it() }
        }
        override suspend fun updatePassword(newPassword: String) { passwordCalls += "update" }
        override fun signOut() {
            signOutCalls++
            identity = null
            listeners.toList().forEach { it() }
        }
    }
}
