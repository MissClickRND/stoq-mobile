package bob.colbaskin.stoq.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import kotlin.math.roundToInt

private val DashboardGreen = Color(0xFF22A51C)
private val DashboardBlue = Color(0xFF319BF4)
private val DashboardPurple = Color(0xFF9864E9)
private val DashboardRed = Color(0xFFFF5A68)
private val DashboardYellow = Color(0xFFFFBE35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var selectedRange by remember { mutableIntStateOf(1) }
    var selectedMetric by remember { mutableIntStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()
    val ranges = listOf("Сегодня", "7 дней", "30 дней")
    val metrics = listOf("Выручка", "Прибыль", "Заказы")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Large)
    ) {
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
                Text("Дашборд", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Ключевые показатели вашего ресторана",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                DateRangeButton(
                    text = datePickerState.selectedStartDateMillis?.let { start ->
                        datePickerState.selectedEndDateMillis?.let { end -> "${formatDate(start)} – ${formatDate(end)}" }
                    } ?: "01.06.2024 – 30.06.2024",
                    onClick = { showDatePicker = true }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranges.forEachIndexed { index, label ->
                        FilterChip(label, selected = selectedRange == index, Modifier.weight(1f)) { selectedRange = index }
                    }
                }
            }
        }
        item {
            StoqCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    metrics.forEachIndexed { index, label ->
                        FilterChip(label, selectedMetric == index, Modifier.weight(1f)) { selectedMetric = index }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(metrics[selectedMetric], style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(metricValue(selectedMetric), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(12.dp))
                    Text("↑ ${metricGrowth(selectedMetric)}", color = DashboardGreen, fontWeight = FontWeight.Bold)
                }
                Text("по сравнению с предыдущим периодом", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                MetricChart()
            }
        }
        item { MetricGrid() }
        item { LowStockSection() }
        item { PromotionSection() }
        item { CategoriesSection() }
        item { Spacer(Modifier.height(12.dp)) }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { Text("Применить", modifier = Modifier.padding(16.dp).clickable { selectedRange = 3; showDatePicker = false }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            dismissButton = { Text("Отмена", modifier = Modifier.padding(16.dp).clickable { showDatePicker = false }) }
        ) { DateRangePicker(state = datePickerState, modifier = Modifier.weight(1f, fill = false)) }
    }
}

@Composable
private fun DateRangeButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = background,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun MetricChart() {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = .24f)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val values = remember { listOf(.30f, .40f, .32f, .62f, .48f, .37f, .50f, .61f, .54f, .69f, .46f, .57f, .38f, .45f, .34f, .43f, .42f, .70f, .66f, .83f, .69f, .62f, .65f) }
    val labels = remember { listOf("01.06", "02.06", "03.06", "04.06", "05.06", "06.06", "07.06", "08.06", "09.06", "10.06", "15.06", "16.06", "17.06", "18.06", "19.06", "20.06", "21.06", "24.06", "25.06", "26.06", "27.06", "28.06", "30.06") }
    var selectedIndex by remember { mutableIntStateOf(10) }
    var chartWidth by remember { mutableStateOf(0f) }
    fun selectAt(x: Float) {
        if (chartWidth > 0f) selectedIndex = (x / chartWidth * (values.size - 1)).roundToInt().coerceIn(0, values.lastIndex)
    }
    Column {
        Row {
            Column(modifier = Modifier.height(172.dp), verticalArrangement = Arrangement.SpaceBetween) {
                listOf("200 000", "150 000", "100 000", "50 000", "0").forEach { Text(it, color = muted, style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f).height(172.dp)) {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .onSizeChanged { chartWidth = it.width.toFloat() }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { selectAt(it.x) },
                                onDrag = { change, _ -> change.consume(); selectAt(change.position.x) }
                            )
                        }
                ) {
                    val step = size.width / (values.size - 1)
                    repeat(5) { line -> drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, line * size.height / 4f), androidx.compose.ui.geometry.Offset(size.width, line * size.height / 4f)) }
                    repeat(7) { line -> drawLine(gridColor, androidx.compose.ui.geometry.Offset(line * size.width / 6f, 0f), androidx.compose.ui.geometry.Offset(line * size.width / 6f, size.height)) }
                    val linePath = Path()
                    values.forEachIndexed { index, value ->
                        val point = androidx.compose.ui.geometry.Offset(index * step, size.height * (1f - value))
                        if (index == 0) linePath.moveTo(point.x, point.y) else linePath.lineTo(point.x, point.y)
                    }
                    val fillPath = Path().apply { addPath(linePath); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                    drawPath(fillPath, Brush.verticalGradient(listOf(DashboardGreen.copy(alpha = .26f), DashboardGreen.copy(alpha = .02f))))
                    drawPath(linePath, DashboardGreen, style = Stroke(width = 6f, cap = StrokeCap.Round))
                    val marker = androidx.compose.ui.geometry.Offset(selectedIndex * step, size.height * (1f - values[selectedIndex]))
                    drawLine(DashboardGreen.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(marker.x, marker.y), androidx.compose.ui.geometry.Offset(marker.x, size.height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f)))
                    drawCircle(Color.White, 12f, marker)
                    drawCircle(DashboardGreen, 7f, marker)
                }
                Surface(modifier = Modifier.align(Alignment.TopCenter).offset(x = (-18).dp), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small, shadowElevation = 4.dp) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text("${labels[selectedIndex]}.2024", style = MaterialTheme.typography.bodySmall, color = muted); Text("● ${((values[selectedIndex] * 210_000).roundToInt() / 100) * 100} ₽", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DashboardGreen) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.padding(start = 72.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("01.06", "05.06", "09.06", "13.06", "17.06", "21.06", "25.06", "30.06").forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = muted) } }
    }
}

