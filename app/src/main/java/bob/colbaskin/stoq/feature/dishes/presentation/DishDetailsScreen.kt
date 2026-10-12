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
import bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.dishes.domain.model.IngredientStockStatus

@Composable
fun DishDetailsScreen(id: String, onBack: () -> Unit, viewModel: DishDetailsViewModel = hiltViewModel()) {
    LaunchedEffect(id) { viewModel.load(id) }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(title = state.title, text = state.text, onRetry = { viewModel.load(id) })
        is UiState.Success -> DishDetailsContent(state.data, onBack)
    }
}

@Composable
private fun DishDetailsContent(dish: Dish, onBack: () -> Unit) {
    val colors = LocalColors.current
    LazyColumn(Modifier.fillMaxSize().background(colors.background), contentPadding = PaddingValues(start = StoqSpacing.ScreenHorizontal, top = 8.dp, end = StoqSpacing.ScreenHorizontal, bottom = StoqSpacing.ScreenHorizontal), verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)) {
        item {
            Row(Modifier.padding(vertical = StoqSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(28.dp).clickable(onClick = onBack), tint = colors.textPrimary)
                Spacer(Modifier.width(StoqSpacing.Medium)); Text("Блюда", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            StoqAsyncImage(dish.imageUrl, dish.name, Modifier.fillMaxWidth().height(235.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
        }
        item { DishHero(dish) }
        item { DetailMetrics(dish) }
        item { Ingredients(dish) }
        item { SalesChart() }
        item { KeyStock(dish) }
    }
}

@Composable private fun DishHero(dish: Dish) {
    val colors = LocalColors.current
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(StoqSpacing.Large)) {
            Text(dish.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(dish.description, color = colors.textSecondary); Spacer(Modifier.height(StoqSpacing.Medium)); StatusPill(dish.active); Spacer(Modifier.height(StoqSpacing.Large))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeroValue("Цена продажи", dish.price.dataOrRubles(), Modifier.weight(1f))
                VerticalDivider(Modifier.height(62.dp), color = colors.outline)
                HeroValue("Себестоимость", dish.cost.dataOrRubles(), Modifier.weight(1f).padding(start = StoqSpacing.Medium))
                VerticalDivider(Modifier.height(62.dp), color = colors.outline)
                HeroValue("Наценка", dish.markup.dataOrPercent(), Modifier.weight(1f).padding(start = StoqSpacing.Medium), colors.success)
            }
        }
    }
}

@Composable private fun HeroValue(label: String, value: String, modifier: Modifier, color: Color? = null) { val colors = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color ?: colors.textPrimary) } }

@Composable private fun DetailMetrics(dish: Dish) {
    val colors = LocalColors.current
    val items = listOf(
        DetailMetric("Продано сегодня", dish.soldToday.dataOrText(), "мог", "", Icons.Outlined.ShoppingCart, colors.success),
        DetailMetric("Выручка за неделю", dish.weeklyRevenue.dataOrRubles(), "мог", "", Icons.Outlined.BarChart, colors.success),
        DetailMetric("Популярность", dish.popularity.takeIf { it >= 0 }?.let { "#$it" } ?: "мог", "мог", "", Icons.Outlined.StarOutline, colors.warning),
        DetailMetric("Остатки ингредиентов", "мог", "мог", "", Icons.Outlined.Inventory2, colors.warning)
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) { items.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) { row.forEach { metric -> DetailMetricCard(metric, Modifier.weight(1f)) } } } }
}

