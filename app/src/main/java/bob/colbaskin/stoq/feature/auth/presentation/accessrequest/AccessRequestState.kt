package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import bob.colbaskin.stoq.feature.auth.presentation.FieldError

data class AccessRequestState(
    val fullName: String = "",
    val email: String = "",
    val companyName: String = "",
    val selectedRole: UserRole? = null,
    val reason: String = "",
    val consentAccepted: Boolean = false,
    val fieldError: FieldError = FieldError(),
    val requestState: UiState<Unit>? = null
)
