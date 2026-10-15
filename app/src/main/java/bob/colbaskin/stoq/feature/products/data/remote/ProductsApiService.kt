package bob.colbaskin.stoq.feature.products.data.remote

import bob.colbaskin.stoq.feature.products.data.model.CreateProductDto
import bob.colbaskin.stoq.feature.products.data.model.CreateUnitDto
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.products.data.model.ProductUnitDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductsApiService {
    @GET("product") suspend fun getProducts(@Query("search") search: String? = null): List<ProductDto>
    @GET("product/{id}") suspend fun getProduct(@Path("id") id: Long): ProductDto
    @POST("product") suspend fun createProduct(@Body request: CreateProductDto)
    @POST("product/few") suspend fun createProducts(@Body request: List<CreateProductDto>)
    @DELETE("product/{id}") suspend fun deleteProduct(@Path("id") id: Long)
    @GET("product/units") suspend fun getUnits(): List<ProductUnitDto>
    @GET("product/units/{id}") suspend fun getUnit(@Path("id") id: Long): ProductUnitDto
    @POST("product/units") suspend fun createUnit(@Body request: CreateUnitDto)
}
