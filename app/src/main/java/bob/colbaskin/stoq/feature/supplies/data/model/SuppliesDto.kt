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
    val items: List<SupplyItemDto> = emptyList()
)

@Serializable
data class SupplyItemDto(
    val id: Long,
    val supplyId: Long,
    val productId: Long,
    val quantity: JsonElement,
    val expiresAt: String? = null,
    val createdAt: String? = null
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
