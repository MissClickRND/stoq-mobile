package bob.colbaskin.stoq.feature.dashboard.data.remote

import bob.colbaskin.stoq.feature.dashboard.data.model.DishesStatisticsDto
import bob.colbaskin.stoq.feature.dashboard.data.model.OrdersTodayStatisticsDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import bob.colbaskin.stoq.feature.dashboard.data.model.*

interface StatisticsApiService {
    @GET("statistics/dashboard")
    suspend fun dashboard(@Query("days") days: Int): DashboardStatisticsDto

    @GET("statistics/shortages")
    suspend fun shortages(@Query("days") days: Int): ShortagesDto

    @GET("inventory/statistics")
    suspend fun inventory(): InventoryStatisticsDto

    @GET("inventory/write-offs")
    suspend fun writeOffs(): List<WriteOffDto>

    @GET("statistics/supplies")
    suspend fun supplies(): SupplyStatisticsDto

    @GET("statistics/supplies/{id}")
    suspend fun supply(@Path("id") id: Long): SupplyDetailsStatisticsDto

    @GET("statistics/dishes/{id}")
    suspend fun dish(@Path("id") id: Long): DishDetailsStatisticsDto

    @GET("auth/admin/statistics")
    suspend fun admin(): AdminStatisticsDto

    @GET("statistics/dishes")
    suspend fun getDishesStatistics(): DishesStatisticsDto

    @GET("statistics/orders/today")
    suspend fun getOrdersTodayStatistics(): OrdersTodayStatisticsDto
}
