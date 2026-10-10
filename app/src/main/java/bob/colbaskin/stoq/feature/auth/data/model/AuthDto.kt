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
    val companyName: String,
    val role: String,
    val reason: String
)

@Serializable
data class AuthUserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    val avatarUrl: String? = null
)
