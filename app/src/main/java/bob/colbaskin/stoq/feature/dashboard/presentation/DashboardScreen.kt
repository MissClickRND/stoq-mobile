package bob.colbaskin.stoq.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.theme.ColorTokens
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing

private val DashboardGreen = Color(0xFF22A51C)
private val DashboardBlue = Color(0xFF319BF4)
private val DashboardPurple = Color(0xFF9864E9)
private val DashboardRed = Color(0xFFFF5A68)
private val DashboardYellow = Color(0xFFFFBE35)

@Composable
fun DashboardScreen() {
    var selectedRange by remember { mutableIntStateOf(1) }
    var selectedMetric by remember { mutableIntStateOf(1) }
    val ranges = listOf("Сегодня", "7 дней", "30 дней")
    val metrics = listOf("Выручка", "Прибыль", "Заказы")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Large)
    ) {
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                Text("Дашборд", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Ключевые показатели вашего ресторана",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                AssistChip(
                    onClick = { selectedRange = 3 },
                    label = { Text("01.06.2024 – 30.06.2024") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranges.forEachIndexed { index, label ->
                        FilterChip(label, selected = selectedRange == index) { selectedRange = index }
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
        item { RecommendationBanner() }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = if (selected) ColorTokens.LightPrimaryContainer else MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier.height(42.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = background,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun MetricChart() {
    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val values = listOf(.28f, .42f, .32f, .58f, .39f, .61f, .54f, .64f, .47f, .36f, .51f, .42f, .71f, .67f, .83f, .66f, .62f)
        val step = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val point = androidx.compose.ui.geometry.Offset(index * step, size.height * (1f - value))
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        drawPath(path, DashboardGreen, style = Stroke(width = 6f, cap = StrokeCap.Round))
        values.forEachIndexed { index, value ->
            if (index % 4 == 0) drawCircle(Color(0x1A22A51C), radius = 18f, center = androidx.compose.ui.geometry.Offset(index * step, size.height * (1f - value)))
        }
    }
}

@Composable
private fun MetricGrid() {
    val items = listOf(
        Triple("Выручка", "487 320 ₽", "↑ 18%"), Triple("Прибыль", "162 450 ₽", "↑ 23%"),
        Triple("Заказы", "248", "↑ 12%"), Triple("Средний чек", "1 965 ₽", "↑ 6%")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (title, value, growth) ->
                    StoqCard(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(value, style = MaterialTheme.typography.titleLarge)
                        Text(growth, color = DashboardGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockSection() {
    val products = listOf("Помидоры свежие" to "5 кг", "Сыр моцарелла" to "3 кг", "Молоко 3,2%" to "4 л")
    SectionCard("Товары с низким остатком", "Все товары") {
        products.forEachIndexed { index, (name, amount) ->
            if (index != 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Inventory2, null, tint = DashboardRed)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.Medium); Text("Артикул 00${123 + index * 331}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(amount, color = DashboardRed, fontWeight = FontWeight.Bold)
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
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun RecommendationBanner() {
    Card(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorTokens.LightPrimaryContainer), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.TrendingUp, null, tint = DashboardGreen, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text("Получите персональные рекомендации блюд на основе ваших продаж", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionCard(title: String, action: String, content: @Composable () -> Unit) {
    StoqCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); Text(action, color = DashboardGreen, style = MaterialTheme.typography.labelLarge) }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

private fun metricValue(index: Int) = listOf("487 320 ₽", "162 450 ₽", "248")[index]
private fun metricGrowth(index: Int) = listOf("18%", "23%", "12%")[index]
