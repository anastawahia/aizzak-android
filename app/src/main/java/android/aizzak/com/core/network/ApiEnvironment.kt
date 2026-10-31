package android.aizzak.com.core.network

import android.aizzak.com.BuildConfig
import java.net.URI
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Public environment configuration; cleartext exists only on the approved debug USB loopback. */
object ApiEnvironment {
    fun baseUrl(value: String): HttpUrl = validatedBaseUrl(value, BuildConfig.DEBUG)

    internal fun validatedBaseUrl(value: String, debug: Boolean): HttpUrl {
        val uri = runCatching { URI(value) }.getOrNull()
        val url = value.toHttpUrlOrNull()
        require(uri != null && uri.host != null && uri.rawUserInfo == null &&
            uri.rawQuery == null && uri.rawFragment == null && uri.rawPath == "/api/v1/" &&
            url != null && url.username.isEmpty() && url.password.isEmpty() &&
            url.query == null && url.fragment == null && url.encodedPath == "/api/v1/" &&
            (url.isHttps || (debug && uri.scheme == "http" && uri.host == "127.0.0.1"))) {
            "API environment requires HTTPS, or the approved debug loopback, with /api/v1/."
        }
        return url
    }
}
