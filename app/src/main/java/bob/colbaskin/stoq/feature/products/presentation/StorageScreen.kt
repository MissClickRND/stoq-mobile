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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.drafts.presentation.DraftsBanner
import bob.colbaskin.stoq.common.drafts.presentation.DraftSnackbar
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import compose.icons.TablerIcons
import compose.icons.tablericons.Package

private enum class ProductStatus { Active, New, Ending, Expired }

private data class Product(
    val stableId: String,
    val id: Long,
    val supplyId: Long?,
    val name: String,
    val article: String,
    val category: String,
    val currentStock: String,
    val minimumStock: String,
    val suppliers: String,
    val expiry: String,
    val expiryDate: String,
    val status: ProductStatus
)

@Composable
fun StorageScreen(
    onInventoryClick: () -> Unit,
    onInventoryDraftsClick: () -> Unit,
    onSupplyClick: (String) -> Unit,
    draftFeedback: String? = null,
    onDraftFeedbackShown: () -> Unit = {},
    viewModel: StorageViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Все") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
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

    PullToRefreshContainer(viewModel.isRefreshing, viewModel::refresh, Modifier.fillMaxSize()) {
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
        item {
            Column {
                ProductSearchField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.searchProducts(it)
                    },
                    onFocusChanged = { focused ->
                        searchFocused = focused
                        if (focused) viewModel.searchProducts(query)
                    }
                )
                if (searchFocused) {
                    ProductSearchSuggestions(
                        products = filteredProducts,
                        onSelected = { product ->
                            query = product.name
                            searchFocused = false
                        }
                    )
                }
            }
        }
        item { DraftsBanner(viewModel.inventoryDrafts.size, "инвентаризация", onInventoryDraftsClick) }
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
        items(filteredProducts, key = Product::stableId) { product ->
            ProductCard(product, onSupplyClick = onSupplyClick) { productToDelete = product }
        }
    }
    }
    FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = CustomTheme.colors.primary, contentColor = CustomTheme.colors.textOnPrimary, shape = CircleShape) { Icon(Icons.Outlined.Add, "Добавить продукт") }
    when (inventoryState) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(inventoryState.title, inventoryState.text, viewModel::refresh)
        is UiState.Success -> Unit
    }
    if (viewModel.actionState == UiState.Loading) LoadingScreen()
    }
    if (showAddDialog) {
        AddProductDialog(
            units = viewModel.units,
            actionState = viewModel.actionState,
            onCreateProduct = viewModel::createProduct,
            onCreateUnit = viewModel::createUnit,
            onDismiss = { showAddDialog = false }
        )
    }
    productToDelete?.let { product ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Удалить продукт?") },
            text = { Text("Продукт «${product.name}» будет удалён.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteProduct(product.id) { productToDelete = null }
                }) { Text("Удалить") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { productToDelete = null }) { Text("Отмена") } }
        )
    }
    DraftSnackbar(draftFeedback, onDraftFeedbackShown, Modifier.padding(bottom = 20.dp))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddProductDialog(
    units: List<bob.colbaskin.stoq.feature.products.data.model.ProductUnitDto>,
    actionState: UiState<Unit>?,
    onCreateProduct: (String, Long, () -> Unit) -> Unit,
    onCreateUnit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedUnitId by remember { mutableStateOf<Long?>(null) }
    var unitsExpanded by remember { mutableStateOf(false) }
    var showUnitForm by remember { mutableStateOf(false) }
    var unitName by remember { mutableStateOf("") }
    var unitShortName by remember { mutableStateOf("") }
    val selectedUnit = units.firstOrNull { it.id == selectedUnitId }
    val colors = CustomTheme.colors

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.91f),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Добавление продукта", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Закрыть") }
                }
                StoqTextField(productName, { productName = it }, "Название продукта", isRequired = true)
                ExposedDropdownMenuBox(
                    expanded = unitsExpanded,
                    onExpandedChange = { unitsExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUnit?.let { "${it.name.orEmpty()} (${it.shortName.orEmpty()})" }.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Единица измерения *") },
                        placeholder = { Text(if (units.isEmpty()) "Загрузка единиц…" else "Выберите единицу") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitsExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = unitsExpanded,
                        onDismissRequest = { unitsExpanded = false },
                        shape = MaterialTheme.shapes.medium,
                        containerColor = CustomTheme.colors.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, CustomTheme.colors.outline)
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.name.orEmpty()} (${unit.shortName.orEmpty()})", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textPrimary) },
                                onClick = { selectedUnitId = unit.id; unitsExpanded = false },
                                modifier = Modifier.background(if (unit.id == selectedUnitId) CustomTheme.colors.primary.copy(alpha = .14f) else CustomTheme.colors.surface)
                            )
                        }
                    }
                }
                if (showUnitForm) {
                    StoqTextField(unitName, { unitName = it }, "Название единицы", isRequired = true)
                    StoqTextField(unitShortName, { unitShortName = it }, "Сокращение", isRequired = true)
                    Button(
                        onClick = { if (unitName.isNotBlank() && unitShortName.isNotBlank()) onCreateUnit(unitName, unitShortName) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Добавить единицу") }
                } else {
                    androidx.compose.material3.TextButton(onClick = { showUnitForm = true }) { Text("Добавить единицу измерения") }
                }
                if (actionState is UiState.Error) Text(actionState.text, color = colors.error, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    androidx.compose.material3.OutlinedButton(onClick = onDismiss) { Text("Отмена") }
                    Button(
                        onClick = { selectedUnitId?.let { onCreateProduct(productName, it, onDismiss) } },
                        enabled = productName.isNotBlank() && selectedUnitId != null && actionState != UiState.Loading
                    ) { Text("Добавить продукт") }
                }
            }
        }
    }
}

