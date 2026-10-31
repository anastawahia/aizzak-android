package android.aizzak.com.core.network

/** In-memory Firebase bearer access bound to the requesting account. */
interface IdTokenProvider {
    suspend fun getIdToken(expectedUid: String, forceRefresh: Boolean = false): String
}
