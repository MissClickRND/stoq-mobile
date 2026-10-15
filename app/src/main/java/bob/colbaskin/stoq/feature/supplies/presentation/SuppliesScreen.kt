package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.drafts.presentation.DraftsBanner
import bob.colbaskin.stoq.common.drafts.presentation.UnsavedChangesDialog
import bob.colbaskin.stoq.common.drafts.presentation.DraftSnackbar
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.supplies.data.Supply as SupplyData
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyItemDto
import kotlinx.serialization.Serializable

private enum class SupplyStatus { Received, InTransit, Cancelled }

@Composable
fun SuppliesScreen(
    onCreateSupply: () -> Unit,
    onScanSupply: () -> Unit,
    onSupplyClick: (String) -> Unit,
    onDraftsClick: () -> Unit,
    draftFeedback: String? = null,
    onDraftFeedbackShown: () -> Unit = {},
    viewModel: SuppliesViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<SupplyStatus?>(null) }
    val supplies = (viewModel.state as? UiState.Success)?.data.orEmpty()
    val visible = remember(query, filter, supplies) {
        supplies.filter { supply ->
            (filter == null || supply.toUiStatus() == filter) &&
                listOf(supply.number, supply.items.joinToString { it.productName }).any { it.contains(query, true) }
        }
    }
    PullToRefreshContainer(viewModel.isRefreshing, viewModel::refresh, Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            Text("Поставки", style = MaterialTheme.typography.headlineMedium)
            Text("Входящие поставки от поставщиков и контроль приёмки товаров", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SearchField(query, { query = it })
        }
        item {
            SupplyScanBanner(onScanSupply)
        }
        item { DraftsBanner(viewModel.drafts.size, "поставка", onDraftsClick) }
        item { StorageMetrics(supplies) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                StorageFilter("Все", supplies.size.toString(), Icons.Outlined.Inventory2, filter == null, { filter = null })
                StorageFilter("Приняты", supplies.count { it.status == "RECEIVED" }.toString(), Icons.Outlined.CheckCircle, filter == SupplyStatus.Received, { filter = SupplyStatus.Received })
                StorageFilter("В пути", supplies.count { it.status == "IN_TRANSIT" }.toString(), Icons.Outlined.LocalShipping, filter == SupplyStatus.InTransit, { filter = SupplyStatus.InTransit })
                StorageFilter("Отменены", supplies.count { it.status == "CANCELLED" }.toString(), Icons.Outlined.Close, filter == SupplyStatus.Cancelled, { filter = SupplyStatus.Cancelled })
            }
        }
            items(visible, key = { it.id }) { supply -> SupplyCard(supply) { onSupplyClick(supply.id.toString()) } }
        }
        FloatingActionButton(
            onClick = onCreateSupply,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = CustomTheme.colors.primary,
            contentColor = CustomTheme.colors.textOnPrimary,
            shape = CircleShape
        ) { Icon(Icons.Outlined.Add, "Создать поставку") }
    }
    }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, viewModel::refresh)
        is UiState.Success -> Unit
    }
    DraftSnackbar(draftFeedback, onDraftFeedbackShown, Modifier.padding(bottom = 20.dp))
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, onFocusChanged: ((Boolean) -> Unit)? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().onFocusChanged { onFocusChanged?.invoke(it.isFocused) },
        placeholder = { Text("Поиск поставок, поставщиков или товаров…", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun StorageMetrics(supplies: List<SupplyData>) {
    StoqMetricGrid(listOf(
        StoqMetric("Всего поставок", supplies.size.toString(), "мог", Icons.Outlined.LocalShipping, CustomTheme.colors.success),
        StoqMetric("В пути", supplies.count { it.status == "IN_TRANSIT" }.toString(), "мог", Icons.Outlined.LocalShipping, CustomTheme.colors.warning),
        StoqMetric("Принято поставок", supplies.count { it.status == "RECEIVED" }.toString(), "мог", Icons.Outlined.CheckCircle, CustomTheme.colors.success),
        StoqMetric("Сумма поставок", supplies.sumOf { it.totalPrice ?: 0.0 }.takeIf { it > 0 }?.toRubles() ?: "мог", "мог", Icons.Outlined.Inventory2, CustomTheme.colors.success)
    ))
}

@Composable
private fun StorageFilter(text: String, count: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val tint = when (text) {
        "Отменены" -> CustomTheme.colors.error
        "В пути" -> CustomTheme.colors.warning
        "Приняты" -> CustomTheme.colors.success
        else -> CustomTheme.colors.primary
    }
    Surface(modifier = Modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = if (selected) tint.copy(alpha = .18f) else MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, if (selected) tint else MaterialTheme.colorScheme.outline)) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = tint)
            Spacer(Modifier.width(6.dp)); Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            Spacer(Modifier.width(7.dp)); Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(count, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SupplyStatusSelector(
    status: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = status.toSupplyStatusLabel()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("Статус поставки") },
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomTheme.colors.primaryDark,
                focusedLabelColor = CustomTheme.colors.primaryDark
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = CustomTheme.colors.surface
        ) {
            listOf("RECEIVED", "IN_TRANSIT", "CANCELLED").forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toSupplyStatusLabel()) },
                    onClick = { onStatusChange(value); expanded = false }
                )
            }
        }
    }
}

