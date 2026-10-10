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
    var allDishes = repository.getDishes()
        private set
    var state by mutableStateOf(DishesState(dishes = allDishes))
        private set

    fun updateQuery(query: String) { state = state.copy(query = query).filtered() }
    fun selectCategory(category: String) { state = state.copy(category = category).filtered() }
    fun dish(id: String) = repository.getDish(id)
    fun addDish(name: String, price: Int) {
        repository.addDish(Dish("custom-${System.currentTimeMillis()}", name, "Новое блюдо", "Горячее", price, (price * .35).toInt(), "", true, emptyList(), 0, 0, 0))
        allDishes = repository.getDishes()
        state = state.copy(category = "Все", query = "", dishes = allDishes)
    }

    private fun DishesState.filtered() = copy(dishes = allDishes.filter { dish ->
        (category == "Все" || if (category == "В стоп-листе") !dish.active else dish.category == category) &&
            (query.isBlank() || dish.name.contains(query, ignoreCase = true) || dish.description.contains(query, ignoreCase = true))
    })
}
