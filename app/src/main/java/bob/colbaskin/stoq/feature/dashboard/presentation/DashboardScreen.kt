package bob.colbaskin.stoq.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import kotlin.math.roundToInt
import bob.colbaskin.stoq.feature.dashboard.data.model.*
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.dishes.data.RecommendedDish
import androidx.compose.material3.CircularProgressIndicator
import bob.colbaskin.stoq.common.core.presentation.RefreshOnResume
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer

private val DashboardGreen = Color(0xFF22A51C)
private val DashboardBlue = Color(0xFF319BF4)
private val DashboardPurple = Color(0xFF9864E9)
private val DashboardRed = Color(0xFFFF5A68)
private val DashboardYellow = Color(0xFFFFBE35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onProductsClick: () -> Unit,
    onDishesClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onSupplyClick: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var selectedRange by remember { mutableIntStateOf(1) }
    var selectedMetric by remember { mutableIntStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()
    val ranges = listOf("7 дней", "14 дней", "30 дней")
    val metrics = listOf("Выручка", "Прибыль", "Заказы")
    var expiringDays by remember { mutableStateOf("7") }

    LaunchedEffect(selectedRange) { viewModel.selectPeriod(listOf(7, 14, 30)[selectedRange.coerceIn(0, 2)]) }
    RefreshOnResume(remember(viewModel) { viewModel::refresh })
    val overview = (viewModel.overview as? UiState.Success)?.data

    PullToRefreshContainer(viewModel.isRefreshing, viewModel::refresh, Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Large)
    ) {
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
                Text("Дашборд", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Ключевые показатели вашего ресторана",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                DateRangeButton(
                    text = overview?.let { "${dashboardDate(it.from)} – ${dashboardDate(it.to)}" } ?: "Выберите период",
                    onClick = { showDatePicker = true }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranges.forEachIndexed { index, label ->
                        FilterChip(label, selected = selectedRange == index, Modifier.weight(1f)) { selectedRange = index }
                    }
                }
            }
        }
        item {
            StoqCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    metrics.forEachIndexed { index, label ->
                        FilterChip(label, selectedMetric == index, Modifier.weight(1f)) { selectedMetric = index }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(metrics[selectedMetric], style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(overview?.summary?.let { when (selectedMetric) { 0 -> dashboardMoney(it.revenue); 1 -> it.earning?.let(::dashboardMoney) ?: "—"; else -> it.orders.toString() } } ?: "Загрузка…", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(12.dp))
                }
                Text("За выбранный период", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                StatisticsState(viewModel.overview, onRetry = { viewModel.loadPeriod(listOf(7, 14, 30)[selectedRange.coerceIn(0, 2)]) }) {
                    MetricChart(it.points, selectedMetric)
                }
            }
        }
        item { MetricGrid(overview?.summary, onOrdersClick, if (viewModel.overview == UiState.Loading) "Загрузка…" else "Не получено") }
        item { ShortagesSection(viewModel.shortages, onProductsClick) }
        item { LowStockSection(viewModel.stock, onProductsClick) }
        item {
            ExpiringBatchesSection(
                daysThreshold = expiringDays,
                state = viewModel.expiringInventoryState,
                onDaysThresholdChange = { expiringDays = it.filter(Char::isDigit) },
                onLoad = { expiringDays.toIntOrNull()?.let(viewModel::loadExpiringInventory) },
                onBatchClick = { batchId, productId -> viewModel.openSupplyForBatch(productId, batchId, onSupplyClick) }
            )
        }
        item { PromotionSection(viewModel.promotion, onDishesClick) }
        item { CategoriesSection(viewModel.categories, onDishesClick) }
        item { Spacer(Modifier.height(12.dp)) }
    }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { Text("Применить", modifier = Modifier.padding(16.dp).clickable {
                val start = datePickerState.selectedStartDateMillis
                val end = datePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    val length = ((end - start) / 86_400_000 + 1).toInt()
                    selectedRange = when { length <= 7 -> 0; length <= 14 -> 1; else -> 2 }
                }
                showDatePicker = false
            }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            dismissButton = { Text("Отмена", modifier = Modifier.padding(16.dp).clickable { showDatePicker = false }) }
        ) {
            Column(Modifier.weight(1f, fill = false)) {
                Text("Сервер поддерживает последние 7, 14 или 30 дней. По длине выбранного диапазона будет применён соответствующий период.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                DateRangePicker(state = datePickerState, modifier = Modifier.weight(1f, fill = false))
            }
        }
    }
}

