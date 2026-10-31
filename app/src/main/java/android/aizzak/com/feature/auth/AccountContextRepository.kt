package android.aizzak.com.feature.auth

/** Server IDs are independent of the Firebase UID. No permissions are invented locally. */
data class AccountContext(
    val userId: String,
    val workspaceId: String,
    val roles: Set<String>,
    val permissions: Set<String>,
)

enum class AccountContextError {
    UNAUTHENTICATED, FORBIDDEN, NETWORK, RATE_LIMITED, UNAVAILABLE, INVALID_RESPONSE,
}

class AccountContextFailure(
    val error: AccountContextError,
    val correlationId: String? = null,
) : Exception()

interface AccountContextRepository {
    suspend fun load(expectedUid: String): AccountContext

    /** One bounded server attempt; callers must still guarantee local cleanup. */
    suspend fun logout(expectedUid: String)
}

/** Tests and unconfigured environments remain closed to authenticated content. */
object UnavailableAccountContextRepository : AccountContextRepository {
    override suspend fun load(expectedUid: String): AccountContext =
        throw AccountContextFailure(AccountContextError.UNAVAILABLE)

    override suspend fun logout(expectedUid: String) = Unit
}
