package bob.colbaskin.stoq.common.network

import okhttp3.Cookie
import okhttp3.HttpUrl

/** The gateway adds an API prefix but older auth services still issue Path=/auth. */
internal fun Cookie.withApiAuthPath(baseUrl: HttpUrl): Cookie {
    val prefix = baseUrl.encodedPath.trimEnd('/')
    if (name != "refreshToken" || path != "/auth" || prefix.isEmpty()) return this
    return Cookie.Builder().name(name).value(value).path("$prefix/auth")
        .apply {
            if (hostOnly) hostOnlyDomain(domain) else domain(domain)
            if (persistent) expiresAt(expiresAt)
            if (secure) secure()
            if (httpOnly) httpOnly()
        }.build()
}
