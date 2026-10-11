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
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard

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

private val detailProducts = listOf(
    SupplyDetailProduct("TOM-001", "Томаты свежие", "Овощи", "50", "кг", "120 ₽", "6 000 ₽", "01.07.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("MOZ-001", "Моцарелла", "Молочные продукты", "10", "кг", "450 ₽", "4 500 ₽", "05.07.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("SLV-001", "Сливки 33%", "Молочные продукты", "5", "л", "280 ₽", "1 400 ₽", "10.07.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("LOS-001", "Лосось филе", "Рыба и морепродукты", "8", "кг", "980 ₽", "7 840 ₽", "28.06.2024", ReceptionStatus.NeedsReview),
    SupplyDetailProduct("RUK-001", "Руккола", "Зелень", "4", "кг", "320 ₽", "1 280 ₽", "29.06.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("MAS-001", "Оливковое масло", "Бакалея", "6", "л", "650 ₽", "3 900 ₽", "15.12.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("BAZ-001", "Базилик свежий", "Зелень", "3", "кг", "400 ₽", "1 200 ₽", "30.06.2024", ReceptionStatus.Accepted),
    SupplyDetailProduct("PAR-001", "Пармезан", "Молочные продукты", "2", "кг", "1 200 ₽", "2 400 ₽", "20.08.2024", ReceptionStatus.Waiting),
    SupplyDetailProduct("KRE-001", "Креветки тигровые", "Рыба и морепродукты", "3", "кг", "1 150 ₽", "3 450 ₽", "12.07.2024", ReceptionStatus.Accepted)
)

@Composable
fun SupplyDetailsScreen(supplyNumber: String, onBack: () -> Unit) {
    val colors = LocalColors.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<ReceptionStatus?>(null) }
    val visibleProducts = remember(query, filter) {
        detailProducts.filter { product ->
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
        item { SupplyOverview(supplyNumber) }
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
private fun SupplyOverview(supplyNumber: String) {
    val colors = LocalColors.current
    val metrics = listOf(
        DetailMetric("ID поставки", supplyNumber, Icons.AutoMirrored.Outlined.ReceiptLong, colors.primaryDark),
        DetailMetric("Поступила", "24.06.2024", Icons.Outlined.CalendarToday, colors.primaryDark),
        DetailMetric("Позиций", "12", Icons.Outlined.Inventory2, colors.success),
        DetailMetric("Сумма", "12 450 ₽", Icons.AutoMirrored.Outlined.ReceiptLong, colors.success),
        DetailMetric("Принял", "Артём Козлов", Icons.Outlined.PersonOutline, colors.secondary)
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
