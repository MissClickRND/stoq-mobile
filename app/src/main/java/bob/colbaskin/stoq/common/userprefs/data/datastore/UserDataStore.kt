package bob.colbaskin.stoq.common.userprefs.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import bob.colbaskin.stoq.common.userprefs.data.model.AuthConfig
import bob.colbaskin.stoq.common.userprefs.data.model.UserPreferences
import bob.colbaskin.stoq.common.userprefs.data.model.toData
import bob.colbaskin.stoq.datastore.AuthStatus
import bob.colbaskin.stoq.datastore.RoleStatus
import bob.colbaskin.stoq.datastore.UserPreferencesProto
import bob.colbaskin.stoq.datastore.copy
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private const val UserPreferencesFileName = "user_preferences.pb"

private val Context.userPreferencesStore: DataStore<UserPreferencesProto> by dataStore(
    fileName = UserPreferencesFileName,
    serializer = UserPreferencesSerializer
)

class UserDataStore(context: Context) {
    private val dataStore: DataStore<UserPreferencesProto> = context.userPreferencesStore

    fun getUserPreferences(): Flow<UserPreferences> = dataStore.data.map { it.toData() }

    suspend fun saveUser(user: AuthUser) {
        dataStore.updateData { preferences ->
            preferences.copy {
                authStatus = AuthStatus.AUTHENTICATED
                roleStatus = user.role.toProto()
                userId = user.id
                userName = user.fullName
                userEmail = user.email
                restaurantId = user.restaurantId ?: 0
                restaurantName = user.restaurantName.orEmpty()
                restaurantAddress = user.restaurantAddress.orEmpty()
                restaurantEmail = user.restaurantEmail.orEmpty()
                restaurantPhone = user.restaurantPhone.orEmpty()
                userAvatarUrl = user.avatarUrl.orEmpty()
            }
        }
    }

    suspend fun clearUser() {
        dataStore.updateData { UserPreferencesProto.getDefaultInstance() }
    }

    suspend fun readEncryptedSession(): String = dataStore.data.first().encryptedSession

    suspend fun saveEncryptedSession(value: String) {
        dataStore.updateData { preferences -> preferences.copy { encryptedSession = value } }
    }

    suspend fun saveAuthStatus(status: AuthConfig) {
        if (status == AuthConfig.NotAuthenticated) clearUser()
    }
}

private fun UserRole.toProto(): RoleStatus = when (this) {
    UserRole.Manager -> RoleStatus.MANAGER
    UserRole.Administrator -> RoleStatus.ADMINISTRATOR
    UserRole.Waiter -> RoleStatus.WAITER
    UserRole.Storekeeper -> RoleStatus.STOREKEEPER
    UserRole.Pending -> RoleStatus.PENDING
}