@Composable
private fun ProductSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).onFocusChanged { onFocusChanged(it.isFocused) },
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
private fun ProductSearchSuggestions(products: List<Product>, onSelected: (Product) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        products.take(5).forEach { product ->
            Surface(
                modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { onSelected(product) },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .55f))
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Inventory2, null, tint = CustomTheme.colors.primaryDark)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(product.currentStock, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
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
    StoqMetricGrid(metrics.map { StoqMetric(it.label, it.value, it.change, it.icon, it.tint, it.supportingText) })
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
private fun ProductCard(product: Product, onSupplyClick: (String) -> Unit, onDelete: () -> Unit) {
    StoqCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small).background(CustomTheme.colors.primary.copy(alpha = .14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(TablerIcons.Package, contentDescription = product.name, modifier = Modifier.size(32.dp), tint = CustomTheme.colors.primaryDark)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Арт. ${product.article}", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusPill(product.status)
                        IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Outlined.DeleteOutline, "Удалить продукт", tint = CustomTheme.colors.error)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                ProductFact(Icons.Outlined.Inventory2, "Остаток", product.currentStock, product.status, Modifier.weight(1f))
                ProductFact(Icons.Outlined.CalendarToday, "Годен до", product.expiry, product.status, Modifier.weight(1f))
            }
        }
        product.supplyId?.let { supplyId ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clip(MaterialTheme.shapes.small)
                    .clickable { onSupplyClick(supplyId.toString()) },
                shape = MaterialTheme.shapes.small,
                color = CustomTheme.colors.primary.copy(alpha = .12f),
                border = BorderStroke(1.dp, CustomTheme.colors.primary.copy(alpha = .42f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocalShipping, null, modifier = Modifier.size(19.dp), tint = CustomTheme.colors.primaryDark)
                    Column(Modifier.padding(start = 9.dp).weight(1f)) {
                        Text("Поставка №$supplyId", style = MaterialTheme.typography.labelLarge, color = CustomTheme.colors.primaryDark)
                        Text("Открыть детали поставки", style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary)
                    }
                    Icon(Icons.Outlined.ChevronRight, "Открыть поставку", modifier = Modifier.size(20.dp), tint = CustomTheme.colors.primaryDark)
                }
            }
        }
    }
}

private fun StorageProduct.toProduct() = Product(
    stableId = batchId.toString(),
    id = id,
    supplyId = supplyId,
    name = name,
    article = id.toString(),
    category = "мог",
    currentStock = currentStock.ifBlank { "мог" },
    minimumStock = "мог",
    suppliers = "мог",
    expiry = expiryDate ?: "мог",
    expiryDate = expiryDate ?: "мог",
    status = status.toProductStatus()
)

private fun String?.toProductStatus() = when (this?.uppercase()) {
    "ACTIVE" -> ProductStatus.Active
    "ENDING" -> ProductStatus.Ending
    "EXPIRED" -> ProductStatus.Expired
    "NEW" -> ProductStatus.New
    else -> ProductStatus.New
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
        ProductStatus.Active -> "●  В норме" to CustomTheme.colors.success
        ProductStatus.New -> "●  Новый" to CustomTheme.colors.primaryDark
        ProductStatus.Ending -> "●  Заканчивается" to CustomTheme.colors.warning
        ProductStatus.Expired -> "●  Истёк срок" to CustomTheme.colors.error
    }
    Surface(color = color.copy(alpha = .10f), shape = androidx.compose.material3.MaterialTheme.shapes.medium) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
private fun ProductFact(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, status: ProductStatus, modifier: Modifier, date: String? = null) {
    val valueColor = if (status == ProductStatus.Expired || status == ProductStatus.Ending) {
        if (status == ProductStatus.Expired) CustomTheme.colors.error else CustomTheme.colors.warning
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
