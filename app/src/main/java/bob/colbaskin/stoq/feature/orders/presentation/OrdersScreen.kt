package bob.colbaskin.stoq.feature.orders.presentation

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.drafts.presentation.DraftsBanner
import bob.colbaskin.stoq.common.drafts.presentation.UnsavedChangesDialog
import bob.colbaskin.stoq.common.drafts.presentation.DraftSnackbar
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.feature.orders.domain.model.Availability
import bob.colbaskin.stoq.feature.orders.domain.model.Order
import bob.colbaskin.stoq.feature.orders.domain.model.OrderItem
import bob.colbaskin.stoq.feature.orders.domain.model.OrderStatus
import bob.colbaskin.stoq.feature.orders.domain.model.OrderType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun OrdersScreen(
    onCreateOrder: () -> Unit,
    onOrderClick: (String) -> Unit,
    onEditOrder: (String) -> Unit,
    onDraftsClick: () -> Unit,
    draftFeedback: String? = null,
    onDraftFeedbackShown: () -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val orders = state.orders.filter { order ->
        val matchesText = state.query.isBlank() || (listOf(order.id, order.customer, order.location) + order.items.map { it.name }).any { it.contains(state.query, true) }
        val matchesStatus = state.status == null || order.status == state.status
        val matchesType = state.type == "Все типы заказа" || (state.type == "В зале" && order.type == OrderType.DineIn) || (state.type == "Доставка" && order.type == OrderType.Delivery)
        matchesText && matchesStatus && matchesType
    }
    var orderToComplete by remember { mutableStateOf<Order?>(null) }
    var orderToDelete by remember { mutableStateOf<Order?>(null) }
    PullToRefreshContainer(state.isRefreshing, viewModel::reload, Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val loadState = state.loadState) {
            UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(loadState.title, loadState.text, viewModel::reload)
            is UiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { ScreenHeader("Заказы", "Отслеживайте и обрабатывайте заказы вашего ресторана") }
                    item { DraftsBanner(state.drafts.size, "заказ") { onDraftsClick() } }
                    item { OrderSearch(state.query, state.orders) { viewModel.onAction(OrdersAction.Query(it)) } }
                    item { MetricsGrid(state) }
                    item { Filters(state, viewModel::onAction) }
                    state.error?.let { error -> item { ErrorBanner("Ошибка заказа", error) } }
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order, onOrderClick, onEditOrder, onDelete = { orderToDelete = order }) { orderToComplete = order }
                    }
                }
                FloatingActionButton(
                    onClick = onCreateOrder,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                    containerColor = CustomTheme.colors.primary,
                    contentColor = CustomTheme.colors.textOnPrimary,
                    shape = CircleShape
                ) { Icon(Icons.Outlined.Add, "Создать заказ", modifier = Modifier.size(30.dp)) }
        }
        }
    }
    }
    orderToComplete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToComplete = null },
            title = { Text("Завершить заказ?") },
            text = { Text("Заказ #${order.id} будет отмечен как выполненный. Добавить новые блюда после завершения уже нельзя.") },
            confirmButton = { Button(onClick = { viewModel.complete(order.id); orderToComplete = null }) { Text("Завершить") } },
            dismissButton = { OutlinedButton(onClick = { orderToComplete = null }) { Text("Отмена") } }
        )
    }
    orderToDelete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Удалить заказ?") },
            text = { Text("Заказ #${order.id} будет удалён без возможности восстановления.") },
            confirmButton = { Button(onClick = { viewModel.delete(order.id); orderToDelete = null }) { Text("Удалить") } },
            dismissButton = { OutlinedButton(onClick = { orderToDelete = null }) { Text("Отмена") } }
        )
    }
    DraftSnackbar(draftFeedback, onDraftFeedbackShown, Modifier.padding(bottom = 20.dp))
}

