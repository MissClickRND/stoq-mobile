package bob.colbaskin.stoq.feature.dishes.presentation

import bob.colbaskin.stoq.feature.dashboard.data.model.DishSalesPointDto
import org.junit.Assert.*
import org.junit.Test

class DishSalesPeriodTest {
    private val week = (9..15).map { DishSalesPointDto("2026-10-$it".replace("-9", "-09"), "$it окт.", it, it * 250.0) }

    @Test fun selectingPeriodChangesVisibleDaysAndTotals() {
        assertEquals(listOf(15), filterDishSalesPeriod(week, 1).map { it.quantity })
        assertEquals(listOf(13, 14, 15), filterDishSalesPeriod(week, 3).map { it.quantity })
        assertEquals(7, filterDishSalesPeriod(week, 7).size)
        assertEquals(42, filterDishSalesPeriod(week, 3).sumOf { it.quantity })
    }

    @Test fun missingDaysAreNotReplacedByOlderRowsOrInventedZeros() {
        val sparse = listOf(week.first(), week.last())
        assertEquals(listOf(week.last()), filterDishSalesPeriod(sparse, 3))
    }

    @Test fun emptyAndUnsortedResponsesAreHandled() {
        assertTrue(filterDishSalesPeriod(emptyList(), 7).isEmpty())
        assertEquals(week.takeLast(3), filterDishSalesPeriod(week.reversed(), 3))
    }
}
