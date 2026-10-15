package bob.colbaskin.stoq.common.network

import bob.colbaskin.stoq.BuildConfig
import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAuthenticator internal constructor(
    private val cookies: SessionCookies,
    private val baseUrl: okhttp3.HttpUrl,
    private val refreshClient: OkHttpClient,
    private val log: (String) -> Unit
) : Authenticator {
    @Inject constructor(cookies: SessionCookieJar) : this(cookies, BuildConfig.BASE_API_URL.toHttpUrl(), OkHttpClient.Builder().build(), {
        if (BuildConfig.DEBUG) android.util.Log.d("StoqAuth", it)
    })
    private data class SessionVersion(val value: Long, val epoch: Long)
    private val lock = Any()
    private var retryAfterNanos = 0L
    private var failedEpoch = -1L

    fun tagRequest(request: Request): Request = request.newBuilder()
        .tag(SessionVersion::class.java, SessionVersion(cookies.revision(), cookies.epoch())).build()

    override fun authenticate(route: Route?, response: Response): Request? = synchronized(lock) {
        val path = response.request.url.encodedPath.removePrefix(baseUrl.encodedPath.trimEnd('/'))
        log("HTTP 401: path=$path, sameOrigin=${cookies.sameOrigin(response.request.url)}, retried=${response.priorResponse != null}, hasSession=${cookies.hasSession()}")
        if (!cookies.sameOrigin(response.request.url) || path in setOf("/auth/login", "/auth/refresh", "/auth/register-user", "/auth/register-restaurant")) return null
        val attempt = response.request.tag(SessionVersion::class.java)
        if (attempt != null && attempt.epoch != cookies.epoch()) return null
        if (response.priorResponse != null) {
            log("Повторный HTTP 401 после обновления: повтор не выполняется")
            return null
        }
        val requestVersion = attempt?.value
        val currentCookies = cookies.loadForRequest(response.request.url)
        if (requestVersion != null && requestVersion != cookies.revision() && currentCookies.any { it.name == "accessToken" }) {
            return tagRequest(response.request).newBuilder()
                .header("Cookie", currentCookies.joinToString("; ") { "${it.name}=${it.value}" }).build()
        }
        if (failedEpoch == cookies.epoch() && System.nanoTime() < retryAfterNanos) {
            log("Повтор refresh отложен после временной ошибки, сессия сохранена")
            return null
        }
        val refreshUrl = baseUrl.resolve("auth/refresh") ?: return null
        val refreshCookies = cookies.loadForRequest(refreshUrl).filter { it.name == "refreshToken" }
        if (refreshCookies.isEmpty()) {
            log("Обновление невозможно: refresh-cookie отсутствует или истекла. Требуется повторный вход")
            cookies.clear()
            return null
        }
        val refreshEpoch = cookies.epoch()
        try {
            log("Обновляем сессию после HTTP 401: refresh-cookie найдена")
            refreshClient.newCall(Request.Builder()
                .url(refreshUrl)
                .header("Cookie", refreshCookies.joinToString("; ") { "${it.name}=${it.value}" })
                .post(ByteArray(0).toRequestBody()).build()).execute().use { refreshed ->
                if (refreshEpoch != cookies.epoch()) return null
                if (refreshed.code == 401 || refreshed.code == 403) {
                    cookies.clear()
                    log("Сессия завершена: refresh отклонён сервером (${refreshed.code})")
                    return null
                }
                if (!refreshed.isSuccessful) {
                    failedEpoch = refreshEpoch
                    retryAfterNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5)
                    log("Обновление временно недоступно (${refreshed.code}), локальная сессия сохранена")
                    return null
                }
                cookies.saveFromResponse(refreshUrl, okhttp3.Cookie.parseAll(refreshUrl, refreshed.headers))
                retryAfterNanos = 0
                log("Сессия успешно обновлена, повторяем исходный запрос")
            }
        } catch (_: IOException) {
            failedEpoch = refreshEpoch
            retryAfterNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5)
            log("Сеть недоступна при обновлении, локальная сессия сохранена")
            return null
        }
        val header = cookies.loadForRequest(response.request.url).joinToString("; ") { "${it.name}=${it.value}" }
        tagRequest(response.request).newBuilder().header("Cookie", header).build()
    }
}
