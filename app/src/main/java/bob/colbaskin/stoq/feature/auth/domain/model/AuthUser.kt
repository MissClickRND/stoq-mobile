package bob.colbaskin.stoq.feature.auth.domain.model

data class AuthUser(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val restaurantId: Long? = null,
    val restaurantName: String? = null,
    val restaurantAddress: String? = null,
    val restaurantEmail: String? = null,
    val restaurantPhone: String? = null
)
