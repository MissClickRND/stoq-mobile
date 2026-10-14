package bob.colbaskin.stoq.feature.dashboard.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class DishesStatisticsDto(
    val total: Int = 0,
    val active: Int = 0,
    val lowStock: Int = 0,
    val stopList: Int = 0
)

@Serializable
data class OrdersTodayStatisticsDto(
    val total: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0,
    val active: Int = 0,
    val revenue: JsonElement? = null
)

data class DishesStatistics(
    val total: Int,
    val active: Int,
    val lowStock: Int,
    val stopList: Int
)

data class OrdersTodayStatistics(
    val total: Int,
    val completed: Int,
    val cancelled: Int,
    val active: Int,
    val revenue: Int
)

fun DishesStatisticsDto.toStatistics() = DishesStatistics(total, active, lowStock, stopList)

fun OrdersTodayStatisticsDto.toStatistics() = OrdersTodayStatistics(
    total = total,
    completed = completed,
    cancelled = cancelled,
    active = active,
    revenue = revenue?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()?.toInt() ?: 0
)
