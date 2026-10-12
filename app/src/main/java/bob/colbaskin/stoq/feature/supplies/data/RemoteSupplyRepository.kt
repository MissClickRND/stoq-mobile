package bob.colbaskin.stoq.feature.supplies.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.SupplyDto
import bob.colbaskin.stoq.feature.supplies.data.remote.SuppliesApiService
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

data class Supply(
    val id: Long,
    val number: String,
    val status: String,
    val receivedAt: String?,
    val createdAt: String?,
    val items: List<SupplyItem>
)

data class SupplyItem(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val expiresAt: String?
)

@Singleton
class RemoteSupplyRepository @Inject constructor(
    private val api: SuppliesApiService,
    private val productRepository: RemoteProductRepository
) {
    suspend fun getSupplies(): UiState<List<Supply>> = safeApiCall(
        apiCall = {
            val products = when (val result = productRepository.getProducts()) {
                is UiState.Success -> result.data.associateBy { it.id }
                else -> emptyMap()
            }
            api.getSupplies().map { it.toSupply(products) }
        },
        successHandler = { it }
    ).toUiState()

    suspend fun getSupply(id: Long): UiState<Supply> = safeApiCall(
        apiCall = {
            val products = when (val result = productRepository.getProducts()) {
                is UiState.Success -> result.data.associateBy { it.id }
                else -> emptyMap()
            }
            api.getSupply(id).toSupply(products)
        },
        successHandler = { it }
    ).toUiState()

    suspend fun createSupply(request: CreateSupplyDto): UiState<Unit> = safeApiCall(
        apiCall = { api.createSupply(request) }, successHandler = { Unit }
    ).toUiState()
}

private fun SupplyDto.toSupply(products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>) = Supply(
    id = id,
    number = supplyNumber,
    status = status.orEmpty(),
    receivedAt = receivedAt,
    createdAt = createdAt,
    items = items.map { item ->
        val product = products[item.productId]
        SupplyItem(
            id = item.id,
            productId = item.productId,
            productName = product?.name ?: "Товар #${item.productId}",
            unit = product?.unit?.shortName.orEmpty(),
            quantity = item.quantity.jsonPrimitive.content.toDoubleOrNull() ?: 0.0,
            expiresAt = item.expiresAt
        )
    }
)
