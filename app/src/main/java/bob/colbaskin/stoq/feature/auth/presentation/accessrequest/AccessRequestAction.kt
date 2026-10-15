package bob.colbaskin.stoq.feature.auth.presentation.accessrequest


sealed interface AccessRequestAction {
    data class FullNameChanged(val value: String) : AccessRequestAction
    data class EmailChanged(val value: String) : AccessRequestAction
    data class RestaurantSelected(val id: Long) : AccessRequestAction
    data object ReloadRestaurants : AccessRequestAction
    data class PasswordChanged(val value: String) : AccessRequestAction
    data object PasswordVisibilityClicked : AccessRequestAction
    data object SubmitClicked : AccessRequestAction
    data object ErrorDismissed : AccessRequestAction
}
