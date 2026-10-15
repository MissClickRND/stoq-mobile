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

    init { loadRestaurants() }

    private fun loadRestaurants() {
        state = state.copy(restaurantsState = UiState.Loading)
        viewModelScope.launch {
            val result = authRepository.restaurants().toUiState()
            state = state.copy(
                restaurantsState = result,
                restaurantId = state.restaurantId?.takeIf { id -> (result as? UiState.Success)?.data?.any { it.id == id } == true }
            )
        }
    }

    fun onAction(action: AccessRequestAction) {
        when (action) {
            is AccessRequestAction.FullNameChanged -> state = state.copy(fullName = action.value, fieldError = state.fieldError.copy(fullName = null))
            is AccessRequestAction.EmailChanged -> state = state.copy(email = action.value, fieldError = state.fieldError.copy(email = null))
            is AccessRequestAction.RestaurantSelected -> state = state.copy(restaurantId = action.id, fieldError = state.fieldError.copy(companyName = null))
            AccessRequestAction.ReloadRestaurants -> loadRestaurants()
            is AccessRequestAction.PasswordChanged -> state = state.copy(password = action.value, fieldError = state.fieldError.copy(password = null))
            AccessRequestAction.PasswordVisibilityClicked -> state = state.copy(passwordVisible = !state.passwordVisible)
            AccessRequestAction.SubmitClicked -> submit()
            AccessRequestAction.ErrorDismissed -> state = state.copy(requestState = null)
        }
    }

    private fun submit() {
        if (state.requestState == UiState.Loading) return
        val fieldError = AuthValidation.accessRequest(
            fullName = state.fullName,
            email = state.email,
            companyName = state.restaurantId?.toString().orEmpty()
        ).copy(
            companyName = if ((state.restaurantsState as? UiState.Success)?.data?.any { it.id == state.restaurantId } == true) null else "Выберите ресторан",
            password = AuthValidation.signIn(state.email, state.password).password
        )
        if (fieldError.hasErrors()) {
            state = state.copy(fieldError = fieldError)
            return
        }
        val request = AccessRequest(state.fullName.trim(), state.email.trim(), state.password, state.restaurantId ?: return)
        state = state.copy(requestState = UiState.Loading)
        viewModelScope.launch {
            state = state.copy(
                requestState = authRepository.requestAccess(request).toUiState()
            )
        }
    }
}
