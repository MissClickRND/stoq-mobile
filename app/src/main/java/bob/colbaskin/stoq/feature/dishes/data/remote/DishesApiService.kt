package bob.colbaskin.stoq.feature.dishes.data.remote

import bob.colbaskin.stoq.feature.dishes.data.model.CreateDishDto
import bob.colbaskin.stoq.feature.dishes.data.model.DishDto
import bob.colbaskin.stoq.feature.dishes.data.model.RecipeDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST

interface DishesApiService {
    @GET("dishes")
    suspend fun getDishes(): List<DishDto>

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") dishId: Long): RecipeDto

    @POST("dishes")
    suspend fun createDish(@Body request: CreateDishDto)
}
