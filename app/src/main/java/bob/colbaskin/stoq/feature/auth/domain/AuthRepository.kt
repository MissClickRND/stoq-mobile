package bob.colbaskin.stoq.feature.auth.domain

import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser

interface AuthRepository {
    suspend fun restaurants(): ApiResult<List<bob.colbaskin.stoq.feature.auth.domain.model.RestaurantOption>>
    suspend fun signIn(email: String, password: String): ApiResult<AuthUser>
    suspend fun requestAccess(request: AccessRequest): ApiResult<Unit>
    suspend fun registerRestaurant(fullName: String, email: String, password: String, name: String, address: String): ApiResult<AuthUser>
    suspend fun refreshProfile(): ApiResult<AuthUser>
    suspend fun restoreSession()
    suspend fun signOut(): ApiResult<Unit>
}
