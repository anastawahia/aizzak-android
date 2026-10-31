package android.aizzak.com.feature.auth

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val store = ViewModelStore()
    @Before fun prepare() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun clean() { store.clear(); Dispatchers.resetMain() }

    @Test fun restoredVerifiedIdentityCannotBypassMissingAccountContext() = runTest {
        val repository = FakeAuthRepository(AuthSession.SignedIn(identity(verified = true)))
        val subject = subject(repository)
        runCurrent()
        assertEquals(AuthStage.CONTEXT_UNAVAILABLE, subject.uiState.value.stage)
    }

    @Test fun restoredUnverifiedIdentityNeedsVerification() = runTest {
        val subject = subject(FakeAuthRepository(AuthSession.SignedIn(identity())))
        runCurrent()
        assertEquals(AuthStage.NEEDS_VERIFICATION, subject.uiState.value.stage)
    }

    @Test fun invalidInputsDoNotDispatchAndPasswordsHaveNoInventedMinimum() = runTest {
        val repository = FakeAuthRepository()
        val subject = subject(repository)
        runCurrent()
        subject.signIn("invalid", "value")
        assertEquals(AuthError.INVALID_EMAIL, subject.uiState.value.error)
        subject.signIn("person@example.com", "")
        assertEquals(AuthError.EMPTY_PASSWORD, subject.uiState.value.error)
        subject.signUp("person@example.com", "x", "y")
        assertEquals(AuthError.PASSWORD_MISMATCH, subject.uiState.value.error)
        assertEquals(0, repository.calls)
        subject.signIn(" person@example.com ", "x")
        advanceUntilIdle()
        assertEquals(1, repository.calls)
        assertEquals("person@example.com", repository.lastEmail)
    }

    @Test fun duplicateSubmissionDispatchesOnceAndTypedFailureIsVisible() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAuthRepository().apply {
            onSignIn = { gate.await(); throw AuthFailure(AuthError.NETWORK) }
        }
        val subject = subject(repository)
        runCurrent()
        subject.signIn("person@example.com", "value")
        subject.signIn("person@example.com", "value")
        runCurrent()
        assertEquals(1, repository.calls)
        assertTrue(subject.uiState.value.busy)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(AuthError.NETWORK, subject.uiState.value.error)
        assertFalse(subject.uiState.value.busy)
    }

    @Test fun signupVerificationFailureRetainsCreatedIdentityWithoutSuccessMessage() = runTest {
        val repository = FakeAuthRepository().apply {
            onSignUp = {
                current.value = AuthSession.SignedIn(identity())
                throw AuthFailure(AuthError.TOO_MANY_REQUESTS)
            }
        }
        val subject = subject(repository)
        runCurrent()
        subject.signUp("person@example.com", "value", "value")
        advanceUntilIdle()
        assertEquals(AuthStage.NEEDS_VERIFICATION, subject.uiState.value.stage)
        assertEquals(AuthError.TOO_MANY_REQUESTS, subject.uiState.value.error)
        assertNull(subject.uiState.value.message)
    }

    @Test fun resetReportsNeutralResultAndVerificationRefreshRetainsGate() = runTest {
        val repository = FakeAuthRepository()
        val subject = subject(repository)
        runCurrent()
        subject.resetPassword("person@example.com")
        advanceUntilIdle()
        assertEquals(AuthMessage.RESET_REQUESTED, subject.uiState.value.message)
        repository.current.value = AuthSession.SignedIn(identity())
        runCurrent()
        repository.refreshed = identity()
        subject.refreshIdentity()
        advanceUntilIdle()
        assertEquals(AuthStage.NEEDS_VERIFICATION, subject.uiState.value.stage)
        assertEquals(AuthMessage.EMAIL_NOT_VERIFIED, subject.uiState.value.message)
        repository.refreshed = identity(verified = true)
        subject.refreshIdentity()
        advanceUntilIdle()
        assertEquals(AuthStage.CONTEXT_UNAVAILABLE, subject.uiState.value.stage)
    }

    @Test fun signupCompletionFeedbackSurvivesDeferredSessionDelivery() = runTest {
        val repository = FakeAuthRepository().apply {
            onSignUp = { current.value = AuthSession.SignedIn(identity()) }
        }
        val subject = subject(repository)
        runCurrent()
        subject.signUp("person@example.com", "value", "value")
        advanceUntilIdle()
        assertEquals(AuthStage.NEEDS_VERIFICATION, subject.uiState.value.stage)
        assertEquals(AuthMessage.VERIFICATION_SENT, subject.uiState.value.message)
        assertFalse(subject.uiState.value.busy)
    }

    @Test fun lateCancellationIgnoringRefreshCannotRestoreIdentityAfterSignout() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAuthRepository(AuthSession.SignedIn(identity())).apply {
            onRefresh = { withContext(NonCancellable) { gate.await(); identity(verified = true) } }
        }
        val subject = subject(repository)
        runCurrent()
        subject.refreshIdentity()
        runCurrent()
        subject.signOut()
        runCurrent()
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(AuthStage.SIGNED_OUT, subject.uiState.value.stage)
        assertNull(subject.uiState.value.identity)
    }

    @Test fun lateAccountAErrorCannotLeakIntoAccountB() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAuthRepository(AuthSession.SignedIn(identity())).apply {
            onRefresh = { withContext(NonCancellable) { gate.await(); throw AuthFailure(AuthError.NETWORK) } }
        }
        val subject = subject(repository)
        runCurrent()
        subject.refreshIdentity()
        runCurrent()
        repository.current.value = AuthSession.SignedIn(identity(uid = "b", verified = true))
        runCurrent()
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals("b", subject.uiState.value.identity!!.uid)
        assertNull(subject.uiState.value.error)
        assertFalse(subject.uiState.value.busy)
    }

    private fun subject(repository: AuthRepository) = AuthViewModel(repository).also { store.put("auth", it) }
    private fun identity(uid: String = "a", verified: Boolean = false) = AuthIdentity(uid, "person@example.com", verified)

    private class FakeAuthRepository(initial: AuthSession = AuthSession.SignedOut) : AuthRepository {
        val current = MutableStateFlow(initial)
        override val session: Flow<AuthSession> = current
        var calls = 0
        var lastEmail: String? = null
        var refreshed: AuthIdentity? = null
        var onSignIn: suspend () -> Unit = {}
        var onSignUp: suspend () -> Unit = {}
        var onRefresh: (suspend () -> AuthIdentity?)? = null
        override suspend fun signIn(email: String, password: String) { calls++; lastEmail = email; onSignIn() }
        override suspend fun signUp(email: String, password: String) { calls++; onSignUp() }
        override suspend fun sendPasswordReset(email: String) { calls++ }
        override suspend fun resendVerification() { calls++ }
        override suspend fun refreshIdentity(): AuthIdentity? = onRefresh?.invoke() ?: refreshed
        override suspend fun signOut() { current.value = AuthSession.SignedOut }
    }
}
