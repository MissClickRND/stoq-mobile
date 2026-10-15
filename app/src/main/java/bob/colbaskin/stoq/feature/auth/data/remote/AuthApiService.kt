package bob.colbaskin.stoq.feature.auth.data.remote

import bob.colbaskin.stoq.feature.auth.data.model.AccessRequestDto
import bob.colbaskin.stoq.feature.auth.data.model.*
import bob.colbaskin.stoq.feature.auth.data.model.SignInRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path

interface AuthApiService {
    @GET("auth/admin/invitations") suspend fun invitations(): List<InvitationDto>
    @POST("auth/admin/invitations") suspend fun createInvitation(@Body request: CreateInvitationDto): InvitationDto
    @PATCH("auth/admin/invitations/{id}/revoke") suspend fun revokeInvitation(@Path("id") id: String)
    @DELETE("auth/admin/invitations/{id}") suspend fun deleteInvitation(@Path("id") id: String)
    @PATCH("auth/admin/users/{id}/block") suspend fun setUserBlocked(@Path("id") id: Long, @Body request: SetUserBlockDto)

    @GET("auth/admin/users")
    suspend fun users(): List<AdminUserDto>

    @GET("auth/admin/restaurant")
    suspend fun restaurant(): RestaurantDto

    @PATCH("auth/admin/users/{id}/role")
    suspend fun changeUserRole(@Path("id") id: Long, @Body request: ApproveUserDto)

    @GET("restaurants")
    suspend fun restaurants(): List<RestaurantDto>

    @POST("auth/login")
    suspend fun signIn(@Body request: SignInRequestDto): AuthResponseDto

    @POST("auth/register-user")
    suspend fun requestAccess(@Body request: AccessRequestDto)

    @POST("auth/register-restaurant")
    suspend fun registerRestaurant(@Body request: RegisterRestaurantDto): RegisteredRestaurantDto

    @GET("auth/status")
    suspend fun status(): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()

    @GET("auth/admin/users/pending")
    suspend fun pendingUsers(): List<PendingUserDto>

    @PATCH("auth/admin/users/{id}/approve")
    suspend fun approveUser(@Path("id") id: Long, @Body request: ApproveUserDto)

    @DELETE("auth/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long)

    @PATCH("auth/admin/restaurant")
    suspend fun updateRestaurant(@Body request: UpdateRestaurantDto): RestaurantDto
}
