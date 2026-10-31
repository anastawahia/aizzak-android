package android.aizzak.com.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthStage { RESTORING, SIGNED_OUT, NEEDS_VERIFICATION, CONTEXT_UNAVAILABLE }
enum class AuthAction { SIGN_IN, SIGN_UP, RESET_PASSWORD, RESEND_VERIFICATION, REFRESH_IDENTITY, SIGN_OUT }
enum class AuthMessage { RESET_REQUESTED, VERIFICATION_SENT, EMAIL_NOT_VERIFIED }

data class AuthUiState(
    val stage: AuthStage = AuthStage.RESTORING,
    val identity: AuthIdentity? = null,
    val busy: Boolean = false,
    val action: AuthAction? = null,
    val error: AuthError? = null,
    val message: AuthMessage? = null,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()
    private var operation: Job? = null
    private var generation = 0L

    init {
        viewModelScope.launch {
            try {
                repository.session.collect { session ->
                    val previous = mutableUiState.value
                    val nextUid = (session as? AuthSession.SignedIn)?.identity?.uid
                    val accountChanged = previous.identity != null && previous.identity.uid != nextUid
                    if (accountChanged) {
                        generation++
                        operation?.cancel()
                        mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT)
                    }
                    mutableUiState.update { state ->
                        when (session) {
                            AuthSession.Restoring -> state.copy(stage = AuthStage.RESTORING, identity = null)
                            AuthSession.SignedOut -> state.copy(stage = AuthStage.SIGNED_OUT, identity = null)
                            is AuthSession.SignedIn -> state.copy(
                                stage = stageFor(session.identity), identity = session.identity,
                            )
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(stage = AuthStage.SIGNED_OUT, identity = null, error = AuthError.SERVICE_UNAVAILABLE)
                }
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
        if (password != confirmation) {
            showError(AuthError.PASSWORD_MISMATCH)
            return
        }
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
        val identity = repository.refreshIdentity()
        AuthOutcome(
            identity = identity, replaceIdentity = true,
            message = if (identity != null && !identity.emailVerified) AuthMessage.EMAIL_NOT_VERIFIED else null,
        )
    }

    fun signOut() {
        if (mutableUiState.value.action == AuthAction.SIGN_OUT) return
        generation++
        operation?.cancel()
        mutableUiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT)
        submit(AuthAction.SIGN_OUT) { repository.signOut(); AuthOutcome() }
    }

    fun clearFeedback() {
        mutableUiState.update { it.copy(error = null, message = null) }
    }

    private fun submit(action: AuthAction, block: suspend () -> AuthOutcome) {
        if (mutableUiState.value.busy) return
        val expected = generation
        mutableUiState.update { it.copy(busy = true, action = action, error = null, message = null) }
        operation = viewModelScope.launch {
            try {
                val outcome = block()
                if (expected == generation) mutableUiState.update {
                    if (outcome.replaceIdentity) it.copy(
                        identity = outcome.identity,
                        stage = outcome.identity?.let(::stageFor) ?: AuthStage.SIGNED_OUT,
                        message = outcome.message,
                    ) else it.copy(message = outcome.message)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: AuthFailure) {
                if (expected == generation) showError(failure.error)
            } catch (_: Exception) {
                if (expected == generation) showError(AuthError.UNKNOWN)
            } finally {
                if (expected == generation) mutableUiState.update { it.copy(busy = false, action = null) }
            }
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (mutableUiState.value.busy) return false
        if (!validEmail(email)) { showError(AuthError.INVALID_EMAIL); return false }
        if (password.isEmpty()) { showError(AuthError.EMPTY_PASSWORD); return false }
        return true
    }

    private fun showError(error: AuthError) {
        mutableUiState.update { it.copy(error = error, message = null) }
    }

    private fun stageFor(identity: AuthIdentity): AuthStage =
        if (identity.emailVerified) AuthStage.CONTEXT_UNAVAILABLE else AuthStage.NEEDS_VERIFICATION

    private fun validEmail(email: String): Boolean = EMAIL.matches(email.trim())

    private data class AuthOutcome(
        val message: AuthMessage? = null,
        val replaceIdentity: Boolean = false,
        val identity: AuthIdentity? = null,
    )

    private companion object {
        val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
