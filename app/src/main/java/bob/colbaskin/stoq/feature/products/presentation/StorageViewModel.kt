package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val repository: RemoteInventoryRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        state = UiState.Loading
        state = repository.getInventory()
    }
}
