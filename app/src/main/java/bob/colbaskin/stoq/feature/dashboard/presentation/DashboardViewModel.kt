package bob.colbaskin.stoq.feature.dashboard.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import bob.colbaskin.stoq.feature.dashboard.data.RemoteStatisticsRepository
import bob.colbaskin.stoq.feature.dashboard.data.model.*
import bob.colbaskin.stoq.feature.dishes.data.RemoteDishRepository
import bob.colbaskin.stoq.feature.dishes.data.RemoteRecommendationsRepository
import bob.colbaskin.stoq.feature.dishes.data.RecommendedDish
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val inventoryRepository: RemoteInventoryRepository,
    private val statistics: RemoteStatisticsRepository,
    private val dishes: RemoteDishRepository,
    private val recommendations: RemoteRecommendationsRepository
) : ViewModel() {
    var overview by mutableStateOf<UiState<DashboardStatisticsDto>>(UiState.Loading)
        private set
    var shortages by mutableStateOf<UiState<ShortagesDto>>(UiState.Loading)
        private set
    var stock by mutableStateOf<UiState<InventoryStatisticsDto>>(UiState.Loading)
        private set
    var categories by mutableStateOf<UiState<List<Dish>>>(UiState.Loading)
        private set
    var promotion by mutableStateOf<UiState<List<RecommendedDish>>>(UiState.Loading)
        private set
    private var periodJob: Job? = null
    private var currentDays = 14
    private var currentExpiringDays = 7
    private var refreshJob: Job? = null
    var isRefreshing by mutableStateOf(false)
        private set

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            isRefreshing = true
            try {
                coroutineScope {
                    launch { loadPeriod(currentDays).join() }
                    launch { stock = statistics.inventory() }
                    launch { categories = dishes.getDishes() }
                    launch { promotion = recommendations.getDishRecommendations() }
                    launch { expiringInventoryState = inventoryRepository.getExpiringInventory(currentExpiringDays) }
                }
            } finally { isRefreshing = false }
        }
    }

    fun selectPeriod(days: Int) {
        if (days != currentDays) loadPeriod(days)
    }

    fun loadPeriod(days: Int): Job {
        currentDays = days
        periodJob?.cancel()
        periodJob = viewModelScope.launch {
            overview = UiState.Loading
            shortages = UiState.Loading
            launch { overview = statistics.dashboard(days) }
            launch { shortages = statistics.shortages(days) }
        }
        return periodJob!!
    }

    var expiringInventoryState by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set

    fun loadExpiringInventory(daysThreshold: Int) = viewModelScope.launch {
        currentExpiringDays = daysThreshold.coerceAtLeast(1)
        expiringInventoryState = UiState.Loading
        expiringInventoryState = inventoryRepository.getExpiringInventory(daysThreshold.coerceAtLeast(1))
    }

    fun openSupplyForBatch(productId: Long, batchId: Long, onSupplyFound: (Long) -> Unit) = viewModelScope.launch {
        val batch = (inventoryRepository.getInventoryByProduct(productId) as? UiState.Success)
            ?.data
            ?.firstOrNull { it.batchId == batchId }
        batch?.supplyId?.let(onSupplyFound)
    }
}
