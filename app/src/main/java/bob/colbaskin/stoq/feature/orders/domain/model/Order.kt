package bob.colbaskin.stoq.feature.orders.domain.model

enum class OrderStatus { Processing, Preparing, Completed }
enum class OrderType { DineIn, Delivery }

data class Order(
    val id: String,
    val date: String,
    val time: String,
    val status: OrderStatus,
    val type: OrderType,
    val location: String,
    val customer: String,
    val items: List<OrderItem>,
    val table: Int? = null,
    val guests: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val total: Int get() = items.sumOf { it.price * it.quantity }
    val positions: Int get() = items.sumOf { it.quantity }
}

data class OrderItem(
    val dishId: String,
    val name: String,
    val category: String,
    val price: Int,
    val imageUrl: String,
    val quantity: Int,
    val availability: Availability,
    val unavailableIngredient: String? = null
)

enum class Availability { Available, Unavailable, Unknown }

data class OrderMetrics(
    val totalToday: Int,
    val processing: Int,
    val completed: Int,
    val revenue: Int
)
