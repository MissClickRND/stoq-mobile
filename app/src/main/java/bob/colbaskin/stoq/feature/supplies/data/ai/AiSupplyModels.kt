package bob.colbaskin.stoq.feature.supplies.data.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiSupplyRecognitionDto(
    val sellerName: String? = null,
    val items: List<AiSupplyItemDto> = emptyList()
)

@Serializable
data class AiSupplyItemDto(
    val productId: Long,
    val name: String,
    val quantity: Double? = null,
    val price: Double? = null,
    val totalPrice: Double? = null,
    val expiresAt: String? = null,
    val unit: AiSupplyUnitDto? = null
)

@Serializable
data class AiSupplyUnitDto(
    val id: Long,
    val name: String,
    val shortName: String
)

data class AiSupplyRecognition(
    val sellerName: String?,
    val items: List<AiSupplyItem>
)

data class AiSupplyItem(
    val productId: Long,
    val name: String,
    val quantity: Double?,
    val price: Double?,
    val totalPrice: Double?,
    val expiresAt: String?,
    val unit: String?
)
