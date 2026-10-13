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
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DishCollectionScreen(recommended: Boolean, onBack: () -> Unit, onDishClick: (String) -> Unit, viewModel: DishesViewModel = hiltViewModel()) {
    val colors = LocalColors.current
    val filters = viewModel.collectionFilters(recommended)
    LaunchedEffect(recommended) { if (!recommended) viewModel.loadStopList() }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val source = if (recommended) viewModel.allDishes.filter { it.active } else viewModel.stopListDishes
    val dishes = source.filter { dish ->
        val categoryMatches = filters.category == "Все категории" || dish.category == filters.category
        val textMatches = filters.query.isBlank() || dish.name.contains(filters.query, true) || dish.description.contains(filters.query, true)
        val filterMatches = if (recommended) {
            val urgencyMatches = filters.firstFilter == "Срочность" || filters.firstFilter == "Высокая" && dish.popularity < 4 || filters.firstFilter == "Средняя" && dish.popularity >= 4
            val markupMatches = filters.markupFilter == "Наценка" || filters.markupFilter == "Высокая (70%+)" && dish.markup >= 70 || filters.markupFilter == "Средняя (50–69%)" && dish.markup in 50..69
            urgencyMatches && markupMatches
        } else {
            val reasonMatches = filters.firstFilter == "Любая причина" || filters.firstFilter == "Истёк срок годности" && (dish.id == "pasta" || dish.id == "burger") || filters.firstFilter == "Нет ингредиентов" && dish.id != "pasta" && dish.id != "burger"
            val date = deliveryDateFor(dish)
            val deliveryMatches = filters.deliveryFilter == "Ожидаемое возвращение" ||
                filters.deliveryFilter == "Сегодня" && date == deliveryDateAfter(0) ||
                filters.deliveryFilter == "На этой неделе" && date in setOf(deliveryDateAfter(0), deliveryDateAfter(2), deliveryDateAfter(4), deliveryDateAfter(6)) ||
                filters.deliveryFilter == date
            reasonMatches && deliveryMatches
        }
        categoryMatches && textMatches && filterMatches
    }
    val sortedDishes = if (recommended) when (filters.ratingFilter) {
        "По убыванию" -> dishes.sortedByDescending { 82 + it.popularity * 2 }
        "По возрастанию" -> dishes.sortedBy { 82 + it.popularity * 2 }
        else -> dishes
    } else dishes
    val title = if (recommended) "Рекомендуемые блюда" else "Стоп-лист блюд"
    val subtitle = if (recommended) "Эти блюда рекомендуется продать в первую очередь на основе текущих остатков, сроков годности и маржи." else "Блюда, которые в настоящий момент нельзя заказать из-за отсутствия ингредиентов или истечения срока годности продуктов."
    PullToRefreshContainer(
        isRefreshing = if (recommended) viewModel.state.isRefreshing else viewModel.isStopListRefreshing,
        onRefresh = { viewModel.refreshCollection(recommended) },
        modifier = Modifier.fillMaxSize()
    ) {
    LazyColumn(Modifier.fillMaxSize().background(colors.background), contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(30.dp).clickable(onClick = onBack).padding(3.dp), tint = colors.textPrimary); Spacer(Modifier.width(6.dp)); Text(title, style = MaterialTheme.typography.headlineMedium) }; Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) }
        item { CollectionSearch(filters.query) { viewModel.updateCollectionFilters(recommended) { current -> current.copy(query = it) } } }
        item { CollectionKpis(recommended) }
        item {
            CollectionFilters(
                recommended, filters.firstFilter, filters.category, filters.deliveryFilter, filters.markupFilter, filters.ratingFilter,
                { value -> viewModel.updateCollectionFilters(recommended) { current -> current.copy(firstFilter = value) } },
                { value -> viewModel.updateCollectionFilters(recommended) { current -> current.copy(category = value) } },
                { value -> viewModel.updateCollectionFilters(recommended) { current -> current.copy(deliveryFilter = value) } },
                { value -> viewModel.updateCollectionFilters(recommended) { current -> current.copy(markupFilter = value) } },
                { value -> viewModel.updateCollectionFilters(recommended) { current -> current.copy(ratingFilter = value) } },
                { showCustomDatePicker = true }
            ) {
                viewModel.updateCollectionFilters(recommended) {
                    CollectionFilterState(firstFilter = if (recommended) "Срочность" else "Любая причина")
                }
            }
        }
        item { Text(if (recommended) "Рекомендуемые блюда" else "Блюда в стоп-листе", style = MaterialTheme.typography.titleLarge) }
        items(sortedDishes, key = Dish::id) { dish ->
            CollectionDishCard(dish, recommended, viewModel.stopListReasonTitle(dish.id), viewModel.stopListReason(dish.id)) { onDishClick(dish.id) }
        }
    }
    }
    if (!recommended) {
        when (val state = viewModel.stopListState) {
            UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(state.title, state.text, viewModel::loadStopList, onBack)
            is UiState.Success -> Unit
        }
    }
    if (showCustomDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        viewModel.updateCollectionFilters(recommended) { current -> current.copy(deliveryFilter = formatDeliveryDate(selectedDate)) }
                    }
                    showCustomDatePicker = false
                }) { Text("Готово") }
            },
            dismissButton = { TextButton(onClick = { showCustomDatePicker = false }) { Text("Отмена") } },
            shape = MaterialTheme.shapes.large,
            colors = DatePickerDefaults.colors(
                containerColor = colors.surface,
                selectedDayContainerColor = colors.primary,
                selectedDayContentColor = colors.textOnPrimary,
                todayDateBorderColor = colors.primaryDark
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = colors.surface,
                    selectedDayContainerColor = colors.primary,
                    selectedDayContentColor = colors.textOnPrimary,
                    todayDateBorderColor = colors.primaryDark
                )
            )
        }
    }
}

