package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import bob.colbaskin.stoq.feature.auth.presentation.AuthValidation
import bob.colbaskin.stoq.feature.auth.presentation.hasErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccessRequestViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf(AccessRequestState())
        private set

    fun onAction(action: AccessRequestAction) {
        when (action) {
            is AccessRequestAction.FullNameChanged -> state = state.copy(fullName = action.value, fieldError = state.fieldError.copy(fullName = null))
            is AccessRequestAction.EmailChanged -> state = state.copy(email = action.value, fieldError = state.fieldError.copy(email = null))
            is AccessRequestAction.CompanyNameChanged -> state = state.copy(companyName = action.value, fieldError = state.fieldError.copy(companyName = null))
            is AccessRequestAction.RoleSelected -> state = state.copy(selectedRole = action.value, fieldError = state.fieldError.copy(role = null))
            is AccessRequestAction.ReasonChanged -> state = state.copy(reason = action.value, fieldError = state.fieldError.copy(reason = null))
            AccessRequestAction.ConsentClicked -> state = state.copy(consentAccepted = !state.consentAccepted, fieldError = state.fieldError.copy(consent = null))
            AccessRequestAction.SubmitClicked -> submit()
            AccessRequestAction.ErrorDismissed -> state = state.copy(requestState = null)
        }
    }

    private fun submit() {
        val fieldError = AuthValidation.accessRequest(
            fullName = state.fullName,
            email = state.email,
            companyName = state.companyName
        )
        if (fieldError.hasErrors()) {
            state = state.copy(fieldError = fieldError)
            return
        }
        state = state.copy(requestState = UiState.Loading)
        viewModelScope.launch {
            state = state.copy(
                requestState = authRepository.requestAccess(
                    AccessRequest(
                        fullName = state.fullName.trim(),
                        email = state.email.trim(),
                        companyName = state.companyName.trim(),
                        requestedRole = UserRole.Manager,
                        reason = "Запрос доступа через мобильное приложение"
                    )
                ).toUiState()
            )
        }
    }
}
