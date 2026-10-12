package bob.colbaskin.stoq.feature.products.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityItemDto
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
            api.getInventory().mapNotNull(InventoryDto::toStorageProduct)
        },
        successHandler = { it }
    ).toUiState()

    suspend fun getInventoryByProduct(productId: Long): UiState<List<StorageProduct>> = safeApiCall(
        apiCall = { api.getInventoryByProduct(productId) },
        successHandler = { items -> items.mapNotNull(InventoryDto::toStorageProduct) }
    ).toUiState()

    suspend fun updateQuantity(batchId: Long, quantity: Double): UiState<Unit> = safeApiCall(
        apiCall = { api.updateQuantity(UpdateQuantityDto(listOf(UpdateQuantityItemDto(batchId, quantity)))) },
        successHandler = { Unit }
    ).toUiState()

    suspend fun updateQuantities(changes: List<Pair<Long, Double>>): UiState<Unit> = safeApiCall(
        apiCall = { api.updateQuantity(UpdateQuantityDto(changes.map { (batchId, quantity) -> UpdateQuantityItemDto(batchId, quantity) })) },
        successHandler = { Unit }
    ).toUiState()
}

private fun InventoryDto.toStorageProduct(): StorageProduct? {
    val product = product ?: return null
    val unit = product.unit?.shortName.orEmpty()
    val quantity = availableQuantity ?: balance?.availableQuantity ?: initialQuantity
    return StorageProduct(
        id = product.id,
        batchId = id,
        name = product.name,
        availableQuantity = quantity,
        unit = unit,
        currentStock = listOf(quantity.toDisplayQuantity(), unit).filter(String::isNotBlank).joinToString(" "),
        expiryDate = expiresAt?.take(10)
    )
}

private fun Double.toDisplayQuantity() = if (this % 1.0 == 0.0) toLong().toString() else toString()