@Composable private fun CollectionSearch(value: String, onChange: (String) -> Unit) { val c = LocalColors.current; OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Поиск блюд, ингредиентов или причин…", maxLines = 1) }, shape = MaterialTheme.shapes.medium, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = c.surface, unfocusedContainerColor = c.surface, focusedBorderColor = c.primaryDark, unfocusedBorderColor = c.outline)) }

@Composable private fun CollectionKpis(recommended: Boolean) { val c = LocalColors.current; val metrics = if (recommended) listOf(Metric("Всего рекомендуемых блюд", "мог", "", Icons.Outlined.Sell, c.success), Metric("Потенциально спасти продуктов", "мог", "", Icons.Outlined.Inventory2, c.success), Metric("Средняя наценка", "мог", "", Icons.Outlined.BarChart, c.success), Metric("Срочных блюд сегодня", "мог", "", Icons.Outlined.AccessTime, c.error)) else listOf(Metric("Всего в стоп-листе", "мог", "", Icons.Outlined.WarningAmber, c.error), Metric("Нет ингредиентов", "мог", "", Icons.Outlined.Inventory2, c.warning), Metric("Заблокированы из-за сроков", "мог", "", Icons.Outlined.AccessTime, c.textSecondary), Metric("Потенциальная выручка", "мог", "", Icons.Outlined.BarChart, c.success)); Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { metrics.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { StoqMetricCard(it.label, it.value, it.change, it.icon, it.tint, Modifier.weight(1f)) } } } } }

@Composable private fun CollectionFilters(recommended: Boolean, first: String, category: String, delivery: String, markup: String, rating: String, onFirst: (String) -> Unit, onCategory: (String) -> Unit, onDelivery: (String) -> Unit, onMarkup: (String) -> Unit, onRating: (String) -> Unit, onCustomDate: () -> Unit, onReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterSelect(first, if (recommended) listOf("Срочность", "Высокая", "Средняя") else listOf("Любая причина", "Нет ингредиентов", "Истёк срок годности"), Modifier.weight(1f), if (recommended) Icons.Outlined.AccessTime else Icons.Outlined.WarningAmber, onFirst)
            FilterSelect(category, listOf("Все категории", "Горячее", "Салаты", "Десерты"), Modifier.weight(1f), Icons.Outlined.Category, onCategory)
        }
        if (recommended) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterSelect(markup, listOf("Наценка", "Высокая (70%+)", "Средняя (50–69%)"), Modifier.weight(1f), Icons.Outlined.Sell, onMarkup)
                FilterSelect(rating, listOf("Рейтинг", "По убыванию", "По возрастанию"), Modifier.weight(1f), Icons.Outlined.BarChart, onRating)
                FilterButton("Сбросить", Modifier.weight(1f), onReset)
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterSelect(delivery, listOf("Сегодня", "На этой неделе", "Выбрать дату"), Modifier.weight(1f), Icons.Outlined.CalendarToday, onDelivery, onCustomDate)
                FilterButton("Сбросить", Modifier.weight(1f), onReset)
            }
        }
    }
}
@Composable private fun FilterSelect(value: String, options: List<String>, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, onSelect: (String) -> Unit, onCustomDate: (() -> Unit)? = null) {
    val c = LocalColors.current; var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(Modifier.fillMaxWidth().height(48.dp).clickable { expanded = true }, color = c.surface, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, c.outline)) { Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(18.dp), c.primaryDark); Spacer(Modifier.width(7.dp)); Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.KeyboardArrowDown, null, tint = c.primaryDark) } }
        DropdownMenu(expanded, { expanded = false }, shape = MaterialTheme.shapes.medium, containerColor = c.surface, shadowElevation = 8.dp) {
            options.forEach { option -> DropdownMenuItem({ Text(option, color = if (option == value) c.primaryDark else c.textPrimary) }, {
                expanded = false
                if (option == "Выбрать дату") onCustomDate?.invoke() else onSelect(option)
            }) }
        }
    }
}
@Composable private fun FilterButton(text: String, modifier: Modifier, onClick: () -> Unit, primary: Boolean = false) { val c = LocalColors.current; Surface(modifier.height(48.dp).clickable(onClick = onClick), color = if (primary) c.primary else c.surface, shape = MaterialTheme.shapes.medium, border = if (primary) null else BorderStroke(1.dp, c.outline)) { Box(contentAlignment = Alignment.Center) { Text(text, style = MaterialTheme.typography.labelMedium, color = if (primary) c.textOnPrimary else c.textPrimary) } } }

