package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.feature.supplies.data.Supply
import bob.colbaskin.stoq.feature.supplies.data.SupplyItem
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyItemDto

private enum class ReceptionStatus(val title: String, val tone: StoqStatusTone) {
    Accepted("Принято", StoqStatusTone.Success),
    InTransit("В пути", StoqStatusTone.Warning),
    Cancelled("Отменена", StoqStatusTone.Error)
}

private data class SupplyDetailProduct(
    val stableId: String,
    val sku: String,
    val name: String,
    val quantity: String,
    val unit: String,
    val unitPrice: String,
    val total: String,
    val expiresAt: String,
    val reception: ReceptionStatus
)

private data class EditableSupplyItem(
    val productId: Long,
    val name: String,
    val unit: String,
    val quantity: String,
    val price: String,
    val expiresAt: String
)

@Composable
fun SupplyDetailsScreen(
    supplyNumber: String,
    onBack: () -> Unit,
    viewModel: SupplyDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(supplyNumber) { viewModel.load(supplyNumber) }
    val colors = LocalColors.current
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("RECEIVED") }
    var editedItems by remember { mutableStateOf(emptyList<EditableSupplyItem>()) }
    var editValidationError by remember { mutableStateOf<String?>(null) }
    val supply = (viewModel.state as? UiState.Success)?.data
    LaunchedEffect(supply?.id) {
        supply ?: return@LaunchedEffect
        selectedStatus = supply.status
        editedItems = supply.items.map(SupplyItem::toEditableItem)
    }
    LaunchedEffect(supply, viewModel.shouldStartEditing) {
        if (supply != null && viewModel.shouldStartEditing) {
            selectedStatus = supply.status
            editedItems = supply.items.map(SupplyItem::toEditableItem)
            editing = true
            viewModel.consumeStartEditing()
        }
    }
    val products = supply?.items?.map { item ->
        SupplyDetailProduct(
            stableId = item.id.toString(),
            sku = item.productId.toString(),
            name = item.productName,
            quantity = item.quantity.toDisplayQuantity(),
            unit = item.unit.ifBlank { "мог" },
            unitPrice = item.price?.toRubles() ?: "мог",
            total = item.totalPrice?.toRubles() ?: "мог",
            expiresAt = item.expiresAt.toSupplyExpiryDisplay(),
            reception = supply.status.toReceptionStatus()
        )
    }.orEmpty()
    val visibleProducts = remember(query, products) {
        products.filter { product ->
            listOf(product.name, product.sku).any { it.contains(query, ignoreCase = true) }
        }
    }
    val visibleEditableItems = remember(query, editedItems) {
        editedItems.filter { item ->
            listOf(item.name, item.productId.toString()).any { it.contains(query, ignoreCase = true) }
        }
    }

    PullToRefreshContainer(
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.load(supplyNumber) },
        modifier = Modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = StoqSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item { DetailHeader(supplyNumber, onBack, editing, onEditClick = { editing = !editing }) }
        item { SupplyOverview(supply) }
        item {
            Column {
                Text("Товары в поставке", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(
                    "Проверьте позиции, сроки годности и статус приёмки.",
                    modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
        if (editing && supply != null) {
            item {
                SupplyEditControls(
                    status = selectedStatus,
                    onStatusChange = { selectedStatus = it },
                    products = viewModel.products,
                    existingProductIds = editedItems.map(EditableSupplyItem::productId),
                    onAddProduct = { product ->
                        editedItems = editedItems + EditableSupplyItem(
                            productId = product.id,
                            name = product.name,
                            unit = product.unit?.shortName.orEmpty(),
                            quantity = "1",
                            price = "0",
                            expiresAt = ""
                        )
                    }
                )
            }
        }
        item { DetailSearchField(query, { query = it }) }
        if (if (editing) visibleEditableItems.isEmpty() else visibleProducts.isEmpty()) {
            item { ProductsEmptyState() }
        } else {
            if (editing) {
                items(visibleEditableItems, key = { it.productId }) { item ->
                    EditableSupplyItemCard(
                        item = item,
                        onChange = { changed -> editedItems = editedItems.map { if (it.productId == changed.productId) changed else it } },
                        onDelete = { editedItems = editedItems.filterNot { it.productId == item.productId } }
                    )
                }
            } else {
                items(visibleProducts, key = { it.stableId }) { product -> SupplyDetailProductCard(product) }
            }
        }
        if (editing && supply != null) {
            item {
                Column {
                editValidationError?.let { message ->
                    Text(message, modifier = Modifier.padding(bottom = StoqSpacing.Small), color = colors.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        val requestItems = editedItems.mapNotNull { item ->
                            val quantity = item.quantity.replace(',', '.').toDoubleOrNull()
                            val price = item.price.replace(',', '.').toDoubleOrNull()
                            val expiresAt = item.expiresAt.toApiExpiry()
                            if (quantity != null && quantity > 0 && price != null && price >= 0 && expiresAt != null) {
                                CreateSupplyItemDto(item.productId, quantity, quantity * price, expiresAt)
                            } else null
                        }
                        if (requestItems.size == editedItems.size && requestItems.isNotEmpty()) {
                            editValidationError = null
                            viewModel.update(
                                CreateSupplyDto(
                                    status = selectedStatus,
                                    supplyNumber = supply.number,
                                    supplier = supply.supplier.orEmpty(),
                                    items = requestItems,
                                    receivedAt = supply.receivedAt
                                )
                            )
                            editing = false
                        } else {
                            editValidationError = "Проверьте количество, цену и срок годности у каждой позиции."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editedItems.isNotEmpty()
                ) {
                    Icon(Icons.Outlined.Check, null)
                    Spacer(Modifier.width(StoqSpacing.Small))
                    Text("Сохранить изменения")
                }
                }
            }
        }
    }
    }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, onRetry = { viewModel.load(supplyNumber) }, onBack = onBack)
        is UiState.Success -> Unit
    }
}

@Composable
private fun DetailHeader(supplyNumber: String, onBack: () -> Unit, editing: Boolean, onEditClick: () -> Unit) {
    val colors = LocalColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
            shape = CircleShape,
            color = colors.surfaceSecondary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", tint = colors.textPrimary)
            }
        }
        Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
            Text("Поставка #$supplyNumber", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
            Text("Детали поставки и контроль приёмки", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        IconButton(onClick = onEditClick) {
            Icon(if (editing) Icons.Outlined.Close else Icons.Outlined.Edit, if (editing) "Отменить редактирование" else "Изменить поставку", tint = colors.primaryDark)
        }
    }
}

