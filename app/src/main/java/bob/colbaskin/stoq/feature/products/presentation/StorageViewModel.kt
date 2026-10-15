package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.drafts.data.Draft
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import bob.colbaskin.stoq.feature.products.data.realtime.InventorySocketClient
import bob.colbaskin.stoq.feature.products.data.model.CreateProductDto
import bob.colbaskin.stoq.feature.products.data.model.CreateUnitDto
import bob.colbaskin.stoq.feature.products.data.model.ProductUnitDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val repository: RemoteInventoryRepository,
    private val productRepository: RemoteProductRepository,
    private val draftsRepository: DraftRepository,
    private val inventorySocket: InventorySocketClient
) : ViewModel() {
    var state by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set
    var units by mutableStateOf<List<ProductUnitDto>>(emptyList())
        private set
    var actionState by mutableStateOf<UiState<Unit>?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    private var allProducts = emptyList<StorageProduct>()
    private var searchJob: Job? = null
    var inventoryDrafts by mutableStateOf<List<Draft>>(emptyList())
        private set
    var socketUpdateMessage by mutableStateOf<String?>(null)
        private set
    private var currentQuery = ""

    init {
        viewModelScope.launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            inventorySocket.changes.collect { change ->
                if (change.event != "connected") {
                    socketUpdateMessage = change.reason?.takeIf(String::isNotBlank)?.let { "Склад обновлён: $it" }
                        ?: if (change.event == "inventory:batchExpired") "У партии истёк срок годности" else "Остатки на складе обновлены"
                }
                refresh()
            }
        }
        inventorySocket.acquire()
        refresh()
        refreshUnits()
        viewModelScope.launch {
            draftsRepository.observe(DraftType.Inventory).collect { inventoryDrafts = it }
        }
    }

    fun refresh() = viewModelScope.launch {
        val hasData = state is UiState.Success
        if (hasData) isRefreshing = true else state = UiState.Loading
        when (val result = repository.getInventory()) {
            is UiState.Success -> {
                allProducts = result.data
                inventorySocket.syncStock(result.data.map(StorageProduct::id))
                state = UiState.Success(result.data.filter { it.name.contains(currentQuery, ignoreCase = true) || it.id.toString().contains(currentQuery) })
            }
            is UiState.Error -> state = if (hasData) UiState.Success(allProducts) else result
            UiState.Loading -> Unit
        }
        isRefreshing = false
    }

    fun searchProducts(query: String) {
        currentQuery = query
        if (query.isBlank() && allProducts.isNotEmpty()) state = UiState.Success(allProducts)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            when (val result = repository.getInventory(query)) {
                is UiState.Success -> {
                    if (query.isBlank()) allProducts = result.data
                    state = result
                }
                else -> Unit
            }
        }
    }

    fun createProduct(name: String, unitId: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.createProduct(CreateProductDto(name.trim(), unitId))
        if (actionState is UiState.Success) {
            refresh()
            onSuccess()
        }
    }

    fun createUnit(name: String, shortName: String) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.createUnit(CreateUnitDto(name.trim(), shortName.trim()))
        if (actionState is UiState.Success) refreshUnits()
    }

    fun deleteProduct(id: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        actionState = UiState.Loading
        actionState = productRepository.deleteProduct(id)
        if (actionState is UiState.Success) {
            refresh()
            onSuccess()
        }
    }

    fun writeOff(batchId: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        if (actionState == UiState.Loading) return@launch
        actionState = UiState.Loading
        actionState = repository.writeOff(batchId)
        if (actionState is UiState.Success) {
            refresh()
            onSuccess()
        }
    }

    private fun refreshUnits() = viewModelScope.launch {
        when (val result = productRepository.getUnits()) {
            is UiState.Success -> units = result.data
            else -> Unit
        }
    }

    fun consumeSocketUpdateMessage() { socketUpdateMessage = null }

    override fun onCleared() {
        inventorySocket.release()
        super.onCleared()
    }
}
