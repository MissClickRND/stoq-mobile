package bob.colbaskin.stoq.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.userprefs.data.model.UserPreferences
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: UserPreferencesRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private var checkedUserId: String? = null
    val uiState: StateFlow<UiState<UserPreferences>> = repository.getUserPreferences()
        .onStart { authRepository.restoreSession() }
        .onEach { preferences ->
            val id = preferences.user?.id
            if (id == null) checkedUserId = null
            else if (checkedUserId != id) {
                checkedUserId = id
                viewModelScope.launch { authRepository.refreshProfile() }
            }
        }
        .map { UiState.Success(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            initialValue = UiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )
}
