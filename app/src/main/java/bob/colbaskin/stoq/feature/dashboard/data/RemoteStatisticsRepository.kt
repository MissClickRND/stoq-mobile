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
import bob.colbaskin.stoq.feature.dashboard.data.model.*

@Singleton
class RemoteStatisticsRepository @Inject constructor(
    private val api: StatisticsApiService
) {
    suspend fun dashboard(days: Int): UiState<DashboardStatisticsDto> = safeApiCall(apiCall = { api.dashboard(days) }, successHandler = { it }).toUiState()
    suspend fun shortages(days: Int): UiState<ShortagesDto> = safeApiCall(apiCall = { api.shortages(days) }, successHandler = { it }).toUiState()
    suspend fun inventory(): UiState<InventoryStatisticsDto> = safeApiCall(apiCall = api::inventory, successHandler = { it }).toUiState()
    suspend fun writeOffs(): UiState<List<WriteOffDto>> = safeApiCall(apiCall = api::writeOffs, successHandler = { it }).toUiState()
    suspend fun supplies(): UiState<SupplyStatisticsDto> = safeApiCall(apiCall = api::supplies, successHandler = { it }).toUiState()
    suspend fun supply(id: Long): UiState<SupplyDetailsStatisticsDto> = safeApiCall(apiCall = { api.supply(id) }, successHandler = { it }).toUiState()
    suspend fun dish(id: Long): UiState<DishDetailsStatisticsDto> = safeApiCall(apiCall = { api.dish(id) }, successHandler = { it }).toUiState()
    suspend fun admin(): UiState<AdminStatisticsDto> = safeApiCall(apiCall = api::admin, successHandler = { it }).toUiState()

    suspend fun dishes(): UiState<DishesStatistics> = safeApiCall(
        apiCall = api::getDishesStatistics,
        successHandler = { it.toStatistics() }
    ).toUiState()

    suspend fun ordersToday(): UiState<OrdersTodayStatistics> = safeApiCall(
        apiCall = api::getOrdersTodayStatistics,
        successHandler = { it.toStatistics() }
    ).toUiState()
}
