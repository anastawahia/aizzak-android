package android.aizzak.com.feature.auth

import kotlinx.coroutines.flow.Flow

data class AuthIdentity(val uid: String, val email: String?, val emailVerified: Boolean)

sealed interface AuthSession {
    data object Restoring : AuthSession
    data object SignedOut : AuthSession
    data class SignedIn(val identity: AuthIdentity) : AuthSession
}

enum class AuthError {
    INVALID_EMAIL, EMPTY_PASSWORD, PASSWORD_MISMATCH, INVALID_CREDENTIALS,
    EMAIL_IN_USE, WEAK_PASSWORD, NETWORK, TOO_MANY_REQUESTS, USER_DISABLED,
    SESSION_EXPIRED, SERVICE_UNAVAILABLE, UNKNOWN,
}

class AuthFailure(val error: AuthError) : Exception()

interface AuthRepository {
    val session: Flow<AuthSession>
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun resendVerification()
    suspend fun refreshIdentity(): AuthIdentity?
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        throw AuthFailure(AuthError.SERVICE_UNAVAILABLE)
    }
    suspend fun signOut()
}
