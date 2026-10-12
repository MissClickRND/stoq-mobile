package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import bob.colbaskin.stoq.feature.products.data.model.CreateProductDto
import bob.colbaskin.stoq.feature.products.data.model.CreateUnitDto
import bob.colbaskin.stoq.feature.products.data.model.ProductUnitDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val repository: RemoteInventoryRepository,
    private val productRepository: RemoteProductRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set
    var units by mutableStateOf<List<ProductUnitDto>>(emptyList())
        private set
    var actionState by mutableStateOf<UiState<Unit>?>(null)
        private set

    init {
        refresh()
        refreshUnits()
    }

    fun refresh() = viewModelScope.launch {
        state = UiState.Loading
        state = repository.getInventory()
    }

    fun createProduct(name: String, unitId: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.createProduct(CreateProductDto(name.trim(), unitId))
        if (actionState is UiState.Success) {
            refresh()
            onSuccess()
        }
    }

    fun createUnit(name: String, shortName: String) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.createUnit(CreateUnitDto(name.trim(), shortName.trim()))
        if (actionState is UiState.Success) refreshUnits()
    }

    fun deleteProduct(id: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.deleteProduct(id)
        if (actionState is UiState.Success) {
            refresh()
            onSuccess()
        }
    }

    private fun refreshUnits() = viewModelScope.launch {
        when (val result = productRepository.getUnits()) {
            is UiState.Success -> units = result.data
            else -> Unit
        }
    }
}