@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    onOrdersClick: () -> Unit,
    draftId: String? = null,
    editOrderId: String? = null,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var restoredDraftId by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val hasChanges = state.basket.isNotEmpty() && state.hasUnsavedChanges
    LaunchedEffect(draftId, state.menu) {
        if (draftId != null && restoredDraftId != draftId && state.menu.isNotEmpty()) {
            restoredDraftId = draftId
            viewModel.restoreDraft(draftId)
        }
    }
    LaunchedEffect(editOrderId, state.menu) {
        if (editOrderId != null && state.menu.isNotEmpty() && state.editingOrderId != editOrderId) {
            viewModel.startEditing(editOrderId)
        }
    }
    BackHandler(enabled = hasChanges) { showExitDialog = true }
    val screenLoadState: UiState<Unit> = if (editOrderId == null) state.loadState else state.editLoadState ?: UiState.Loading
    when (val loadState = screenLoadState) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(
            loadState.title,
            loadState.text,
            onRetry = { editOrderId?.let(viewModel::startEditing) ?: viewModel.reload() },
            onBack = onBack
        )
        is UiState.Success -> CreateOrderContent(
            state = state,
            onBack = { if (hasChanges) showExitDialog = true else onBack() },
            onOrdersClick = onOrdersClick,
            onSaveAndReturn = { viewModel.saveDraft(onOrdersClick, showMessage = false, markClean = true) },
            onAction = viewModel::onAction
        )
    }
    UnsavedChangesDialog(
        visible = showExitDialog,
        entityName = "создание заказа",
        onDismiss = { showExitDialog = false },
        onSaveAndExit = { showExitDialog = false; viewModel.saveDraft(onOrdersClick, showMessage = false, markClean = true) },
        onDiscardAndExit = { showExitDialog = false; viewModel.discardDraft(onBack) }
    )
    DraftSnackbar(state.draftMessage, viewModel::consumeDraftMessage, Modifier.padding(bottom = 20.dp))
}

@Composable
private fun CreateOrderContent(
    state: OrdersState,
    onBack: () -> Unit,
    onOrdersClick: () -> Unit,
    onSaveAndReturn: () -> Unit,
    onAction: (OrdersAction) -> Unit
) {
    val menu = state.menu.filter { item ->
        item.dishId !in state.existingDishIds &&
            (state.menuQuery.isBlank() || item.name.contains(state.menuQuery, true))
    }
    val isEditing = state.editingOrderId != null
    var guestOrderDialogVisible by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader(if (isEditing) "Редактирование заказа" else "Новый заказ", if (isEditing) "Добавьте блюда в незавершённый заказ" else "Добавьте блюда в заказ", onBack) }
        if (!isEditing) item { NewOrderParameters(state, onAction) }
        state.editingOrder?.let { order ->
            item { OrderDetailsInfo(order) }
            item { Text("Текущий состав заказа", style = MaterialTheme.typography.titleLarge) }
            items(order.items, key = { "current-${it.dishId}" }) { OrderDetailItem(it) }
            item { Text("Добавить блюда", style = MaterialTheme.typography.titleLarge) }
        }
        item { DishPicker(state.menuQuery, menu, onAction) { guestOrderDialogVisible = true } }
        if (state.basket.isNotEmpty()) item { OrderSummary(state) }
        if (state.basket.isNotEmpty()) item { Text(if (isEditing) "Новые блюда" else "Блюда в заказе", style = MaterialTheme.typography.titleLarge) }
        items(state.basket, key = { it.dishId }) { BasketItem(it, onAction) }
        if (state.created) {
            item { CreatedBanner(isEditing) { onAction(OrdersAction.ResetCreation); onOrdersClick() } }
        } else if (state.basket.isNotEmpty()) {
            state.basket.count { it.availability == Availability.Unavailable }.takeIf { it > 0 }?.let { unavailableCount ->
                item {
                    ErrorBanner(
                        "Нельзя создать заказ",
                        "${unavailableCount} ${if (unavailableCount == 1) "блюдо недоступно" else "блюда недоступны"}. Удалите его из заказа или выберите другое."
                    )
                }
            }
            state.error?.let { text -> item { ErrorBanner("Ошибка заказа", text) } }
            item { CreateActions(state, onAction, onSaveAndReturn) }
        } else if (!isEditing) item { EmptyBasket() }
        else item { EditOrderHint() }
        item { Spacer(Modifier.height(22.dp)) }
    }
    if (guestOrderDialogVisible) {
        GuestOrderQrDialog(onDismiss = { guestOrderDialogVisible = false })
    }
}

