package bob.colbaskin.stoq.common.userprefs.domain

import bob.colbaskin.stoq.common.userprefs.data.model.UserPreferences
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUser(user: AuthUser)
    suspend fun clearUser()
}
