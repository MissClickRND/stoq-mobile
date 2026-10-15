package bob.colbaskin.stoq.feature.dishes.presentation

import bob.colbaskin.stoq.feature.dashboard.data.model.DishSalesPointDto
import java.time.LocalDate

/** Smaller periods use the available server week; older sales are never fabricated. */
internal fun filterDishSalesPeriod(points: List<DishSalesPointDto>, days: Int): List<DishSalesPointDto> {
    require(days in setOf(1, 3, 7))
    val dated = points.mapNotNull { point ->
        runCatching { LocalDate.parse(point.date) }.getOrNull()?.let { it to point }
    }
    val lastDate = dated.maxOfOrNull { it.first } ?: return emptyList()
    val firstDate = lastDate.minusDays(days.toLong() - 1)
    return dated.filter { it.first >= firstDate && it.first <= lastDate }.sortedBy { it.first }.map { it.second }
}
