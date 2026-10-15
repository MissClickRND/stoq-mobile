package bob.colbaskin.stoq.feature.auth.presentation

data class FieldError(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val companyName: String? = null,
    val role: String? = null,
    val reason: String? = null,
    val consent: String? = null
)

object AuthValidation {
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun signIn(email: String, password: String): FieldError = FieldError(
        email = emailError(email),
        password = when {
            password.isBlank() -> "Введите пароль"
            password.length < 4 -> "Пароль должен содержать не менее 4 символов"
            else -> null
        }
    )

    fun accessRequest(fullName: String, email: String, companyName: String): FieldError = FieldError(
        fullName = when {
            fullName.trim().isBlank() -> "Введите имя и фамилию"
            fullName.trim().split(Regex("\\s+")).size < 2 -> "Укажите имя и фамилию"
            else -> null
        },
        email = emailError(email),
        companyName = if (companyName.trim().isBlank()) "Укажите компанию или ресторан" else null
    )

    fun emailError(email: String): String? = when {
        email.trim().isBlank() -> "Введите почту"
        !emailPattern.matches(email.trim()) -> "Введите корректный email"
        else -> null
    }
}

fun FieldError.hasErrors(): Boolean = listOf(
    fullName,
    email,
    password,
    companyName
).any { it != null }