@Composable
private fun SupplyOverview(supply: Supply?) {
    val colors = LocalColors.current
    val metrics = listOf(
        DetailMetric("ID поставки", supply?.id?.toString() ?: "мог", Icons.AutoMirrored.Outlined.ReceiptLong, colors.primaryDark),
        DetailMetric("Поступила", supply?.receivedAt?.take(10) ?: "мог", Icons.Outlined.CalendarToday, colors.primaryDark),
        DetailMetric("Позиций", supply?.items?.size?.toString() ?: "мог", Icons.Outlined.Inventory2, colors.success),
        DetailMetric("Сумма", supply?.totalPrice?.toRubles() ?: "мог", Icons.AutoMirrored.Outlined.ReceiptLong, colors.success),
        DetailMetric("Принял", supply?.supplier ?: "мог", Icons.Outlined.PersonOutline, colors.secondary)
    )
    Column {
        Text("Информация о поставке", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        StoqMetricGrid(
            metrics = metrics.map { StoqMetric(it.label, it.value, icon = it.icon, tint = it.tint) },
            modifier = Modifier.padding(top = StoqSpacing.Medium)
        )
    }
}

@Composable
private fun DetailSearchField(value: String, onValueChange: (String) -> Unit) {
    val colors = LocalColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Поиск товаров в поставке", maxLines = 1) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = colors.textSecondary) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryDark,
            focusedLabelColor = colors.primaryDark,
            unfocusedBorderColor = colors.outline,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface
        )
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SupplyEditControls(
    status: String,
    onStatusChange: (String) -> Unit,
    products: List<bob.colbaskin.stoq.feature.products.data.model.ProductDto>,
    existingProductIds: List<Long>,
    onAddProduct: (bob.colbaskin.stoq.feature.products.data.model.ProductDto) -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    StoqCard {
        Text("Редактирование поставки", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(StoqSpacing.Small))
        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = status.toSupplyDetailStatusLabel(),
                onValueChange = {},
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text("Статус поставки") },
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }
            )
            ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                listOf("RECEIVED", "IN_TRANSIT", "CANCELLED").forEach { value ->
                    DropdownMenuItem(text = { Text(value.toSupplyDetailStatusLabel()) }, onClick = {
                        onStatusChange(value)
                        statusExpanded = false
                    })
                }
            }
        }
        Spacer(Modifier.height(StoqSpacing.Small))
        ExposedDropdownMenuBox(
            expanded = productExpanded,
            onExpandedChange = { productExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { productExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(StoqSpacing.Small))
                Text("Добавить продукт")
            }
            ExposedDropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                products.filterNot { it.id in existingProductIds }.forEach { product ->
                    DropdownMenuItem(
                        text = { Text("${product.name} · ${product.unit?.shortName.orEmpty()}") },
                        onClick = { onAddProduct(product); productExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableSupplyItemCard(
    item: EditableSupplyItem,
    onChange: (EditableSupplyItem) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalColors.current
    StoqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = MaterialTheme.shapes.small, color = colors.primary.copy(alpha = .14f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = colors.primaryDark) }
            }
            Column(Modifier.padding(start = StoqSpacing.Small).weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text("ID ${item.productId} · ${item.unit}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Удалить продукт", tint = colors.error) }
        }
        Spacer(Modifier.height(StoqSpacing.Small))
        Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
            OutlinedTextField(item.quantity, { onChange(item.copy(quantity = it)) }, Modifier.weight(1f), label = { Text("Количество") }, singleLine = true)
            OutlinedTextField(item.price, { onChange(item.copy(price = it)) }, Modifier.weight(1f), label = { Text("Цена, ₽") }, singleLine = true)
        }
        Spacer(Modifier.height(StoqSpacing.Small))
        OutlinedTextField(
            value = item.expiresAt,
            onValueChange = { onChange(item.copy(expiresAt = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Срок годности") },
            placeholder = { Text("ДД.ММ.ГГГГ ЧЧ:ММ") },
            singleLine = true
        )
    }
}

@Composable
private fun SupplyDetailProductCard(product: SupplyDetailProduct) {
    val colors = LocalColors.current
    StoqCard(contentPadding = PaddingValues(StoqSpacing.Large)) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.small,
                color = colors.primary.copy(alpha = .14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Inventory2, product.name, modifier = Modifier.size(30.dp), tint = colors.primaryDark)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text("Арт. ${product.sku}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                    StoqStatusPill(product.reception.title, product.reception.tone)
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = StoqSpacing.Medium), color = colors.outline.copy(alpha = 0.55f))
        Row(Modifier.fillMaxWidth()) {
            SupplyProductFact(Icons.Outlined.Inventory2, "Количество", "${product.quantity} ${product.unit}", Modifier.weight(1f))
            SupplyProductFact(Icons.Outlined.QueryStats, "Цена за ед.", product.unitPrice, Modifier.weight(1f))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = StoqSpacing.Medium), color = colors.outline.copy(alpha = 0.55f))
        Row(Modifier.fillMaxWidth()) {
            SupplyProductFact(Icons.Outlined.CalendarToday, "Срок годности", product.expiresAt, Modifier.weight(1f))
            SupplyProductFact(Icons.AutoMirrored.Outlined.ReceiptLong, "Сумма", product.total, Modifier.weight(1f), accent = true)
        }
    }
}

