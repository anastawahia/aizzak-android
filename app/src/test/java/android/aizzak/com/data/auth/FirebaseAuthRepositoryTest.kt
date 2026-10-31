package android.aizzak.com.data.auth

import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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

    private class FakeGateway : FirebaseAuthGateway {
        override var identity: AuthIdentity? = null
        val listeners = mutableListOf<() -> Unit>()
        var credentialGate: CompletableDeferred<Unit>? = null
        var signOutCalls = 0
        var tokenRefreshes = 0
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
        override suspend fun refreshIdToken() { tokenRefreshes++ }
        override fun signOut() {
            signOutCalls++
            identity = null
            listeners.toList().forEach { it() }
        }
    }
}
