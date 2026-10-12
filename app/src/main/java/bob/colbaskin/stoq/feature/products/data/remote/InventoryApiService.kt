package bob.colbaskin.stoq.feature.products.data.remote

import bob.colbaskin.stoq.feature.products.data.model.InventoryDto
import bob.colbaskin.stoq.feature.products.data.model.InventoryProductBatchDto
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.products.data.model.UpdateQuantityDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface InventoryApiService {
    @GET("inventory")
    suspend fun getInventory(): List<InventoryDto>

    @GET("inventory/{productId}")
    suspend fun getInventoryByProduct(@Path("productId") productId: Long): List<InventoryProductBatchDto>

    @PUT("inventory")
    suspend fun updateQuantity(@Body request: UpdateQuantityDto)

    @GET("product")
    suspend fun getProducts(): List<ProductDto>
}
