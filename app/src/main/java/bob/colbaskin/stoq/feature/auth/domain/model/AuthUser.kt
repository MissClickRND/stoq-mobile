package bob.colbaskin.stoq.feature.auth.domain.model

data class AuthUser(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String? = null
)
