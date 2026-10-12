package bob.colbaskin.stoq.feature.orders.data.remote

import bob.colbaskin.stoq.feature.orders.data.model.AddOrderItemsDto
import bob.colbaskin.stoq.feature.orders.data.model.OrderDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrdersApiService {
    @GET("orders") suspend fun getOrders(): List<OrderDto>
    @GET("orders/{id}") suspend fun getOrder(@Path("id") id: Long): OrderDto
    @POST("orders") suspend fun createOrder(): OrderDto
    @POST("orders/{id}/items") suspend fun addOrderItems(@Path("id") id: Long, @Body request: AddOrderItemsDto)
    @DELETE("orders/{id}") suspend fun deleteOrder(@Path("id") id: Long)
}
