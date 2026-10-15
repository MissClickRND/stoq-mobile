package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.feature.dashboard.data.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test

class OverviewContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun dashboardKeepsMoneyPrecisionAndServerTotals() {
        val dto = json.decodeFromString<DashboardStatisticsDto>("""{"days":7,"timezone":"Europe/Moscow","from":"2026-10-08T21:00:00Z","to":"2026-10-15T18:00:00Z","points":[{"date":"2026-10-15","label":"15 окт.","revenue":26250,"orders":7,"earning":23448.55}],"summary":{"orders":7,"completed":3,"revenue":26250,"earning":23448.55,"averageCheck":8750}}""")
        assertEquals(23448.55, dto.summary.earning!!, 0.001)
        assertEquals(7, dto.summary.orders)
        assertEquals(3, dto.summary.completed)
    }

    @Test fun expiredCountDoesNotDependOnVisibleStockList() {
        val dto = json.decodeFromString<InventoryStatisticsDto>("""{"totalProducts":6,"depletedProducts":0,"expiringProducts":2,"spoiledProducts":2,"lowStockProducts":[]}""")
        assertEquals(2, dto.spoiledProducts)
        assertTrue(dto.lowStockProducts.isEmpty())
    }

    @Test fun writeOffKeepsBatchReasonAndFractionalQuantity() {
        val dto = json.decodeFromString<List<WriteOffDto>>("""[{"batchId":8,"productId":5,"product":{"id":5,"name":"Моцарелла","unit":{"id":1,"shortName":"кг"}},"writtenOffQuantity":0.3,"writtenOffAt":"2026-10-15T17:07:00.011Z","expiresAt":"2026-10-15T17:01:45.895Z","reason":"EXPIRED"}]""").single()
        assertEquals(8L, dto.batchId)
        assertEquals(0.3, dto.writtenOffQuantity, 0.001)
        assertEquals("EXPIRED", dto.reason)
    }

    @Test fun shortagesReadServerIncidentsWithoutAccumulating() {
        val payload = """{"trackingStartedAt":"2026-10-15T18:01:19.622Z","items":[{"productId":4,"name":"Томаты","incidents":2,"affectedDishes":2}]}"""
        repeat(3) { assertEquals(2, json.decodeFromString<ShortagesDto>(payload).items.single().incidents) }
    }

    @Test fun nullProfitDoesNotDiscardRevenueOrdersOrChart() {
        val dto = json.decodeFromString<DashboardStatisticsDto>("""{"days":14,"from":"2026-10-01T21:00:00Z","to":"2026-10-15T18:53:00Z","points":[{"date":"2026-10-14","label":"14 окт.","revenue":0,"orders":0,"earning":0},{"date":"2026-10-15","label":"15 окт.","revenue":26250,"orders":8,"earning":null}],"summary":{"orders":8,"completed":3,"revenue":26250,"earning":null,"averageCheck":8750}}""")
        assertEquals(26250.0, dto.summary.revenue, 0.001)
        assertEquals(8, dto.summary.orders)
        assertNull(dto.summary.earning)
        assertNull(dto.points.last().earning)
        assertEquals(0.0, dto.points.first().earning!!, 0.001)
    }

    @Test fun dishWithUnknownCostsStillShowsSalesStatistics() {
        val dto = json.decodeFromString<DishDetailsStatisticsDto>("""{"dishId":3,"today":20,"week":20,"popularity":2,"sales":[{"date":"2026-10-15","label":"15 окт.","quantity":20,"revenue":5000}],"revenue":5000,"ingredientCost":null,"earning":null,"availablePortions":0,"status":"STOP_LIST","costPrice":null,"profit":null,"markup":null}""")
        assertEquals(20, dto.today)
        assertEquals(5000.0, dto.revenue, 0.001)
        assertEquals(20, dto.sales.single().quantity)
        assertEquals(0, dto.availablePortions)
        assertNull(dto.costPrice)
        assertNull(dto.profit)
        assertNull(dto.markup)
    }
}
