package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

sealed interface AccessRequestAction {
    data class FullNameChanged(val value: String) : AccessRequestAction
    data class EmailChanged(val value: String) : AccessRequestAction
    data class CompanyNameChanged(val value: String) : AccessRequestAction
    data class RoleSelected(val value: UserRole) : AccessRequestAction
    data class ReasonChanged(val value: String) : AccessRequestAction
    data object ConsentClicked : AccessRequestAction
    data object SubmitClicked : AccessRequestAction
    data object ErrorDismissed : AccessRequestAction
}
