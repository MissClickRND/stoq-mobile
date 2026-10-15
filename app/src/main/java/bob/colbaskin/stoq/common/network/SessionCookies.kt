package bob.colbaskin.stoq.common.network

import okhttp3.CookieJar
import okhttp3.HttpUrl

interface SessionCookies : CookieJar {
    fun revision(): Long
    fun epoch(): Long
    fun hasSession(): Boolean
    fun clear()
    fun sameOrigin(url: HttpUrl): Boolean
}
