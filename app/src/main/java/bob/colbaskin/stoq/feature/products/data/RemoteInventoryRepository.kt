package bob.colbaskin.stoq.feature.products.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.ExpiringInventoryDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityItemDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityDto
import bob.colbaskin.stoq.feature.products.data.remote.InventoryApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class StorageProduct(
    val id: Long,
    val batchId: Long,
    val supplyId: Long?,
    val name: String,
    val availableQuantity: Double,
    val unit: String,
    val currentStock: String,
    val expiryDate: String?,
    val expiresAt: String? = null,
    val daysRemaining: Int? = null,
    val price: Double?,
    val totalPrice: Double?,
    val status: String?
)

@Singleton
class RemoteInventoryRepository @Inject constructor(
    private val api: InventoryApiService
) {
    suspend fun dispose(batchId: Long): UiState<Unit> = safeApiCall(
        apiCall = { api.dispose(batchId) },
        successHandler = { Unit }
    ).toUiState()

    suspend fun writeOff(batchId: Long): UiState<Unit> = safeApiCall(
        apiCall = { api.writeOff(batchId) },
        successHandler = { Unit }
    ).toUiState()

    suspend fun getInventory(search: String? = null): UiState<List<StorageProduct>> = safeApiCall(
        apiCall = {
            api.getInventory(search?.trim()?.takeIf(String::isNotEmpty)).mapNotNull(InventoryDto::toStorageProduct)
        },
        successHandler = { it }
    ).toUiState()

    suspend fun getInventoryByProduct(productId: Long): UiState<List<StorageProduct>> = safeApiCall(
        apiCall = { api.getInventoryByProduct(productId) },
        successHandler = { items -> items.mapNotNull(InventoryDto::toStorageProduct) }
    ).toUiState()

    suspend fun getExpiringInventory(daysThreshold: Int): UiState<List<StorageProduct>> = safeApiCall(
        apiCall = { api.getExpiringInventory(daysThreshold) },
        successHandler = { items -> items.mapNotNull(ExpiringInventoryDto::toStorageProduct) }
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
    val quantity = availableQuantity ?: legacyQuantity ?: balance?.availableQuantity ?: initialQuantity
    return StorageProduct(
        id = product.id,
        batchId = id,
        supplyId = supplyId,
        name = product.name,
        availableQuantity = quantity,
        unit = unit,
        currentStock = listOf(quantity.toDisplayQuantity(), unit).filter(String::isNotBlank).joinToString(" "),
        expiryDate = expiresAt?.take(10),
        expiresAt = expiresAt,
        daysRemaining = null,
        price = price,
        totalPrice = totalPrice,
        status = status
    )
}

private fun ExpiringInventoryDto.toStorageProduct(): StorageProduct? {
    val product = product ?: return null
    val unit = product.unit?.shortName.orEmpty()
    val quantity = availableQuantity.toDoubleOrNull() ?: 0.0
    return StorageProduct(
        id = productId,
        batchId = batchId,
        supplyId = null,
        name = product.name,
        availableQuantity = quantity,
        unit = unit,
        currentStock = listOf(quantity.toDisplayQuantity(), unit).filter(String::isNotBlank).joinToString(" "),
        expiryDate = expiresAt?.take(10),
        expiresAt = expiresAt,
        daysRemaining = daysRemaining,
        price = price.toDoubleOrNull(),
        totalPrice = null,
        status = null
    )
}

private fun JsonElement?.toDoubleOrNull(): Double? = this?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()

private fun Double.toDisplayQuantity() = if (this % 1.0 == 0.0) toLong().toString() else toString()
