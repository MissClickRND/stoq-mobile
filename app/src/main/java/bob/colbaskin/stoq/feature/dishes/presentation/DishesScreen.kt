package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import coil.compose.AsyncImage

@Composable
fun DishesScreen(
    onDishClick: (String) -> Unit,
    onStopListClick: () -> Unit,
    onRecommendationsClick: () -> Unit,
    viewModel: DishesViewModel = viewModel()
) {
    val state = viewModel.state
    val colors = LocalColors.current
    val allDishes = viewModel.allDishes
    val tabs = listOf("Все", "Горячее", "Салаты", "Десерты", "В стоп-листе")
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(colors.background)) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = StoqSpacing.ScreenHorizontal, top = 8.dp, end = StoqSpacing.ScreenHorizontal, bottom = StoqSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item {
            Text("Блюда", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text("Управляйте меню, составами и себестоимостью блюд", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(StoqSpacing.Large))
            SearchBar(state.query, viewModel::updateQuery)
            Spacer(Modifier.height(StoqSpacing.Medium))
            DishSummary(allDishes)
            Spacer(Modifier.height(StoqSpacing.Medium))
            DishFeatureLinks(onStopListClick, onRecommendationsClick)
            Spacer(Modifier.height(StoqSpacing.Medium))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                tabs.forEach { tab -> DishFilter(tab, categoryCount(tab, allDishes), tab == state.category) { viewModel.selectCategory(tab) } }
            }
        }
        items(state.dishes, key = Dish::id) { dish -> DishCard(dish) { onDishClick(dish.id) } }
        item { Spacer(Modifier.height(76.dp)) }
    }
    FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = colors.primary, contentColor = colors.textOnPrimary, shape = CircleShape) { Icon(Icons.Outlined.Add, "Добавить блюдо") }
    }
    if (showAddDialog) AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Новое блюдо") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(newName, { newName = it }, label = { Text("Название") }, singleLine = true); OutlinedTextField(newPrice, { newPrice = it.filter(Char::isDigit) }, label = { Text("Цена") }, singleLine = true) } },
        confirmButton = { TextButton(onClick = { val price = newPrice.toIntOrNull(); if (newName.isNotBlank() && price != null) { viewModel.addDish(newName, price); newName = ""; newPrice = ""; showAddDialog = false } }) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Отмена") } }
    )
}

@Composable
private fun DishFeatureLinks(onStopListClick: () -> Unit, onRecommendationsClick: () -> Unit) {
    val colors = LocalColors.current
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        FeatureLinkCard(
            title = "Стоп-лист блюд",
            subtitle = "Блюда, которые сейчас нельзя заказывать из-за отсутствия ингредиентов, низких остатков или других ограничений.",
            image = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=520&q=80",
            accent = colors.error,
            icon = Icons.Outlined.WarningAmber,
            onClick = onStopListClick
        )
        FeatureLinkCard(
            title = "Рекомендуемые блюда",
            subtitle = "Блюда, которые стоит продавать в первую очередь. Учитываем остатки продуктов, себестоимость и прогноз спроса.",
            image = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&w=520&q=80",
            accent = colors.success,
            icon = Icons.Outlined.BarChart,
            onClick = onRecommendationsClick
        )
    }
}

@Composable
private fun FeatureLinkCard(
    title: String,
    subtitle: String,
    image: String,
    accent: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalColors.current
    val imageShape = GenericShape { size, _ ->
        moveTo(size.width * .32f, 0f)
        cubicTo(size.width * .08f, size.height * .14f, size.width * .08f, size.height * .86f, size.width * .32f, size.height)
        lineTo(size.width, size.height)
        lineTo(size.width, 0f)
        close()
    }
    Card(
        modifier = Modifier.fillMaxWidth().height(164.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(accent.copy(alpha = .13f), colors.surface)))) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1.24f).padding(12.dp)) {
                    Box(Modifier.size(34.dp).background(accent.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accent)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 2)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.weight(.96f).fillMaxHeight()) {
                    AsyncImage(image, null, Modifier.fillMaxSize().clip(imageShape), contentScale = ContentScale.Crop)
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(38.dp),
                        shape = CircleShape,
                        color = colors.surface,
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    val colors = LocalColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(value = query, onValueChange = onQueryChanged, modifier = Modifier.weight(1f), placeholder = { Text("Поиск блюд, ингредиентов или категории...", maxLines = 1) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.medium, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = colors.surface, unfocusedContainerColor = colors.surface, focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent))
        Spacer(Modifier.width(StoqSpacing.Small))
        Surface(Modifier.size(56.dp), shape = MaterialTheme.shapes.medium, color = colors.surface) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Tune, null, tint = colors.primaryDark) } }
    }
}

