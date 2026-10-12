package bob.colbaskin.stoq.feature.orders.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Long,
    val status: String = "created",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val items: List<OrderItemDto> = emptyList()
)

@Serializable
data class OrderItemDto(
    val id: Long? = null,
    val orderId: Long? = null,
    val dishId: Long,
    val quantity: Int
)

@Serializable
data class AddOrderItemsDto(val items: List<OrderItemDto>)
