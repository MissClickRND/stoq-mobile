package bob.colbaskin.stoq.feature.inventory.domain

import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import bob.colbaskin.stoq.common.core.UiState

interface InventoryRepository {
    suspend fun products(): UiState<List<InventoryProduct>>
    suspend fun updateQuantities(changes: List<Pair<Long, Double>>): UiState<Unit>
}
