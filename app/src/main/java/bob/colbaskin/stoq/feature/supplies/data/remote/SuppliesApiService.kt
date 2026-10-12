package bob.colbaskin.stoq.feature.supplies.data.remote

import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.SupplyDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SuppliesApiService {
    @GET("supplies") suspend fun getSupplies(): List<SupplyDto>
    @GET("supplies/{id}") suspend fun getSupply(@Path("id") id: Long): SupplyDto
    @POST("supplies") suspend fun createSupply(@Body request: CreateSupplyDto)
}
