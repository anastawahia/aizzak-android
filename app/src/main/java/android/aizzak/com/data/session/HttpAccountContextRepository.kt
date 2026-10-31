package android.aizzak.com.data.session

import android.aizzak.com.core.network.ApiEnvironment
import android.aizzak.com.core.network.IdTokenProvider
import android.aizzak.com.feature.auth.AccountContext
import android.aizzak.com.feature.auth.AccountContextError
import android.aizzak.com.feature.auth.AccountContextFailure
import android.aizzak.com.feature.auth.AccountContextRepository
import android.aizzak.com.feature.auth.AuthError
import android.aizzak.com.feature.auth.AuthFailure
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/** No disk cache, cookies, redirects, auth interceptor, body logging or automatic write retries. */
class HttpAccountContextRepository private constructor(
    private val api: AccountApi,
    private val tokens: IdTokenProvider,
    private val json: Json,
    private val operationTimeoutMillis: Long,
) : AccountContextRepository {
    override suspend fun load(expectedUid: String): AccountContext = bounded {
        var token = token(expectedUid, force = false)
        var response = api.context("Bearer $token").awaitResponse()
        currentCoroutineContext().ensureActive()
        if (response.code() == 401) {
            response.errorBody()?.close()
            token = token(expectedUid, force = true)
            currentCoroutineContext().ensureActive()
            response = api.context("Bearer $token").awaitResponse()
        }
        if (!response.isSuccessful) throw failure(response)
        val mediaType = response.headers()["Content-Type"]?.substringBefore(';')?.trim()
        if (mediaType != "application/json") throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
        val context = response.body() ?: throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
        if (!validUuid(context.user.id) || !validUuid(context.workspace.id)) {
            throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
        }
        AccountContext(context.user.id, context.workspace.id, context.roles.toSet(), context.permissions.toSet())
    }

    override suspend fun logout(expectedUid: String) {
        bounded {
            val bearer = token(expectedUid, force = false)
            val response = api.logout("Bearer $bearer").awaitResponse()
            currentCoroutineContext().ensureActive()
            if (response.code() != 204) {
                if (response.isSuccessful) throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
                throw failure(response)
            }
        }
    }

    private suspend fun token(uid: String, force: Boolean): String = try {
        tokens.getIdToken(uid, force).also {
            if (it.isBlank() || it.any { character -> character == '\r' || character == '\n' }) {
                throw AccountContextFailure(AccountContextError.UNAUTHENTICATED)
            }
        }
    } catch (failure: AuthFailure) {
        throw AccountContextFailure(when (failure.error) {
            AuthError.SESSION_EXPIRED, AuthError.USER_DISABLED, AuthError.INVALID_CREDENTIALS -> AccountContextError.UNAUTHENTICATED
            AuthError.NETWORK -> AccountContextError.NETWORK
            AuthError.TOO_MANY_REQUESTS -> AccountContextError.RATE_LIMITED
            else -> AccountContextError.UNAVAILABLE
        })
    }

    private suspend fun <T : Any> bounded(block: suspend () -> T): T = try {
        withTimeoutOrNull(operationTimeoutMillis) { block() }
            ?: throw AccountContextFailure(AccountContextError.NETWORK)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: AccountContextFailure) {
        throw failure
    } catch (_: ResponseTooLarge) {
        throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
    } catch (_: SerializationException) {
        throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
    } catch (_: IOException) {
        throw AccountContextFailure(AccountContextError.NETWORK)
    } catch (_: IllegalArgumentException) {
        throw AccountContextFailure(AccountContextError.INVALID_RESPONSE)
    }

    private fun failure(response: Response<*>): AccountContextFailure {
        val body = response.errorBody()?.use { it.string() }
        val problem = body?.let { runCatching { json.decodeFromString<ProblemDto>(it) }.getOrNull() }
        val correlationId = safeCorrelationId(response.headers()["X-Correlation-Id"])
            ?: safeCorrelationId(problem?.correlationId)
        val error = when (response.code()) {
            401 -> AccountContextError.UNAUTHENTICATED
            403 -> AccountContextError.FORBIDDEN
            429 -> AccountContextError.RATE_LIMITED
            in 500..599 -> AccountContextError.UNAVAILABLE
            else -> AccountContextError.INVALID_RESPONSE
        }
        return AccountContextFailure(error, correlationId)
    }

    companion object {
        fun create(baseUrl: String, tokenProvider: IdTokenProvider): AccountContextRepository =
            createForTests(baseUrl, tokenProvider, 15_000)

        internal fun createForTests(
            baseUrl: String,
            tokenProvider: IdTokenProvider,
            timeoutMillis: Long,
        ): AccountContextRepository {
            require(timeoutMillis in 1..15_000)
            val json = Json { ignoreUnknownKeys = true }
            val client = OkHttpClient.Builder()
                .cache(null)
                .cookieJar(CookieJar.NO_COOKIES)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .callTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .addInterceptor(ResponseLimitInterceptor())
                .build()
            val api = Retrofit.Builder()
                .baseUrl(ApiEnvironment.baseUrl(baseUrl))
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(AccountApi::class.java)
            return HttpAccountContextRepository(api, tokenProvider, json, timeoutMillis)
        }

        private fun validUuid(value: String): Boolean = UUID_PATTERN.matches(value) &&
            runCatching { UUID.fromString(value).toString().equals(value, ignoreCase = true) }.getOrDefault(false)

        private fun safeCorrelationId(value: String?): String? = value?.takeIf { CORRELATION_PATTERN.matches(it) }

        private val UUID_PATTERN = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        private val CORRELATION_PATTERN = Regex("[a-zA-Z0-9._:-]{1,128}")
    }
}

private interface AccountApi {
    @GET("me/context")
    fun context(@Header("Authorization") bearer: String): Call<ContextDto>

    @POST("me/logout")
    fun logout(@Header("Authorization") bearer: String): Call<Unit>
}

@Serializable private data class ContextDto(
    val user: IdentityDto,
    val workspace: IdentityDto,
    val roles: List<String>,
    val permissions: List<String>,
)
@Serializable private data class IdentityDto(val id: String)
@Serializable private data class ProblemDto(
    @kotlinx.serialization.SerialName("correlation_id") val correlationId: String? = null,
)

private class ResponseTooLarge : IOException()

private class ResponseLimitInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request().newBuilder().header("Accept", "application/json").build())
        try {
            val source = response.body?.source()
            source?.request(65_537)
            if (source != null && source.buffer.size > 65_536) throw ResponseTooLarge()
            return response
        } catch (failure: Exception) {
            response.close()
            throw failure
        }
    }
}

private suspend fun <T : Any> Call<T>.awaitResponse(): Response<T> = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (continuation.isActive) continuation.resume(response)
            else response.errorBody()?.close()
        }

        override fun onFailure(call: Call<T>, failure: Throwable) {
            if (continuation.isActive) continuation.resumeWithException(failure)
        }
    })
}
