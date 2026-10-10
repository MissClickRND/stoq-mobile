package bob.colbaskin.stoq.feature.auth.presentation

import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

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
            password.length < 8 -> "Пароль должен содержать не менее 8 символов"
            else -> null
        }
    )

    fun accessRequest(
        fullName: String,
        email: String,
        companyName: String,
        role: UserRole?,
        reason: String,
        consentAccepted: Boolean
    ): FieldError = FieldError(
        fullName = when {
            fullName.trim().isBlank() -> "Введите имя и фамилию"
            fullName.trim().split(Regex("\\s+")).size < 2 -> "Укажите имя и фамилию"
            else -> null
        },
        email = emailError(email),
        companyName = if (companyName.trim().isBlank()) "Укажите компанию или ресторан" else null,
        role = if (role == null) "Выберите роль" else null,
        reason = if (reason.trim().length < 10) "Опишите цель запроса не менее чем в 10 символах" else null,
        consent = if (consentAccepted) null else "Необходимо согласие на обработку данных"
    )

    private fun emailError(email: String): String? = when {
        email.trim().isBlank() -> "Введите рабочую почту"
        !emailPattern.matches(email.trim()) -> "Введите корректный email"
        else -> null
    }
}

fun FieldError.hasErrors(): Boolean = listOf(
    fullName,
    email,
    password,
    companyName,
    role,
    reason,
    consent
).any { it != null }
