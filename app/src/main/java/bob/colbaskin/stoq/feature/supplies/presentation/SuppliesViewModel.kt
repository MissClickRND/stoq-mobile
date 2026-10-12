package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.supplies.data.RemoteSupplyRepository
import bob.colbaskin.stoq.feature.supplies.data.Supply
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuppliesViewModel @Inject constructor(
    private val repository: RemoteSupplyRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<List<Supply>>>(UiState.Loading)
        private set

    init { refresh() }

    fun refresh() = viewModelScope.launch { state = repository.getSupplies() }
}

@HiltViewModel
class SupplyDetailsViewModel @Inject constructor(
    private val repository: RemoteSupplyRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<Supply>>(UiState.Loading)
        private set

    fun load(id: String) = viewModelScope.launch {
        state = id.toLongOrNull()?.let { repository.getSupply(it) }
            ?: UiState.Error("Некорректный идентификатор", "Поставка не найдена.")
    }
}

@HiltViewModel
class CreateSupplyViewModel @Inject constructor(
    private val supplyRepository: RemoteSupplyRepository,
    private val productRepository: RemoteProductRepository
) : ViewModel() {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
        private set
    var savingState by mutableStateOf<UiState<Unit>?>(null)
        private set

    init {
        viewModelScope.launch {
            when (val result = productRepository.getProducts()) {
                is UiState.Success -> products = result.data
                else -> Unit
            }
        }
    }

    fun create(request: CreateSupplyDto, onSuccess: () -> Unit) = viewModelScope.launch {
        savingState = UiState.Loading
        savingState = supplyRepository.createSupply(request)
        if (savingState is UiState.Success) onSuccess()
    }
}
