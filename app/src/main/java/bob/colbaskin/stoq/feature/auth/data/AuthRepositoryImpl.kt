package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.BuildConfig
import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import bob.colbaskin.stoq.feature.auth.data.model.SignInRequestDto
import bob.colbaskin.stoq.feature.auth.data.remote.AuthApiService
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val mockAuthDataSource: MockAuthDataSource,
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): ApiResult<AuthUser> {
        val result = if (BuildConfig.BASE_API_URL == DEFAULT_API_URL) {
            mockAuthDataSource.signIn(email, password)
        } else when (val remoteResult = safeApiCall(
            apiCall = { authApiService.signIn(SignInRequestDto(email, password)) },
            successHandler = { it.toDomain() }
        )) {
            is ApiResult.Success -> remoteResult
            is ApiResult.Error -> mockAuthDataSource.signIn(email, password)
        }
        if (result is ApiResult.Success) userPreferencesRepository.saveUser(result.data)
        return result
    }

    override suspend fun requestAccess(request: AccessRequest): ApiResult<Unit> {
        if (BuildConfig.BASE_API_URL == DEFAULT_API_URL) {
            return mockAuthDataSource.requestAccess(request)
        }
        return when (val result = safeApiCall(
            apiCall = { authApiService.requestAccess(request.toDto()) },
            successHandler = { Unit }
        )) {
            is ApiResult.Success -> result
            is ApiResult.Error -> mockAuthDataSource.requestAccess(request)
        }
    }

    private companion object {
        const val DEFAULT_API_URL = "https://example.invalid/"
    }
}
