package bob.colbaskin.stoq.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileAction {
    data object SignOut : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<AuthUser>?>(null)
        private set
    var signingOut by mutableStateOf(false)
        private set

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        state = UiState.Loading
        state = authRepository.refreshProfile().toUiState()
    }
    fun onAction(action: ProfileAction) {
        if (action == ProfileAction.SignOut) {
            viewModelScope.launch {
                signingOut = true
                authRepository.signOut()
                signingOut = false
            }
        }
    }
}