private fun String.toSupplyStatusLabel() = when (this) {
    "RECEIVED" -> "Принята"
    "IN_TRANSIT" -> "В пути"
    "CANCELLED" -> "Отменена"
    else -> "мог"
}

@Composable
private fun SupplyScanBanner(onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = CustomTheme.colors.primary.copy(alpha = .15f), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .24f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CameraAlt, null, tint = CustomTheme.colors.success) }
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Сканировать УПД", style = MaterialTheme.typography.titleMedium); Text("Сфотографируйте УПД или выберите готовое фото", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Surface(shape = MaterialTheme.shapes.small, color = CustomTheme.colors.primary) { Text("Сканировать", Modifier.padding(horizontal = 11.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium) }
        }
    }
}

@Composable
private fun SupplyCard(supply: SupplyData, onClick: () -> Unit) {
    val status = supply.toUiStatus()
    StoqCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(statusColor(status).copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(statusIcon(status), null, tint = statusColor(status)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(supply.number, style = MaterialTheme.typography.titleMedium)
                        Text(supply.supplier ?: "мог", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("мог", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(status)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Info(Icons.Outlined.CalendarToday, supply.receivedAt?.take(10) ?: "мог", Modifier.weight(1f))
                    Info(Icons.Outlined.Inventory2, "${supply.items.size} позиций", Modifier.weight(1f), alignEnd = true)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PersonOutline, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(supply.supplier ?: "мог", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(supply.totalPrice?.toRubles() ?: "мог", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun Info(icon: ImageVector, text: String, modifier: Modifier, alignEnd: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(5.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusPill(status: SupplyStatus) {
    val label = when (status) { SupplyStatus.Received -> "Принята"; SupplyStatus.InTransit -> "В пути"; SupplyStatus.Cancelled -> "Отменена" }
    Surface(color = statusColor(status).copy(alpha = .12f), shape = MaterialTheme.shapes.small) { Text(label, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.bodySmall, color = statusColor(status), fontWeight = FontWeight.Medium) }
}

@Composable
private fun statusColor(status: SupplyStatus) = when (status) { SupplyStatus.Received -> CustomTheme.colors.success; SupplyStatus.InTransit -> CustomTheme.colors.warning; SupplyStatus.Cancelled -> CustomTheme.colors.error }

private fun statusIcon(status: SupplyStatus) = when (status) { SupplyStatus.Received -> Icons.Outlined.CheckCircle; SupplyStatus.InTransit -> Icons.Outlined.LocalShipping; SupplyStatus.Cancelled -> Icons.Outlined.Close }

private fun SupplyData.toUiStatus() = when (status) {
    "RECEIVED" -> SupplyStatus.Received
    "CANCELLED" -> SupplyStatus.Cancelled
    "IN_TRANSIT" -> SupplyStatus.InTransit
    else -> SupplyStatus.InTransit
}

@Serializable
data class SupplyProduct(
    val productId: Long,
    val name: String,
    val category: String,
    val quantity: String,
    val price: String,
    val batch: String,
    val expires: String
)

@Serializable
data class SupplyDraftPayload(
    val name: String,
    val date: String,
    val supplier: String,
    val status: String = "RECEIVED",
    val products: List<SupplyProduct>
) {
    val hasContent: Boolean get() = name.isNotBlank() || supplier.isNotBlank() || products.isNotEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSupplyScreen(
    onBack: () -> Unit,
    onSuppliesClick: () -> Unit,
    onScanSupply: () -> Unit,
    draftId: String? = null,
    scanToken: String? = null,
    viewModel: CreateSupplyViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("24.06.2024") }
    var supplier by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("RECEIVED") }
    var search by remember { mutableStateOf("") }
    var showProductSuggestions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var products by remember { mutableStateOf(emptyList<SupplyProduct>()) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var restoredDraftId by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    LaunchedEffect(draftId) {
        if (draftId != null && restoredDraftId != draftId) {
            restoredDraftId = draftId
            viewModel.loadDraft(draftId)
        }
    }
    LaunchedEffect(scanToken) {
        scanToken?.let(viewModel::consumeAiRecognition)
    }
    LaunchedEffect(viewModel.restoredDraft) {
        viewModel.restoredDraft?.let { draft ->
            name = draft.name
            date = draft.date
            supplier = draft.supplier
            status = draft.status
            products = draft.products
            hasUnsavedChanges = false
        }
    }
    LaunchedEffect(viewModel.aiRecognition) {
        viewModel.aiRecognition?.let { recognition ->
            supplier = recognition.sellerName.orEmpty()
            products = recognition.items.map { item ->
                SupplyProduct(
                    productId = item.productId,
                    name = item.name,
                    category = item.unit ?: "мог",
                    quantity = item.quantity.toSupplyNumber(),
                    price = item.price.toSupplyNumber(),
                    batch = "мог",
                    expires = item.expiresAt?.toExpiryDisplayValue() ?: "Укажите срок"
                )
            }
            hasUnsavedChanges = products.isNotEmpty() || supplier.isNotBlank()
        }
    }
    val currentDraft = SupplyDraftPayload(name, date, supplier, status, products)
    val hasValidProducts = products.isNotEmpty() && products.all { product ->
        product.quantity.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true &&
            product.price.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true &&
            product.expires.toExpiryIsoDateTime() != null
    }
    val canCreateSupply = name.isNotBlank() && supplier.isNotBlank() && hasValidProducts
    LaunchedEffect(name, date, supplier, status, products, hasUnsavedChanges) {
        if (hasUnsavedChanges && currentDraft.hasContent) viewModel.saveDraft(currentDraft, showMessage = false)
    }
    val hasChanges = hasUnsavedChanges && currentDraft.hasContent
    BackHandler(enabled = hasChanges) { showExitDialog = true }
    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ArrowBack, "Назад", modifier = Modifier.clip(CircleShape).clickable { if (hasChanges) showExitDialog = true else onBack() }.padding(6.dp))
                Spacer(Modifier.width(8.dp)); Column { Text("Новая поставка", style = MaterialTheme.typography.headlineMedium); Text("Создайте поставку и добавьте полученные продукты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item {
            SupplyScanBanner(onClick = onScanSupply)
        }
        item {
            StoqCard {
                CompactTextField(name, { name = it; hasUnsavedChanges = true }, "Номер поставки *", modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(date, { showDatePicker = true }, Modifier.weight(1f))
                    CompactTextField(supplier, { supplier = it; hasUnsavedChanges = true }, "Поставщик *", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                SupplyStatusSelector(
                    status = status,
                    onStatusChange = { status = it; hasUnsavedChanges = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            StoqCard {
                Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Продукты в поставке", style = MaterialTheme.typography.titleLarge); Text("Добавьте продукты, укажите количество и проверьте данные", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                Spacer(Modifier.height(10.dp))
                SearchField(
                    value = search,
                    onChange = { value -> search = value; showProductSuggestions = true; viewModel.searchProducts(value) },
                    onFocusChanged = { focused ->
                        showProductSuggestions = focused
                        if (focused) viewModel.searchProducts(search)
                    }
                )
                if (showProductSuggestions) {
                    viewModel.products.filter { it.name.contains(search, true) }.forEach { candidate ->
                        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable {
                            if (products.none { it.productId == candidate.id }) {
                                products = listOf(SupplyProduct(candidate.id, candidate.name, "мог", "1", "0", "мог", "Укажите срок")) + products
                                hasUnsavedChanges = true
                            }
                            search = ""
                            showProductSuggestions = false
                        }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, null, tint = CustomTheme.colors.success); Spacer(Modifier.width(8.dp)); Text(candidate.name, Modifier.weight(1f)); Text(candidate.unit?.shortName ?: "мог", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                StoqPrimaryButton("Добавить продукт", { search = " " })
            }
        }
        item { SupplyTotal(products) }
        items(products, key = { it.productId }) { product ->
            SupplyProductCard(product, { products = products.filterNot { it == product }; hasUnsavedChanges = true }, { updated -> products = products.map { if (it == product) updated else it }; hasUnsavedChanges = true })
        }
        item {
            StoqCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(modifier = Modifier.weight(1f).height(52.dp).clip(MaterialTheme.shapes.medium).clickable(enabled = currentDraft.hasContent) { viewModel.saveDraft(currentDraft, onSaved = { hasUnsavedChanges = false; onSuppliesClick() }, showMessage = false) }, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Box(contentAlignment = Alignment.Center) { Text("Сохранить", style = MaterialTheme.typography.labelLarge) } }
                    StoqPrimaryButton("На приёмку", {
                        val items = products.mapNotNull { product ->
                            val quantity = product.quantity.replace(',', '.').toDoubleOrNull()
                            val price = product.price.replace(',', '.').toDoubleOrNull()
                            val expiresAt = product.expires.toExpiryIsoDateTime()
                            if (quantity != null && quantity > 0 && price != null && price >= 0 && expiresAt != null) {
                                CreateSupplyItemDto(product.productId, quantity, quantity * price, expiresAt)
                            } else null
                        }
                        if (canCreateSupply && items.size == products.size) {
                            viewModel.create(
                                CreateSupplyDto(
                                    status = status,
                                    supplyNumber = name.trim(),
                                    supplier = supplier.trim(),
                                    items = items,
                                    receivedAt = date.toIsoDateTime()
                                ),
                                currentDraft,
                                onBack
                            )
                        }
                    }, Modifier.weight(1.3f), enabled = canCreateSupply)
                }
            }
        }
    }
    if (showDatePicker) DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)); hasUnsavedChanges = true }; showDatePicker = false }) { Text("Выбрать") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }) { DatePicker(datePickerState) }
    if (viewModel.savingState == UiState.Loading) LoadingScreen()
    UnsavedChangesDialog(
        visible = showExitDialog,
        entityName = "создание поставки",
        onDismiss = { showExitDialog = false },
        onSaveAndExit = { showExitDialog = false; viewModel.saveDraft(currentDraft, { hasUnsavedChanges = false; onSuppliesClick() }, showMessage = false) },
        onDiscardAndExit = { showExitDialog = false; viewModel.discardDraft(onBack) }
    )
    DraftSnackbar(viewModel.draftMessage, viewModel::consumeDraftMessage, Modifier.padding(bottom = 20.dp))
}

@Composable
private fun SupplyTotal(products: List<SupplyProduct>) {
    val total = products.sumOf { product ->
        (product.quantity.replace(',', '.').toDoubleOrNull() ?: 0.0) *
            (product.price.replace(',', '.').toDoubleOrNull() ?: 0.0)
    }
    Surface(color = CustomTheme.colors.primary.copy(alpha = .12f), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = CustomTheme.colors.success) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Всего позиций", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${products.size}", style = MaterialTheme.typography.titleLarge) }; Box(Modifier.width(1.dp).height(42.dp).background(MaterialTheme.colorScheme.outline)); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1.5f)) { Text("Общая сумма поставки", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(total.toRubles(), style = MaterialTheme.typography.titleLarge) } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplyProductCard(product: SupplyProduct, onDelete: () -> Unit, onUpdate: (SupplyProduct) -> Unit) {
    var editing by remember(product.productId) { mutableStateOf(product.expires.toExpiryIsoDateTime() == null) }
    var quantity by remember(product.productId) { mutableStateOf(product.quantity) }
    var price by remember(product.productId) { mutableStateOf(product.price) }
    var batch by remember(product.productId) { mutableStateOf(product.batch) }
    var expires by remember(product.productId) { mutableStateOf(product.expires) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val context = LocalContext.current
    StoqCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.small).background(CustomTheme.colors.primary.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, modifier = Modifier.size(32.dp), tint = CustomTheme.colors.success) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row { Column(Modifier.weight(1f)) { Text(product.name, style = MaterialTheme.typography.titleMedium); Text(product.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Surface(shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Icon(if (editing) Icons.Outlined.Check else Icons.Outlined.Edit, if (editing) "Подтвердить" else "Редактировать", modifier = Modifier.clickable { if (editing) onUpdate(product.copy(quantity = quantity, price = price, batch = batch, expires = expires)); editing = !editing }.padding(9.dp), tint = CustomTheme.colors.primaryDark) }; Spacer(Modifier.width(8.dp)); Surface(shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, CustomTheme.colors.error.copy(alpha = .5f))) { Icon(Icons.Outlined.DeleteOutline, "Удалить", modifier = Modifier.clickable { showDeleteDialog = true }.padding(9.dp), tint = CustomTheme.colors.error) } }
                Spacer(Modifier.height(8.dp))
                if (editing) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CompactTextField(quantity, { quantity = it; onUpdate(product.copy(quantity = it, price = price, batch = batch, expires = expires)) }, "Количество", Modifier.weight(.7f)); CompactTextField(price, { price = it; onUpdate(product.copy(quantity = quantity, price = it, batch = batch, expires = expires)) }, "Цена", Modifier.weight(.7f)); CompactTextField(batch, { batch = it; onUpdate(product.copy(quantity = quantity, price = price, batch = it, expires = expires)) }, "Партия", Modifier.weight(1.2f)) }; Spacer(Modifier.height(6.dp)); Surface(modifier = Modifier.fillMaxWidth().height(48.dp).clip(MaterialTheme.shapes.small).clickable { showDatePicker = true }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = MaterialTheme.shapes.small) { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(expires, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp)) } } } else { ProductDetailsRow(quantity, batch, expires) }
                Spacer(Modifier.height(8.dp))
                ProductPriceSummary(price, quantity)
            }
        }
    }
    if (showDatePicker) DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { selected ->
                    val selectedDate = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(selected))
                    android.app.TimePickerDialog(context, { _, hour, minute ->
                        expires = "$selectedDate ${String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)}"
                        onUpdate(product.copy(quantity = quantity, price = price, batch = batch, expires = expires))
                    }, 12, 0, true).show()
                }
                showDatePicker = false
            }) { Text("Далее") }
        }
    ) { DatePicker(datePickerState) }
    if (showDeleteDialog) AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text("Удалить товар из поставки?") },
        text = { Text("${product.name} будет удалён из текущей поставки.") },
        confirmButton = { TextButton(onClick = { showDeleteDialog = false; onDelete() }) { Text("Удалить", color = CustomTheme.colors.error) } },
        dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
    )
}

