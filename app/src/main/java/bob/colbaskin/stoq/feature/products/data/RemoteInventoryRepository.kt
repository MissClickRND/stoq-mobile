package bob.colbaskin.stoq.feature.products.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.InventoryProductBatchDto
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityDto
import bob.colbaskin.stoq.feature.products.data.remote.InventoryApiService
import javax.inject.Inject
import javax.inject.Singleton

data class StorageProduct(
    val id: Long,
    val batchId: Long,
    val name: String,
    val availableQuantity: Double,
    val unit: String,
    val currentStock: String,
    val expiryDate: String?
)

@Singleton
class RemoteInventoryRepository @Inject constructor(
    private val api: InventoryApiService
) {
    suspend fun getInventory(): UiState<List<StorageProduct>> = safeApiCall(
        apiCall = {
            val products = api.getProducts().filterNot(ProductDto::isDeleted).associateBy(ProductDto::id)
            api.getInventory().mapNotNull { inventory -> products[inventory.batch.productId]?.let { inventory.toStorageProduct(it) } }
        },
        successHandler = { it }
    ).toUiState()

    suspend fun getInventoryByProduct(productId: Long): UiState<List<InventoryProductBatchDto>> = safeApiCall(
        apiCall = { api.getInventoryByProduct(productId) },
        successHandler = { it }
    ).toUiState()

    suspend fun updateQuantity(batchId: Long, quantity: Double): UiState<Unit> = safeApiCall(
        apiCall = { api.updateQuantity(UpdateQuantityDto(batchId, quantity)) },
        successHandler = { Unit }
    ).toUiState()

    suspend fun updateQuantities(changes: List<Pair<Long, Double>>): UiState<Unit> = safeApiCall(
        apiCall = { changes.forEach { (batchId, quantity) -> api.updateQuantity(UpdateQuantityDto(batchId, quantity)) } },
        successHandler = { Unit }
    ).toUiState()
}

private fun InventoryDto.toStorageProduct(product: ProductDto): StorageProduct {
    val unit = product.unit?.shortName.orEmpty()
    return StorageProduct(
        id = product.id,
        batchId = batchId,
        name = product.name,
        availableQuantity = availableQuantity.toDoubleOrNull() ?: 0.0,
        unit = unit,
        currentStock = listOf(availableQuantity, unit).filter(String::isNotBlank).joinToString(" "),
        expiryDate = batch.expiresAt?.take(10)
    )
}