@Composable
private fun DateRangeButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = background,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun MetricChart(points: List<DashboardPointDto>, metric: Int) {
    if (points.isEmpty()) { Text("За выбранный период данных нет"); return }
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = .24f)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val amounts = points.map { when (metric) { 0 -> it.revenue; 1 -> it.earning; else -> it.orders.toDouble() } }
    if (amounts.all { it == null }) { Text("Сервер пока не рассчитал прибыль за этот период."); return }
    val minimum = (amounts.filterNotNull().minOrNull() ?: 0.0).coerceAtMost(0.0)
    val maximum = (amounts.filterNotNull().maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    val scale = maximum - minimum
    val values = amounts.map { amount -> amount?.let { ((it - minimum) / scale).toFloat() } }
    val labels = points.map { it.label }
    var selectedIndex by remember(points, metric) { mutableIntStateOf(points.lastIndex) }
    var chartWidth by remember { mutableStateOf(0f) }
    fun selectAt(x: Float) {
        if (chartWidth > 0f) selectedIndex = (x / chartWidth * (values.size - 1)).roundToInt().coerceIn(0, values.lastIndex)
    }
    Column {
        Row {
            Column(modifier = Modifier.height(172.dp), verticalArrangement = Arrangement.SpaceBetween) {
                (4 downTo 0).forEach { Text(dashboardNumber(minimum + scale * it / 4), color = muted, style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f).height(172.dp)) {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .onSizeChanged { chartWidth = it.width.toFloat() }
                        .pointerInput(points, metric, chartWidth) {
                            detectTapGestures { selectAt(it.x) }
                        }
                        .pointerInput(points, metric, chartWidth) {
                            detectHorizontalDragGestures(
                                onDragStart = { selectAt(it.x) },
                                onHorizontalDrag = { change, _ -> change.consume(); selectAt(change.position.x) }
                            )
                        }
                ) {
                    val step = size.width / (values.size - 1).coerceAtLeast(1)
                    repeat(5) { line -> drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, line * size.height / 4f), androidx.compose.ui.geometry.Offset(size.width, line * size.height / 4f)) }
                    repeat(7) { line -> drawLine(gridColor, androidx.compose.ui.geometry.Offset(line * size.width / 6f, 0f), androidx.compose.ui.geometry.Offset(line * size.width / 6f, size.height)) }
                    val linePath = Path()
                    var connected = false
                    values.forEachIndexed { index, value ->
                        if (value == null) connected = false
                        else {
                            val point = androidx.compose.ui.geometry.Offset(index * step, size.height * (1f - value))
                            if (!connected) linePath.moveTo(point.x, point.y) else linePath.lineTo(point.x, point.y)
                            connected = true
                        }
                    }
                    val fillPath = Path().apply { addPath(linePath); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                    if (values.none { it == null }) drawPath(fillPath, Brush.verticalGradient(listOf(DashboardGreen.copy(alpha = .26f), DashboardGreen.copy(alpha = .02f))))
                    drawPath(linePath, DashboardGreen, style = Stroke(width = 6f, cap = StrokeCap.Round))
                    values[selectedIndex]?.let { selectedValue ->
                    val marker = androidx.compose.ui.geometry.Offset(selectedIndex * step, size.height * (1f - selectedValue))
                    drawLine(DashboardGreen.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(marker.x, marker.y), androidx.compose.ui.geometry.Offset(marker.x, size.height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f)))
                    drawCircle(Color.White, 12f, marker)
                    drawCircle(DashboardGreen, 7f, marker)
                    }
                }
                Surface(modifier = Modifier.align(Alignment.TopCenter).offset(x = (-18).dp), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small, shadowElevation = 4.dp) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text(points[selectedIndex].date, style = MaterialTheme.typography.bodySmall, color = muted); Text(amounts[selectedIndex]?.let { if (metric == 2) dashboardNumber(it) else dashboardMoney(it) } ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DashboardGreen) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        if (amounts.any { it == null }) Text("За часть дней сервер не рассчитал прибыль. Пропуски не равны нулю.", style = MaterialTheme.typography.bodySmall, color = muted)
        Row(modifier = Modifier.padding(start = 72.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { labels.filterIndexed { index, _ -> index % (labels.size / 4).coerceAtLeast(1) == 0 }.take(5).forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = muted) } }
    }
}

