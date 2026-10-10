package bob.colbaskin.stoq.feature.dishes.domain

import bob.colbaskin.stoq.feature.dishes.domain.model.Dish

interface DishRepository {
    fun getDishes(): List<Dish>
    fun getDish(id: String): Dish?
    fun addDish(dish: Dish)
}
