package bob.colbaskin.stoq.common.userprefs.data

import bob.colbaskin.stoq.common.userprefs.data.datastore.UserDataStore
import bob.colbaskin.stoq.common.userprefs.data.model.UserPreferences
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userDataStore: UserDataStore
) : UserPreferencesRepository {
    override fun getUserPreferences(): Flow<UserPreferences> = userDataStore.getUserPreferences()

    override suspend fun saveUser(user: AuthUser) = userDataStore.saveUser(user)

    override suspend fun clearUser() = userDataStore.clearUser()
}
