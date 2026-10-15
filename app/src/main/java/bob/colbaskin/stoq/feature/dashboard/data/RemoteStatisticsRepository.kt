package bob.colbaskin.stoq.feature.dashboard.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.dashboard.data.model.DishesStatistics
import bob.colbaskin.stoq.feature.dashboard.data.model.OrdersTodayStatistics
import bob.colbaskin.stoq.feature.dashboard.data.model.toStatistics
import bob.colbaskin.stoq.feature.dashboard.data.remote.StatisticsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteStatisticsRepository @Inject constructor(
    private val api: StatisticsApiService
) {
    suspend fun dishes(): UiState<DishesStatistics> = safeApiCall(
        apiCall = api::getDishesStatistics,
        successHandler = { it.toStatistics() }
    ).toUiState()

    suspend fun ordersToday(): UiState<OrdersTodayStatistics> = safeApiCall(
        apiCall = api::getOrdersTodayStatistics,
        successHandler = { it.toStatistics() }
    ).toUiState()
}
