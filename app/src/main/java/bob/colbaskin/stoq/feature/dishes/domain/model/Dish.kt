package bob.colbaskin.stoq.feature.dishes.domain.model

data class Dish(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Int,
    val cost: Int,
    val imageUrl: String,
    val active: Boolean,
    val ingredients: List<RecipeItem>,
    val soldToday: Int,
    val weeklyRevenue: Int,
    val popularity: Int,
    val recipeLoaded: Boolean = true
) {
    val markup: Int get() = if (price < 0 || cost <= 0) -1 else ((price - cost) * 100 / cost)
}

data class RecipeItem(
    val name: String,
    val amount: String,
    val stock: String,
    val status: IngredientStockStatus
)

enum class IngredientStockStatus { Available, RunningLow, Unknown }
