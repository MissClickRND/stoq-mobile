package bob.colbaskin.stoq.feature.auth.presentation.signin

sealed interface SignInAction {
    data class EmailChanged(val value: String) : SignInAction
    data class PasswordChanged(val value: String) : SignInAction
    data object PasswordVisibilityClicked : SignInAction
    data object SignInClicked : SignInAction
    data object ErrorDismissed : SignInAction
}
