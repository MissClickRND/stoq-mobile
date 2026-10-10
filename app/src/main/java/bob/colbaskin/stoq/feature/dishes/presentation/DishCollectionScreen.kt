package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import coil.compose.AsyncImage

@Composable
fun DishCollectionScreen(recommended: Boolean, onBack: () -> Unit, onDishClick: (String) -> Unit, viewModel: DishesViewModel = viewModel()) {
    val colors = LocalColors.current
    val dishes = if (recommended) viewModel.allDishes.filter { it.active } else viewModel.allDishes.filterNot { it.active }
    val title = if (recommended) "Рекомендуемые блюда" else "Стоп-лист блюд"
    val description = if (recommended) "Блюда для приоритетной продажи на основе остатков, сроков годности и маржи." else "Блюда, которые сейчас нельзя заказать из-за отсутствия или срока годности ингредиентов."
    LazyColumn(Modifier.fillMaxSize().background(colors.background), contentPadding = PaddingValues(start = StoqSpacing.ScreenHorizontal, top = 8.dp, end = StoqSpacing.ScreenHorizontal, bottom = StoqSpacing.ScreenHorizontal), verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)) {
        item {
            Row(Modifier.padding(vertical = StoqSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(28.dp).clickable(onClick = onBack), tint = colors.textPrimary)
                Spacer(Modifier.width(StoqSpacing.Medium)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            Spacer(Modifier.height(StoqSpacing.Medium))
            CollectionSummary(recommended, dishes.size)
        }
        items(dishes, key = Dish::id) { dish -> CollectionDishCard(dish, recommended) { onDishClick(dish.id) } }
    }
}

@Composable private fun CollectionSummary(recommended: Boolean, count: Int) { val colors = LocalColors.current; Card(colors = CardDefaults.cardColors(containerColor = if (recommended) colors.primary.copy(alpha = .12f) else colors.error.copy(alpha = .1f)), shape = MaterialTheme.shapes.medium) { Row(Modifier.padding(StoqSpacing.Large), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).background((if (recommended) colors.success else colors.error).copy(alpha = .15f), CircleShape), contentAlignment = Alignment.Center) { Icon(if (recommended) Icons.Outlined.Inventory2 else Icons.Outlined.WarningAmber, null, tint = if (recommended) colors.success else colors.error) }; Spacer(Modifier.width(StoqSpacing.Medium)); Column { Text(if (recommended) "Рекомендовано к продаже" else "Сейчас недоступно", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary); Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary) } } } }

@Composable private fun CollectionDishCard(dish: Dish, recommended: Boolean, onClick: () -> Unit) { val colors = LocalColors.current; val accent = if (recommended) colors.success else colors.error; Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = colors.surface), shape = MaterialTheme.shapes.medium) { Row(Modifier.padding(StoqSpacing.Medium)) { AsyncImage(dish.imageUrl, dish.name, Modifier.size(88.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop); Spacer(Modifier.width(StoqSpacing.Medium)); Column(Modifier.weight(1f)) { Text(dish.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary); Text(dish.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(7.dp)); Text(if (recommended) "Высокая маржа и актуальные остатки" else "Требуется пополнение ингредиентов", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold) } } } }
