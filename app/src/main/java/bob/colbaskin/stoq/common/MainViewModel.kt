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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: UserPreferencesRepository
) : ViewModel() {
    val uiState: StateFlow<UiState<UserPreferences>> = repository.getUserPreferences()
        .map { UiState.Success(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            initialValue = UiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )
}