@Composable
fun OrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    onEditOrder: (String) -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var completeDialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(orderId) { viewModel.load(orderId) }
    when (val orderState = state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(orderState.title, orderState.text, { viewModel.load(orderId) }, onBack)
        is UiState.Success -> PullToRefreshContainer(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.load(orderId) },
            modifier = Modifier.fillMaxSize()
        ) {
            OrderDetailsContent(
                orderId = orderId,
                order = orderState.data,
                onBack = onBack,
                onDelete = { deleteDialogVisible = true },
                onEdit = { onEditOrder(orderId) },
                onComplete = { completeDialogVisible = true }
            )
        }
    }
    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Удалить заказ?") },
            text = { Text("Заказ #$orderId будет удалён без возможности восстановления.") },
            confirmButton = {
                Button(onClick = { deleteDialogVisible = false; viewModel.delete(orderId, onBack) }) { Text("Удалить") }
            },
            dismissButton = { OutlinedButton(onClick = { deleteDialogVisible = false }) { Text("Отмена") } }
        )
    }
    if (completeDialogVisible) {
        AlertDialog(
            onDismissRequest = { completeDialogVisible = false },
            title = { Text("Завершить заказ?") },
            text = { Text("Заказ #$orderId будет отмечен как выполненный. Добавить новые блюда после завершения уже нельзя.") },
            confirmButton = { Button(onClick = { completeDialogVisible = false; viewModel.complete(orderId) }) { Text("Завершить") } },
            dismissButton = { OutlinedButton(onClick = { completeDialogVisible = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun OrderDetailsContent(
    orderId: String,
    order: Order,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Заказ #$orderId", "Подробная информация и состав заказа", onBack) }
        item { OrderDetailsInfo(order) }
        item { Text("Блюда в заказе", style = MaterialTheme.typography.titleLarge) }
        items(order.items, key = { it.dishId }) { OrderDetailItem(it) }
        item { OrderDetailsSummary(order) }
        if (order.status != OrderStatus.Completed) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Добавить блюда") }
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Check, null); Text("Завершить", Modifier.padding(start = 6.dp)) }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onDelete,
                enabled = order.status != OrderStatus.Completed,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CustomTheme.colors.error)
            ) {
                Icon(Icons.Outlined.DeleteOutline, null, tint = CustomTheme.colors.error)
                Text(if (order.status == OrderStatus.Completed) "Удаление недоступно" else "Удалить заказ", modifier = Modifier.padding(start = StoqSpacing.Small), color = CustomTheme.colors.error)
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.Top) {
        if (onBack != null) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                "Назад",
                Modifier.size(32.dp).clickable(onClick = onBack).padding(4.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(6.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NewOrderParameters(state: OrdersState, action: (OrdersAction) -> Unit) = BoxWithConstraints {
    val delivery = state.orderType == "Доставка"
    if (maxWidth >= 650.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ParameterGroup("Дата и время", Modifier.weight(1.15f)) { DateTimeField(state.orderDate, Modifier.fillMaxWidth()) { action(OrdersAction.OrderDate(it)) } }
            ParameterGroup("Тип заказа", Modifier.weight(1f)) { SelectField("Тип заказа", state.orderType, listOf("В зале", "Доставка"), Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.LocalDining) { action(OrdersAction.OrderType(it)) } }
            ParameterGroup("Стол", Modifier.weight(1f)) { TableField(state, delivery, action) }
            ParameterGroup("Кол-во гостей", Modifier.weight(.9f)) { GuestsField(state.guests, !delivery) { action(OrdersAction.Guests(it)) } }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ParameterGroup("Дата и время", Modifier.weight(1.15f)) { DateTimeField(state.orderDate, Modifier.fillMaxWidth()) { action(OrdersAction.OrderDate(it)) } }
                ParameterGroup("Тип заказа", Modifier.weight(1f)) { SelectField("Тип заказа", state.orderType, listOf("В зале", "Доставка"), Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.LocalDining) { action(OrdersAction.OrderType(it)) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ParameterGroup("Стол", Modifier.weight(1f)) { TableField(state, delivery, action) }
                ParameterGroup("Кол-во гостей", Modifier.weight(1f)) { GuestsField(state.guests, !delivery) { action(OrdersAction.Guests(it)) } }
            }
        }
    }
}

@Composable
private fun TableField(state: OrdersState, delivery: Boolean, action: (OrdersAction) -> Unit) {
    if (delivery) NewOrderValueField(Icons.Outlined.TableRestaurant, "Недоступно", false) { }
    else SelectField("Стол", state.table, listOf("Стол 3", "Стол 5", "Стол 8", "Стол 12"), Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.TableRestaurant) { action(OrdersAction.Table(it)) }
}

@Composable
private fun ParameterGroup(label: String, modifier: Modifier, content: @Composable () -> Unit) = Column(modifier) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
    content()
}

@Composable
private fun NewOrderValueField(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, enabled: Boolean, onClick: () -> Unit) = Surface(
    Modifier.fillMaxWidth().height(56.dp).clip(MaterialTheme.shapes.medium).clickable(enabled = enabled, onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = .45f)),
    color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
) { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (enabled) CustomTheme.colors.primaryDark else MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(10.dp)); Text(value, style = MaterialTheme.typography.bodyMedium, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1); if (enabled) { Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.KeyboardArrowDown, null, tint = CustomTheme.colors.primaryDark) } } }

