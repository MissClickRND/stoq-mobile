package bob.colbaskin.stoq.feature.dishes.data.remote

import bob.colbaskin.stoq.feature.dishes.data.model.StopListDishDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendationsApiService {
    @GET("recommendations")
    suspend fun getStopList(@Query("search") search: String? = null): List<StopListDishDto>
}
