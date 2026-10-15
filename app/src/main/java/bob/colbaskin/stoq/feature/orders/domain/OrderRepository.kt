package bob.colbaskin.stoq.feature.orders.domain

import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.orders.domain.model.OrderMetrics

interface OrderRepository {
    suspend fun orders(): UiState<List<Order>>
    suspend fun order(id: String): UiState<Order>
    suspend fun menu(search: String? = null): UiState<List<OrderItem>>
    suspend fun create(
        date: String,
        type: String,
        table: Int?,
        guests: Int,
        items: List<OrderItem>
    ): UiState<Order>
    suspend fun addItems(id: String, items: List<OrderItem>): UiState<Order>
    suspend fun complete(id: String): UiState<Order>
    suspend fun delete(id: String): UiState<Unit>
    fun metrics(orders: List<Order>): OrderMetrics
}