@Composable
private fun GuestsField(value: String, enabled: Boolean, onChange: (String) -> Unit) = OutlinedTextField(
    value = if (enabled) value else "Не требуется",
    onValueChange = { onChange(it.filter(Char::isDigit).take(2)) },
    modifier = Modifier.fillMaxWidth().height(56.dp),
    enabled = enabled,
    singleLine = true,
    leadingIcon = { Icon(Icons.Outlined.PeopleOutline, null, modifier = Modifier.size(24.dp)) },
    shape = MaterialTheme.shapes.medium,
    colors = fieldColors()
)

@Composable
private fun OrderSearch(value: String, orders: List<Order>, onChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val suggestions = orders.filter { order ->
        value.isBlank() || (listOf(order.id, order.customer, order.location) + order.items.map { it.name })
            .any { it.contains(value, ignoreCase = true) }
    }
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Поиск по номеру заказа, блюду, клиенту…", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors()
        )
        if (focused) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestions.take(5).forEach { order ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable {
                            onChange(order.id)
                            focused = false
                        },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .55f))
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalDining, null, tint = CustomTheme.colors.primaryDark)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Заказ #${order.id}", style = MaterialTheme.typography.labelLarge)
                                Text(order.items.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(order.status.label(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(state: OrdersState) = Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val statistics = state.todayStatistics
    StoqMetricGrid(listOf(
        StoqMetric("Всего заказов сегодня", statistics?.total?.toString() ?: "мог", icon = Icons.Outlined.LocalDining, tint = CustomTheme.colors.success),
        StoqMetric("В обработке", statistics?.active?.toString() ?: "мог", icon = Icons.Outlined.Refresh, tint = CustomTheme.colors.warning),
        StoqMetric("Выполнено", statistics?.completed?.toString() ?: "мог", icon = Icons.Outlined.Check, tint = CustomTheme.colors.success),
        StoqMetric("Отменено", statistics?.cancelled?.toString() ?: "мог", icon = Icons.Outlined.Close, tint = CustomTheme.colors.error),
        StoqMetric("Выручка от заказов", statistics?.revenue?.let(::money) ?: "мог", icon = Icons.Outlined.LocalDining, tint = CustomTheme.colors.success)
    ))
}

@Composable
private fun Filters(state: OrdersState, action: (OrdersAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectField("Все статусы", state.status?.label() ?: "Все статусы", listOf("Все статусы", "В обработке", "Готовится", "Выполнен"), Modifier.weight(1f)) { selected ->
                action(OrdersAction.Status(when (selected) { "В обработке" -> OrderStatus.Processing; "Готовится" -> OrderStatus.Preparing; "Выполнен" -> OrderStatus.Completed; else -> null }))
            }
            SelectField("Тип заказа", state.type, listOf("Все типы заказа", "В зале", "Доставка"), Modifier.weight(1f)) { action(OrdersAction.Type(it)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateTimeField(state.date, Modifier.weight(1f)) { action(OrdersAction.Date(it)) }
            Button(onClick = { action(OrdersAction.ResetFilters) }, modifier = Modifier.height(56.dp), shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text("Сбросить") }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) = StoqCard(
    modifier = Modifier.fillMaxWidth().clickable { onClick(order.id) },
    contentPadding = PaddingValues(14.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.LocalDining, null, modifier = Modifier.size(22.dp), tint = CustomTheme.colors.primaryDark)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("Заказ #${order.id}", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(order.dateTimeLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            StoqStatusPill(order.status.label(), order.status.tone())
            if (order.status != OrderStatus.Completed) {
                Surface(
                    modifier = Modifier.padding(top = 7.dp).size(34.dp).clickable(onClick = onDelete),
                    shape = MaterialTheme.shapes.small,
                    color = CustomTheme.colors.error.copy(alpha = .88f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OrderCardValue("Тип", order.type.cardLabel(), Modifier.weight(1f))
        OrderCardValue("Стол", order.table?.toString() ?: "—", Modifier.weight(.62f))
        OrderCardValue("Гостей", order.guests?.toString() ?: "—", Modifier.weight(.72f))
        OrderCardValue("Позиций", order.positions.toString(), Modifier.weight(.85f))
    }
    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
    Row(modifier = Modifier.fillMaxWidth()) {
        OrderCardValue("Создан", order.createdAt.toCardTimestamp(), Modifier.weight(1f))
        OrderCardValue("Обновлён", order.updatedAt.toCardTimestamp(), Modifier.weight(1f))
    }
    Text("Сумма заказа: ${money(order.total)}", modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelLarge, color = CustomTheme.colors.primaryDark)
    if (order.status != OrderStatus.Completed) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { onEdit(order.id) }, modifier = Modifier.weight(1f)) { Text("Изменить") }
            Button(onClick = onComplete, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Check, null, Modifier.size(18.dp)); Text("Завершить", Modifier.padding(start = 5.dp)) }
        }
    }
}

@Composable
private fun OrderCardValue(label: String, value: String, modifier: Modifier) = Column(modifier) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    Text(value, modifier = Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

private fun Order.dateTimeLabel() = listOf(date, time).filter { it.isNotBlank() && it != "мог" }.joinToString(" • ").ifBlank { "мог" }
private fun OrderType.cardLabel() = if (this == OrderType.DineIn) "В зале" else "На вынос"
private fun String?.toCardTimestamp() = this?.replace('T', ' ')?.take(16)?.takeIf(String::isNotBlank) ?: "мог"

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) = Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) { Icon(icon, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(7.dp)); Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }

@Composable
private fun OrderDetailsInfo(order: Order) = StoqCard(Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("#${order.id}", style = MaterialTheme.typography.titleLarge)
            Text("${order.date} в ${order.time}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StoqStatusPill(order.status.label(), order.status.tone())
    }
    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
    InfoLine(if (order.type == OrderType.DineIn) Icons.Outlined.TableRestaurant else Icons.Outlined.LocalShipping, order.location)
    if (order.guests != null) InfoLine(Icons.Outlined.PeopleOutline, "${order.guests} ${guestsLabel(order.guests)}")
    else InfoLine(Icons.Outlined.PersonOutline, order.customer)
    Text(if (order.type == OrderType.DineIn) "Заказ в зале" else "Заказ с доставкой", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.primaryDark)
}

private fun guestsLabel(count: Int) = when {
    count % 10 == 1 && count % 100 != 11 -> "гость"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "гостя"
    else -> "гостей"
}

@Composable
private fun OrderDetailItem(item: OrderItem) = StoqCard(Modifier.fillMaxWidth(), PaddingValues(10.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DishImage(item, 64.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${item.quantity} шт. × ${money(item.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(money(item.price * item.quantity), style = MaterialTheme.typography.titleMedium)
            StoqStatusPill(item.availability.label(), item.availability.tone(), Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun OrderDetailsSummary(order: Order) = Surface(Modifier.fillMaxWidth(), color = CustomTheme.colors.primary.copy(alpha = .14f), shape = MaterialTheme.shapes.medium) {
    Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.SpaceAround) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocalDining, null, tint = CustomTheme.colors.success)
            Spacer(Modifier.width(9.dp))
            SummaryValue("Всего позиций", order.positions.toString())
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Refresh, null, tint = CustomTheme.colors.success)
            Spacer(Modifier.width(9.dp))
            SummaryValue("Общая сумма заказа", money(order.total))
        }
    }
}

@Composable
private fun EmptyOrderDetails() = Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
    Text("Заказ не найден", modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun DishPicker(query: String, dishes: List<OrderItem>, action: (OrdersAction) -> Unit, onGuestOrderQrClick: () -> Unit) = StoqCard(Modifier.fillMaxWidth()) {
    var showSuggestions by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Text("Добавьте блюда в заказ", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { value ->
                showSuggestions = true
                action(OrdersAction.MenuQuery(value))
            },
            modifier = Modifier.weight(1f).onFocusChanged { focused ->
                showSuggestions = focused.isFocused
                if (focused.isFocused) action(OrdersAction.MenuQuery(query))
            },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) Icon(Icons.Outlined.Close, "Очистить", Modifier.clickable { action(OrdersAction.MenuQuery("")) }) },
            placeholder = { Text("Поиск блюд") },
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors()
        )
        Surface(Modifier.height(56.dp).width(76.dp).clickable(onClick = onGuestOrderQrClick), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = MaterialTheme.colorScheme.surface) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.QrCodeScanner, "Гостевой заказ по QR", tint = CustomTheme.colors.primaryDark) } }
    }
    if (showSuggestions) {
        Spacer(Modifier.height(8.dp))
        dishes.take(5).forEach { dish ->
            Surface(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { showSuggestions = false; focusManager.clearFocus(); action(OrdersAction.AddDish(dish)) }, color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) { Row(Modifier.padding(vertical = 6.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) { DishImage(dish, 52.dp); Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(dish.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(dish.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(money(dish.price), style = MaterialTheme.typography.labelLarge) } }
        }
    }
}

@Composable
private fun GuestOrderQrDialog(onDismiss: () -> Unit) {
    val guestOrderLink = "https://stoq.app/order/new/guest/42"
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val qrBitmap = remember(guestOrderLink) { guestOrderQrBitmap(guestOrderLink) }
    var copied by remember { mutableStateOf(false) }
    val qrDownloadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = StoqSpacing.ScreenHorizontal),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(StoqSpacing.Large)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("Гостевой заказ по QR", style = MaterialTheme.typography.titleLarge)
                        Text("Позвольте гостям создавать заказы самостоятельно", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.Close, "Закрыть", modifier = Modifier.size(24.dp).clickable(onClick = onDismiss), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = StoqSpacing.Large).size(210.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = CustomTheme.colors.primary.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(178.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR-код гостевого заказа",
                                modifier = Modifier.padding(9.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.stoq_leaf),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }
                Text("Сканируйте для открытия меню", modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = StoqSpacing.Small), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Ссылка на гостевой заказ", modifier = Modifier.padding(top = StoqSpacing.Large), style = MaterialTheme.typography.labelLarge)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.ExtraSmall),
                    shape = MaterialTheme.shapes.small,
                    color = CustomTheme.colors.surfaceSecondary
                ) {
                    Text(guestOrderLink, modifier = Modifier.padding(horizontal = StoqSpacing.Medium, vertical = 10.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                    Button(
                        onClick = { clipboard.setText(AnnotatedString(guestOrderLink)); copied = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Text(if (copied) "Скопировано" else "Копировать", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
                    }
                    OutlinedButton(onClick = { qrDownloadLauncher.launch("guest-order-qr.png") }, modifier = Modifier.weight(1f).height(50.dp)) {
                        Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp))
                        Text("Скачать QR", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
                    }
                }
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, guestOrderLink)
                        }, "Поделиться гостевым заказом"))
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Small).height(48.dp)
                ) {
                    Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
                    Text("Поделиться ссылкой", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium),
                    shape = MaterialTheme.shapes.small,
                    color = CustomTheme.colors.primary.copy(alpha = 0.10f)
                ) {
                    Row(Modifier.padding(StoqSpacing.Medium), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Info, null, modifier = Modifier.size(19.dp), tint = CustomTheme.colors.primaryDark)
                        Text("Поделитесь ссылкой или QR-кодом, чтобы гости могли самостоятельно оформить заказ.", modifier = Modifier.padding(start = StoqSpacing.Small), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun guestOrderQrBitmap(link: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        link,
        BarcodeFormat.QR_CODE,
        300,
        300,
        mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, EncodeHintType.MARGIN to 1)
    )
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.rgb(12, 32, 43) else android.graphics.Color.WHITE)
            }
        }
    }
}

