package bob.colbaskin.stoq.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.drafts.data.Draft
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.feature.orders.domain.model.OrderMetrics
import bob.colbaskin.stoq.feature.orders.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val menu: List<OrderItem> = emptyList(),
    val allMenu: List<OrderItem> = emptyList(),
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
    val hasUnsavedChanges: Boolean = false,
    val draftMessage: String? = null,
    val created: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val drafts: List<Draft> = emptyList(),
    val draftId: String? = null
)

@Serializable
private data class OrderDraftPayload(
    val orderDate: String,
    val orderType: String,
    val table: String,
    val guests: String,
    val items: List<OrderDraftItem>
)

@Serializable
private data class OrderDraftItem(val dishId: String, val quantity: Int)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val draftsRepository: DraftRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OrdersState())
    val state = _state.asStateFlow()
    private var menuSearchJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            draftsRepository.observe(DraftType.Order).collect { drafts ->
                _state.update { it.copy(drafts = drafts) }
            }
        }
    }

    fun reload() = refresh()

    fun onAction(action: OrdersAction) {
        when (action) {
            is OrdersAction.Query -> _state.update { it.copy(query = action.value) }
            is OrdersAction.Status -> _state.update { it.copy(status = action.value) }
            is OrdersAction.Type -> _state.update { it.copy(type = action.value) }
            is OrdersAction.Date -> _state.update { it.copy(date = action.value) }
            OrdersAction.ResetFilters -> _state.update { it.copy(query = "", status = null, type = "Все типы заказа", date = currentDateTime()) }
            is OrdersAction.MenuQuery -> {
                _state.update {
                    it.copy(menuQuery = action.value, menu = if (action.value.isBlank()) it.allMenu else it.menu)
                }
                searchMenu(action.value)
            }
            is OrdersAction.OrderDate -> _state.update { it.copy(orderDate = action.value) }
            is OrdersAction.OrderType -> _state.update {
                it.copy(orderType = action.value, table = if (action.value == "Доставка") "" else it.table, guests = if (action.value == "Доставка") "" else it.guests)
            }
            is OrdersAction.Table -> _state.update { it.copy(table = action.value) }
            is OrdersAction.Guests -> _state.update { it.copy(guests = action.value) }
            is OrdersAction.AddDish -> addDish(action.item)
            is OrdersAction.ChangeQuantity -> changeQuantity(action.id, action.delta)
            is OrdersAction.Remove -> updateBasket(_state.value.basket.filterNot { it.dishId == action.id })
            OrdersAction.Save -> saveDraft(showMessage = true, markClean = true)
            OrdersAction.Create -> create()
            OrdersAction.ResetCreation -> clearDraftAndReset()
        }
        if (action.shouldMarkDirty) _state.update { it.copy(hasUnsavedChanges = true) }
        if (action.shouldAutosave) saveDraft(showMessage = false)
    }

    private fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loadState = UiState.Loading) }
        val orders = repository.orders()
        val menu = repository.menu()
        when {
            orders is UiState.Success && menu is UiState.Success -> _state.update {
                it.copy(orders = orders.data, menu = menu.data, allMenu = menu.data, metrics = repository.metrics(orders.data), loadState = UiState.Success(Unit))
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
            menuQuery = "", menu = state.allMenu, checked = true, error = null, hasUnsavedChanges = true
        )
    }

    private fun searchMenu(query: String) {
        menuSearchJob?.cancel()
        menuSearchJob = viewModelScope.launch {
            delay(250)
            when (val result = repository.menu(query)) {
                is UiState.Success -> _state.update {
                    it.copy(menu = result.data, allMenu = if (query.isBlank()) result.data else it.allMenu)
                }
                else -> Unit
            }
        }
    }

    private fun changeQuantity(id: String, delta: Int) = _state.update { state ->
        state.copy(
            basket = state.basket.mapNotNull { if (it.dishId != id) it else it.copy(quantity = it.quantity + delta).takeIf { changed -> changed.quantity > 0 } },
            checked = true, error = null, hasUnsavedChanges = true
        )
    }

    private fun create() = viewModelScope.launch {
        val basket = _state.value.basket
        if (basket.isEmpty()) return@launch
        _state.update { it.copy(isCreating = true, error = null) }
        when (val result = repository.create(basket)) {
            is UiState.Success -> {
                val draftId = _state.value.draftId
                _state.update {
                    val updatedOrders = listOf(result.data) + it.orders.filterNot { order -> order.id == result.data.id }
                    it.copy(orders = updatedOrders, metrics = repository.metrics(updatedOrders), created = true, checked = true, basket = emptyList(), hasUnsavedChanges = false, isCreating = false, draftId = null)
                }
                draftId?.let { draftsRepository.delete(it) }
            }
            is UiState.Error -> {
                _state.update { it.copy(isCreating = false, error = result.text) }
                saveDraft()
            }
            UiState.Loading -> Unit
        }
    }

    private fun updateBasket(items: List<OrderItem>) = _state.update { it.copy(basket = items, checked = true, error = null) }

    fun restoreDraft(id: String) = viewModelScope.launch {
        val draft = draftsRepository.find(id) ?: return@launch
        val payload = runCatching { Json.decodeFromString<OrderDraftPayload>(draft.payloadJson) }.getOrNull() ?: return@launch
        val menuById = _state.value.allMenu.associateBy(OrderItem::dishId)
        val basket = payload.items.mapNotNull { item -> menuById[item.dishId]?.copy(quantity = item.quantity) }
        _state.update {
            it.copy(
                basket = basket, orderDate = payload.orderDate, orderType = payload.orderType,
                table = payload.table, guests = payload.guests, checked = basket.isNotEmpty(),
                saved = true, hasUnsavedChanges = false, draftId = draft.id, error = null,
                draftMessage = "Черновик восстановлен"
            )
        }
    }

    fun saveDraft(onSaved: (() -> Unit)? = null, showMessage: Boolean = true, markClean: Boolean = false) {
        val state = _state.value
        if (state.basket.isEmpty()) {
            onSaved?.invoke()
            return
        }
        val draftId = state.draftId ?: UUID.randomUUID().toString().also { generatedId ->
            _state.update { it.copy(draftId = generatedId) }
        }
        viewModelScope.launch {
            val payload = OrderDraftPayload(
                orderDate = state.orderDate, orderType = state.orderType, table = state.table,
                guests = state.guests, items = state.basket.map { OrderDraftItem(it.dishId, it.quantity) }
            )
            val draft = draftsRepository.save(
                type = DraftType.Order,
                title = "Заказ: ${state.basket.sumOf(OrderItem::quantity)} поз.",
                summary = listOf(state.orderType, state.table.takeIf { it.isNotBlank() }).filterNotNull().joinToString(" • "),
                payloadJson = Json.encodeToString(payload), id = draftId
            )
            _state.update {
                it.copy(
                    saved = true,
                    hasUnsavedChanges = if (markClean) false else it.hasUnsavedChanges,
                    draftId = draft.id,
                    draftMessage = if (showMessage) "Черновик сохранён" else it.draftMessage
                )
            }
            onSaved?.invoke()
        }
    }

    private fun clearDraftAndReset() {
        val draftId = _state.value.draftId
        _state.update { it.copy(created = false, basket = emptyList(), checked = false, saved = false, hasUnsavedChanges = false, error = null, draftId = null) }
        draftId?.let { id -> viewModelScope.launch { draftsRepository.delete(id) } }
    }

    fun consumeDraftMessage() {
        _state.update { it.copy(draftMessage = null) }
    }

    fun discardDraft(onDiscarded: () -> Unit) {
        val currentDraftId = _state.value.draftId
        _state.update { it.copy(draftId = null) }
        viewModelScope.launch {
            currentDraftId?.let { draftsRepository.delete(it) }
            onDiscarded()
        }
    }
}

private val OrdersAction.shouldAutosave: Boolean
    get() = when (this) {
        OrdersAction.Save, OrdersAction.Create, OrdersAction.ResetCreation,
        is OrdersAction.Query, is OrdersAction.Status, is OrdersAction.Type,
        is OrdersAction.Date, OrdersAction.ResetFilters, is OrdersAction.MenuQuery -> false
        else -> true
    }

private val OrdersAction.shouldMarkDirty: Boolean
    get() = when (this) {
        OrdersAction.Save, OrdersAction.Create, OrdersAction.ResetCreation,
        is OrdersAction.Query, is OrdersAction.Status, is OrdersAction.Type,
        is OrdersAction.Date, OrdersAction.ResetFilters, is OrdersAction.MenuQuery -> false
        else -> true
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
