package bob.colbaskin.stoq.feature.dishes.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class StopListDishDto(
    val dishId: Long,
    val dishName: String? = null,
    val salePrice: Double? = null,
    val status: String? = null,
    val maxPortions: Double? = null,
    val priority: Int? = null,
    val reasons: List<StopListReasonDto> = emptyList()
)

@Serializable
data class StopListReasonDto(
    val status: String? = null,
    val name: String? = null
)

@Serializable
data class DishRecommendationDto(
    val dishId: Long,
    val dishName: String? = null,
    val salePrice: Double? = null,
    val costPrice: Double? = null,
    val profit: Double? = null,
    val maxPortions: Double? = null,
    val expiringProducts: List<JsonElement> = emptyList(),
    val recommendationScore: Double? = null
)
