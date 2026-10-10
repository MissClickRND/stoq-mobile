package bob.colbaskin.stoq.feature.auth.presentation.signin

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.presentation.FieldError

data class SignInState(
    val email: String = "",
    val password: String = "",
    val fieldError: FieldError = FieldError(),
    val signInState: UiState<AuthUser>? = null,
    val isPasswordVisible: Boolean = false
)
