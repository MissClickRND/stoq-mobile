package bob.colbaskin.stoq.feature.supplies.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.drafts.data.Draft
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.feature.products.data.RemoteProductRepository
import bob.colbaskin.stoq.feature.products.data.model.ProductDto
import bob.colbaskin.stoq.feature.supplies.data.RemoteSupplyRepository
import bob.colbaskin.stoq.feature.supplies.data.Supply
import bob.colbaskin.stoq.feature.supplies.data.model.CreateSupplyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SuppliesViewModel @Inject constructor(
    private val repository: RemoteSupplyRepository,
    private val draftsRepository: DraftRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<List<Supply>>>(UiState.Loading)
        private set
    private var cachedSupplies = emptyList<Supply>()
    var isRefreshing by mutableStateOf(false)
        private set

    init { refresh() }

    var drafts by mutableStateOf<List<Draft>>(emptyList())
        private set

    init {
        viewModelScope.launch { draftsRepository.observe(DraftType.Supply).collect { drafts = it } }
    }

    fun refresh() = viewModelScope.launch {
        val hasData = state is UiState.Success
        if (hasData) isRefreshing = true else state = UiState.Loading
        when (val result = repository.getSupplies()) {
            is UiState.Success -> {
                cachedSupplies = result.data
                state = result
            }
            is UiState.Error -> state = if (hasData) UiState.Success(cachedSupplies) else result
            UiState.Loading -> Unit
        }
        isRefreshing = false
    }
}

@HiltViewModel
class SupplyDetailsViewModel @Inject constructor(
    private val repository: RemoteSupplyRepository
) : ViewModel() {
    var state by mutableStateOf<UiState<Supply>>(UiState.Loading)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    fun load(id: String) = viewModelScope.launch {
        val previous = state
        val hasData = previous is UiState.Success
        if (hasData) isRefreshing = true else state = UiState.Loading
        val result = id.toLongOrNull()?.let { repository.getSupply(it) }
            ?: UiState.Error("Некорректный идентификатор", "Поставка не найдена.")
        state = if (hasData && result is UiState.Error) previous else result
        isRefreshing = false
    }
}

@HiltViewModel
class CreateSupplyViewModel @Inject constructor(
    private val supplyRepository: RemoteSupplyRepository,
    private val productRepository: RemoteProductRepository,
    private val draftsRepository: DraftRepository
) : ViewModel() {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
        private set
    private var allProducts = emptyList<ProductDto>()
    var savingState by mutableStateOf<UiState<Unit>?>(null)
        private set
    var restoredDraft by mutableStateOf<SupplyDraftPayload?>(null)
        private set
    var draftMessage by mutableStateOf<String?>(null)
        private set
    private var draftId: String? = null
    private var productSearchJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = productRepository.getProducts()) {
                is UiState.Success -> {
                    allProducts = result.data
                    products = result.data
                }
                else -> Unit
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank() && allProducts.isNotEmpty()) products = allProducts
        productSearchJob?.cancel()
        productSearchJob = viewModelScope.launch {
            delay(250)
            when (val result = productRepository.getProducts(query)) {
                is UiState.Success -> {
                    if (query.isBlank()) allProducts = result.data
                    products = result.data
                }
                else -> Unit
            }
        }
    }

    fun create(request: CreateSupplyDto, draft: SupplyDraftPayload, onSuccess: () -> Unit) = viewModelScope.launch {
        savingState = UiState.Loading
        savingState = supplyRepository.createSupply(request)
        if (savingState is UiState.Success) {
            draftId?.let { draftsRepository.delete(it) }
            draftId = null
            onSuccess()
        } else saveDraft(draft)
    }

    fun loadDraft(id: String) = viewModelScope.launch {
        val record = draftsRepository.find(id) ?: return@launch
        val draft = runCatching { Json.decodeFromString<SupplyDraftPayload>(record.payloadJson) }.getOrNull() ?: return@launch
        draftId = record.id
        restoredDraft = draft
        draftMessage = "Черновик восстановлен"
    }

    fun saveDraft(draft: SupplyDraftPayload, onSaved: (() -> Unit)? = null, showMessage: Boolean = true) {
        if (!draft.hasContent) {
            onSaved?.invoke()
            return
        }
        val currentDraftId = draftId ?: UUID.randomUUID().toString().also { generatedId -> draftId = generatedId }
        viewModelScope.launch {
            val record = draftsRepository.save(
                type = DraftType.Supply,
                title = draft.name.ifBlank { "Новая поставка" },
                summary = listOf(draft.supplier.takeIf { it.isNotBlank() }, "${draft.products.size} поз.").filterNotNull().joinToString(" • "),
                payloadJson = Json.encodeToString(draft),
                id = currentDraftId
            )
            draftId = record.id
            if (showMessage) draftMessage = "Черновик сохранён"
            onSaved?.invoke()
        }
    }

    fun consumeDraftMessage() {
        draftMessage = null
    }

    fun discardDraft(onDiscarded: () -> Unit) = viewModelScope.launch {
        val currentDraftId = draftId
        draftId = null
        currentDraftId?.let { draftsRepository.delete(it) }
        onDiscarded()
    }
}