@Composable
private fun SupplyProductFact(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier,
    accent: Boolean = false
) {
    val colors = LocalColors.current
    Column(modifier.padding(horizontal = StoqSpacing.ExtraSmall)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(17.dp), tint = colors.textSecondary)
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = if (accent) colors.primaryDark else colors.textPrimary)
    }
}

@Composable
private fun ProductsEmptyState() {
    val colors = LocalColors.current
    StoqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = colors.textSecondary)
            Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
                Text("Товары не найдены", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text("Измените поисковый запрос.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

private data class DetailMetric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: androidx.compose.ui.graphics.Color
)

private fun Double.toDisplayQuantity() = if (this % 1.0 == 0.0) toLong().toString() else toString()
private fun Double.toRubles() = "${toDisplayQuantity()} ₽"

private fun String?.toSupplyExpiryDisplay(): String {
    val value = this ?: return "мог"
    val date = value.substringBefore('T').split('-')
    val time = value.substringAfter('T', "").take(5)
    return if (date.size == 3) "${date[2]}.${date[1]}.${date[0]}${if (time.isNotBlank()) ", $time" else ""}" else "мог"
}

private fun SupplyItem.toEditableItem() = EditableSupplyItem(
    productId = productId,
    name = productName,
    unit = unit,
    quantity = quantity.toDisplayQuantity(),
    price = (price ?: totalPrice?.let { total -> if (quantity != 0.0) total / quantity else 0.0 } ?: 0.0).toDisplayQuantity(),
    expiresAt = expiresAt.toSupplyExpiryDisplay().removePrefix("мог")
)

private fun String.toApiExpiry(): String? {
    val normalized = trim().replace(",", "")
    val date = normalized.substringBefore(' ').split('.')
    val time = normalized.substringAfter(' ', "00:00")
    if (date.size != 3 || date.any(String::isBlank) || !time.matches(Regex("\\d{2}:\\d{2}"))) return null
    return "${date[2]}-${date[1].padStart(2, '0')}-${date[0].padStart(2, '0')}T$time:00+03:00"
}

private fun String.toSupplyDetailStatusLabel() = when (this) {
    "RECEIVED" -> "Принята"
    "IN_TRANSIT" -> "В пути"
    "CANCELLED" -> "Отменена"
    else -> "мог"
}

private fun String.toReceptionStatus() = when (this) {
    "RECEIVED" -> ReceptionStatus.Accepted
    "IN_TRANSIT" -> ReceptionStatus.InTransit
    "CANCELLED" -> ReceptionStatus.Cancelled
    else -> ReceptionStatus.InTransit
}
