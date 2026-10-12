package bob.colbaskin.stoq.feature.orders.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.dishes.data.model.DishDto
import bob.colbaskin.stoq.feature.dishes.data.remote.DishesApiService
import bob.colbaskin.stoq.feature.orders.data.model.AddOrderItemsDto
import bob.colbaskin.stoq.feature.orders.data.model.OrderDto
import bob.colbaskin.stoq.feature.orders.data.model.OrderItemDto
import bob.colbaskin.stoq.feature.orders.data.remote.OrdersApiService
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
class RemoteOrderRepository @Inject constructor(
    private val ordersApi: OrdersApiService,
    private val dishesApi: DishesApiService
) : OrderRepository {
    override suspend fun orders(): UiState<List<Order>> = safeApiCall(
        apiCall = {
            val dishes = dishesApi.getDishes().associateBy(DishDto::id)
            ordersApi.getOrders().map { it.toOrder(dishes) }
        },
        successHandler = { it }
    ).toUiState()

    override suspend fun order(id: String): UiState<Order> = safeApiCall(
        apiCall = {
            val dishes = dishesApi.getDishes().associateBy(DishDto::id)
            ordersApi.getOrder(id.toLong()).toOrder(dishes)
        },
        successHandler = { it }
    ).toUiState()

    override suspend fun menu(): UiState<List<OrderItem>> = safeApiCall(
        apiCall = dishesApi::getDishes,
        successHandler = { dishes -> dishes.filterNot(DishDto::isDeleted).map(DishDto::toOrderItem) }
    ).toUiState()

    override suspend fun create(items: List<OrderItem>): UiState<Order> = safeApiCall(
        apiCall = {
            val order = ordersApi.createOrder()
            ordersApi.addOrderItems(order.id, AddOrderItemsDto(items.map { OrderItemDto(dishId = it.dishId.toLong(), quantity = it.quantity) }))
            val dishes = dishesApi.getDishes().associateBy(DishDto::id)
            ordersApi.getOrder(order.id).toOrder(dishes)
        },
        successHandler = { it }
    ).toUiState()

    override suspend fun delete(id: String): UiState<Unit> = safeApiCall(
        apiCall = { ordersApi.deleteOrder(id.toLong()) }, successHandler = { Unit }
    ).toUiState()

    override fun metrics(orders: List<Order>) = OrderMetrics(
        totalToday = orders.size,
        processing = orders.count { it.status != OrderStatus.Completed },
        completed = orders.count { it.status == OrderStatus.Completed },
        revenue = orders.sumOf(Order::total)
    )
}

private fun DishDto.toOrderItem() = OrderItem(
    dishId = id.toString(), name = name, category = "мог", price = salePrice?.toInt() ?: 0,
    imageUrl = imageURL.orEmpty(), quantity = 1, availability = Availability.Unknown
)

private fun OrderDto.toOrder(dishes: Map<Long, DishDto>): Order {
    val dateTime = createdAt.orEmpty().replace('T', ' ').take(16)
    return Order(
        id = id.toString(), date = dateTime.take(10).ifBlank { "мог" }, time = dateTime.drop(11).ifBlank { "мог" },
        status = when (status.lowercase()) { "completed", "done" -> OrderStatus.Completed; "preparing" -> OrderStatus.Preparing; else -> OrderStatus.Processing },
        type = OrderType.DineIn, location = "мог", customer = "мог", supplyId = supplyId?.toString(),
        items = items.map { item -> dishes[item.dishId]?.toOrderItem()?.copy(quantity = item.quantity)
            ?: OrderItem(item.dishId.toString(), "Блюдо #${item.dishId}", "мог", 0, "", item.quantity, Availability.Unknown) }
    )
}
