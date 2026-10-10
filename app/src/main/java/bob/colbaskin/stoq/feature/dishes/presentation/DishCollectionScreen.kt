package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import coil.compose.AsyncImage

@Composable
fun DishCollectionScreen(recommended: Boolean, onBack: () -> Unit, onDishClick: (String) -> Unit, viewModel: DishesViewModel = viewModel()) {
    val colors = LocalColors.current
    var query by remember { mutableStateOf("") }; var firstFilter by remember { mutableStateOf(if (recommended) "Срочность" else "Любая причина") }; var category by remember { mutableStateOf("Все категории") }
    val source = if (recommended) viewModel.allDishes.filter { it.active } else viewModel.allDishes
    val dishes = source.filter { dish ->
        val categoryMatches = category == "Все категории" || dish.category == category
        val textMatches = query.isBlank() || dish.name.contains(query, true) || dish.description.contains(query, true)
        val filterMatches = if (recommended) {
            firstFilter == "Срочность" || firstFilter == "Высокая" && dish.popularity < 4 || firstFilter == "Средняя" && dish.popularity >= 4
        } else {
            firstFilter == "Любая причина" || firstFilter == "Истёк срок годности" && (dish.id == "pasta" || dish.id == "burger") || firstFilter == "Нет ингредиентов" && dish.id != "pasta" && dish.id != "burger"
        }
        categoryMatches && textMatches && filterMatches
    }
    val title = if (recommended) "Рекомендуемые блюда" else "Стоп-лист блюд"
    val subtitle = if (recommended) "Эти блюда рекомендуется продать в первую очередь на основе текущих остатков, сроков годности и маржи." else "Блюда, которые в настоящий момент нельзя заказать из-за отсутствия ингредиентов или истечения срока годности продуктов."
    LazyColumn(Modifier.fillMaxSize().background(colors.background), contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(30.dp).clickable(onClick = onBack).padding(3.dp), tint = colors.textPrimary); Spacer(Modifier.width(6.dp)); Text(title, style = MaterialTheme.typography.headlineMedium) }; Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) }
        item { CollectionSearch(query) { query = it } }
        item { CollectionKpis(recommended) }
        item { CollectionFilters(recommended, firstFilter, category, { firstFilter = it }, { category = it }) { firstFilter = if (recommended) "Срочность" else "Любая причина"; category = "Все категории"; query = "" } }
        item { Text(if (recommended) "Рекомендуемые блюда" else "Блюда в стоп-листе", style = MaterialTheme.typography.titleLarge) }
        items(dishes, key = Dish::id) { dish -> CollectionDishCard(dish, recommended) { onDishClick(dish.id) } }
    }
}

@Composable private fun CollectionSearch(value: String, onChange: (String) -> Unit) { val c = LocalColors.current; OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Поиск блюд, ингредиентов или причин…", maxLines = 1) }, shape = MaterialTheme.shapes.medium, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = c.surface, unfocusedContainerColor = c.surface, focusedBorderColor = c.primaryDark, unfocusedBorderColor = c.outline)) }

@Composable private fun CollectionKpis(recommended: Boolean) { val c = LocalColors.current; val metrics = if (recommended) listOf(Metric("Всего рекомендуемых блюд", "12", "+33%", Icons.Outlined.Sell, c.success), Metric("Потенциально спасти продуктов", "28 кг", "+42%", Icons.Outlined.Inventory2, c.success), Metric("Средняя наценка", "67%", "+8%", Icons.Outlined.BarChart, c.success), Metric("Срочных блюд сегодня", "5", "+2", Icons.Outlined.AccessTime, c.error)) else listOf(Metric("Всего в стоп-листе", "6", "+25%", Icons.Outlined.WarningAmber, c.error), Metric("Нет ингредиентов", "3", "+50%", Icons.Outlined.Inventory2, c.warning), Metric("Заблокированы из-за сроков", "2", "+100%", Icons.Outlined.AccessTime, c.textSecondary), Metric("Потенциальная выручка", "12 340 ₽", "+40%", Icons.Outlined.BarChart, c.success)); Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { metrics.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { Kpi(it, Modifier.weight(1f)) } } } } }
@Composable private fun Kpi(m: Metric, modifier: Modifier) { val c = LocalColors.current; Card(modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = c.surface)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(m.tint.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(m.icon, null, tint = m.tint) }; Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(m.label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2); Text(m.value, style = MaterialTheme.typography.titleLarge); Text("↑ ${m.change}", style = MaterialTheme.typography.bodySmall, color = m.tint, fontWeight = FontWeight.Bold) } } } }

