package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.supplies.data.Supply as SupplyData
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyItemDto

private enum class SupplyStatus { Waiting, OnWay, Accepted, Mismatch }

@Composable
fun SuppliesScreen(
    onCreateSupply: () -> Unit,
    onSupplyClick: (String) -> Unit,
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
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
            UpdBanner()
        }
        item {
            StoqPrimaryButton(text = "Создать поставку", onClick = onCreateSupply)
        }
        item { StorageMetrics(supplies) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                StorageFilter("Все", supplies.size.toString(), Icons.Outlined.Inventory2, filter == null, { filter = null })
                StorageFilter("Ожидаются", supplies.count { it.status != "RECEIVED" }.toString(), Icons.Outlined.WarningAmber, filter == SupplyStatus.Waiting, { filter = SupplyStatus.Waiting })
                StorageFilter("Приняты", supplies.count { it.status == "RECEIVED" }.toString(), Icons.Outlined.CheckCircle, filter == SupplyStatus.Accepted, { filter = SupplyStatus.Accepted })
                StorageFilter("Есть расхождения", "мог", Icons.Outlined.WarningAmber, filter == SupplyStatus.Mismatch, { filter = SupplyStatus.Mismatch })
            }
        }
        items(visible, key = { it.id }) { supply -> SupplyCard(supply) { onSupplyClick(supply.id.toString()) } }
    }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, viewModel::refresh)
        is UiState.Success -> Unit
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Всего поставок", supplies.size.toString(), "мог", Icons.Outlined.LocalShipping, Modifier.weight(1f))
            Metric("Ожидают приёмки", supplies.count { it.status != "RECEIVED" }.toString(), "мог", Icons.Outlined.WarningAmber, Modifier.weight(1f), warning = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Принято поставок", supplies.count { it.status == "RECEIVED" }.toString(), "мог", Icons.Outlined.CheckCircle, Modifier.weight(1f))
            Metric("Сумма поставок", "мог", "мог", Icons.Outlined.Inventory2, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Metric(title: String, value: String, change: String, icon: ImageVector, modifier: Modifier, warning: Boolean = false) {
    StoqCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background((if (warning) CustomTheme.colors.warning else CustomTheme.colors.success).copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (warning) CustomTheme.colors.warning else CustomTheme.colors.success)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text("↑ $change", style = MaterialTheme.typography.bodySmall, color = if (warning) CustomTheme.colors.error else CustomTheme.colors.success, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StorageFilter(text: String, count: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = if (selected) CustomTheme.colors.primary.copy(alpha = .28f) else MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, if (selected) CustomTheme.colors.primary else MaterialTheme.colorScheme.outline)) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (text == "Есть расхождения") CustomTheme.colors.error else if (text == "Ожидаются") CustomTheme.colors.warning else CustomTheme.colors.success)
            Spacer(Modifier.width(6.dp)); Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            Spacer(Modifier.width(7.dp)); Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(count, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun UpdBanner() {
    Surface(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { }, color = CustomTheme.colors.primary.copy(alpha = .15f), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .24f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CameraAlt, null, tint = CustomTheme.colors.success) }
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Сканировать УПД", style = MaterialTheme.typography.titleMedium); Text("Быстро создайте поставку, отсканировав QR-код из УПД поставщика", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                        Text("мог", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
                    Text("мог", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("мог", style = MaterialTheme.typography.titleMedium)
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
    val label = when (status) { SupplyStatus.Waiting -> "Ожидается"; SupplyStatus.OnWay -> "В пути"; SupplyStatus.Accepted -> "Принята"; SupplyStatus.Mismatch -> "Есть расхождения" }
    Surface(color = statusColor(status).copy(alpha = .12f), shape = MaterialTheme.shapes.small) { Text(label, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.bodySmall, color = statusColor(status), fontWeight = FontWeight.Medium) }
}

@Composable
private fun statusColor(status: SupplyStatus) = when (status) { SupplyStatus.Waiting -> CustomTheme.colors.warning; SupplyStatus.OnWay -> CustomTheme.colors.primaryDark; SupplyStatus.Accepted -> CustomTheme.colors.success; SupplyStatus.Mismatch -> CustomTheme.colors.error }

private fun statusIcon(status: SupplyStatus) = when (status) { SupplyStatus.Waiting -> Icons.Outlined.WarningAmber; SupplyStatus.OnWay -> Icons.Outlined.LocalShipping; SupplyStatus.Accepted -> Icons.Outlined.CheckCircle; SupplyStatus.Mismatch -> Icons.Outlined.WarningAmber }

private fun SupplyData.toUiStatus() = if (status == "RECEIVED") SupplyStatus.Accepted else SupplyStatus.Waiting

private data class SupplyProduct(
    val productId: Long,
    val name: String,
    val category: String,
    val quantity: String,
    val price: String,
    val batch: String,
    val expires: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSupplyScreen(onBack: () -> Unit, viewModel: CreateSupplyViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("Поставка от 24.06.2024") }
    var date by remember { mutableStateOf("24.06.2024") }
    var supplier by remember { mutableStateOf("ООО «ФудСервис»") }
    var search by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var products by remember { mutableStateOf(emptyList<SupplyProduct>()) }
    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ArrowBack, "Назад", modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(6.dp))
                Spacer(Modifier.width(8.dp)); Column { Text("Новая поставка", style = MaterialTheme.typography.headlineMedium); Text("Создайте поставку и добавьте полученные продукты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item {
            StoqCard {
                CompactTextField(name, { name = it }, "Название *", modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(date, { showDatePicker = true }, Modifier.weight(1f))
                    CompactTextField(supplier, { supplier = it }, "Поставщик *", Modifier.weight(1f))
                }
            }
        }
        item {
            StoqCard {
                Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Продукты в поставке", style = MaterialTheme.typography.titleLarge); Text("Добавьте продукты, укажите количество и проверьте данные", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Box(Modifier.size(44.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .15f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CameraAlt, "Сфотографировать УПД", tint = CustomTheme.colors.success) } }
                Spacer(Modifier.height(10.dp))
                SearchField(search, { search = it })
                if (search.isNotBlank()) {
                    viewModel.products.filter { it.name.contains(search, true) }.forEach { candidate ->
                        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable {
                            if (products.none { it.productId == candidate.id }) {
                                products = listOf(SupplyProduct(candidate.id, candidate.name, "мог", "1", "мог", "мог", date.toIsoDateTime())) + products
                            }
                            search = ""
                        }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, null, tint = CustomTheme.colors.success); Spacer(Modifier.width(8.dp)); Text(candidate.name, Modifier.weight(1f)); Text(candidate.unit?.shortName ?: "мог", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                StoqPrimaryButton("Добавить продукт", { search = " " })
            }
        }
        item { SupplyTotal(products.size) }
        items(products, key = { it.productId }) { product ->
            SupplyProductCard(product, { products = products.filterNot { it == product } }, { updated -> products = products.map { if (it == product) updated else it } })
        }
        item {
            StoqCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(modifier = Modifier.weight(1f).height(52.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onBack), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Box(contentAlignment = Alignment.Center) { Text("Сохранить", style = MaterialTheme.typography.labelLarge) } }
                    StoqPrimaryButton("На приёмку", {
                        val items = products.mapNotNull { product ->
                            product.quantity.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }?.let { quantity ->
                                CreateSupplyItemDto(product.productId, quantity, product.expires.toIsoDateTime())
                            }
                        }
                        if (name.isNotBlank() && items.isNotEmpty()) {
                            viewModel.create(CreateSupplyDto(name.trim(), items, date.toIsoDateTime()), onBack)
                        }
                    }, Modifier.weight(1.3f))
                }
            }
        }
    }
    if (showDatePicker) DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) }; showDatePicker = false }) { Text("Выбрать") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }) { DatePicker(datePickerState) }
    if (viewModel.savingState == UiState.Loading) LoadingScreen()
}

