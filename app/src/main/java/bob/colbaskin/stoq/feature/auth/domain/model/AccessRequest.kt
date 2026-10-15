package bob.colbaskin.stoq.feature.auth.domain.model

data class AccessRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val restaurantId: Long
)
