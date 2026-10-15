package bob.colbaskin.stoq.feature.dashboard.data.remote

import bob.colbaskin.stoq.feature.dashboard.data.model.DishesStatisticsDto
import bob.colbaskin.stoq.feature.dashboard.data.model.OrdersTodayStatisticsDto
import retrofit2.http.GET

interface StatisticsApiService {
    @GET("statistics/dishes")
    suspend fun getDishesStatistics(): DishesStatisticsDto

    @GET("statistics/orders/today")
    suspend fun getOrdersTodayStatistics(): OrdersTodayStatisticsDto
}
