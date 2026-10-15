package bob.colbaskin.stoq.feature.auth.presentation.restaurantregistration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantRegistrationViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    var state by mutableStateOf<UiState<AuthUser>?>(null)
        private set

    fun register(fullName: String, email: String, password: String, name: String, address: String) {
        if (state == UiState.Loading) return
        state = UiState.Loading
        viewModelScope.launch { state = repository.registerRestaurant(fullName, email, password, name, address).toUiState() }
    }
}
