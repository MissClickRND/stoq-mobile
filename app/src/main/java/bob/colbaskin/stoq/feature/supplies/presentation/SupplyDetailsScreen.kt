package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard
import bob.colbaskin.stoq.feature.supplies.data.Supply

private enum class ReceptionStatus(val title: String, val tone: StoqStatusTone) {
    Accepted("Принято", StoqStatusTone.Success),
    NeedsReview("Проверить", StoqStatusTone.Error),
    Waiting("Ожидается", StoqStatusTone.Warning)
}

private data class SupplyDetailProduct(
    val sku: String,
    val name: String,
    val category: String,
    val quantity: String,
    val unit: String,
    val unitPrice: String,
    val total: String,
    val expiresAt: String,
    val reception: ReceptionStatus
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
    var filter by remember { mutableStateOf<ReceptionStatus?>(null) }
    val supply = (viewModel.state as? UiState.Success)?.data
    val products = supply?.items?.map { item ->
        SupplyDetailProduct(
            sku = item.productId.toString(),
            name = item.productName,
            category = "мог",
            quantity = item.quantity.toDisplayQuantity(),
            unit = item.unit.ifBlank { "мог" },
            unitPrice = "мог",
            total = "мог",
            expiresAt = item.expiresAt?.take(10) ?: "мог",
            reception = if (supply.status == "RECEIVED") ReceptionStatus.Accepted else ReceptionStatus.Waiting
        )
    }.orEmpty()
    val visibleProducts = remember(query, filter, products) {
        products.filter { product ->
            (filter == null || product.reception == filter) &&
                listOf(product.name, product.sku, product.category).any { it.contains(query, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = StoqSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item { DetailHeader(supplyNumber, onBack) }
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
        item { DetailSearchField(query, { query = it }) }
        item {
            ReceptionFilters(selected = filter, onSelected = { filter = it })
        }
        if (visibleProducts.isEmpty()) {
            item { ProductsEmptyState() }
        } else {
            items(visibleProducts, key = { it.sku }) { product -> SupplyDetailProductCard(product) }
        }
    }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, onRetry = { viewModel.load(supplyNumber) }, onBack = onBack)
        is UiState.Success -> Unit
    }
}

@Composable
private fun DetailHeader(supplyNumber: String, onBack: () -> Unit) {
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
    }
}

@Composable
private fun SupplyOverview(supply: Supply?) {
    val colors = LocalColors.current
    val metrics = listOf(
        DetailMetric("ID поставки", supply?.number ?: "мог", Icons.AutoMirrored.Outlined.ReceiptLong, colors.primaryDark),
        DetailMetric("Поступила", supply?.receivedAt?.take(10) ?: "мог", Icons.Outlined.CalendarToday, colors.primaryDark),
        DetailMetric("Позиций", supply?.items?.size?.toString() ?: "мог", Icons.Outlined.Inventory2, colors.success),
        DetailMetric("Сумма", "мог", Icons.AutoMirrored.Outlined.ReceiptLong, colors.success),
        DetailMetric("Принял", "мог", Icons.Outlined.PersonOutline, colors.secondary)
    )
    Column {
        Text("Информация о поставке", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Column(
            modifier = Modifier.padding(top = StoqSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
        ) {
            metrics.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                    row.forEach { metric ->
                        StoqMetricCard(
                            label = metric.label,
                            value = metric.value,
                            change = "",
                            icon = metric.icon,
                            tint = metric.tint,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
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
private fun ReceptionFilters(selected: ReceptionStatus?, onSelected: (ReceptionStatus?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small), modifier = Modifier.fillMaxWidth()) {
        DetailFilter("Все", selected == null, Modifier.weight(1f)) { onSelected(null) }
        DetailFilter("Принято", selected == ReceptionStatus.Accepted, Modifier.weight(1f)) { onSelected(ReceptionStatus.Accepted) }
        DetailFilter("Проверить", selected == ReceptionStatus.NeedsReview, Modifier.weight(1f)) { onSelected(ReceptionStatus.NeedsReview) }
    }
}

@Composable
private fun DetailFilter(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = LocalColors.current
    Surface(
        modifier = modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primary.copy(alpha = 0.25f) else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelMedium, color = colors.textPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun SupplyDetailProductCard(product: SupplyDetailProduct) {
    val colors = LocalColors.current
    StoqCard(contentPadding = PaddingValues(StoqSpacing.Large)) {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(productImage(product.name)),
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text("Арт. ${product.sku}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        CategoryTag(product.category)
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
private fun CategoryTag(category: String) {
    val tint = categoryColor(category)
    Surface(
        modifier = Modifier.padding(top = StoqSpacing.Small),
        shape = MaterialTheme.shapes.large,
        color = tint.copy(alpha = 0.13f)
    ) {
        Text(category, modifier = Modifier.padding(horizontal = StoqSpacing.Small, vertical = StoqSpacing.ExtraSmall), style = MaterialTheme.typography.labelMedium, color = tint)
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
                Text("Измените поисковый запрос или фильтр.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun categoryColor(category: String) = when (category) {
    "Овощи", "Зелень" -> LocalColors.current.success
    "Рыба и морепродукты" -> LocalColors.current.primaryDark
    "Бакалея" -> LocalColors.current.warning
    else -> LocalColors.current.secondary
}

private fun productImage(name: String) = when {
    name.contains("Томат") -> R.drawable.product_tomatoes
    name.contains("Моцарелла") || name.contains("Пармезан") -> R.drawable.product_mozzarella
    name.contains("Лосось") -> R.drawable.product_salmon
    name.contains("Креветки") -> R.drawable.product_salmon
    else -> R.drawable.product_iceberg
}

private data class DetailMetric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: androidx.compose.ui.graphics.Color
)

private fun Double.toDisplayQuantity() = if (this % 1.0 == 0.0) toLong().toString() else toString()
