package bob.colbaskin.stoq.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import android.database.sqlite.SQLiteDatabase
import bob.colbaskin.stoq.common.drafts.data.StoqDatabase
import bob.colbaskin.stoq.common.drafts.data.DraftOwnerMigration
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.BuildConfig
import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.common.network.SessionAuthenticator
import bob.colbaskin.stoq.common.network.SessionCipher
import bob.colbaskin.stoq.common.network.SessionCookieJar
import bob.colbaskin.stoq.common.userprefs.data.UserPreferencesRepositoryImpl
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserDataStore
import bob.colbaskin.stoq.common.userprefs.data.model.AuthConfig
import bob.colbaskin.stoq.di.NetworkModule
import bob.colbaskin.stoq.feature.auth.data.AuthRepositoryImpl
import bob.colbaskin.stoq.feature.auth.data.remote.AuthApiService
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SessionIntegrationTest {
    private fun isolatedDataStore(): UserDataStore {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(app.cacheDir, "auth-tests/${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(app) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = directory
        }
        return UserDataStore(context)
    }

    @Test fun keystoreEncryptsWithRandomIvAndRejectsTampering() {
        val cipher = SessionCipher()
        val plaintext = "accessToken=test-access;refreshToken=test-refresh"
        val encrypted = cipher.encrypt(plaintext)
        assertNotEquals(encrypted, cipher.encrypt(plaintext))
        assertFalse(encrypted.contains("test-access"))
        assertEquals(plaintext, cipher.decrypt(encrypted))
        val tampered = (if (encrypted.first() == 'A') "B" else "A") + encrypted.drop(1)
        assertTrue(runCatching { cipher.decrypt(tampered) }.isFailure)
    }

    @Test fun cookiePersistenceRespectsRefreshPathAndOrigin() = runBlocking {
        val store = isolatedDataStore()
        val cipher = SessionCipher()
        val jar = SessionCookieJar(store, cipher)
        val base = BuildConfig.BASE_API_URL.toHttpUrl()
        val login = base.resolve("auth/login")!!
        jar.saveFromResponse(login, listOf(
            Cookie.parse(login, "accessToken=test-access; Max-Age=900; Path=/; HttpOnly")!!,
            Cookie.parse(login, "refreshToken=test-refresh; Max-Age=604800; Path=/auth; HttpOnly")!!
        ))
        val persisted = store.readEncryptedSession()
        assertFalse(persisted.contains("test-access"))
        assertFalse(persisted.contains("test-refresh"))
        val restored = SessionCookieJar(store, SessionCipher())
        assertEquals(listOf("accessToken"), restored.loadForRequest(base.resolve("inventory")!!).map { it.name })
        assertEquals(2, restored.loadForRequest(base.resolve("auth/refresh")!!).size)
        assertTrue(restored.loadForRequest("https://example.com/inventory".toHttpUrl()).isEmpty())
        restored.clear()
        assertFalse(restored.hasSession())
        assertEquals("", store.readEncryptedSession())
    }

    @Test fun draftMigrationPreservesDataAndSeparatesAccounts() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(app.cacheDir, "draft-migration-${UUID.randomUUID()}.db")
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE drafts (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, title TEXT NOT NULL, summary TEXT NOT NULL, updatedAtEpochMillis INTEGER NOT NULL, payloadJson TEXT NOT NULL)")
            db.execSQL("CREATE INDEX index_drafts_type_updatedAtEpochMillis ON drafts(type, updatedAtEpochMillis)")
            db.execSQL("INSERT INTO drafts VALUES ('legacy','Order','Legacy draft','',0,'{}')")
            db.version = 1
        }
        val database = Room.databaseBuilder(app, StoqDatabase::class.java, file.absolutePath).addMigrations(DraftOwnerMigration).build()
        try {
            val store = isolatedDataStore()
            val preferences = UserPreferencesRepositoryImpl(store)
            val repository = DraftRepository(database.draftDao(), preferences)
            val owner = AuthUser("5", "Тест Администратор", "owner@example.com", UserRole.Administrator, restaurantId = 4)
            preferences.saveUser(owner)
            val saved = repository.save(DraftType.Order, "Test", "", "{}")
            assertEquals(listOf(saved.id), repository.observe().first().map { it.id })
            preferences.saveUser(owner.copy(id = "6", role = UserRole.Waiter))
            assertTrue(repository.observe().first().isEmpty())
            assertNull(repository.find(saved.id))
            repository.delete(saved.id)
            preferences.saveUser(owner)
            assertNotNull(repository.find(saved.id))
            preferences.saveUser(owner.copy(restaurantId = 100))
            assertTrue(repository.observe().first().isEmpty())
            assertTrue(database.draftDao().observeAll().first().any { it.id == "legacy" })
        } finally { database.close() }
    }

    @Test fun realSessionRefreshOfflineCacheAndLogout() = runBlocking {
        val store = isolatedDataStore()
        val preferences = UserPreferencesRepositoryImpl(store)
        val jar = SessionCookieJar(store, SessionCipher())
        val client = NetworkModule.provideOkHttpClient(jar, SessionAuthenticator(jar))
        val api = NetworkModule.provideRetrofit(client, NetworkModule.provideJson()).create(AuthApiService::class.java)
        val repository = AuthRepositoryImpl(api, jar, preferences)
        val signedIn = repository.signIn("stoq.android.auth.20261015@example.com", "StoqTest2026!")
        assertTrue("The test API must be reachable and smoke-test owner must exist", signedIn is ApiResult.Success)
        val user = (signedIn as ApiResult.Success).data
        assertEquals(UserRole.Administrator, user.role)
        assertEquals(4L, user.restaurantId)
        assertFalse(store.readEncryptedSession().contains("accessToken"))

        // Force the access cookie to expire; a real 401 must transparently refresh it.
        val base = BuildConfig.BASE_API_URL.toHttpUrl()
        jar.saveFromResponse(base, listOf(Cookie.Builder().name("accessToken").value("expired").hostOnlyDomain(base.host).path("/").expiresAt(0).build()))
        assertTrue("Refresh cookie must remain after access expires", jar.hasSession())
        assertEquals(listOf("refreshToken"), jar.loadForRequest(base.resolve("auth/status")!!).map { it.name })
        val concurrentIds = coroutineScope { List(4) { async { api.status().user.id } }.awaitAll() }
        assertTrue(concurrentIds.all { it == user.id.toLong() })
        assertTrue(jar.loadForRequest(base).any { it.name == "accessToken" })

        val offlineClient = client.newBuilder().addInterceptor { throw IOException("Simulated offline") }.build()
        val offlineApi = NetworkModule.provideRetrofit(offlineClient, NetworkModule.provideJson()).create(AuthApiService::class.java)
        val offlineRepository = AuthRepositoryImpl(offlineApi, jar, preferences)
        offlineRepository.restoreSession()
        assertTrue(offlineRepository.refreshProfile() is ApiResult.Error)
        assertEquals(user.id, preferences.getUserPreferences().first().user?.id)
        assertTrue(jar.hasSession())

        val restoredJar = SessionCookieJar(store, SessionCipher())
        val restoredClient = NetworkModule.provideOkHttpClient(restoredJar, SessionAuthenticator(restoredJar))
        val restoredApi = NetworkModule.provideRetrofit(restoredClient, NetworkModule.provideJson()).create(AuthApiService::class.java)
        assertEquals(user.id.toLong(), restoredApi.status().user.id)
        val restoredRepository = AuthRepositoryImpl(restoredApi, restoredJar, preferences)
        assertTrue(restoredRepository.signOut() is ApiResult.Success)
        assertFalse(restoredJar.hasSession())
        assertEquals(AuthConfig.NotAuthenticated, preferences.getUserPreferences().first().authStatus)
        assertNull(preferences.getUserPreferences().first().user)
        assertTrue(restoredRepository.signIn("stoq.android.auth.20261015@example.com", "IncorrectPassword!") is ApiResult.Error)
        assertNull(preferences.getUserPreferences().first().user)

        // A missing refresh token must end the local session instead of retrying forever.
        assertTrue(restoredRepository.signIn("stoq.android.auth.20261015@example.com", "StoqTest2026!") is ApiResult.Success)
        restoredJar.saveFromResponse(base, listOf(
            Cookie.Builder().name("refreshToken").value("removed").hostOnlyDomain(base.host).path("/auth").expiresAt(0).build(),
            Cookie.Builder().name("accessToken").value("invalid").hostOnlyDomain(base.host).path("/").expiresAt(System.currentTimeMillis() + 60_000).build()
        ))
        assertTrue(restoredRepository.refreshProfile() is ApiResult.Error)
        assertFalse(restoredJar.hasSession())
        assertNull(preferences.getUserPreferences().first().user)
    }
}
