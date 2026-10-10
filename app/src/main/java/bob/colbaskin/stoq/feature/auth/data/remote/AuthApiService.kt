package bob.colbaskin.stoq.feature.auth.data.remote

import bob.colbaskin.stoq.feature.auth.data.model.AccessRequestDto
import bob.colbaskin.stoq.feature.auth.data.model.AuthUserDto
import bob.colbaskin.stoq.feature.auth.data.model.SignInRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/sign-in")
    suspend fun signIn(@Body request: SignInRequestDto): AuthUserDto

    @POST("access-requests")
    suspend fun requestAccess(@Body request: AccessRequestDto)
}
