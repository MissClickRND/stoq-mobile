package bob.colbaskin.stoq.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AccessRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val restaurantId: Long
)

@Serializable
data class AuthUserDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val avatarUrl: String? = null,
    val restaurantId: Long
)

@Serializable
data class AuthResponseDto(val user: AuthUserDto, val authenticated: Boolean = true)

@Serializable
data class RestaurantDto(val id: Long, val name: String, val address: String? = null, val email: String? = null, val phone: String? = null)

@Serializable
data class RegisterRestaurantDto(val fullName: String, val email: String, val password: String, val restaurantName: String, val restaurantAddress: String? = null)

@Serializable
data class RegisteredRestaurantDto(val user: AuthUserDto, val restaurant: RestaurantDto)

@Serializable
data class PendingUserDto(val id: Long, val fullName: String, val email: String, val createdAt: String? = null)

@Serializable
data class ApproveUserDto(val role: String)

@Serializable
data class UpdateRestaurantDto(val name: String? = null, val address: String? = null, val email: String? = null, val phone: String? = null)
