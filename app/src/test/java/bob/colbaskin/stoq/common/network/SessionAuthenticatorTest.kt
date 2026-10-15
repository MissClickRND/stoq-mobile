package bob.colbaskin.stoq.common.network

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class SessionAuthenticatorTest {
    private val base = "https://example.test/api/".toHttpUrl()

    private class Cookies(private val base: HttpUrl) : SessionCookies {
        var values = listOf(Cookie.parse(base, "refreshToken=refresh; Path=/api/auth; Secure; HttpOnly; Max-Age=3600")!!)
        private var version = 0L
        private var sessionEpoch = 0L
        @Synchronized override fun revision() = version
        @Synchronized override fun epoch() = sessionEpoch
        @Synchronized override fun hasSession() = values.isNotEmpty()
        @Synchronized override fun clear() { values = emptyList(); version++; sessionEpoch++ }
        override fun sameOrigin(url: HttpUrl) = url.host == base.host && url.scheme == base.scheme && url.port == base.port
        @Synchronized override fun loadForRequest(url: HttpUrl) = values.filter { sameOrigin(url) && it.matches(url) }
        @Synchronized override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            values = values.filterNot { old -> cookies.any { it.name == old.name } } + cookies
            version++
        }
    }

    private fun unauthorized(request: Request) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(401).message("Unauthorized").build()

    private fun refreshingClient(code: Int, calls: AtomicInteger, onRequest: (Request) -> Unit = {}): OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
        calls.incrementAndGet()
        onRequest(chain.request())
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message("Test")
            .apply { if (code == 201) addHeader("Set-Cookie", "accessToken=fresh; Path=/; Max-Age=900; Secure; HttpOnly") }
            .body("{}".toResponseBody()).build()
    }.build()

    @Test fun parallelUnauthorizedRequestsRefreshOnlyOnceAndRetryWithNewCookie() {
        val cookies = Cookies(base)
        val count = AtomicInteger()
        val auth = SessionAuthenticator(cookies, base, refreshingClient(201, count) {
            assertEquals("/api/auth/refresh", it.url.encodedPath)
            assertEquals("refreshToken=refresh", it.header("Cookie"))
        }, {})
        val requests = List(8) { auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build()) }
        val pool = Executors.newFixedThreadPool(8)
        try {
            val results = pool.invokeAll(requests.map { request -> Callable { auth.authenticate(null, unauthorized(request)) } }).map { it.get() }
            assertEquals(1, count.get())
            assertTrue(results.all { it?.header("Cookie") == "accessToken=fresh" })
            assertTrue(cookies.hasSession())
        } finally { pool.shutdownNow() }
    }

    @Test fun temporaryRefreshFailureDoesNotLogUserOut() {
        val cookies = Cookies(base)
        val count = AtomicInteger()
        val auth = SessionAuthenticator(cookies, base, refreshingClient(503, count), {})
        assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build()))))
        assertTrue(cookies.hasSession())
        assertEquals(1, count.get())
        repeat(5) { assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build())))) }
        assertEquals("Temporary failures must not cause a refresh storm", 1, count.get())
    }

    @Test fun rejectedRefreshEndsSessionButLoginNeverTriggersRefresh() {
        val cookies = Cookies(base)
        val count = AtomicInteger()
        val auth = SessionAuthenticator(cookies, base, refreshingClient(401, count), {})
        assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("auth/login")!!).build()))))
        assertEquals(0, count.get())
        assertTrue(cookies.hasSession())
        assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build()))))
        assertFalse(cookies.hasSession())
    }

    @Test fun missingRefreshDoesNotSendAnEmptyRefreshRequest() {
        val cookies = Cookies(base).apply { values = emptyList() }
        val count = AtomicInteger()
        val auth = SessionAuthenticator(cookies, base, refreshingClient(201, count), {})
        assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build()))))
        assertEquals(0, count.get())
    }

    @Test fun logoutDuringRefreshCannotRestoreOldSession() {
        val cookies = Cookies(base)
        val count = AtomicInteger()
        val auth = SessionAuthenticator(cookies, base, refreshingClient(201, count) { cookies.clear() }, {})
        assertNull(auth.authenticate(null, unauthorized(auth.tagRequest(Request.Builder().url(base.resolve("logs")!!).build()))))
        assertFalse(cookies.hasSession())
    }
}
