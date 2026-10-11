package bob.colbaskin.stoq.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileAction {
    data object SignOut : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun onAction(action: ProfileAction) {
        if (action == ProfileAction.SignOut) {
            viewModelScope.launch { userPreferencesRepository.clearUser() }
        }
    }
}
