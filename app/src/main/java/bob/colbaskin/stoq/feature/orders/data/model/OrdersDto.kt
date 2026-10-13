package bob.colbaskin.stoq.feature.orders.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Long,
    val date: String? = null,
    val type: String? = null,
    val table: Int? = null,
    val guests: Int? = null,
    val status: String = "created",
    val supplyId: Long? = null,
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

@Serializable
data class ChangeStatusDto(val status: String)

@Serializable
data class CreateOrderDto(
    val date: String,
    val type: String,
    val table: Int?,
    val guests: Int,
    val items: List<OrderItemDto>
)
