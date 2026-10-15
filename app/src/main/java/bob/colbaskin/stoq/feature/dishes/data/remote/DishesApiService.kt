package bob.colbaskin.stoq.feature.dishes.data.remote

import bob.colbaskin.stoq.feature.dishes.data.model.CreateDishDto
import bob.colbaskin.stoq.feature.dishes.data.model.DishDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface DishesApiService {
    @GET("dishes")
    suspend fun getDishes(@Query("search") search: String? = null): List<DishDto>

    @GET("dishes/{id}")
    suspend fun getDish(@Path("id") dishId: Long): DishDto

    @POST("dishes")
    suspend fun createDish(@Body request: CreateDishDto)

    @DELETE("dishes/{id}")
    suspend fun deleteDish(@Path("id") dishId: Long)
}
