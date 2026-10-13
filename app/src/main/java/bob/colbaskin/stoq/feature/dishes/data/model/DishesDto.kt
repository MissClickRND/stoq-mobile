package bob.colbaskin.stoq.feature.dishes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DishDto(
    val id: Long,
    val name: String,
    val category: String? = null,
    val description: String = "мог",
    val imageURL: String? = null,
    val salePrice: Double? = null,
    val costPrice: Double? = null,
    val availablePortions: Double? = null,
    val isDeleted: Boolean = false,
    val recipeItems: List<DishIngredientDto> = emptyList()
)

@Serializable
data class ProductUnitDto(
    val shortName: String? = null
)

@Serializable
data class RecipeProductDto(
    val name: String? = null,
    val unit: ProductUnitDto? = null
)

@Serializable
data class DishIngredientDto(
    val productId: Long,
    val quantity: Double,
    val product: RecipeProductDto? = null,
    val price: Double? = null,
    val stock: Double? = null,
    val portions: Double? = null,
    val expiresAt: String? = null
)

@Serializable
data class CreateDishDto(
    val name: String,
    val category: String? = null,
    val description: String,
    val imageURL: String? = null,
    val salePrice: Double,
    val recipeItems: List<CreateDishIngredientDto>
)

@Serializable
data class CreateDishIngredientDto(
    val productId: Long,
    val quantity: Double
)