@Composable private fun CollectionDishCard(dish: Dish, recommended: Boolean, stopListReasonTitle: String, stopListReason: String, onClick: () -> Unit) {
    val c = LocalColors.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = c.surface)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                StoqAsyncImage(dish.imageUrl, dish.name, Modifier.size(96.dp).clip(MaterialTheme.shapes.small).background(c.surfaceSecondary), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) { Text(dish.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); if (recommended) Rating(dish.popularity.takeIf { it >= 0 } ?: 0, hasData = dish.popularity >= 0) }
                    Spacer(Modifier.height(3.dp)); Text(dish.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp)); Tag(dish.category)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = c.outline.copy(alpha = .5f))
            if (recommended) {
                Row(Modifier.fillMaxWidth()) {
                    Fact("Наценка", dish.markup.takeIf { it >= 0 }?.let { "$it%" } ?: "мог", c.success, Modifier.weight(1f))
                    VerticalDivider(Modifier.height(44.dp), color = c.outline.copy(alpha = .5f))
                    Fact("Ингредиенты", if (dish.recipeLoaded) dish.ingredients.size.toString() else "мог", c.textPrimary, Modifier.weight(1f).padding(start = 10.dp))
                    VerticalDivider(Modifier.height(44.dp), color = c.outline.copy(alpha = .5f))
                    Fact("Причина", "мог", c.textPrimary, Modifier.weight(1.25f).padding(start = 10.dp))
                }
                Spacer(Modifier.height(10.dp))
                Surface(Modifier.fillMaxWidth(), color = c.error.copy(alpha = .10f), shape = MaterialTheme.shapes.small) { Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Alarm, null, Modifier.size(19.dp), c.error); Spacer(Modifier.width(8.dp)); Text("мог", style = MaterialTheme.typography.labelMedium, color = c.error, fontWeight = FontWeight.Bold) } }
            } else {
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(stopListReasonTitle, style = MaterialTheme.typography.bodySmall, color = c.error, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(stopListReason, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelMedium, color = c.textPrimary, maxLines = 2)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) { Text("Ожидаем поставку", style = MaterialTheme.typography.bodySmall, color = c.textSecondary, fontWeight = FontWeight.SemiBold, maxLines = 1); Text("мог", style = MaterialTheme.typography.labelMedium, color = c.textPrimary, maxLines = 1) }
                }
            }
        }
    }
}
@Composable private fun Fact(label: String, value: String, tint: androidx.compose.ui.graphics.Color, modifier: Modifier, emphasize: Boolean = false) { val c = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.bodySmall, color = if (emphasize) tint else c.textSecondary, fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal, maxLines = 1); Text(value, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 2) } }
private fun deliveryDateFor(dish: Dish) = when (dish.id) { "pasta" -> deliveryDateAfter(0); "burger" -> deliveryDateAfter(2); else -> if (dish.popularity < 4) deliveryDateAfter(4) else deliveryDateAfter(6) }
private fun deliveryDateAfter(days: Int) = java.util.Calendar.getInstance().run { add(java.util.Calendar.DAY_OF_YEAR, days); formatDeliveryDate(timeInMillis) }
private fun formatDeliveryDate(millis: Long) = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
@Composable private fun Tag(text: String) { val c = LocalColors.current; Surface(color = c.warning.copy(alpha = .13f), shape = CircleShape) { Text(text, Modifier.padding(horizontal = 9.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = c.warning) } }
@Composable private fun Rating(value: Int, hasData: Boolean) { val c = LocalColors.current; Surface(color = c.primary.copy(alpha = .16f), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(horizontal = 9.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(if (hasData) value.toString() else "—", style = MaterialTheme.typography.titleMedium); Text("рейтинг", style = MaterialTheme.typography.bodySmall, color = c.textSecondary) } } }
private data class Metric(val label: String, val value: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: androidx.compose.ui.graphics.Color)
