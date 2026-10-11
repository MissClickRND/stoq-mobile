package bob.colbaskin.stoq.feature.inventory.domain

import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct

interface InventoryRepository {
    fun products(): List<InventoryProduct>
}