@Composable private fun DishSummary(dishes: List<Dish>) {
    val colors = LocalColors.current
    val entries = listOf(
        DishMetric("Всего блюд", dishes.size.toString(), "+12%", Icons.Outlined.Inventory2, colors.success),
        DishMetric("Активных блюд", dishes.count { it.active }.toString(), "+8%", Icons.Outlined.CheckCircle, colors.success),
        DishMetric("В стоп-листе", dishes.count { !it.active }.toString(), "−25%", Icons.Outlined.PauseCircle, colors.error),
        DishMetric("Средняя наценка", "${dishes.map { it.markup }.average().toInt()}%", "+5%", Icons.Outlined.Tune, colors.primaryDark)
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                row.forEach { metric -> Card(Modifier.weight(1f), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                    Column(Modifier.padding(StoqSpacing.Medium)) {
                        Box(Modifier.size(42.dp).background(metric.tint.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(metric.icon, null, tint = metric.tint) }
                        Spacer(Modifier.height(StoqSpacing.Small)); Text(metric.label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(metric.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary); Spacer(Modifier.width(6.dp)); Text("↑ ${metric.change}", style = MaterialTheme.typography.labelSmall, color = metric.tint, fontWeight = FontWeight.Bold) }
                    }
                } }
            }
        }
    }
}

@Composable private fun DishFilter(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalColors.current; val tint = if (label == "В стоп-листе") colors.error else colors.success
    Surface(Modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = if (selected) colors.primary.copy(alpha = .25f) else colors.surface, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline)) {
        Row(Modifier.padding(horizontal = StoqSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(tint, CircleShape)); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal); Spacer(Modifier.width(7.dp)); Box(Modifier.size(23.dp).background(colors.surfaceSecondary, CircleShape), contentAlignment = Alignment.Center) { Text(count.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable private fun DishCard(dish: Dish, onClick: () -> Unit) {
    val colors = LocalColors.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(StoqSpacing.Small), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(dish.imageUrl, dish.name, Modifier.size(120.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(StoqSpacing.Medium)); Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) { Text(dish.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = colors.textPrimary); StatusPill(dish.active) }
                Spacer(Modifier.height(3.dp)); Text(dish.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(StoqSpacing.Medium))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DishCardMetric("Цена", "${dish.price} ₽", Modifier.weight(1f)); VerticalDivider(Modifier.height(40.dp), color = colors.outline); DishCardMetric("Наценка", "${dish.markup}%", Modifier.weight(1f).padding(start = 6.dp), colors.success); VerticalDivider(Modifier.height(40.dp), color = colors.outline); DishCardMetric("Ингридиенты", dish.ingredients.size.toString(), Modifier.weight(1f).padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable private fun DishCardMetric(label: String, value: String, modifier: Modifier, valueColor: androidx.compose.ui.graphics.Color? = null) { val colors = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary, maxLines = 1); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor ?: colors.textPrimary) } }

@Composable fun StatusPill(active: Boolean) { val colors = LocalColors.current; val color = if (active) colors.success else colors.error; Surface(color = color.copy(alpha = .12f), shape = CircleShape) { Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).background(color, CircleShape)); Spacer(Modifier.width(6.dp)); Text(if (active) "Активно" else "В стоп-листе", style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1) } } }

private data class DishMetric(val label: String, val value: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: androidx.compose.ui.graphics.Color)
private fun categoryCount(category: String, dishes: List<Dish>) = when (category) { "Все" -> dishes.size; "В стоп-листе" -> dishes.count { !it.active }; else -> dishes.count { it.category == category } }
