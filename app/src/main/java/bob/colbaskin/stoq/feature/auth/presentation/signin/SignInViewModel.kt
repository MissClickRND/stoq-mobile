package bob.colbaskin.stoq.feature.auth.presentation.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.feature.auth.presentation.AuthValidation
import bob.colbaskin.stoq.feature.auth.presentation.hasErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf(SignInState())
        private set

    fun onAction(action: SignInAction) {
        when (action) {
            is SignInAction.EmailChanged -> state = state.copy(email = action.value, fieldError = state.fieldError.copy(email = null))
            is SignInAction.PasswordChanged -> state = state.copy(password = action.value, fieldError = state.fieldError.copy(password = null))
            SignInAction.PasswordVisibilityClicked -> state = state.copy(isPasswordVisible = !state.isPasswordVisible)
            SignInAction.SignInClicked -> signIn()
            SignInAction.ErrorDismissed -> state = state.copy(signInState = null)
        }
    }

    private fun signIn() {
        val fieldError = AuthValidation.signIn(state.email, state.password)
        if (fieldError.hasErrors()) {
            state = state.copy(fieldError = fieldError)
            return
        }
        state = state.copy(signInState = bob.colbaskin.stoq.common.core.UiState.Loading)
        viewModelScope.launch {
            state = state.copy(
                signInState = authRepository.signIn(
                    email = state.email.trim(),
                    password = state.password
                ).toUiState()
            )
        }
    }
}
