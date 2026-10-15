package bob.colbaskin.stoq.feature.dashboard.presentation

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
class DashboardViewModel @Inject constructor(
    private val inventoryRepository: RemoteInventoryRepository
) : ViewModel() {
    var expiringInventoryState by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set

    fun loadExpiringInventory(daysThreshold: Int) = viewModelScope.launch {
        expiringInventoryState = UiState.Loading
        expiringInventoryState = inventoryRepository.getExpiringInventory(daysThreshold.coerceAtLeast(1))
    }

    fun openSupplyForBatch(productId: Long, batchId: Long, onSupplyFound: (Long) -> Unit) = viewModelScope.launch {
        val batch = (inventoryRepository.getInventoryByProduct(productId) as? UiState.Success)
            ?.data
            ?.firstOrNull { it.batchId == batchId }
        batch?.supplyId?.let(onSupplyFound)
    }
}