@Composable
private fun AvailabilityBanner() = Surface(Modifier.fillMaxWidth(), color = CustomTheme.colors.primary.copy(alpha = .12f), shape = MaterialTheme.shapes.medium) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).clip(CircleShape).background(CustomTheme.colors.success), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.surface) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Автоматическая проверка доступности", style = MaterialTheme.typography.labelLarge); Text("Система проверяет наличие всех ингредиентов для каждого блюда в реальном времени.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun BasketItem(item: OrderItem, action: (OrdersAction) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    StoqCard(Modifier.fillMaxWidth(), PaddingValues(10.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            DishImage(item, 70.dp); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row { Column(Modifier.weight(1f)) { Text(item.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; StoqStatusPill(item.availability.label(), item.availability.tone()) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) { QuantityControl(item.quantity, { action(OrdersAction.ChangeQuantity(item.dishId, -1)) }, { action(OrdersAction.ChangeQuantity(item.dishId, 1)) }); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(money(item.price), style = MaterialTheme.typography.labelLarge); Text("Цена за ед.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Column(horizontalAlignment = Alignment.End) { Text(money(item.price * item.quantity), style = MaterialTheme.typography.titleMedium); Text("Сумма", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Outlined.DeleteOutline, "Удалить", Modifier.padding(start = 8.dp).size(25.dp).clickable { showDeleteDialog = true }, tint = CustomTheme.colors.error) }
            }
        }
        item.unavailableIngredient?.let { ErrorBanner("Нет ингредиентов", it) }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить блюдо из заказа?") },
            text = { Text("${item.name} будет удалено из текущего заказа.") },
            confirmButton = { Button(onClick = { showDeleteDialog = false; action(OrdersAction.Remove(item.dishId)) }) { Text("Удалить") } },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun QuantityControl(quantity: Int, onMinus: () -> Unit, onPlus: () -> Unit) = Surface(shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = MaterialTheme.colorScheme.surface) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Remove, "Уменьшить", Modifier.size(34.dp).padding(7.dp).clickable(onClick = onMinus)); Text(quantity.toString(), Modifier.padding(horizontal = 7.dp), style = MaterialTheme.typography.labelLarge); Icon(Icons.Outlined.Add, "Увеличить", Modifier.size(34.dp).padding(7.dp).clickable(onClick = onPlus)) } }

@Composable
private fun OrderSummary(state: OrdersState) = Surface(Modifier.fillMaxWidth(), color = CustomTheme.colors.primary.copy(alpha = .14f), shape = MaterialTheme.shapes.medium) {
    val existingOrder = state.editingOrder
    val positions = (existingOrder?.positions ?: 0) + state.basket.sumOf { it.quantity }
    val total = (existingOrder?.total ?: 0) + state.basket.sumOf { it.price * it.quantity }
    Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.SpaceAround) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.LocalDining, null, tint = CustomTheme.colors.success); Spacer(Modifier.width(9.dp)); SummaryValue("Всего позиций", positions.toString()) }
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Refresh, null, tint = CustomTheme.colors.success); Spacer(Modifier.width(9.dp)); SummaryValue("Общая сумма заказа", money(total)) }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) = Column { Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleLarge) }

