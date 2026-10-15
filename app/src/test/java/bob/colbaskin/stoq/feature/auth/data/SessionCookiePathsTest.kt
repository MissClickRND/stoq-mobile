package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.common.network.withApiAuthPath
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Test

class SessionCookiePathsTest {
    @Test fun refreshCookieUsesGatewayAuthPathOnly() {
        val origin = "https://example.test/api/".toHttpUrl()
        val cookie = Cookie.parse(origin, "refreshToken=test; Path=/auth; Secure; HttpOnly; Max-Age=3600")!!.withApiAuthPath(origin)
        assertEquals("/api/auth", cookie.path)
        assertTrue(cookie.matches(origin.resolve("auth/refresh")!!))
        assertFalse(cookie.matches(origin.resolve("inventory")!!))
        assertFalse(cookie.matches("https://other.test/api/auth/refresh".toHttpUrl()))
        assertTrue(cookie.secure)
        assertTrue(cookie.httpOnly)
        assertTrue(cookie.hostOnly)
    }

    @Test fun rootAndAlreadyPrefixedCookiesStayUnchanged() {
        val root = "https://example.test/".toHttpUrl()
        val cookie = Cookie.parse(root, "refreshToken=test; Path=/auth; HttpOnly")!!
        assertSame(cookie, cookie.withApiAuthPath(root))
        val prefixed = Cookie.parse(root, "refreshToken=test; Path=/api/auth")!!
        assertSame(prefixed, prefixed.withApiAuthPath("https://example.test/api/".toHttpUrl()))
    }
}
