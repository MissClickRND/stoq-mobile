package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.common.network.SessionCookieJar
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import bob.colbaskin.stoq.feature.auth.data.model.RegisterRestaurantDto
import bob.colbaskin.stoq.feature.auth.data.model.SignInRequestDto
import bob.colbaskin.stoq.feature.auth.data.remote.AuthApiService
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val cookies: SessionCookieJar,
    private val preferences: UserPreferencesRepository
) : AuthRepository {
    override suspend fun restaurants() = safeApiCall(
        apiCall = { api.restaurants() },
        successHandler = { restaurants -> restaurants.map { bob.colbaskin.stoq.feature.auth.domain.model.RestaurantOption(it.id, it.name, it.address) } }
    )

    override suspend fun signIn(email: String, password: String): ApiResult<AuthUser> = withContext(Dispatchers.IO) {
        safeApiCall(apiCall = {
            cookies.clear()
            api.signIn(SignInRequestDto(email.trim(), password))
            val user = api.status().user.toDomain()
            check(cookies.hasSession()) { "Session cookies missing" }
            preferences.saveUser(user)
            user
        }, successHandler = { it })
    }

    override suspend fun requestAccess(request: AccessRequest): ApiResult<Unit> = safeApiCall(
        apiCall = { api.requestAccess(request.toDto()) }, successHandler = { Unit }
    )

    override suspend fun registerRestaurant(fullName: String, email: String, password: String, name: String, address: String): ApiResult<AuthUser> = withContext(Dispatchers.IO) {
        safeApiCall(apiCall = {
            cookies.clear()
            val registered = api.registerRestaurant(RegisterRestaurantDto(fullName.trim(), email.trim(), password, name.trim(), address.trim().takeIf(String::isNotBlank)))
            api.signIn(SignInRequestDto(email.trim(), password))
            val user = api.status().user.toDomain().copy(
                restaurantName = registered.restaurant.name,
                restaurantAddress = registered.restaurant.address,
                restaurantEmail = registered.restaurant.email,
                restaurantPhone = registered.restaurant.phone
            )
            check(cookies.hasSession())
            preferences.saveUser(user)
            user
        }, successHandler = { it })
    }

    override suspend fun refreshProfile(): ApiResult<AuthUser> = withContext(Dispatchers.IO) {
        safeApiCall(apiCall = {
            // A single serialized refresh/retry is handled by SessionAuthenticator.
            val response = api.status()
            check(response.authenticated)
            val previous = preferences.getUserPreferences().first().user
            val user = response.user.toDomain().let { current ->
                if (previous != null && previous.restaurantId == current.restaurantId) current.copy(
                    restaurantName = previous.restaurantName,
                    restaurantAddress = previous.restaurantAddress,
                    restaurantEmail = previous.restaurantEmail,
                    restaurantPhone = previous.restaurantPhone
                ) else current
            }
            preferences.saveUser(user)
            user
        }, successHandler = { it })
    }

    override suspend fun restoreSession() = withContext(Dispatchers.IO) {
        if (!cookies.hasSession()) cookies.clear()
        Unit
    }

    override suspend fun signOut(): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            safeApiCall(apiCall = { api.logout() }, successHandler = { Unit })
        } finally {
            cookies.clear()
        }
    }
}
