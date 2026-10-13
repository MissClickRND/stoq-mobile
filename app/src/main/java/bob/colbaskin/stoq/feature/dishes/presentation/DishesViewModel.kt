package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.dishes.data.RemoteDishRepository
import bob.colbaskin.stoq.feature.dishes.data.RemoteRecommendationsRepository
import bob.colbaskin.stoq.feature.dishes.data.StopListDish
import bob.colbaskin.stoq.feature.dishes.data.model.CreateDishDto
import bob.colbaskin.stoq.feature.dishes.data.model.CreateDishIngredientDto
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DishesState(
    val query: String = "",
    val category: String = "Все",
    val dishes: List<Dish> = emptyList(),
    val loadState: UiState<List<Dish>> = UiState.Loading,
    val isRefreshing: Boolean = false
)

data class CollectionFilterState(
    val query: String = "",
    val firstFilter: String,
    val category: String = "Все категории",
    val deliveryFilter: String = "Ожидаемое возвращение",
    val markupFilter: String = "Наценка",
    val ratingFilter: String = "Рейтинг"
)

@HiltViewModel
class DishesViewModel @Inject constructor(
    private val repository: RemoteDishRepository,
    private val recommendationsRepository: RemoteRecommendationsRepository,
    private val productRepository: RemoteProductRepository
) : ViewModel() {
    var allDishes by mutableStateOf<List<Dish>>(emptyList())
        private set
    var state by mutableStateOf(DishesState())
        private set
    var products by mutableStateOf<List<ProductDto>>(emptyList())
        private set
    var stopListDishes by mutableStateOf<List<Dish>>(emptyList())
        private set
    var stopListState by mutableStateOf<UiState<List<StopListDish>>>(UiState.Loading)
        private set
    private var stopList = emptyList<StopListDish>()
    private var stopListFilterState by mutableStateOf(CollectionFilterState(firstFilter = "Любая причина"))
    private var recommendationsFilterState by mutableStateOf(CollectionFilterState(firstFilter = "Срочность"))
    private var searchJob: Job? = null

    init {
        refresh()
        refreshProducts()
    }

    fun refresh() = viewModelScope.launch {
        val hasData = allDishes.isNotEmpty()
        state = if (hasData) state.copy(isRefreshing = true) else state.copy(loadState = UiState.Loading)
        when (val result = repository.getDishes()) {
            is UiState.Success -> {
                allDishes = result.data
                rebuildStopListDishes()
                state = state.copy(dishes = state.filtered(result.data), loadState = result, isRefreshing = false)
            }
            is UiState.Error -> state = state.copy(dishes = if (hasData) state.dishes else emptyList(), loadState = if (hasData) state.loadState else result, isRefreshing = false)
            UiState.Loading -> Unit
        }
    }

    fun updateQuery(query: String) {
        val updated = state.copy(query = query)
        state = updated.copy(dishes = updated.filtered())
        searchDishes(query)
    }

    private fun searchDishes(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            when (val result = repository.getDishes(query)) {
                is UiState.Success -> {
                    if (query.isBlank()) allDishes = result.data
                    state = state.copy(dishes = state.filtered(result.data))
                }
                else -> Unit
            }
        }
    }

    fun selectCategory(category: String) {
        val updated = state.copy(category = category)
        state = updated.copy(dishes = updated.filtered())
    }

    fun loadStopList() = viewModelScope.launch {
        stopListState = UiState.Loading
        when (val result = recommendationsRepository.getStopList()) {
            is UiState.Success -> {
                stopList = result.data
                rebuildStopListDishes()
                stopListState = result
            }
            is UiState.Error -> stopListState = result
            UiState.Loading -> Unit
        }
    }

    fun stopListReason(dishId: String): String = stopList.firstOrNull { it.dishId == dishId }?.reason ?: "мог"
    fun stopListReasonTitle(dishId: String): String = stopList.firstOrNull { it.dishId == dishId }?.reasonTitle ?: "мог"

    private fun rebuildStopListDishes() {
        val dishesById = allDishes.associateBy(Dish::id)
        stopListDishes = stopList.mapNotNull { item -> dishesById[item.dishId]?.copy(active = false) }
    }
    fun collectionFilters(recommended: Boolean) = if (recommended) recommendationsFilterState else stopListFilterState
    fun updateCollectionFilters(recommended: Boolean, update: (CollectionFilterState) -> CollectionFilterState) {
        if (recommended) recommendationsFilterState = update(recommendationsFilterState)
        else stopListFilterState = update(stopListFilterState)
    }
    fun addDish(
        name: String,
        description: String,
        salePrice: String,
        ingredients: List<Pair<Long, Double>>
    ) = viewModelScope.launch {
        if (name.isBlank() || description.isBlank() || salePrice.toDoubleOrNull() == null || ingredients.isEmpty()) return@launch
        when (repository.createDish(CreateDishDto(
            name = name.trim(),
            description = description.trim(),
            salePrice = salePrice.toDouble(),
            recipeItems = ingredients.map { (productId, quantity) -> CreateDishIngredientDto(productId, quantity) }
        ))) {
            is UiState.Success -> refresh()
            is UiState.Error -> Unit
            UiState.Loading -> Unit
        }
    }

    private fun refreshProducts() = viewModelScope.launch {
        when (val result = productRepository.getProducts()) {
            is UiState.Success -> products = result.data
            else -> Unit
        }
    }

    private fun DishesState.filtered(source: List<Dish> = allDishes) = source.filter { dish ->
        (category == "Все" || if (category == "В стоп-листе") !dish.active else dish.category == category) &&
            (query.isBlank() || dish.name.contains(query, ignoreCase = true) || dish.description.orEmpty().contains(query, ignoreCase = true))
    }

}

@HiltViewModel
class DishDetailsViewModel @Inject constructor(
    private val repository: RemoteDishRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<Dish>>(UiState.Loading)
        private set
    var deleteState by mutableStateOf<UiState<Unit>?>(null)
        private set

    fun load(id: String) = viewModelScope.launch {
        val numericId = id.toLongOrNull()
        state = if (numericId == null) UiState.Error("Некорректный идентификатор", "Блюдо не удалось загрузить.") else UiState.Loading
        if (numericId != null) state = repository.getDish(numericId)
    }

    fun delete(id: String, onSuccess: () -> Unit) = viewModelScope.launch {
        val numericId = id.toLongOrNull() ?: return@launch
        deleteState = UiState.Loading
        deleteState = repository.deleteDish(numericId)
        if (deleteState is UiState.Success) onSuccess()
    }
}