@Composable
private fun SupplyTotal(count: Int) {
    Surface(color = CustomTheme.colors.primary.copy(alpha = .12f), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = CustomTheme.colors.success) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Всего позиций", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("$count", style = MaterialTheme.typography.titleLarge) }; Box(Modifier.width(1.dp).height(42.dp).background(MaterialTheme.colorScheme.outline)); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1.5f)) { Text("Общая сумма поставки", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("мог", style = MaterialTheme.typography.titleLarge) } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplyProductCard(product: SupplyProduct, onDelete: () -> Unit, onUpdate: (SupplyProduct) -> Unit) {
    var editing by remember(product) { mutableStateOf(false) }
    var quantity by remember(product) { mutableStateOf(product.quantity) }
    var price by remember(product) { mutableStateOf(product.price) }
    var batch by remember(product) { mutableStateOf(product.batch) }
    var expires by remember(product) { mutableStateOf(product.expires) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    StoqCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.small).background(CustomTheme.colors.primary.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, modifier = Modifier.size(32.dp), tint = CustomTheme.colors.success) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row { Column(Modifier.weight(1f)) { Text(product.name, style = MaterialTheme.typography.titleMedium); Text(product.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Surface(shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Icon(if (editing) Icons.Outlined.Check else Icons.Outlined.Edit, if (editing) "Подтвердить" else "Редактировать", modifier = Modifier.clickable { if (editing) onUpdate(product.copy(quantity = quantity, price = price, batch = batch, expires = expires)); editing = !editing }.padding(9.dp), tint = CustomTheme.colors.primaryDark) }; Spacer(Modifier.width(8.dp)); Surface(shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, CustomTheme.colors.error.copy(alpha = .5f))) { Icon(Icons.Outlined.DeleteOutline, "Удалить", modifier = Modifier.clickable(onClick = onDelete).padding(9.dp), tint = CustomTheme.colors.error) } }
                Spacer(Modifier.height(8.dp))
                if (editing) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CompactTextField(quantity, { quantity = it }, "Количество", Modifier.weight(.7f)); CompactTextField(price, { price = it }, "Цена", Modifier.weight(.7f)); CompactTextField(batch, { batch = it }, "Партия", Modifier.weight(1.2f)) }; Spacer(Modifier.height(6.dp)); Surface(modifier = Modifier.fillMaxWidth().height(48.dp).clip(MaterialTheme.shapes.small).clickable { showDatePicker = true }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = MaterialTheme.shapes.small) { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(expires, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp)) } } } else { ProductDetailsRow(quantity, batch, expires) }
                Spacer(Modifier.height(8.dp))
                ProductPriceSummary(price, quantity)
            }
        }
    }
    if (showDatePicker) DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { expires = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) }; showDatePicker = false }) { Text("Выбрать") } }) { DatePicker(datePickerState) }
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

private fun String.toIsoDateTime(): String {
    val parts = trim().removePrefix("Годен до: ").split('.')
    return if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}T00:00:00.000Z" else "2026-01-01T00:00:00.000Z"
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