@Composable private fun DetailMetricCard(metric: DetailMetric, modifier: Modifier) { val colors = LocalColors.current; Card(modifier, colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(StoqSpacing.Medium)) { Box(Modifier.size(40.dp).background(metric.tint.copy(alpha = .13f), CircleShape), contentAlignment = Alignment.Center) { Icon(metric.icon, null, tint = metric.tint) }; Spacer(Modifier.height(7.dp)); Text(metric.label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2); Row(verticalAlignment = Alignment.CenterVertically) { Text(metric.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1); if (metric.change.isNotEmpty()) { Spacer(Modifier.width(4.dp)); Text("↑ ${metric.change}", style = MaterialTheme.typography.labelSmall, color = metric.tint, fontWeight = FontWeight.Bold) } }; Spacer(Modifier.height(3.dp)); Text(metric.note, style = MaterialTheme.typography.bodySmall, color = metric.tint, maxLines = 2) } } }

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
        Surface(color = color.copy(alpha = .12f), shape = CircleShape) { Row(Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).background(color, CircleShape)); Spacer(Modifier.width(4.dp)); Text(when (status) { IngredientStockStatus.Available -> "Есть"; IngredientStockStatus.RunningLow -> "Мало"; IngredientStockStatus.Unknown -> "мог" }, style = MaterialTheme.typography.labelSmall, color = color) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SalesChart() {
    val colors = LocalColors.current; var period by remember { mutableStateOf("За 14 дней") }; var menuOpen by remember { mutableStateOf(false) }; var customDates by remember { mutableStateOf(false) }; val pickerState = rememberDateRangePickerState()
    SectionCard("Динамика продаж", period, onActionClick = { menuOpen = true }) { Text("мог", color = colors.textSecondary) }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) { listOf("За 7 дней", "За 14 дней", "За 30 дней", "Выбрать даты").forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { menuOpen = false; if (option == "Выбрать даты") customDates = true else period = option }) } }
    if (customDates) DatePickerDialog(onDismissRequest = { customDates = false }, confirmButton = { Text("Применить", Modifier.padding(16.dp).clickable { period = "Свой период"; customDates = false }, color = colors.primaryDark, fontWeight = FontWeight.Bold) }, dismissButton = { Text("Отмена", Modifier.padding(16.dp).clickable { customDates = false }) }) { DateRangePicker(state = pickerState, modifier = Modifier.weight(1f, fill = false)) }
}

@Composable
private fun SalesBars(values: List<Int>) {
    val colors = LocalColors.current
    val max = (values.maxOrNull() ?: 30).toFloat()
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(132.dp)) {
            Column(Modifier.width(26.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                listOf("30", "20", "10", "0").forEach { Text(it, color = colors.textSecondary, style = MaterialTheme.typography.labelSmall) }
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
                Text("${10 + index} мая", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable private fun KeyStock(dish: Dish) {
    val colors = LocalColors.current
    SectionCard("Остатки ключевых ингредиентов") {
        dish.ingredients.forEach { item ->
            val low = item.status == IngredientStockStatus.RunningLow
            val fraction = if (item.status == IngredientStockStatus.Unknown) 0f else if (low) .28f else .68f
            val color = if (low) colors.warning else colors.primary
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, Modifier.weight(1.15f), color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Box(Modifier.weight(1f).height(12.dp).clip(CircleShape).background(colors.surfaceSecondary)) { Box(Modifier.fillMaxWidth(fraction).height(12.dp).background(color)) }
                Spacer(Modifier.width(StoqSpacing.Small)); Text(item.stock, color = colors.textPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun SectionCard(title: String, action: String? = null, showChevron: Boolean = true, onActionClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val colors = LocalColors.current
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(StoqSpacing.Large)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                if (action != null) {
                    Surface(Modifier.clickable { onActionClick?.invoke() }, shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline), color = colors.surface) {
                        Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(action, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                        }
                    }
                } else if (showChevron) {
                    Icon(Icons.Outlined.ChevronRight, null, tint = colors.textPrimary)
                }
            }
            Spacer(Modifier.height(StoqSpacing.Small))
            content()
        }
    }
}

private data class DetailMetric(val label: String, val value: String, val note: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: Color)

private fun Int.dataOrText(): String = if (this < 0) "мог" else toString()
private fun Int.dataOrRubles(): String = if (this < 0) "мог" else "$this ₽"
private fun Int.dataOrPercent(): String = if (this < 0) "мог" else "$this%"
