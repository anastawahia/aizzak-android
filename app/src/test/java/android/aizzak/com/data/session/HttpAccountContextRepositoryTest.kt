package android.aizzak.com.data.session

import android.aizzak.com.core.network.ApiEnvironment
import android.aizzak.com.core.network.IdTokenProvider
import android.aizzak.com.feature.auth.AccountContextError
import android.aizzak.com.feature.auth.AccountContextFailure
import android.aizzak.com.feature.auth.AccountContextRepository
import android.aizzak.com.feature.auth.AuthError
import android.aizzak.com.feature.auth.AuthFailure
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HttpAccountContextRepositoryTest {
    private val server = MockWebServer()
    private val tokens = FakeTokens()
    private lateinit var repository: AccountContextRepository
    private lateinit var baseUrl: String

    @Before fun prepare() {
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        baseUrl = "http://127.0.0.1:${server.port}/api/v1/"
        repository = HttpAccountContextRepository.create(baseUrl, tokens)
    }

    @After fun clean() { server.shutdown() }

    @Test fun resourceIsUnwrappedAndUnknownFieldsRolesAndEmptyPermissionsAreAccepted(): Unit = runBlocking {
        server.enqueue(json(CONTEXT))
        val context = repository.load("firebase-uid")
        assertEquals(USER_ID, context.userId)
        assertEquals(WORKSPACE_ID, context.workspaceId)
        assertEquals(setOf("future_role"), context.roles)
        assertTrue(context.permissions.isEmpty())
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/me/context", request.path)
        assertEquals("Bearer original", request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertNull(request.getHeader("Cookie"))
        assertEquals(listOf("firebase-uid" to false), tokens.calls)
    }

    @Test fun unauthorizedGetRefreshesAndRetriesExactlyOnce(): Unit = runBlocking {
        server.enqueue(problem(401))
        server.enqueue(json(CONTEXT))
        repository.load("uid")
        assertEquals(listOf("uid" to false, "uid" to true), tokens.calls)
        assertEquals("Bearer original", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer renewed", server.takeRequest().getHeader("Authorization"))
        assertEquals(2, server.requestCount)
    }

    @Test fun repeatedUnauthorizedAndForbiddenRemainDifferentAndBounded(): Unit = runBlocking {
        server.enqueue(problem(401))
        server.enqueue(problem(401))
        assertFailure(AccountContextError.UNAUTHENTICATED) { repository.load("uid") }
        assertEquals(2, server.requestCount)
        tokens.calls.clear()
        server.enqueue(problem(403).setHeader("X-Correlation-Id", "trace-403"))
        val forbidden = assertFailure(AccountContextError.FORBIDDEN) { repository.load("uid") }
        assertEquals("trace-403", forbidden.correlationId)
        assertEquals(listOf("uid" to false), tokens.calls)
        assertEquals(3, server.requestCount)
    }

    @Test fun rateLimitAndUnavailableDoNotAutoRetryAndUnsafeCorrelationIsDropped(): Unit = runBlocking {
        server.enqueue(problem(429, "{\"correlation_id\":\"<unsafe>\"}").setHeader("Retry-After", "30"))
        assertNull(assertFailure(AccountContextError.RATE_LIMITED) { repository.load("uid") }.correlationId)
        server.enqueue(problem(503, "{\"correlation_id\":\"trace_503\"}"))
        assertEquals("trace_503", assertFailure(AccountContextError.UNAVAILABLE) { repository.load("uid") }.correlationId)
        assertEquals(2, server.requestCount)
    }

    @Test fun missingNullWrongTypesWrappedDataAndNoncanonicalUuidNeverGrantContext(): Unit = runBlocking {
        val invalid = listOf(
            "{}",
            CONTEXT.replace("\"roles\":[\"future_role\"],", ""),
            CONTEXT.replace("\"permissions\":[]", "\"permissions\":null"),
            CONTEXT.replace("\"permissions\":[]", "\"permissions\":[1]"),
            CONTEXT.replace(USER_ID, "1-1-1-1-1"),
            "{\"data\":$CONTEXT}",
            "not-json",
        )
        for (body in invalid) {
            server.enqueue(json(body))
            assertFailure(AccountContextError.INVALID_RESPONSE) { repository.load("uid") }
        }
    }

    @Test fun oversizedAndWrongContentTypeAreRejected(): Unit = runBlocking {
        server.enqueue(json(CONTEXT.dropLast(1) + ",\"padding\":\"" + "a".repeat(70_000) + "\"}"))
        assertFailure(AccountContextError.INVALID_RESPONSE) { repository.load("uid") }
        server.enqueue(MockResponse().setBody(CONTEXT).setHeader("Content-Type", "text/html"))
        assertFailure(AccountContextError.INVALID_RESPONSE) { repository.load("uid") }
    }

    @Test fun redirectsDoNotForwardBearerToAnotherDestination(): Unit = runBlocking {
        server.enqueue(MockResponse().setResponseCode(307).setHeader("Location", "$baseUrl/other"))
        assertFailure(AccountContextError.INVALID_RESPONSE) { repository.load("uid") }
        assertEquals(1, server.requestCount)
    }

    @Test fun cookiesAreNotPersistedAndContextIsFetchedEachTime(): Unit = runBlocking {
        server.enqueue(json(CONTEXT).setHeader("Set-Cookie", "session=sensitive; Path=/"))
        server.enqueue(json(CONTEXT))
        repository.load("uid")
        repository.load("uid")
        server.takeRequest()
        assertNull(server.takeRequest().getHeader("Cookie"))
        assertEquals(2, server.requestCount)
    }

    @Test fun logoutUsesPostAndNeverRefreshesOrRetriesWrites(): Unit = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        repository.logout("uid")
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/me/logout", request.path)
        assertEquals(0L, request.bodySize)
        server.enqueue(problem(401))
        assertFailure(AccountContextError.UNAUTHENTICATED) { repository.logout("uid") }
        assertEquals(listOf("uid" to false, "uid" to false), tokens.calls)
        assertEquals(2, server.requestCount)
    }

    @Test fun tokenFailuresAreTypedAndNeverDispatchHttp(): Unit = runBlocking {
        tokens.failure = AuthFailure(AuthError.NETWORK)
        assertFailure(AccountContextError.NETWORK) { repository.load("uid") }
        tokens.failure = AuthFailure(AuthError.SESSION_EXPIRED)
        assertFailure(AccountContextError.UNAUTHENTICATED) { repository.load("uid") }
        tokens.failure = AuthFailure(AuthError.TOO_MANY_REQUESTS)
        assertFailure(AccountContextError.RATE_LIMITED) { repository.load("uid") }
        assertEquals(0, server.requestCount)
    }

    @Test fun disconnectedRequestMapsToNetworkWithoutAutomaticRetry(): Unit = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertFailure(AccountContextError.NETWORK) { repository.load("uid") }
        assertEquals(1, tokens.calls.size)
    }

    @Test fun timeoutIsBoundedAndExternalCancellationRemainsCancellation(): Unit = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val short = HttpAccountContextRepository.createForTests(baseUrl, tokens, 150)
        assertFailure(AccountContextError.NETWORK) { short.load("uid") }
        server.takeRequest(2, TimeUnit.SECONDS)
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        var cancelled = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            try { repository.load("uid") }
            catch (failure: CancellationException) { cancelled = true; throw failure }
        }
        assertNotNull(server.takeRequest(2, TimeUnit.SECONDS))
        job.cancelAndJoin()
        assertTrue(cancelled)
        assertTrue(job.isCancelled)
    }

    @Test fun environmentRejectsReleaseHttpNonloopbackAndCredentialUrls() {
        assertTrue(ApiEnvironment.validatedBaseUrl("https://api.example.test/api/v1/", debug = false).isHttps)
        assertEquals("127.0.0.1", ApiEnvironment.validatedBaseUrl(baseUrl, debug = true).host)
        for ((url, debug) in listOf(
            baseUrl to false,
            "http://192.168.1.4/api/v1/" to true,
            "http://localhost/api/v1/" to true,
            "https://user:secret@api.example.test/api/v1/" to false,
            "https://api.example.test/api/v1/?tenant=one" to false,
            "https://api.example.test/api/v1/#fragment" to false,
            "https://api.example.test/other/" to false,
        )) {
            assertTrue(runCatching { ApiEnvironment.validatedBaseUrl(url, debug) }.isFailure)
        }
    }

    private suspend fun assertFailure(error: AccountContextError, block: suspend () -> Any): AccountContextFailure {
        try { block() } catch (failure: AccountContextFailure) {
            assertEquals(error, failure.error)
            return failure
        }
        throw AssertionError("Expected account-context failure")
    }

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
    private fun problem(status: Int, body: String = "{}") = MockResponse().setResponseCode(status)
        .setHeader("Content-Type", "application/problem+json").setBody(body)

    private class FakeTokens : IdTokenProvider {
        val calls = mutableListOf<Pair<String, Boolean>>()
        var failure: AuthFailure? = null
        override suspend fun getIdToken(expectedUid: String, forceRefresh: Boolean): String {
            calls += expectedUid to forceRefresh
            failure?.let { throw it }
            return if (forceRefresh) "renewed" else "original"
        }
    }

    companion object {
        private const val USER_ID = "018f2a00-0000-7000-8000-000000000001"
        private const val WORKSPACE_ID = "018f2a00-0000-7000-8000-000000000002"
        private const val CONTEXT = """{"user":{"id":"$USER_ID"},"workspace":{"id":"$WORKSPACE_ID"},"roles":["future_role"],"permissions":[],"future_field":true}"""
    }
}
