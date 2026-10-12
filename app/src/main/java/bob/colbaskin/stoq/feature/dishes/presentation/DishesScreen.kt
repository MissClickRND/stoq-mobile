package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard
import bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish

@Composable
fun DishesScreen(
    onDishClick: (String) -> Unit,
    onStopListClick: () -> Unit,
    onRecommendationsClick: () -> Unit,
    viewModel: DishesViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val colors = LocalColors.current
    val allDishes = viewModel.allDishes
    val tabs = listOf("Все", "Горячее", "Салаты", "Десерты", "В стоп-листе")
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newIngredients by remember { mutableStateOf("") }
    var addToStopList by remember { mutableStateOf(false) }
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
    }
    FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = colors.primary, contentColor = colors.textOnPrimary, shape = CircleShape) { Icon(Icons.Outlined.Add, "Добавить блюдо") }
    when (val loadState = state.loadState) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(loadState.title, loadState.text, viewModel::refresh)
        is UiState.Success -> Unit
    }
    }
    if (showAddDialog) AddDishDialog(
        name = newName,
        description = newDescription,
        price = newPrice,
        ingredients = newIngredients,
        addToStopList = addToStopList,
        onNameChange = { newName = it },
        onDescriptionChange = { newDescription = it },
        onPriceChange = { newPrice = it.filter(Char::isDigit) },
        onIngredientsChange = { newIngredients = it },
        onStopListChange = { addToStopList = it },
        onDismiss = { showAddDialog = false },
        onSubmit = {
            if (newName.isNotBlank() && newDescription.isNotBlank() && newPrice.isNotBlank() && newIngredients.isNotBlank()) {
                viewModel.addDish(newName)
                newName = ""; newDescription = ""; newPrice = ""; newIngredients = ""; addToStopList = false
                showAddDialog = false
            }
        }
    )
}

@Composable
private fun AddDishDialog(
    name: String,
    description: String,
    price: String,
    ingredients: String,
    addToStopList: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onIngredientsChange: (String) -> Unit,
    onStopListChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val colors = LocalColors.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.91f).heightIn(max = 760.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Добавление блюда", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Закрыть") }
                }
                StoqTextField(name, onNameChange, "Название блюда", isRequired = true)
                StoqTextField(description, onDescriptionChange, "Описание", isRequired = true, singleLine = false, minLines = 3)
                StoqTextField(price, onPriceChange, "Цена продажи, ₽", isRequired = true)
                StoqTextField(ingredients, onIngredientsChange, "Ингредиенты", placeholder = "мог", isRequired = true)
                Text("Себестоимость порции: мог", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = addToStopList, onCheckedChange = onStopListChange)
                    Spacer(Modifier.width(12.dp))
                    Text("Поместить в стоп-лист", style = MaterialTheme.typography.titleMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    OutlinedButton(onClick = onDismiss) { Text("Отмена") }
                    Button(onClick = onSubmit, enabled = name.isNotBlank() && description.isNotBlank() && price.isNotBlank() && ingredients.isNotBlank()) { Text("Добавить блюдо") }
                }
            }
        }
    }
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
                    StoqAsyncImage(image, null, Modifier.fillMaxSize().clip(imageShape), contentScale = ContentScale.Crop)
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
    TextField(value = query, onValueChange = onQueryChanged, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Поиск блюд, ингредиентов или категории...", maxLines = 1) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.medium, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = colors.surface, unfocusedContainerColor = colors.surface, focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent))
}

@Composable private fun DishSummary(dishes: List<Dish>) {
    val colors = LocalColors.current
    val entries = listOf(
        DishMetric("Всего блюд", dishes.size.toString(), "+12%", Icons.Outlined.Inventory2, colors.success),
        DishMetric("Активных блюд", dishes.count { it.active }.toString(), "+8%", Icons.Outlined.CheckCircle, colors.success),
        DishMetric("В стоп-листе", dishes.count { !it.active }.toString(), "−25%", Icons.Outlined.PauseCircle, colors.error),
        DishMetric("Средняя наценка", dishes.map { it.markup }.filter { it >= 0 }.takeIf { it.isNotEmpty() }?.average()?.toInt()?.let { "$it%" } ?: "мог", "", Icons.Outlined.Tune, colors.primaryDark)
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                row.forEach { metric -> StoqMetricCard(metric.label, metric.value, metric.change, metric.icon, metric.tint, Modifier.weight(1f)) }
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
            StoqAsyncImage(dish.imageUrl, dish.name, Modifier.size(120.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(StoqSpacing.Medium)); Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) { Text(dish.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = colors.textPrimary); StatusPill(dish.active) }
                Spacer(Modifier.height(3.dp)); Text(dish.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(StoqSpacing.Medium))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DishCardMetric("Цена", dish.price.takeIf { it >= 0 }?.let { "$it ₽" } ?: "мог", Modifier.weight(1f)); VerticalDivider(Modifier.height(40.dp), color = colors.outline); DishCardMetric("Наценка", dish.markup.takeIf { it >= 0 }?.let { "$it%" } ?: "мог", Modifier.weight(1f).padding(start = 6.dp), colors.success); VerticalDivider(Modifier.height(40.dp), color = colors.outline); DishCardMetric("Ингридиенты", if (dish.recipeLoaded) dish.ingredients.size.toString() else "мог", Modifier.weight(1f).padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable private fun DishCardMetric(label: String, value: String, modifier: Modifier, valueColor: androidx.compose.ui.graphics.Color? = null) { val colors = LocalColors.current; Column(modifier) { Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary, maxLines = 1); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor ?: colors.textPrimary) } }

@Composable fun StatusPill(active: Boolean) { val colors = LocalColors.current; val color = if (active) colors.success else colors.error; Surface(color = color.copy(alpha = .12f), shape = CircleShape) { Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).background(color, CircleShape)); Spacer(Modifier.width(6.dp)); Text(if (active) "Активно" else "В стоп-листе", style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1) } } }

private data class DishMetric(val label: String, val value: String, val change: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: androidx.compose.ui.graphics.Color)
private fun categoryCount(category: String, dishes: List<Dish>) = when (category) { "Все" -> dishes.size; "В стоп-листе" -> dishes.count { !it.active }; else -> dishes.count { it.category == category } }
