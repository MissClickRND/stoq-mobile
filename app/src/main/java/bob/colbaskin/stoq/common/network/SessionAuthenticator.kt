package bob.colbaskin.stoq.common.network

import bob.colbaskin.stoq.BuildConfig
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAuthenticator @Inject constructor(private val cookies: SessionCookieJar) : Authenticator {
    private data class SessionVersion(val value: Long, val epoch: Long)
    private val refreshClient by lazy { OkHttpClient.Builder().build() }
    private val lock = Any()

    fun tagRequest(request: Request): Request = request.newBuilder()
        .tag(SessionVersion::class.java, SessionVersion(cookies.revision(), cookies.epoch())).build()

    override fun authenticate(route: Route?, response: Response): Request? = synchronized(lock) {
        val path = response.request.url.encodedPath
        if (BuildConfig.DEBUG) android.util.Log.d("StoqAuth", "HTTP 401: path=$path, sameOrigin=${cookies.sameOrigin(response.request.url)}, retried=${response.priorResponse != null}, hasSession=${cookies.hasSession()}")
        if (!cookies.sameOrigin(response.request.url) || path in setOf("/auth/login", "/auth/refresh", "/auth/register-user", "/auth/register-restaurant")) return null
        val attempt = response.request.tag(SessionVersion::class.java)
        if (attempt != null && attempt.epoch != cookies.epoch()) return null
        if (response.priorResponse != null) {
            cookies.clear()
            return null
        }
        val requestVersion = attempt?.value
        val currentCookies = cookies.loadForRequest(response.request.url)
        if (requestVersion != null && requestVersion != cookies.revision() && currentCookies.any { it.name == "accessToken" }) {
            return tagRequest(response.request).newBuilder()
                .header("Cookie", currentCookies.joinToString("; ") { "${it.name}=${it.value}" }).build()
        }
        if (!cookies.hasSession()) return null
        val refreshEpoch = cookies.epoch()
        try {
            if (BuildConfig.DEBUG) android.util.Log.d("StoqAuth", "Обновляем сессию после HTTP 401")
            val refreshUrl = response.request.url.newBuilder().encodedPath("/auth/refresh").query(null).build()
            refreshClient.newCall(Request.Builder()
                .url(refreshUrl)
                .header("Cookie", cookies.loadForRequest(refreshUrl).joinToString("; ") { "${it.name}=${it.value}" })
                .post(ByteArray(0).toRequestBody()).build()).execute().use { refreshed ->
                if (refreshEpoch != cookies.epoch()) return null
                if (refreshed.code == 401 || refreshed.code == 403) {
                    cookies.clear()
                    if (BuildConfig.DEBUG) android.util.Log.d("StoqAuth", "Сессия завершена: refresh отклонён сервером")
                }
                if (!refreshed.isSuccessful) return null
                cookies.saveFromResponse(refreshUrl, okhttp3.Cookie.parseAll(refreshUrl, refreshed.headers))
                if (BuildConfig.DEBUG) android.util.Log.d("StoqAuth", "Сессия успешно обновлена")
            }
        } catch (_: IOException) {
            return null
        }
        val header = cookies.loadForRequest(response.request.url).joinToString("; ") { "${it.name}=${it.value}" }
        tagRequest(response.request).newBuilder().header("Cookie", header).build()
    }
}