@Composable
private fun MetricGrid() {
    val items = listOf(
        DashboardMetric("Выручка", "487 320 ₽", "↑ 18%", Icons.Outlined.AccountBalanceWallet, DashboardGreen),
        DashboardMetric("Прибыль", "162 450 ₽", "↑ 23%", Icons.Outlined.BarChart, DashboardGreen),
        DashboardMetric("Заказы", "248", "↑ 12%", Icons.Outlined.ShoppingCart, DashboardRed),
        DashboardMetric("Средний чек", "1 965 ₽", "↑ 6%", Icons.Outlined.Group, MaterialTheme.colorScheme.onSurfaceVariant)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { metric ->
                    StoqCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(CircleShape).background(metric.tint.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(metric.icon, null, tint = metric.tint) }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(metric.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(metric.value, style = MaterialTheme.typography.titleMedium)
                                Text(metric.growth, color = DashboardGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockSection() {
    val products = listOf(
        StockProduct("Помидоры свежие", "5 кг", "72348", R.drawable.auth_tomatoes),
        StockProduct("Сыр моцарелла", "3 кг", "72349", null),
        StockProduct("Молоко 3,2%", "4 л", "72350", null)
    )
    SectionCard("Товары с низким остатком", "Все товары") {
        products.forEachIndexed { index, product ->
            if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                ProductAvatar(product.avatarRes)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(product.name, fontWeight = FontWeight.Medium); Text("Артикул: ${product.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(product.amount, color = DashboardRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PromotionSection() {
    val dishes = listOf("Стейк из лосося" to "1 490 ₽", "Салат «Цезарь» с креветками" to "790 ₽", "Чизкейк с ягодами" to "450 ₽")
    SectionCard("Блюда для продвижения", "Все блюда") {
        dishes.forEachIndexed { index, (name, price) ->
            if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ReceiptLong, null, tint = DashboardGreen)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Medium)
                    Text(if (index == 0) "Высокий спрос и прибыль" else "Рекомендуем продвигать", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) { Text(price, fontWeight = FontWeight.Bold); Text("↑ ${35 - index * 6}%", color = DashboardGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun CategoriesSection() {
    val categories = listOf("Горячее" to 16, "Салаты" to 12, "Десерты" to 8, "В стоп-листе" to 6, "Супы" to 4, "Напитки" to 2)
    val colors = listOf(DashboardGreen, DashboardBlue, DashboardPurple, DashboardRed, DashboardYellow, Color.LightGray)
    SectionCard("Количество блюд по категориям", "Все блюда") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(Modifier.size(130.dp)) {
                    var start = -90f
                    categories.forEachIndexed { index, (_, count) ->
                        val sweep = count / 48f * 360f
                        drawArc(colors[index], start, sweep - 3f, false, style = Stroke(28f))
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("48", style = MaterialTheme.typography.titleLarge); Text("всего", style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                categories.forEachIndexed { index, (name, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(colors[index])); Spacer(Modifier.width(8.dp)); Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, action: String, content: @Composable () -> Unit) {
    StoqCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); Text(action, modifier = Modifier.clip(MaterialTheme.shapes.small).clickable { }.padding(6.dp), color = DashboardGreen, style = MaterialTheme.typography.labelLarge) }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

private fun metricValue(index: Int) = listOf("487 320 ₽", "162 450 ₽", "248")[index]
private fun metricGrowth(index: Int) = listOf("18%", "23%", "12%")[index]

private data class DashboardMetric(val title: String, val value: String, val growth: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: Color)
private data class StockProduct(val name: String, val amount: String, val id: String, val avatarRes: Int?)

@Composable
private fun ProductAvatar(avatarRes: Int?) {
    Box(modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (avatarRes != null) {
            androidx.compose.foundation.Image(painterResource(avatarRes), null, modifier = Modifier.size(48.dp), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatDate(millis: Long): String = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
