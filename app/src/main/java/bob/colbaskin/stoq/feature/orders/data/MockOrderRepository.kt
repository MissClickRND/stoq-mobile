package bob.colbaskin.stoq.feature.orders.data

import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
import bob.colbaskin.stoq.feature.orders.domain.model.Availability
import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.feature.orders.domain.model.OrderMetrics
import bob.colbaskin.stoq.feature.orders.domain.model.OrderStatus
import bob.colbaskin.stoq.feature.orders.domain.model.OrderType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockOrderRepository @Inject constructor() : OrderRepository {
    private data class Batch(val expiresAt: String, var remaining: Int)
    private data class Requirement(val ingredient: String, val amount: Int)

    private val recipes = mapOf(
        "salmon" to listOf(Requirement("Лосось", 200)),
        "caesar" to listOf(Requirement("Креветки", 140)),
        "greek" to listOf(Requirement("Овощи", 180)),
        "avocado" to listOf(Requirement("Лосось", 120)),
        "caprese" to listOf(Requirement("Моцарелла", 100)),
        "beef" to listOf(Requirement("Говядина", 180)),
        "pasta" to listOf(Requirement("Бекон", 100)),
        "cheesecake" to listOf(Requirement("Сливочный сыр", 150)),
        "burger" to listOf(Requirement("Говядина", 180))
    )
    private val batches = mutableMapOf(
        "Лосось" to mutableListOf(Batch("2024-06-02", 600), Batch("2024-06-06", 1400)),
        "Креветки" to mutableListOf(Batch("2024-06-03", 1400)),
        "Овощи" to mutableListOf(Batch("2024-06-02", 1800)),
        "Моцарелла" to mutableListOf(Batch("2024-06-04", 1000)),
        "Говядина" to mutableListOf(Batch("2024-06-03", 1800)),
        "Бекон" to mutableListOf(),
        "Сливочный сыр" to mutableListOf(Batch("2024-06-04", 1200))
    )
    private val dishes = listOf(
        item("salmon", "Стейк из лосося", "Горячее", 1490, "https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&w=360&q=80"),
        item("caesar", "Салат «Цезарь» с креветками", "Салаты", 790, "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=360&q=80"),
        item("greek", "Салат «Греческий»", "Салаты", 620, "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?auto=format&fit=crop&w=360&q=80"),
        item("avocado", "Салат с лососем и авокадо", "Салаты", 690, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=360&q=80"),
        item("caprese", "Салат «Капрезе»", "Салаты", 580, "https://images.unsplash.com/photo-1623855244183-52fd1d3d3d42?auto=format&fit=crop&w=360&q=80"),
        item("beef", "Тёплый салат с говядиной", "Салаты", 720, "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=360&q=80"),
        item("pasta", "Паста «Карбонара»", "Горячее", 690, "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=360&q=80"),
        item("cheesecake", "Чизкейк с ягодами", "Десерты", 450, "https://images.unsplash.com/photo-1565958011703-44f9829ba187?auto=format&fit=crop&w=360&q=80"),
        item("burger", "Бургер с говядиной", "Горячее", 890, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=360&q=80")
    )
    private var orders = listOf(
        order("10248", "01.06.2024", "14:32", OrderStatus.Processing, OrderType.DineIn, "Стол 8", "Анна Петрова", listOf(dishes[0].copy(quantity = 1), dishes[1].copy(quantity = 1), dishes[2].copy(quantity = 2))),
        order("10247", "01.06.2024", "14:18", OrderStatus.Preparing, OrderType.DineIn, "Стол 5", "Иван Соколов", listOf(dishes[1].copy(quantity = 1), dishes[4].copy(quantity = 2))),
        order("10246", "01.06.2024", "14:05", OrderStatus.Completed, OrderType.DineIn, "Стол 12", "Мария Кузнецова", listOf(dishes[0].copy(quantity = 1), dishes[4].copy(quantity = 2), dishes[7].copy(quantity = 2))),
        order("10245", "01.06.2024", "13:48", OrderStatus.Preparing, OrderType.DineIn, "Стол 3", "Анна Петрова", listOf(dishes[3].copy(quantity = 1), dishes[2].copy(quantity = 1))),
        order("10244", "01.06.2024", "13:35", OrderStatus.Completed, OrderType.Delivery, "Доставка", "Курьер №3", listOf(dishes[8].copy(quantity = 1), dishes[1].copy(quantity = 1), dishes[7].copy(quantity = 1)))
    )

    override fun orders() = orders
    override fun order(id: String) = orders.firstOrNull { it.id == id }
    override fun metrics() = OrderMetrics(248, 6, 218, 162450)
    override fun menu() = dishes
    override fun check(items: List<OrderItem>) = items.map { item ->
        val missing = missingIngredient(item)
        item.copy(
            availability = if (missing == null) Availability.Available else Availability.Unavailable,
            unavailableIngredient = missing?.let(::ingredientAvailabilityLabel)
        )
    }
    override fun create(items: List<OrderItem>, type: String, location: String, guests: String, scheduledAt: String): Result<Order> {
        val checked = check(items)
        if (checked.any { it.availability == Availability.Unavailable }) return Result.failure(IllegalStateException("В заказе есть недоступные блюда"))
        checked.forEach(::consumeByFefo)
        val (date, time) = scheduledAt.split(" ", limit = 2).let { it.first() to it.getOrElse(1) { "" } }
        val order = order((10249 + orders.size).toString(), date, time, OrderStatus.Processing, if (type == "В зале") OrderType.DineIn else OrderType.Delivery, location, if (type == "В зале") "Гостей: $guests" else "Доставка", checked)
        orders = listOf(order) + orders
        return Result.success(order)
    }

    private fun item(id: String, name: String, category: String, price: Int, image: String) = OrderItem(id, name, category, price, image, 1, Availability.Available)
    private fun order(id: String, date: String, time: String, status: OrderStatus, type: OrderType, location: String, customer: String, items: List<OrderItem>) = Order(id, date, time, status, type, location, customer, items)
    private fun missingIngredient(item: OrderItem): String? = recipes[item.dishId]?.firstOrNull { requirement ->
        batches[requirement.ingredient].orEmpty().sumOf { it.remaining } < requirement.amount * item.quantity
    }?.ingredient
    private fun consumeByFefo(item: OrderItem) {
        recipes[item.dishId].orEmpty().forEach { requirement ->
            var left = requirement.amount * item.quantity
            batches[requirement.ingredient].orEmpty().sortedBy { it.expiresAt }.forEach { batch ->
                val consumed = minOf(batch.remaining, left)
                batch.remaining -= consumed
                left -= consumed
            }
        }
    }
    private fun ingredientAvailabilityLabel(ingredient: String): String {
        val amount = batches[ingredient].orEmpty().sumOf { it.remaining }
        return "$ingredient ($amount кг)"
    }
}
