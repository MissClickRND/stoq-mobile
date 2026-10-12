package bob.colbaskin.stoq.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
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
    val menu: List<OrderItem> = emptyList(),
    val metrics: OrderMetrics = OrderMetrics(0, 0, 0, 0),
    val loadState: UiState<Unit> = UiState.Loading,
    val query: String = "",
    val status: OrderStatus? = null,
    val type: String = "Все типы заказа",
    val date: String = currentDateTime(),
    val basket: List<OrderItem> = emptyList(),
    val menuQuery: String = "",
    val orderDate: String = currentDateTime(),
    val orderType: String = "В зале",
    val table: String = "Стол 12",
    val guests: String = "4",
    val checked: Boolean = false,
    val saved: Boolean = false,
    val created: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OrdersState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun reload() = refresh()

    fun onAction(action: OrdersAction) {
        when (action) {
            is OrdersAction.Query -> _state.update { it.copy(query = action.value) }
            is OrdersAction.Status -> _state.update { it.copy(status = action.value) }
            is OrdersAction.Type -> _state.update { it.copy(type = action.value) }
            is OrdersAction.Date -> _state.update { it.copy(date = action.value) }
            OrdersAction.ResetFilters -> _state.update { it.copy(query = "", status = null, type = "Все типы заказа", date = currentDateTime()) }
            is OrdersAction.MenuQuery -> _state.update { it.copy(menuQuery = action.value) }
            is OrdersAction.OrderDate -> _state.update { it.copy(orderDate = action.value) }
            is OrdersAction.OrderType -> _state.update {
                it.copy(orderType = action.value, table = if (action.value == "Доставка") "" else it.table, guests = if (action.value == "Доставка") "" else it.guests)
            }
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

    private fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loadState = UiState.Loading) }
        val orders = repository.orders()
        val menu = repository.menu()
        when {
            orders is UiState.Success && menu is UiState.Success -> _state.update {
                it.copy(orders = orders.data, menu = menu.data, metrics = repository.metrics(orders.data), loadState = UiState.Success(Unit))
            }
            orders is UiState.Error -> _state.update { it.copy(loadState = orders) }
            menu is UiState.Error -> _state.update { it.copy(loadState = menu) }
            else -> _state.update { it.copy(loadState = UiState.Error("Не удалось загрузить заказы", "Повторите попытку")) }
        }
    }

    private fun addDish(item: OrderItem) = _state.update { state ->
        val matching = state.basket.find { it.dishId == item.dishId }
        state.copy(
            basket = if (matching == null) state.basket + item else state.basket.map { if (it.dishId == item.dishId) it.copy(quantity = it.quantity + 1) else it },
            menuQuery = "", checked = true, error = null
        )
    }

    private fun changeQuantity(id: String, delta: Int) = _state.update { state ->
        state.copy(
            basket = state.basket.mapNotNull { if (it.dishId != id) it else it.copy(quantity = it.quantity + delta).takeIf { changed -> changed.quantity > 0 } },
            checked = true, error = null
        )
    }

    private fun create() = viewModelScope.launch {
        val basket = _state.value.basket
        if (basket.isEmpty()) return@launch
        _state.update { it.copy(isCreating = true, error = null) }
        when (val result = repository.create(basket)) {
            is UiState.Success -> _state.update {
                val updatedOrders = listOf(result.data) + it.orders.filterNot { order -> order.id == result.data.id }
                it.copy(orders = updatedOrders, metrics = repository.metrics(updatedOrders), created = true, checked = true, basket = emptyList(), isCreating = false)
            }
            is UiState.Error -> _state.update { it.copy(isCreating = false, error = result.text) }
            UiState.Loading -> Unit
        }
    }

    private fun updateBasket(items: List<OrderItem>) = _state.update { it.copy(basket = items, checked = true, error = null) }
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
