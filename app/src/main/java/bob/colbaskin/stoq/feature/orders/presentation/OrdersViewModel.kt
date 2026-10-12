package bob.colbaskin.stoq.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
import bob.colbaskin.stoq.feature.orders.domain.model.Availability
import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.feature.orders.domain.model.OrderMetrics
import bob.colbaskin.stoq.feature.orders.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val metrics: OrderMetrics = OrderMetrics(0, 0, 0, 0),
    val query: String = "",
    val status: OrderStatus? = null,
    val type: String = "Все типы заказа",
    val date: String = currentDateTime(),
    val basket: List<OrderItem> = emptyList(),
    val menuQuery: String = "салат",
    val orderDate: String = currentDateTime(),
    val orderType: String = "В зале",
    val table: String = "Стол 12",
    val guests: String = "4",
    val checked: Boolean = false,
    val saved: Boolean = false,
    val created: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(private val repository: OrderRepository) : ViewModel() {
    private val _state = MutableStateFlow(OrdersState())
    val state = _state.asStateFlow()
    init {
        val menu = repository.menu()
        val initialBasket = repository.check(listOf(
            menu.first { it.dishId == "salmon" },
            menu.first { it.dishId == "caesar" }.copy(quantity = 2),
            menu.first { it.dishId == "pasta" },
            menu.first { it.dishId == "cheesecake" }.copy(quantity = 2),
            menu.first { it.dishId == "burger" }
        ))
        _state.update { it.copy(basket = initialBasket, checked = true) }
        refresh()
    }
    fun reload() = refresh()
    fun onAction(action: OrdersAction) = viewModelScope.launch {
        when (action) {
            is OrdersAction.Query -> _state.update { it.copy(query = action.value) }
            is OrdersAction.Status -> _state.update { it.copy(status = action.value) }
            is OrdersAction.Type -> _state.update { it.copy(type = action.value) }
            is OrdersAction.Date -> _state.update { it.copy(date = action.value) }
            OrdersAction.ResetFilters -> _state.update { it.copy(query = "", status = null, type = "Все типы заказа", date = currentDateTime()) }
            is OrdersAction.MenuQuery -> _state.update { it.copy(menuQuery = action.value) }
            is OrdersAction.OrderDate -> _state.update { it.copy(orderDate = action.value) }
            is OrdersAction.OrderType -> _state.update { it.copy(orderType = action.value, table = if (action.value == "Доставка") "" else it.table, guests = if (action.value == "Доставка") "" else it.guests) }
            is OrdersAction.Table -> _state.update { it.copy(table = action.value) }
            is OrdersAction.Guests -> _state.update { it.copy(guests = action.value) }
            is OrdersAction.AddDish -> addDish(action.item)
            is OrdersAction.ChangeQuantity -> changeQuantity(action.id, action.delta)
            is OrdersAction.Remove -> updateBasket(_state.value.basket.filterNot { it.dishId == action.id })
            OrdersAction.Save -> _state.update { it.copy(saved = true, error = null) }
            OrdersAction.Create -> create()
            OrdersAction.ResetCreation -> _state.update { it.copy(created = false, basket = emptyList(), checked = false, saved = false, error = null) }
        }
    }
    private fun refresh() { _state.update { it.copy(orders = repository.orders(), metrics = repository.metrics()) } }
    private fun addDish(item: OrderItem) = _state.update { state ->
        val matching = state.basket.find { it.dishId == item.dishId }
        state.copy(basket = repository.check(if (matching == null) state.basket + item else state.basket.map { if (it.dishId == item.dishId) it.copy(quantity = it.quantity + 1) else it }), menuQuery = "", checked = true, error = null)
    }
    private fun changeQuantity(id: String, delta: Int) = _state.update { state ->
        state.copy(basket = repository.check(state.basket.mapNotNull { if (it.dishId != id) it else it.copy(quantity = it.quantity + delta).takeIf { changed -> changed.quantity > 0 } }), checked = true, error = null)
    }
    private fun create() {
        val checked = repository.check(_state.value.basket)
        if (checked.any { it.availability == Availability.Unavailable }) { _state.update { it.copy(basket = checked, checked = true, error = "Нельзя создать заказ: есть недоступные блюда") }; return }
        val location = if (_state.value.orderType == "В зале") _state.value.table else "Доставка"
        repository.create(checked, _state.value.orderType, location, _state.value.guests, _state.value.orderDate).onSuccess { refresh(); _state.update { state -> state.copy(created = true, checked = true, basket = emptyList()) } }.onFailure { throwable -> _state.update { state -> state.copy(error = throwable.message) } }
    }
    private fun updateBasket(items: List<OrderItem>) = _state.update { it.copy(basket = repository.check(items), checked = true, error = null) }
    fun menu() = repository.menu()
    fun order(id: String) = repository.order(id)
}

sealed interface OrdersAction {
    data class Query(val value: String) : OrdersAction
    data class Status(val value: OrderStatus?) : OrdersAction
    data class Type(val value: String) : OrdersAction
    data class Date(val value: String) : OrdersAction
    data object ResetFilters : OrdersAction
    data class MenuQuery(val value: String) : OrdersAction
    data class OrderDate(val value: String) : OrdersAction
    data class OrderType(val value: String) : OrdersAction
    data class Table(val value: String) : OrdersAction
    data class Guests(val value: String) : OrdersAction
    data class AddDish(val item: OrderItem) : OrdersAction
    data class ChangeQuantity(val id: String, val delta: Int) : OrdersAction
    data class Remove(val id: String) : OrdersAction
    data object Save : OrdersAction
    data object Create : OrdersAction
    data object ResetCreation : OrdersAction
}

private fun currentDateTime(): String {
    val now = java.time.LocalDateTime.now()
    return "%02d.%02d.%d %02d:%02d".format(now.dayOfMonth, now.monthValue, now.year, now.hour, now.minute)
}
