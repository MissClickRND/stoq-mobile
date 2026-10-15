package bob.colbaskin.stoq.common.network

import bob.colbaskin.stoq.BuildConfig
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCookieJar @Inject constructor(
    private val dataStore: UserDataStore,
    private val cipher: SessionCipher
) : SessionCookies {
    private val origin = BuildConfig.BASE_API_URL.toHttpUrl()
    private var loaded = false
    private var cookies = emptyList<Cookie>()
    private var version = 0L
    private var sessionEpoch = 0L

    @Synchronized override fun revision(): Long = version
    @Synchronized override fun epoch(): Long = sessionEpoch

    private fun load() {
        if (loaded) return
        val saved = runBlocking { dataStore.readEncryptedSession() }
        cookies = if (saved.isBlank()) emptyList() else try {
            val array = JSONArray(cipher.decrypt(saved))
            val refreshUrl = origin.resolve("auth/refresh")!!
            (0 until array.length()).mapNotNull { Cookie.parse(origin, array.getString(it))?.withApiAuthPath(origin) }
                .filter { it.expiresAt > System.currentTimeMillis() && (it.matches(origin) || it.matches(refreshUrl)) }
        } catch (_: Exception) {
            runBlocking { dataStore.clearUser() }
            emptyList()
        }
        loaded = true
    }

    @Synchronized override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (!sameOrigin(url)) return
        load()
        val sessionCookies = cookies.filter { it.name == "accessToken" || it.name == "refreshToken" }.map { it.withApiAuthPath(origin) }
        if (sessionCookies.isEmpty()) return
        val replaced = sessionCookies.map { it.name }.toSet()
        this.cookies = (this.cookies.filterNot { it.name in replaced } + sessionCookies)
            .filter { it.expiresAt > System.currentTimeMillis() }
        version++
        persist()
    }

    @Synchronized override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (!sameOrigin(url)) return emptyList()
        load()
        return cookies.filter { it.expiresAt > System.currentTimeMillis() && it.matches(url) }
    }

    @Synchronized override fun hasSession(): Boolean {
        load()
        return cookies.any { it.expiresAt > System.currentTimeMillis() }
    }

    @Synchronized override fun clear() {
        cookies = emptyList()
        version++
        loaded = true
        sessionEpoch++
        runBlocking { dataStore.clearUser() }
    }

    override fun sameOrigin(url: HttpUrl) = url.host == origin.host && url.port == origin.port && url.scheme == origin.scheme

    fun socketCookieHeader(): String = loadForRequest(origin).joinToString("; ") { "${it.name}=${it.value}" }

    private fun persist() {
        val encoded = if (cookies.isEmpty()) "" else cipher.encrypt(JSONArray(cookies.map(Cookie::toString)).toString())
        runBlocking { dataStore.saveEncryptedSession(encoded) }
    }
}
