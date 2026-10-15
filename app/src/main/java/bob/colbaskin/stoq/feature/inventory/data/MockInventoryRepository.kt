package bob.colbaskin.stoq.feature.inventory.data

import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import bob.colbaskin.stoq.common.core.UiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInventoryRepository @Inject constructor() : InventoryRepository {
    override suspend fun products() = UiState.Success(emptyList<InventoryProduct>())
    override suspend fun updateQuantities(changes: List<Pair<Long, Double>>) = UiState.Success(Unit)
}
