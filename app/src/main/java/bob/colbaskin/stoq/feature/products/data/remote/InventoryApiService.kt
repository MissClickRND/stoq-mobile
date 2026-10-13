package bob.colbaskin.stoq.feature.products.data.remote

import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApiService {
    @GET("inventory")
    suspend fun getInventory(@Query("search") search: String? = null): List<InventoryDto>

    @GET("inventory/{productId}")
    suspend fun getInventoryByProduct(@Path("productId") productId: Long): List<InventoryDto>

    @PUT("inventory")
    suspend fun updateQuantity(@Body request: UpdateQuantityDto)

}
