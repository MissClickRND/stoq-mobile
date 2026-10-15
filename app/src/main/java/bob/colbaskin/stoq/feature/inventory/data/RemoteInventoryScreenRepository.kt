package bob.colbaskin.stoq.feature.inventory.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteInventoryScreenRepository @Inject constructor(
    private val remoteRepository: RemoteInventoryRepository
) : InventoryRepository {
    override suspend fun products(): UiState<List<InventoryProduct>> = when (val result = remoteRepository.getInventory()) {
        is UiState.Success -> UiState.Success(result.data.map(StorageProduct::toInventoryProduct))
        is UiState.Error -> result
        UiState.Loading -> UiState.Loading
    }

    override suspend fun updateQuantities(changes: List<Pair<Long, Double>>): UiState<Unit> =
        remoteRepository.updateQuantities(changes)
}

private fun StorageProduct.toInventoryProduct() = InventoryProduct(
    id = batchId.toString(),
    batchId = batchId,
    name = name,
    article = id.toString(),
    category = "24",
    systemQuantity = availableQuantity,
    unit = unit.ifBlank { "24" }
)