@Composable
private fun ErrorBanner(title: String, text: String) = Surface(Modifier.fillMaxWidth().padding(top = 8.dp), color = CustomTheme.colors.error.copy(alpha = .11f), shape = MaterialTheme.shapes.small) { Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(28.dp).clip(CircleShape).background(CustomTheme.colors.error), contentAlignment = Alignment.Center) { Text("!", color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.titleMedium) }; Spacer(Modifier.width(10.dp)); Text("$title: $text", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.error, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun CreateActions(state: OrdersState, action: (OrdersAction) -> Unit, onSave: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onSave, Modifier.weight(1f).height(52.dp), shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text("Сохранить") }
        Button({ action(OrdersAction.Create) }, Modifier.weight(1.35f).height(52.dp), enabled = state.basket.isNotEmpty() && state.basket.none { it.availability == Availability.Unavailable } && !state.isCreating, shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.buttonColors(containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary)) { Text(if (state.isCreating) "Сохранение…" else if (state.editingOrderId != null) "Добавить блюда" else "Создать заказ  →") }
    }
}

@Composable
private fun CreatedBanner(updated: Boolean, onBack: () -> Unit) = Surface(Modifier.fillMaxWidth(), color = CustomTheme.colors.primary.copy(alpha = .14f), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(42.dp).clip(CircleShape).background(CustomTheme.colors.success), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.surface) }; Text(if (updated) "Заказ обновлён" else "Заказ создан", modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.titleLarge); Text(if (updated) "Новые блюда добавлены в заказ" else "Заказ отправлен в обработку", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onBack, Modifier.padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary)) { Text("К списку заказов") } } }

