package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.feature.products.data.StorageProduct

private enum class ProductStatus { Normal, LowStock, Expiring }

private data class Product(
    val id: Long,
    val name: String,
    val article: String,
    val category: String,
    val currentStock: String,
    val minimumStock: String,
    val suppliers: String,
    val expiry: String,
    val expiryDate: String,
    val status: ProductStatus,
    val imageRes: Int
)

@Composable
fun StorageScreen(onInventoryClick: () -> Unit, viewModel: StorageViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    val inventoryState = viewModel.state
    val catalogProducts = (inventoryState as? UiState.Success)?.data.orEmpty().map(StorageProduct::toProduct)
    val categories = listOf("Все" to catalogProducts.size.toString())
    val filteredProducts = remember(query, selectedCategory, catalogProducts) {
        catalogProducts.filter { product ->
            val matchesCategory = selectedCategory == categories.first().first || selectedCategory.equals(product.category, ignoreCase = true)
            val matchesQuery = listOf(product.name, product.article, product.category).any { it.contains(query, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    Box(Modifier.fillMaxSize().background(CustomTheme.colors.background)) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Склад", style = MaterialTheme.typography.headlineMedium)
                Text("Управляйте складской номенклатурой и контролируйте\nостатки продуктов", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
            }
        }
        item { ProductSearchField(query, { query = it }) }
        item { ProductMetrics(catalogProducts) }
        item { StoqPrimaryButton(text = "Провести инвентаризацию", onClick = onInventoryClick) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { (category, count) ->
                    CategoryTab(category, count, category == selectedCategory) { selectedCategory = category }
                }
            }
        }
        item { ExpiryBanner() }
        items(filteredProducts, key = { it.article }) { product -> ProductCard(product) }
    }
    FloatingActionButton(onClick = { }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary, shape = CircleShape) { Icon(Icons.Outlined.Add, "Добавить продукт") }
    when (inventoryState) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(inventoryState.title, inventoryState.text, viewModel::refresh)
        is UiState.Success -> Unit
    }
    }
}

@Composable
private fun ProductSearchField(value: String, onValueChange: (String) -> Unit) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
            placeholder = { Text("Поиск продуктов, категорий или артикулов...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = CustomTheme.colors.textSecondary) },
            singleLine = true,
            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomTheme.colors.primaryDark,
                unfocusedBorderColor = CustomTheme.colors.outline,
                focusedContainerColor = CustomTheme.colors.surface,
                unfocusedContainerColor = CustomTheme.colors.surface
            )
        )
}

@Composable
private fun ProductMetrics(products: List<Product>) {
    val colors = CustomTheme.colors
    val metrics = listOf(
        StorageMetric("Всего продуктов", products.size.toString(), "мог", "по сравнению с прошлым месяцем", Icons.Outlined.Inventory2, colors.success),
        StorageMetric("С низким остатком", "мог", "мог", "требуют пополнения", Icons.Outlined.WarningAmber, colors.error),
        StorageMetric("Скоро истекут", products.count { it.expiry != "мог" }.toString(), "мог", "в течение 7 дней", Icons.Outlined.AccessTime, colors.warning),
        StorageMetric("Поставщиков", "мог", "мог", "активных поставщиков", Icons.Outlined.Group, colors.success)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { metric ->
                    StoqMetricCard(
                        label = metric.label,
                        value = metric.value,
                        change = metric.change,
                        icon = metric.icon,
                        tint = metric.tint,
                        modifier = Modifier.weight(1f),
                        supportingText = metric.supportingText
                    )
                }
            }
        }
    }
}

private data class StorageMetric(
    val label: String,
    val value: String,
    val change: String,
    val supportingText: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color
)

