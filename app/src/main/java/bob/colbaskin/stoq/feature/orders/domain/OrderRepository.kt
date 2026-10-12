package bob.colbaskin.stoq.feature.orders.domain

import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.feature.orders.domain.model.OrderMetrics

interface OrderRepository {
    fun orders(): List<Order>
    fun order(id: String): Order?
    fun metrics(): OrderMetrics
    fun menu(): List<OrderItem>
    fun check(items: List<OrderItem>): List<OrderItem>
    fun create(items: List<OrderItem>, type: String, location: String, guests: String, scheduledAt: String): Result<Order>
}