@Composable
private fun EmptyBasket() = Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.LocalDining, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Text("Добавьте блюда в заказ", modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
private fun EditOrderHint() = Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Add, null, tint = CustomTheme.colors.primaryDark)
        Text("Выберите блюда через поиск, чтобы добавить их в заказ.", modifier = Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DishImage(item: OrderItem, size: androidx.compose.ui.unit.Dp) = bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage(
    imageUrl = item.imageUrl,
    contentDescription = item.name,
    modifier = Modifier.size(size).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant),
    contentScale = ContentScale.Crop
)

@Composable
private fun StaticField(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, modifier: Modifier) = Surface(modifier.heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = MaterialTheme.colorScheme.surface) { Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)); Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2) } }

@Composable
private fun SelectField(label: String, value: String, options: List<String>, modifier: Modifier, leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(Modifier.fillMaxWidth().height(56.dp).clip(MaterialTheme.shapes.medium).clickable { expanded = true }, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = MaterialTheme.colorScheme.surface) { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { leadingIcon?.let { Icon(it, null, modifier = Modifier.size(23.dp), tint = CustomTheme.colors.primaryDark); Spacer(Modifier.width(9.dp)) }; Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.KeyboardArrowDown, null, tint = CustomTheme.colors.primaryDark) } }
        DropdownMenu(expanded, { expanded = false }, shape = MaterialTheme.shapes.medium, containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp, shadowElevation = 8.dp) { options.forEach { option -> DropdownMenuItem({ Text(option, style = MaterialTheme.typography.bodyMedium, color = if (option == value) CustomTheme.colors.primaryDark else MaterialTheme.colorScheme.onSurface) }, { onSelected(option); expanded = false }, modifier = Modifier.background(if (option == value) CustomTheme.colors.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surface)) } }
    }
}