@Composable private fun CollectionFilters(recommended: Boolean, first: String, category: String, onFirst: (String) -> Unit, onCategory: (String) -> Unit, onReset: () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterSelect(first, if (recommended) listOf("Срочность", "Высокая", "Средняя") else listOf("Любая причина", "Нет ингредиентов", "Истёк срок годности"), Modifier.weight(1f), if (recommended) Icons.Outlined.AccessTime else Icons.Outlined.WarningAmber, onFirst); FilterSelect(category, listOf("Все категории", "Горячее", "Салаты", "Десерты"), Modifier.weight(1f), Icons.Outlined.Category, onCategory) }; if (!recommended) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterSelect("Ожидаемое возвращение", listOf("Ожидаемое возвращение", "Сегодня", "На этой неделе"), Modifier.weight(1.4f), Icons.Outlined.CalendarToday) {}; FilterButton("Сбросить", Modifier.weight(.7f), onReset); FilterButton("Применить", Modifier.weight(.7f), {}, true) } else FilterButton("Сбросить", Modifier.fillMaxWidth(), onReset) } }
@Composable private fun FilterSelect(value: String, options: List<String>, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, onSelect: (String) -> Unit) { val c = LocalColors.current; var expanded by remember { mutableStateOf(false) }; Box(modifier) { Surface(Modifier.fillMaxWidth().height(48.dp).clickable { expanded = true }, color = c.surface, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, c.outline)) { Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(18.dp), c.primaryDark); Spacer(Modifier.width(7.dp)); Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.KeyboardArrowDown, null, tint = c.primaryDark) } }; DropdownMenu(expanded, { expanded = false }, shape = MaterialTheme.shapes.medium, containerColor = c.surface, shadowElevation = 8.dp) { options.forEach { option -> DropdownMenuItem({ Text(option, color = if (option == value) c.primaryDark else c.textPrimary) }, { onSelect(option); expanded = false }) } } } }
@Composable private fun FilterButton(text: String, modifier: Modifier, onClick: () -> Unit, primary: Boolean = false) { val c = LocalColors.current; Surface(modifier.height(48.dp).clickable(onClick = onClick), color = if (primary) c.primary else c.surface, shape = MaterialTheme.shapes.medium, border = if (primary) null else BorderStroke(1.dp, c.outline)) { Box(contentAlignment = Alignment.Center) { Text(text, style = MaterialTheme.typography.labelMedium, color = if (primary) c.textOnPrimary else c.textPrimary) } } }

@Composable private fun CollectionDishCard(dish: Dish, recommended: Boolean, onClick: () -> Unit) { val c = LocalColors.current; val accent = if (recommended) c.success else c.error; Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = c.surface)) { Column(Modifier.padding(10.dp)) { Row(verticalAlignment = Alignment.Top) { AsyncImage(dish.imageUrl, dish.name, Modifier.size(104.dp).clip(MaterialTheme.shapes.small).background(c.surfaceSecondary), contentScale = ContentScale.Crop); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Row(verticalAlignment = Alignment.Top) { Text(dish.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 2); if (recommended) Rating(82 + dish.popularity * 2) else Tag(dish.category) }; Text(dish.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(6.dp)); Tag(dish.category) } }; HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.outline.copy(alpha = .5f)); if (recommended) Row { Fact("Наценка", "${dish.markup}%", c.success, Modifier.weight(1f)); Fact("Ингредиенты", dish.ingredients.size.toString(), c.textPrimary, Modifier.weight(1f)); Fact("Причина", if (dish.popularity < 4) "Сроки" else "Маржа", c.textPrimary, Modifier.weight(1.2f)) } else Row { Fact(if (dish.id == "pasta") "Истёк срок" else "Нет ингредиентов", "1 ингредиент", c.error, Modifier.weight(1.3f)); Fact("Ожидаем поставку", "24.04.2024", c.textPrimary, Modifier.weight(1f)) }; Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Surface(Modifier.weight(1f).height(38.dp), color = accent.copy(alpha = .11f), shape = MaterialTheme.shapes.small) { Row(Modifier.padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (recommended) Icons.Outlined.Sell else Icons.Outlined.WarningAmber, null, Modifier.size(17.dp), accent); Spacer(Modifier.width(5.dp)); Text(if (recommended) "Продать сегодня" else "Недоступно", style = MaterialTheme.typography.labelMedium, color = accent) } }; Surface(Modifier.width(128.dp).height(38.dp), shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, c.outline)) { Row(Modifier.padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) { Text("Подробнее", style = MaterialTheme.typography.labelMedium); Spacer(Modifier.weight(1f)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(17.dp)) } } } } } }
@Composable private fun Fact(label: String, value: String, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) { val c = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 1); Text(value, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 2) } }
@Composable private fun Tag(text: String) { val c = LocalColors.current; Surface(color = c.warning.copy(alpha = .13f), shape = CircleShape) { Text(text, Modifier.padding(horizontal = 9.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = c.warning) } }
@Composable private fun Rating(value: Int) { val c = LocalColors.current; Surface(color = c.primary.copy(alpha = .16f), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(horizontal = 9.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), style = MaterialTheme.typography.titleMedium); Text("рейтинг", style = MaterialTheme.typography.bodySmall, color = c.textSecondary) } } }
private data class Metric(val label: String, val value: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: androidx.compose.ui.graphics.Color)