@Composable
private fun MetricGrid(summary: DashboardSummaryDto?, onOrdersClick: () -> Unit, unavailable: String) {
    val items = listOf(
        StoqMetric("Выручка", summary?.revenue?.let(::dashboardMoney) ?: unavailable, "", Icons.Outlined.AccountBalanceWallet, DashboardGreen),
        StoqMetric("Прибыль", summary?.earning?.let(::dashboardMoney) ?: if (summary == null) unavailable else "—", "", Icons.Outlined.BarChart, DashboardGreen),
        StoqMetric("Заказы", summary?.orders?.toString() ?: unavailable, "", Icons.Outlined.ShoppingCart, DashboardRed, onClick = onOrdersClick),
        StoqMetric("Средний чек", summary?.averageCheck?.let(::dashboardMoney) ?: unavailable, "", Icons.Outlined.Group, MaterialTheme.colorScheme.onSurfaceVariant)
    )
    StoqMetricGrid(items, Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun LowStockSection(state: UiState<InventoryStatisticsDto>, onProductsClick: () -> Unit) {
    SectionCard("Товары с низким остатком", "Все товары", onProductsClick) {
        StatisticsState(state) { statistics ->
        val products = statistics.lowStockProducts.map { StockProduct(it.product.name, "${dashboardNumber(it.quantity)} ${it.product.unit?.shortName.orEmpty()}", it.product.id.toString(), null) }
        if (products.isEmpty()) Text("Товаров с низким остатком нет")
        products.forEachIndexed { index, product ->
            if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable(onClick = onProductsClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                ProductAvatar(product.avatarRes)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(product.name, fontWeight = FontWeight.Medium); Text("Артикул: ${product.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(product.amount, color = DashboardRed, fontWeight = FontWeight.Bold)
            }
        }
        }
    }
}

@Composable
private fun ExpiringBatchesSection(
    daysThreshold: String,
    state: UiState<List<StorageProduct>>,
    onDaysThresholdChange: (String) -> Unit,
    onLoad: () -> Unit,
    onBatchClick: (Long, Long) -> Unit
) {
    val days = daysThreshold.toIntOrNull()
    var expanded by remember { mutableStateOf(false) }
    StoqCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().animateContentSize(animationSpec = spring())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Скоро испортятся", style = MaterialTheme.typography.titleLarge)
                Text("Срок годности истекает в ближайшие ${days ?: "7"} дн.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = DashboardRed.copy(alpha = .16f), shape = MaterialTheme.shapes.large) {
                Text((state as? UiState.Success)?.data?.size?.toString() ?: "…", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = DashboardRed, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = daysThreshold,
                onValueChange = onDaysThresholdChange,
                modifier = Modifier.weight(1f),
                label = { Text("Порог, дней") },
                suffix = { Text("дн.") },
                singleLine = true,
                isError = daysThreshold.isNotBlank() && days == null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DashboardYellow,
                    focusedLabelColor = DashboardYellow
                ),
                trailingIcon = {
                    IconButton(onClick = onLoad, enabled = days != null && days > 0 && state !is UiState.Loading) {
                        Icon(Icons.Outlined.Check, "Подтвердить период", tint = DashboardGreen)
                    }
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        when (state) {
            UiState.Loading -> Text("Загружаем партии…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            is UiState.Error -> Text(
                "${state.title}. Повторить",
                modifier = Modifier.clip(MaterialTheme.shapes.small).clickable(onClick = onLoad).padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = DashboardRed
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Text("В течение ${days ?: "7"} дн. истекающих партий нет", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val visibleBatches = if (expanded) state.data else state.data.take(3)
                    visibleBatches.forEachIndexed { index, batch ->
                        if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                                .clickable { onBatchClick(batch.batchId, batch.id) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(batch.name, color = DashboardGreen, fontWeight = FontWeight.Bold)
                                Text("Партия № ${batch.batchId.toString().padStart(5, '0')}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(batch.expiresAt.toExpiryDateTimeDisplay(), modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(batch.currentStock, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Surface(color = DashboardRed.copy(alpha = .16f), shape = MaterialTheme.shapes.large) {
                                    Text(batch.daysRemaining?.let { "$it ${if (it == 1) "день" else "дн."}" } ?: "2 дня", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = DashboardRed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (state.data.size > 3) {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                if (expanded) "Свернуть список" else "Показать все партии",
                                tint = DashboardGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String?.toExpiryDateTimeDisplay(): String {
    val value = this ?: return "24"
    val date = value.substringBefore('T').split('-')
    val time = value.substringAfter('T', "").take(5)
    return if (date.size == 3) "до ${date[2]}.${date[1]}.${date[0]}${if (time.isNotBlank()) ", $time" else ""}" else "24"
}

@Composable
private fun PromotionSection(state: UiState<List<RecommendedDish>>, onDishesClick: () -> Unit) {
    SectionCard("Блюда для продвижения", "Все блюда", onDishesClick) {
        StatisticsState(state) { dishes ->
        if (dishes.isEmpty()) Text("Рекомендуемых блюд нет")
        dishes.take(3).forEachIndexed { index, dish ->
            if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable(onClick = onDishesClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ReceiptLong, null, tint = DashboardGreen)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(dish.name ?: "Блюдо № ${dish.dishId}", fontWeight = FontWeight.Medium)
                    Text("Доступно порций: ${dish.maxPortions?.let(::dashboardNumber) ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) { Text(dish.salePrice?.let(::dashboardMoney) ?: "—", fontWeight = FontWeight.Bold); Text(dish.profit?.let { "+${dashboardMoney(it)}" } ?: "—", color = DashboardGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall) }
            }
        }
        }
    }
}

@Composable
private fun CategoriesSection(state: UiState<List<Dish>>, onDishesClick: () -> Unit) {
    val colors = listOf(DashboardGreen, DashboardBlue, DashboardPurple, DashboardRed, DashboardYellow, Color.LightGray)
    SectionCard("Количество блюд по категориям", "Все блюда", onDishesClick) {
        StatisticsState(state) { dishes ->
        val categories = dishes.groupingBy { it.category.takeUnless { value -> value == "24" || value.isBlank() } ?: "Без категории" }.eachCount().toList()
        val total = dishes.size
        if (total == 0) Text("Блюд пока нет")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(Modifier.size(130.dp)) {
                    var start = -90f
                    categories.forEachIndexed { index, (_, count) ->
                        val sweep = count / total.coerceAtLeast(1).toFloat() * 360f
                        drawArc(colors[index % colors.size], start, (sweep - 3f).coerceAtLeast(0f), false, style = Stroke(28f))
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(total.toString(), style = MaterialTheme.typography.titleLarge); Text("всего", style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                categories.forEachIndexed { index, (name, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(colors[index % colors.size])); Spacer(Modifier.width(8.dp)); Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun SectionCard(title: String, action: String, onActionClick: () -> Unit, content: @Composable () -> Unit) {
    StoqCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); Text(action, modifier = Modifier.clip(MaterialTheme.shapes.small).clickable(onClick = onActionClick).padding(6.dp), color = DashboardGreen, style = MaterialTheme.typography.labelLarge) }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ShortagesSection(state: UiState<ShortagesDto>, onProductsClick: () -> Unit) {
    SectionCard("История дефицита продуктов", "Все товары", onProductsClick) {
        StatisticsState(state) { history ->
            Text("Сколько раз продукт был недоступен для приготовления блюд за выбранный период.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (history.items.isEmpty()) Text("Случаев дефицита за период нет")
            history.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WarningAmber, null, tint = DashboardYellow)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Medium)
                        Text("Затронуто блюд: ${item.affectedDishes}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Не хватало:\n${item.incidents} ${shortageCountLabel(item.incidents)}", color = DashboardRed, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
            history.trackingStartedAt?.let { Text("Учёт с ${it.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun <T> StatisticsState(state: UiState<T>, onRetry: (() -> Unit)? = null, content: @Composable (T) -> Unit) {
    when (state) {
        UiState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
        is UiState.Error -> Column {
            Text(state.text, color = MaterialTheme.colorScheme.error)
            if (onRetry != null) Text("Повторить", Modifier.clickable(onClick = onRetry).padding(8.dp))
        }
        is UiState.Success -> content(state.data)
    }
}

private fun dashboardNumber(value: Double): String = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("ru-RU")).apply { maximumFractionDigits = 2 }.format(value)
private fun dashboardMoney(value: Double): String = "${dashboardNumber(value)} ₽"
private fun dashboardDate(value: String): String = runCatching {
    java.time.Instant.parse(value).atZone(java.time.ZoneId.of("Europe/Moscow")).format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}.getOrDefault(value.take(10))

private data class StockProduct(val name: String, val amount: String, val id: String, val avatarRes: Int?)

@Composable
private fun ProductAvatar(avatarRes: Int?) {
    Box(modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (avatarRes != null) {
            androidx.compose.foundation.Image(painterResource(avatarRes), null, modifier = Modifier.size(48.dp), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatDate(millis: Long): String = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
