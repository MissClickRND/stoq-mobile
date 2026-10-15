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
    val supplier: String?,
    val totalPrice: Double?,
    val items: List<SupplyItem>
)

data class SupplyItem(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val price: Double?,
    val totalPrice: Double?,
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
                .map { (_, rows) ->
                    val flattenedItems = rows.mapNotNull { it.toListItem(products) }
                    rows.first().toSupply(products, flattenedItems.takeIf { it.isNotEmpty() })
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

    suspend fun updateSupply(id: Long, request: CreateSupplyDto): UiState<Supply> = safeApiCall(
        apiCall = {
            val products = when (val result = productRepository.getProducts()) {
                is UiState.Success -> result.data.associateBy { it.id }
                else -> emptyMap()
            }
            api.updateSupply(id, request).toSupply(products)
        },
        successHandler = { it }
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
    supplier = supplier,
    totalPrice = totalPrice?.jsonPrimitive?.content?.toDoubleOrNull(),
    items = listItems ?: items.map { it.toSupplyItem(products) }
)

private fun SupplyDto.toListItem(
    products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>
): SupplyItem? {
    val responseProduct = product ?: return null
    val productId = responseProduct.id ?: return null
    val fallback = products[productId]
    return SupplyItem(
        id = productId,
        productId = productId,
        productName = responseProduct.name ?: fallback?.name ?: "Товар #$productId",
        unit = responseProduct.unit?.shortName ?: fallback?.unit?.shortName.orEmpty(),
        quantity = 0.0,
        price = null,
        totalPrice = null,
        expiresAt = null
    )
}

private fun SupplyItemDto.toSupplyItem(
    products: Map<Long, bob.colbaskin.stoq.feature.products.data.model.ProductDto>
): SupplyItem {
    val resolvedProductId = productId ?: product?.id ?: 0L
    val fallback = products[resolvedProductId]
    return SupplyItem(
        id = id ?: supplyItemId ?: resolvedProductId,
        productId = resolvedProductId,
        productName = product?.name ?: fallback?.name ?: "Товар #$resolvedProductId",
        unit = product?.unit?.shortName ?: fallback?.unit?.shortName.orEmpty(),
        quantity = quantity?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
        price = price?.jsonPrimitive?.content?.toDoubleOrNull(),
        totalPrice = totalPrice?.jsonPrimitive?.content?.toDoubleOrNull(),
        expiresAt = expiresAt
    )
}
