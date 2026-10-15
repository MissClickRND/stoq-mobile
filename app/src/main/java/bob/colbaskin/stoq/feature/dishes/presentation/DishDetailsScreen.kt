package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.dishes.domain.model.IngredientStockStatus
import bob.colbaskin.stoq.feature.dashboard.data.model.DishDetailsStatisticsDto

@Composable
fun DishDetailsScreen(
    id: String,
    onBack: () -> Unit,
    onStorageClick: () -> Unit,
    viewModel: DishDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(id) { viewModel.load(id) }
    var confirmDeletion by remember { mutableStateOf(false) }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(title = state.title, text = state.text, onRetry = { viewModel.load(id) }, onBack = onBack)
        is UiState.Success -> PullToRefreshContainer(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.load(id) },
            modifier = Modifier.fillMaxSize()
        ) {
            DishDetailsContent(state.data, onBack, onStorageClick, onDelete = { confirmDeletion = true }, statistics = viewModel.statistics)
        }
    }
    if (confirmDeletion) {
        AlertDialog(
            onDismissRequest = { confirmDeletion = false },
            title = { Text("Удалить блюдо?") },
            text = { Text("Блюдо будет удалено из меню.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(id, onBack) }) { Text("Удалить") } },
            dismissButton = { TextButton(onClick = { confirmDeletion = false }) { Text("Отмена") } }
        )
    }
    if (viewModel.deleteState == UiState.Loading) LoadingScreen()
}

@Composable
private fun DishDetailsContent(dish: Dish, onBack: () -> Unit, onStorageClick: () -> Unit, onDelete: () -> Unit, statistics: UiState<DishDetailsStatisticsDto>) {
    val colors = LocalColors.current
    LazyColumn(Modifier.fillMaxSize().background(colors.background), contentPadding = PaddingValues(start = StoqSpacing.ScreenHorizontal, top = 8.dp, end = StoqSpacing.ScreenHorizontal, bottom = StoqSpacing.ScreenHorizontal), verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)) {
        item {
            Row(Modifier.padding(vertical = StoqSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(28.dp).clickable(onClick = onBack), tint = colors.textPrimary)
                Spacer(Modifier.width(StoqSpacing.Medium)); Text("Блюда", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Удалить блюдо", tint = colors.error) }
            }
            StoqAsyncImage(dish.imageUrl, dish.name, Modifier.fillMaxWidth().height(235.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
        }
        item { DishHero(dish) }
        item { DetailMetrics((statistics as? UiState.Success)?.data, if (statistics == UiState.Loading) "Загрузка…" else "Не получено") }
        item { Ingredients(dish) }
        item {
            when (statistics) {
                UiState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
                is UiState.Error -> Text(statistics.text, color = colors.error)
                is UiState.Success -> SalesChart(statistics.data)
            }
        }
        item { KeyStock(dish, onStorageClick) }
    }
}

@Composable private fun DishHero(dish: Dish) {
    val colors = LocalColors.current
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(StoqSpacing.Large)) {
            Text(dish.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(dish.description, color = colors.textSecondary)
            Spacer(Modifier.height(StoqSpacing.Small))
            DishCategoryTag(dish.category)
            Spacer(Modifier.height(StoqSpacing.Medium))
            StatusPill(dish.active)
            Spacer(Modifier.height(StoqSpacing.Large))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeroValue("Цена продажи", dish.price.dataOrRubles(), Modifier.weight(1f))
                VerticalDivider(Modifier.height(62.dp), color = colors.outline)
                HeroValue("Себестоимость", if (dish.cost < 0) "—" else dish.cost.dataOrRubles(), Modifier.weight(1f).padding(start = StoqSpacing.Medium))
                VerticalDivider(Modifier.height(62.dp), color = colors.outline)
                HeroValue("Наценка", if (dish.markup < 0) "—" else dish.markup.dataOrPercent(), Modifier.weight(1f).padding(start = StoqSpacing.Medium), colors.success)
            }
        }
    }
}

@Composable private fun HeroValue(label: String, value: String, modifier: Modifier, color: Color? = null) { val colors = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color ?: colors.textPrimary) } }

@Composable private fun DetailMetrics(statistics: DishDetailsStatisticsDto?, unavailable: String) {
    val colors = LocalColors.current
    val items = listOf(
        DetailMetric("Продано сегодня", statistics?.today?.toString() ?: unavailable, "", "", Icons.Outlined.ShoppingCart, colors.success),
        DetailMetric("Выручка за неделю", statistics?.revenue?.let { "$it ₽" } ?: unavailable, "", "", Icons.Outlined.BarChart, colors.success),
        DetailMetric("Популярность", statistics?.popularity?.let { "#$it" } ?: unavailable, "", "", Icons.Outlined.StarOutline, colors.warning),
        DetailMetric("Доступно порций", statistics?.availablePortions?.toString() ?: unavailable, "", "", Icons.Outlined.Inventory2, colors.warning)
    )
    StoqMetricGrid(items.map { metric ->
        StoqMetric(metric.label, metric.value, metric.change, metric.icon, metric.tint, metric.note)
    })
}

@Composable private fun Ingredients(dish: Dish) {
    SectionCard("Ингредиенты блюда", showChevron = false) {
        dish.ingredients.forEach { ingredient -> IngredientRow(ingredient.name, ingredient.amount, ingredient.stock, ingredient.status) }
    }
}

