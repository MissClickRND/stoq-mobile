package bob.colbaskin.stoq.feature.products.data.remote

import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.ExpiringInventoryDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApiService {
    @POST("inventory/{batchId}/write-off")
    suspend fun writeOff(@Path("batchId") batchId: Long)
    @GET("inventory")
    suspend fun getInventory(@Query("search") search: String? = null): List<InventoryDto>

    @GET("inventory/{productId}")
    suspend fun getInventoryByProduct(@Path("productId") productId: Long): List<InventoryDto>

    @GET("inventory/expiring")
    suspend fun getExpiringInventory(@Query("daysThreshold") daysThreshold: Int): List<ExpiringInventoryDto>

    @PUT("inventory")
    suspend fun updateQuantity(@Body request: UpdateQuantityDto)

}
