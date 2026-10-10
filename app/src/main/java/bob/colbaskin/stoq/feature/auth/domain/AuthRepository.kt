package bob.colbaskin.stoq.feature.auth.domain

import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser

interface AuthRepository {
    suspend fun signIn(email: String, password: String): ApiResult<AuthUser>
    suspend fun requestAccess(request: AccessRequest): ApiResult<Unit>
}
