package bob.colbaskin.stoq.feature.products.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductUnitDto(val shortName: String? = null)

@Serializable
data class ProductDto(
    val id: Long,
    val name: String,
    val isDeleted: Boolean = false,
    val unit: ProductUnitDto? = null
)

@Serializable
data class BatchDto(
    val id: Long,
    val productId: Long,
    val expiresAt: String? = null
)

@Serializable
data class InventoryDto(
    val batchId: Long,
    val availableQuantity: String,
    val reservedQuantity: String = "0",
    val batch: BatchDto
)

@Serializable
data class InventoryProductBatchDto(
    val id: Long,
    val productId: Long,
    val expiresAt: String? = null,
    val balance: InventoryBalanceDto? = null
)

@Serializable
data class InventoryBalanceDto(
    val batchId: Long,
    val availableQuantity: String,
    val reservedQuantity: String = "0"
)

@Serializable
data class UpdateQuantityDto(
    val batchId: Long,
    val quantity: Double
)
