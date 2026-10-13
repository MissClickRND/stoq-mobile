package bob.colbaskin.stoq.feature.orders.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
import bob.colbaskin.stoq.feature.orders.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Order>>(UiState.Loading)
    val state = _state.asStateFlow()
    var isRefreshing by mutableStateOf(false)
        private set

    fun load(id: String) = viewModelScope.launch {
        val previous = _state.value
        val hasData = previous is UiState.Success
        if (hasData) isRefreshing = true else _state.update { UiState.Loading }
        val result = repository.order(id)
        _state.update { if (hasData && result is UiState.Error) previous else result }
        isRefreshing = false
    }

    fun delete(id: String, onDeleted: () -> Unit) = viewModelScope.launch {
        _state.update { UiState.Loading }
        when (val result = repository.delete(id)) {
            is UiState.Success -> onDeleted()
            is UiState.Error -> _state.update { result }
            UiState.Loading -> Unit
        }
    }

    fun complete(id: String) = viewModelScope.launch {
        _state.update { UiState.Loading }
        _state.update { repository.complete(id) }
    }
}
