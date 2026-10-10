package bob.colbaskin.stoq.feature.dishes.presentation

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bob.colbaskin.stoq.feature.dishes.data.MockDishRepository
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish

data class DishesState(val query: String = "", val category: String = "Все", val dishes: List<Dish> = emptyList())

class DishesViewModel : ViewModel() {
    private val repository = MockDishRepository()
    val allDishes = repository.getDishes()
    var state by mutableStateOf(DishesState(dishes = allDishes))
        private set

    fun updateQuery(query: String) { state = state.copy(query = query).filtered() }
    fun selectCategory(category: String) { state = state.copy(category = category).filtered() }
    fun dish(id: String) = repository.getDish(id)

    private fun DishesState.filtered() = copy(dishes = allDishes.filter { dish ->
        (category == "Все" || if (category == "В стоп-листе") !dish.active else dish.category == category) &&
            (query.isBlank() || dish.name.contains(query, ignoreCase = true) || dish.description.contains(query, ignoreCase = true))
    })
}
