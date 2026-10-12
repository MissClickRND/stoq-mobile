package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.dishes.data.RemoteDishRepository
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DishesState(
    val query: String = "",
    val category: String = "Все",
    val dishes: List<Dish> = emptyList(),
    val loadState: UiState<List<Dish>> = UiState.Loading
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
    private val repository: RemoteDishRepository
) : ViewModel() {
    var allDishes by mutableStateOf<List<Dish>>(emptyList())
        private set
    var state by mutableStateOf(DishesState())
        private set
    private var stopListFilterState by mutableStateOf(CollectionFilterState(firstFilter = "Любая причина"))
    private var recommendationsFilterState by mutableStateOf(CollectionFilterState(firstFilter = "Срочность"))

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        state = state.copy(loadState = UiState.Loading)
        when (val result = repository.getDishes()) {
            is UiState.Success -> {
                allDishes = result.data
                state = state.copy(dishes = state.filtered(result.data), loadState = result)
            }
            is UiState.Error -> state = state.copy(dishes = emptyList(), loadState = result)
            UiState.Loading -> Unit
        }
    }

    fun updateQuery(query: String) { state = state.copy(query = query, dishes = state.filtered()) }
    fun selectCategory(category: String) { state = state.copy(category = category, dishes = state.filtered()) }
    fun collectionFilters(recommended: Boolean) = if (recommended) recommendationsFilterState else stopListFilterState
    fun updateCollectionFilters(recommended: Boolean, update: (CollectionFilterState) -> CollectionFilterState) {
        if (recommended) recommendationsFilterState = update(recommendationsFilterState)
        else stopListFilterState = update(stopListFilterState)
    }
    fun addDish(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        when (repository.createDish(name)) {
            is UiState.Success -> refresh()
            is UiState.Error -> Unit
            UiState.Loading -> Unit
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

    fun load(id: String) = viewModelScope.launch {
        val numericId = id.toLongOrNull()
        state = if (numericId == null) UiState.Error("Некорректный идентификатор", "Блюдо не удалось загрузить.") else UiState.Loading
        if (numericId != null) state = repository.getDish(numericId)
    }
}
