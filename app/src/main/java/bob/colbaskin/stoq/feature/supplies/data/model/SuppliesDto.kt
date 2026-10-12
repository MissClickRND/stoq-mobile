package bob.colbaskin.stoq.feature.supplies.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SupplyDto(
    val id: Long,
    val supplyNumber: String,
    val status: String? = null,
    val receivedAt: String? = null,
    val createdAt: String? = null,
    val product: SupplyProductDto? = null,
    val items: List<SupplyItemDto> = emptyList()
)

@Serializable
data class SupplyProductDto(
    val id: Long? = null,
    val name: String? = null,
    val unit: SupplyProductUnitDto? = null
)

@Serializable
data class SupplyProductUnitDto(
    val shortName: String? = null
)

@Serializable
data class SupplyItemDto(
    val id: Long? = null,
    val supplyItemId: Long? = null,
    val supplyId: Long? = null,
    val productId: Long? = null,
    val quantity: JsonElement? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val product: SupplyProductDto? = null
)

@Serializable
data class CreateSupplyDto(
    val supplyNumber: String,
    val items: List<CreateSupplyItemDto>,
    val receivedAt: String? = null
)

@Serializable
data class CreateSupplyItemDto(
    val productId: Long,
    val quantity: Double,
    val expiresAt: String
)
