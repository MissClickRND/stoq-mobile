package bob.colbaskin.stoq.feature.products.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.products.data.model.CreateProductDto
import bob.colbaskin.stoq.feature.products.data.model.CreateUnitDto
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.products.data.model.ProductUnitDto
import bob.colbaskin.stoq.feature.products.data.remote.ProductsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteProductRepository @Inject constructor(private val api: ProductsApiService) {
    suspend fun getProducts(): UiState<List<ProductDto>> = safeApiCall(
        apiCall = api::getProducts,
        successHandler = { products -> products.filterNot(ProductDto::isDeleted) }
    ).toUiState()

    suspend fun getProduct(id: Long): UiState<ProductDto> = safeApiCall({ api.getProduct(id) }) { it }.toUiState()
    suspend fun createProduct(request: CreateProductDto): UiState<Unit> = safeApiCall({ api.createProduct(request) }) { Unit }.toUiState()
    suspend fun createProducts(request: List<CreateProductDto>): UiState<Unit> = safeApiCall({ api.createProducts(request) }) { Unit }.toUiState()
    suspend fun deleteProduct(id: Long): UiState<Unit> = safeApiCall({ api.deleteProduct(id) }) { Unit }.toUiState()
    suspend fun getUnits(): UiState<List<ProductUnitDto>> = safeApiCall(api::getUnits) { it }.toUiState()
    suspend fun getUnit(id: Long): UiState<ProductUnitDto> = safeApiCall({ api.getUnit(id) }) { it }.toUiState()
    suspend fun createUnit(request: CreateUnitDto): UiState<Unit> = safeApiCall({ api.createUnit(request) }) { Unit }.toUiState()
}
