package bob.colbaskin.stoq.feature.supplies.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.SupplyDto
import bob.colbaskin.stoq.feature.supplies.data.model.SupplyItemDto
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
            api.getSupplies()
                .groupBy(SupplyDto::id)
                .map { (_, supplyRows) ->
                    supplyRows.first().toSupply(
                        products = products,
                        listItems = supplyRows.map { it.toListItem(products) }
                    )
                }
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

private fun SupplyDto.toSupply(
    products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>,
    listItems: List<SupplyItem>? = null
) = Supply(
    id = id,
    number = supplyNumber,
    status = status.orEmpty(),
    receivedAt = receivedAt,
    createdAt = createdAt,
    items = listItems ?: items.map { it.toSupplyItem(products) }
)

private fun SupplyDto.toListItem(
    products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>
): SupplyItem {
    val listProduct = requireNotNull(product)
    val fallback = products[listProduct.id]
    return SupplyItem(
        id = listProduct.id,
        productId = listProduct.id,
        productName = listProduct.name.ifBlank { fallback?.name ?: "Товар #${listProduct.id}" },
        unit = listProduct.unit?.shortName ?: fallback?.unit?.shortName.orEmpty(),
        quantity = 0.0,
        expiresAt = null
    )
}

private fun SupplyItemDto.toSupplyItem(
    products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>
): SupplyItem {
    val fallback = products[productId]
    return SupplyItem(
        id = id,
        productId = productId,
        productName = product?.name?.ifBlank { fallback?.name ?: "Товар #$productId" } ?: fallback?.name ?: "Товар #$productId",
        unit = product?.unit?.shortName ?: fallback?.unit?.shortName.orEmpty(),
        quantity = quantity.jsonPrimitive.content.toDoubleOrNull() ?: 0.0,
        expiresAt = expiresAt
    )
}
