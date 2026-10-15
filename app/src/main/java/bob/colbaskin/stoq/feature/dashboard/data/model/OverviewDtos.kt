package bob.colbaskin.stoq.feature.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatisticsDto(
    val days: Int,
    val from: String,
    val to: String,
    val points: List<DashboardPointDto>,
    val summary: DashboardSummaryDto
)

@Serializable
data class DashboardPointDto(val date: String, val label: String, val revenue: Double, val earning: Double? = null, val orders: Int)

@Serializable
data class DashboardSummaryDto(val orders: Int, val completed: Int, val revenue: Double, val earning: Double? = null, val averageCheck: Double)

@Serializable
data class StatisticsUnitDto(val shortName: String)

@Serializable
data class StatisticsProductDto(val id: Long, val name: String, val unit: StatisticsUnitDto? = null)

@Serializable
data class LowStockDto(val product: StatisticsProductDto, val quantity: Double)

@Serializable
data class InventoryStatisticsDto(
    val totalProducts: Int,
    val depletedProducts: Int,
    val expiringProducts: Int,
    val spoiledProducts: Int,
    val lowStockProducts: List<LowStockDto>
)

@Serializable
data class ShortagesDto(val trackingStartedAt: String? = null, val items: List<ShortageDto>)

@Serializable
data class ShortageDto(
    val productId: Long,
    val name: String,
    val incidents: Int,
    val lastOccurredAt: String? = null,
    val affectedDishes: Int
)

@Serializable
data class WriteOffDto(
    val batchId: Long,
    val productId: Long,
    val product: StatisticsProductDto,
    val writtenOffQuantity: Double,
    val writtenOffAt: String,
    val expiresAt: String,
    val reason: String
)

@Serializable
data class SupplyStatisticsDto(val month: String, val count: Int, val products: Int, val total: Double, val average: Double)

@Serializable
data class SupplyDetailsStatisticsDto(val id: Long, val supplyNumber: String, val receivedAt: String? = null, val positions: Int, val total: Double)

@Serializable
data class DishDetailsStatisticsDto(
    val dishId: Long,
    val today: Int,
    val week: Int,
    val popularity: Int,
    val revenue: Double,
    val earning: Double? = null,
    val availablePortions: Int,
    val costPrice: Double? = null,
    val profit: Double? = null,
    val markup: Double? = null,
    val sales: List<DishSalesPointDto>
)

@Serializable
data class DishSalesPointDto(val date: String, val label: String, val quantity: Int, val revenue: Double)

@Serializable
data class AdminStatisticsDto(val employees: Int, val invitations: Int, val requests: Int, val admins: Int)