@Composable
private fun CategoryTab(label: String, count: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.height(48.dp).clickable(onClick = onClick),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        color = if (selected) CustomTheme.colors.primaryDark else CustomTheme.colors.surface,
        border = if (selected) null else BorderStroke(1.dp, CustomTheme.colors.outline)
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) CustomTheme.colors.surface else CustomTheme.colors.textSecondary)
            Spacer(Modifier.width(7.dp))
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(
                    if (selected) CustomTheme.colors.surface.copy(alpha = .20f) else CustomTheme.colors.surfaceSecondary
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(count, style = MaterialTheme.typography.bodySmall, color = if (selected) CustomTheme.colors.surface else CustomTheme.colors.textSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExpiryBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        color = CustomTheme.colors.warning.copy(alpha = .10f),
        border = BorderStroke(1.dp, CustomTheme.colors.warning.copy(alpha = .18f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(CustomTheme.colors.warning.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AccessTime, null, tint = CustomTheme.colors.warning, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("8 продуктов скоро истекут", style = MaterialTheme.typography.titleMedium)
                Text("Проверьте сроки годности и спланируйте использование", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    StoqCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            androidx.compose.foundation.Image(
                painter = painterResource(product.imageRes),
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).clip(androidx.compose.material3.MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Арт. ${product.article}", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
                        Spacer(Modifier.height(5.dp))
                        CategoryPill(product.category)
                    }
                    StatusPill(product.status)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                ProductFact(Icons.Outlined.Inventory2, "Текущий остаток", product.currentStock, if (product.status == ProductStatus.LowStock) product.status else ProductStatus.Normal, Modifier.weight(1f))
                ProductFact(Icons.Outlined.QueryStats, "Мин. остаток", product.minimumStock, ProductStatus.Normal, Modifier.weight(1f))
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = CustomTheme.colors.outline.copy(alpha = .55f))
            Row(Modifier.fillMaxWidth()) {
                ProductFact(Icons.Outlined.Group, "Поставщики", product.suppliers, ProductStatus.Normal, Modifier.weight(1f))
                ProductFact(Icons.Outlined.CalendarToday, "Срок годности", product.expiry, product.status, Modifier.weight(1f), product.expiryDate)
            }
        }
    }
}

private fun StorageProduct.toProduct() = Product(
    id = id,
    name = name,
    article = id.toString(),
    category = "мог",
    currentStock = currentStock.ifBlank { "мог" },
    minimumStock = "мог",
    suppliers = "мог",
    expiry = expiryDate ?: "мог",
    expiryDate = expiryDate ?: "мог",
    status = ProductStatus.Normal,
    imageRes = R.drawable.stoq_leaf
)

@Composable
private fun CategoryPill(category: String) {
    val (background, color) = when (category) {
        "Мясо" -> CustomTheme.colors.error.copy(alpha = .10f) to CustomTheme.colors.error
        "Молочные" -> CustomTheme.colors.primary.copy(alpha = .16f) to CustomTheme.colors.primaryDark
        "Рыба" -> CustomTheme.colors.primary.copy(alpha = .14f) to CustomTheme.colors.primaryDark
        else -> CustomTheme.colors.success.copy(alpha = .10f) to CustomTheme.colors.success
    }
    Surface(color = background, shape = androidx.compose.material3.MaterialTheme.shapes.small) {
        Text(category, Modifier.padding(horizontal = 12.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun StatusPill(status: ProductStatus) {
    val (label, color) = when (status) {
        ProductStatus.Normal -> "●  В норме" to CustomTheme.colors.success
        ProductStatus.LowStock -> "●  Низкий остаток" to CustomTheme.colors.error
        ProductStatus.Expiring -> "●  Скоро истечёт" to CustomTheme.colors.warning
    }
    Surface(color = color.copy(alpha = .10f), shape = androidx.compose.material3.MaterialTheme.shapes.medium) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
private fun ProductFact(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, status: ProductStatus, modifier: Modifier, date: String? = null) {
    val valueColor = if (status == ProductStatus.LowStock || status == ProductStatus.Expiring) {
        if (status == ProductStatus.LowStock) CustomTheme.colors.error else CustomTheme.colors.warning
    } else CustomTheme.colors.textPrimary
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(17.dp), tint = CustomTheme.colors.textSecondary)
            Spacer(Modifier.width(5.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary, maxLines = 1)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, textAlign = TextAlign.Start)
        if (date != null) Text(date, style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary, maxLines = 1)
    }
}