@Composable
private fun DateTimeField(value: String, modifier: Modifier, onSelected: (String) -> Unit) {
    val context = LocalContext.current
    Surface(modifier.height(56.dp).clip(MaterialTheme.shapes.medium).clickable {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(context, { _, year, month, day ->
            android.app.TimePickerDialog(context, { _, hour, minute ->
                onSelected("%02d.%02d.%d %02d:%02d".format(day, month + 1, year, hour, minute))
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
    }, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp), tint = CustomTheme.colors.primaryDark); Spacer(Modifier.width(8.dp)); Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = CustomTheme.colors.primaryDark, unfocusedBorderColor = MaterialTheme.colorScheme.outline)

private fun OrderStatus.label() = when (this) { OrderStatus.Processing -> "В обработке"; OrderStatus.Preparing -> "Готовится"; OrderStatus.Completed -> "Выполнен" }
private fun OrderStatus.tone() = when (this) { OrderStatus.Processing -> StoqStatusTone.Warning; OrderStatus.Preparing -> StoqStatusTone.Info; OrderStatus.Completed -> StoqStatusTone.Success }
private fun Availability.label() = when (this) { Availability.Available -> "Доступно"; Availability.Unavailable -> "Недоступно"; Availability.Unknown -> "мог" }
private fun Availability.tone() = when (this) { Availability.Available -> StoqStatusTone.Success; Availability.Unavailable -> StoqStatusTone.Error; Availability.Unknown -> StoqStatusTone.Info }
private fun money(value: Int) = "${String.format("%,d", value).replace(',', ' ')} ₽"