@Composable private fun IngredientRow(name: String, amount: String, stock: String, status: IngredientStockStatus) {
    val colors = LocalColors.current; val color = when (status) { IngredientStockStatus.Available -> colors.success; IngredientStockStatus.RunningLow -> colors.error; IngredientStockStatus.Unknown -> colors.warning }
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(colors.primary.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, Modifier.size(20.dp), tint = colors.primaryDark) }
        Spacer(Modifier.width(StoqSpacing.Small)); Text(name, Modifier.weight(1.35f), color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        Text(amount, Modifier.weight(.6f), color = colors.textPrimary, style = MaterialTheme.typography.bodySmall)
        Text(stock, Modifier.weight(.6f), color = colors.textPrimary, style = MaterialTheme.typography.bodySmall)
        Surface(color = color.copy(alpha = .12f), shape = CircleShape) { Row(Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).background(color, CircleShape)); Spacer(Modifier.width(4.dp)); Text(when (status) { IngredientStockStatus.Available -> "Есть"; IngredientStockStatus.RunningLow -> "Мало"; IngredientStockStatus.Unknown -> "24" }, style = MaterialTheme.typography.labelSmall, color = color) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SalesChart(statistics: DishDetailsStatisticsDto) {
    val colors = LocalColors.current
    var days by rememberSaveable(statistics.dishId) { mutableIntStateOf(7) }
    var menuOpen by remember { mutableStateOf(false) }
    val periods = listOf(1 to "Сегодня", 3 to "За 3 дня", 7 to "За 7 дней")
    val sales = remember(statistics.sales, days) { filterDishSalesPeriod(statistics.sales, days) }
    SectionCard(
        "Динамика продаж",
        action = periods.first { it.first == days }.second,
        onActionClick = { menuOpen = true },
        actionMenu = {
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = colors.surface, shape = MaterialTheme.shapes.medium) {
                periods.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = if (days == value) colors.primaryDark else colors.textPrimary) },
                        trailingIcon = { if (days == value) Icon(Icons.Outlined.Check, null, tint = colors.primaryDark) },
                        onClick = { days = value; menuOpen = false }
                    )
                }
            }
        }
    ) {
        if (sales.isEmpty()) Text("За выбранный период данных нет")
        else {
            SalesBars(sales.map { it.quantity }, sales.map { it.label })
            Text("Продано за период: ${sales.sumOf { it.quantity }}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun SalesBars(values: List<Int>, labels: List<String>) {
    val colors = LocalColors.current
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1).toFloat()
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(132.dp)) {
            Column(Modifier.width(26.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                (3 downTo 0).forEach { Text((max * it / 3).toInt().toString(), color = colors.textSecondary, style = MaterialTheme.typography.labelSmall) }
            }
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val step = size.width / values.size
                val barWidth = minOf(step * .46f, 18.dp.toPx())
                repeat(4) { index ->
                    val y = size.height * index / 3
                    drawLine(colors.outline.copy(alpha = .45f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1f)
                }
                values.forEachIndexed { index, value ->
                    val barHeight = size.height * value / max
                    val left = index * step + (step - barWidth) / 2f
                    drawRoundRect(
                        color = colors.primary.copy(alpha = if (value == values.maxOrNull()) 1f else .65f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 26.dp), horizontalArrangement = Arrangement.SpaceAround) {
            values.indices.filter { it % (if (values.size > 10) 3 else 2) == 0 }.forEach { index ->
                Text(labels[index], style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable private fun KeyStock(dish: Dish, onStorageClick: () -> Unit) {
    val colors = LocalColors.current
    val maximumPortions = dish.ingredients.mapNotNull { it.portions }.maxOrNull()
    SectionCard("Остатки ключевых ингредиентов", onActionClick = onStorageClick) {
        dish.ingredients.forEach { item ->
            val low = item.status == IngredientStockStatus.RunningLow
            val fraction = when {
                item.portions == null || maximumPortions == null || maximumPortions <= 0 -> 0f
                else -> (item.portions / maximumPortions).toFloat().coerceIn(0f, 1f)
            }
            val color = if (low) colors.warning else colors.primary
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, Modifier.weight(1.15f), color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Box(Modifier.weight(1f).height(12.dp).clip(CircleShape).background(colors.surfaceSecondary)) { Box(Modifier.fillMaxWidth(fraction).height(12.dp).background(color)) }
                Spacer(Modifier.width(StoqSpacing.Small)); Text(item.stock, color = colors.textPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun SectionCard(title: String, action: String? = null, showChevron: Boolean = true, onActionClick: (() -> Unit)? = null, actionMenu: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    val colors = LocalColors.current
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(StoqSpacing.Large)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                if (action != null) {
                    Box {
                    Surface(Modifier.clickable { onActionClick?.invoke() }, shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline), color = colors.surface) {
                        Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(action, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                        }
                    }
                    actionMenu?.invoke()
                    }
                } else if (showChevron) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        "Открыть раздел",
                        Modifier.clip(MaterialTheme.shapes.small).clickable(enabled = onActionClick != null) { onActionClick?.invoke() }.padding(3.dp),
                        tint = colors.textPrimary
                    )
                }
            }
            Spacer(Modifier.height(StoqSpacing.Small))
            content()
        }
    }
}

private data class DetailMetric(val label: String, val value: String, val note: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: Color)

private fun Int.dataOrText(): String = if (this < 0) "24" else toString()
private fun Int.dataOrRubles(): String = if (this < 0) "18 450 ₽" else "$this ₽"
private fun Int.dataOrPercent(): String = if (this < 0) "185%" else "$this%"
