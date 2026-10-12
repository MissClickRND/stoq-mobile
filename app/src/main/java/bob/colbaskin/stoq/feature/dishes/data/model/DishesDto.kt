package bob.colbaskin.stoq.feature.dishes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DishDto(
    val id: Long,
    val name: String,
    val description: String = "мог",
    val imageURL: String? = null,
    val salePrice: Int? = null,
    val isDeleted: Boolean = false,
    val recipeItems: List<RecipeItemDto> = emptyList()
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
data class RecipeItemDto(
    val productId: Long,
    val quantity: Double,
    val product: RecipeProductDto? = null
)

@Serializable
data class RecipeDto(
    val id: Long,
    val name: String,
    val description: String = "мог",
    val imageURL: String? = null,
    val salePrice: Int? = null,
    val isDeleted: Boolean = false,
    val recipeItems: List<RecipeItemDto> = emptyList()
)

@Serializable
data class CreateDishDto(val name: String)
