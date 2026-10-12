package bob.colbaskin.stoq.feature.dishes.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.dishes.data.model.CreateDishDto
import bob.colbaskin.stoq.feature.dishes.data.model.DishDto
import bob.colbaskin.stoq.feature.dishes.data.model.DishIngredientDto
import bob.colbaskin.stoq.feature.dishes.data.remote.DishesApiService
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.dishes.domain.model.RecipeItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDishRepository @Inject constructor(
    private val api: DishesApiService
) {
    suspend fun getDishes(): UiState<List<Dish>> = safeApiCall(
        apiCall = api::getDishes,
        successHandler = { items -> items.filterNot(DishDto::isDeleted).map(DishDto::toDish) }
    ).toUiState()

    suspend fun getDish(id: Long): UiState<Dish> = safeApiCall(
        apiCall = { api.getDish(id) },
        successHandler = DishDto::toDish
    ).toUiState()

    suspend fun createDish(request: CreateDishDto): UiState<Unit> = safeApiCall(
        apiCall = { api.createDish(request) },
        successHandler = { Unit }
    ).toUiState()
}

private fun DishDto.toDish() = Dish(
    id = id.toString(), name = name, description = description, category = "мог", price = salePrice?.toInt() ?: -1, cost = -1,
    imageUrl = imageURL.orEmpty(), active = !isDeleted, ingredients = recipeItems.map(DishIngredientDto::toRecipeItem), soldToday = -1,
    weeklyRevenue = -1, popularity = -1, recipeLoaded = true
)

private fun DishIngredientDto.toRecipeItem(): RecipeItem {
    val quantity = if (quantity % 1.0 == 0.0) quantity.toLong().toString() else quantity.toString()
    val unit = product?.unit?.shortName.orEmpty()
    return RecipeItem(
        name = product?.name ?: "Товар #$productId",
        amount = listOf(quantity, unit).filter(String::isNotBlank).joinToString(" "),
        stock = "мог",
        status = bob.colbaskin.stoq.feature.dishes.domain.model.IngredientStockStatus.Unknown
    )
}
