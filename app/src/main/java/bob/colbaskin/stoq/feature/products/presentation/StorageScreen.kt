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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme

private enum class ProductStatus { Normal, LowStock, Expiring }

private data class Product(
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

private val catalogProducts = listOf(
    Product("Помидоры свежие", "00123", "Овощи", "12 кг", "10 кг", "2", "5 дней", "до 25.04.2024", ProductStatus.Normal, R.drawable.product_tomatoes),
    Product("Куриное филе", "00456", "Мясо", "5 кг", "10 кг", "3", "3 дня", "до 23.04.2024", ProductStatus.LowStock, R.drawable.product_chicken),
    Product("Сыр моцарелла", "00789", "Молочные", "8 кг", "5 кг", "2", "12 дней", "до 02.05.2024", ProductStatus.Normal, R.drawable.product_mozzarella),
    Product("Салат айсберг", "00112", "Овощи", "20 шт", "15 шт", "1", "4 дня", "до 24.04.2024", ProductStatus.Expiring, R.drawable.product_iceberg),
    Product("Лосось атлантический", "00987", "Рыба", "6 кг", "5 кг", "2", "2 дня", "до 22.04.2024", ProductStatus.Expiring, R.drawable.product_salmon)
)

private val categories = listOf(
    "Все" to "128",
    "Овощи" to "24",
    "Фрукты" to "16",
    "Мясо" to "14",
    "Рыба" to "9",
    "Молочные" to "18"
)

@Composable
fun StorageScreen() {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first().first) }
    var showAddDialog by remember { mutableStateOf(false) }
    val filteredProducts = remember(query, selectedCategory) {
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
        item { ProductMetrics() }
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
    FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary, shape = CircleShape) { Icon(Icons.Outlined.Add, "Добавить продукт") }
    }
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Новый продукт") },
            text = { Text("Создание продукта будет доступно после подключения каталога к API.") },
            confirmButton = { TextButton(onClick = { showAddDialog = false }) { Text("Понятно") } }
        )
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
private fun ProductMetrics() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ProductMetric("Всего продуктов", "128", "↑ +12%", "по сравнению\nс прошлым месяцем", Icons.Outlined.Inventory2, ProductStatus.Normal, Modifier.weight(1f))
            ProductMetric("С низким\nостатком", "12", "↑ +3", "требуют\nпополнения", Icons.Outlined.WarningAmber, ProductStatus.LowStock, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ProductMetric("Скоро истекут", "8", "↑ +2", "в течение\n7 дней", Icons.Outlined.AccessTime, ProductStatus.Expiring, Modifier.weight(1f))
            ProductMetric("Поставщиков", "14", "↑ +7%", "активных\nпоставщиков", Icons.Outlined.Group, ProductStatus.Normal, Modifier.weight(1f), group = true)
        }
    }
}

@Composable
private fun ProductMetric(title: String, value: String, change: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, status: ProductStatus, modifier: Modifier, group: Boolean = false) {
    val color = when (status) {
        ProductStatus.Normal -> CustomTheme.colors.success
        ProductStatus.LowStock -> CustomTheme.colors.error
        ProductStatus.Expiring -> CustomTheme.colors.warning
    }
    StoqCard(modifier = modifier.height(174.dp), contentPadding = PaddingValues(12.dp)) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (group) CustomTheme.colors.textSecondary else color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Text(change, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary, maxLines = 2)
    }
}

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
