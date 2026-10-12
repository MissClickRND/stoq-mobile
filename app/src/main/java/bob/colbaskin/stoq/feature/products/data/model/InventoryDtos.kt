package bob.colbaskin.stoq.feature.products.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductUnitDto(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ProductDto(
    val id: Long,
    val name: String,
    val isDeleted: Boolean = false,
    val unitId: Long? = null,
    val unit: ProductUnitDto? = null
)

@Serializable
data class CreateProductDto(val name: String, val unitId: Long)

@Serializable
data class CreateUnitDto(val name: String, val shortName: String)

@Serializable
data class InventoryDto(
    val id: Long,
    val supplyId: Long? = null,
    val productId: Long,
    @SerialName("Quantity")
    val availableQuantity: Double? = null,
    val initialQuantity: Double = 0.0,
    val expiresAt: String? = null,
    val status: String? = null,
    val balance: InventoryBalanceDto? = null,
    val product: ProductDto? = null
)

@Serializable
data class InventoryBalanceDto(
    val availableQuantity: Double = 0.0,
    val reservedQuantity: Double = 0.0
)

@Serializable
data class UpdateQuantityItemDto(
    val batchId: Long,
    val quantity: Double
)

@Serializable
data class UpdateQuantityDto(val items: List<UpdateQuantityItemDto>)