@Composable
private fun ProductDetailsRow(quantity: String, batch: String, expires: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(quantity.substringBefore(' '), style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(28.dp))
        Text(quantity.substringAfter(' ', "кг"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(22.dp))
        Box(Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = .7f)))
        Column(Modifier.weight(1f)) {
            Text(batch, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(expires, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProductPriceSummary(price: String, quantity: String) {
    val priceValue = price.filter(Char::isDigit).toIntOrNull() ?: 0
    val quantityValue = quantity.filter(Char::isDigit).toIntOrNull() ?: 0
    val total = priceValue * quantityValue
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Цена за ед.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${formatRubles(priceValue)} ₽", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.width(1.dp).height(42.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = .7f)))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text("Сумма", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${formatRubles(total)} ₽", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatRubles(value: Int): String = String.format(java.util.Locale("ru"), "%,d", value).replace(',', ' ')
private fun Double.toRubles(): String = "${formatRubles(toInt())} ₽"
private fun Double?.toSupplyNumber(): String = this?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty()

private fun String.toIsoDateTime(): String {
    val parts = trim().removePrefix("Годен до: ").split('.')
    return if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}T00:00:00+03:00" else "2026-01-01T00:00:00+03:00"
}

private fun String.toExpiryIsoDateTime(): String? {
    val raw = trim().removePrefix("Годен до: ")
    val datePart = raw.substringBefore(' ')
    val timePart = raw.substringAfter(' ', "00:00").takeIf { it.matches(Regex("\\d{2}:\\d{2}")) } ?: "00:00"
    val parts = datePart.split('.')
    return if (parts.size == 3 && parts.all(String::isNotBlank)) {
        "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}T${timePart}:00+03:00"
    } else null
}

private fun String.toExpiryDisplayValue(): String? {
    val datePart = substringBefore('T').takeIf { it.length == 10 } ?: return null
    val parts = datePart.split('-')
    val timePart = substringAfter('T', "").take(5)
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}${if (timePart.isNotBlank()) " $timePart" else ""}" else null
}

@Composable
private fun CompactTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValueChange, modifier = modifier.height(52.dp), label = { RequiredLabel(label) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, shape = MaterialTheme.shapes.small, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CustomTheme.colors.primaryDark, focusedLabelColor = CustomTheme.colors.primaryDark))
}

@Composable
private fun DateField(value: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(modifier = modifier.height(52.dp).clip(MaterialTheme.shapes.small).clickable(onClick = onClick), shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { RequiredLabel("Дата *"); Text(value, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun RequiredLabel(label: String) {
    val required = label.endsWith('*')
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label.removeSuffix("*").trimEnd(), style = MaterialTheme.typography.bodySmall)
        if (required) Text(" *", style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.error)
    }
}
