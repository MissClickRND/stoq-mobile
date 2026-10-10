package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import javax.inject.Inject

class MockAuthDataSource @Inject constructor() {
    suspend fun signIn(email: String, password: String): ApiResult<AuthUser> = ApiResult.Success(
        AuthUser(
            id = "local-user",
            fullName = "Пользователь STOQ",
            email = email,
            role = UserRole.Manager
        )
    )

    suspend fun requestAccess(request: AccessRequest): ApiResult<Unit> = ApiResult.Success(Unit)
}
