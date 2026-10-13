package bob.colbaskin.stoq.feature.dishes.data.model

import kotlinx.serialization.Serializable

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
