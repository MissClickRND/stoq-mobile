package bob.colbaskin.stoq.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.drafts.data.Draft
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

enum class InventoryStep { Checking, Confirmation, Report }

data class InventoryLine(
    val product: InventoryProduct,
    val actualQuantity: Double = product.systemQuantity,
    val isChecked: Boolean = false
) {
    val difference: Double
        get() = actualQuantity - product.systemQuantity
    val isDeleted: Boolean
        get() = actualQuantity == 0.0
}

data class InventoryState(
    val step: InventoryStep = InventoryStep.Checking,
    val query: String = "",
    val lines: List<InventoryLine> = emptyList(),
    val isConfirmed: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val notice: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val loadingState: UiState<Unit> = UiState.Loading,
    val isApplying: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val changedLines: List<InventoryLine>
        get() = lines.filter { it.difference != 0.0 }
    val deletedLines: List<InventoryLine>
        get() = lines.filter { it.isDeleted }
    val checkedLines: Int
        get() = lines.count { it.isChecked }
    val hasDraftableProgress: Boolean
        get() = step != InventoryStep.Checking || lines.any { it.isChecked || it.difference != 0.0 }
}

@Serializable
private data class InventoryDraftPayload(
    val step: String,
    val isConfirmed: Boolean,
    val lines: List<InventoryDraftLine>
)

@Serializable
private data class InventoryDraftLine(
    val batchId: Long,
    val actualQuantity: Double,
    val isChecked: Boolean
)

sealed interface InventoryAction {
    data class QueryChanged(val value: String) : InventoryAction
    data class QuantityChanged(val batchId: Long, val value: Double) : InventoryAction
    data class ProductChecked(val batchId: Long) : InventoryAction
    data object ProceedToConfirmation : InventoryAction
    data object BackToChecking : InventoryAction
    data class ConfirmationChanged(val value: Boolean) : InventoryAction
    data object ApplyChanges : InventoryAction
    data object StartNew : InventoryAction
    data object SaveDraft : InventoryAction
    data class ReportSaved(val format: String) : InventoryAction
    data object NoticeDismissed : InventoryAction
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val draftsRepository: DraftRepository
) : ViewModel() {
    private val _state = MutableStateFlow(InventoryState())
    val state = _state.asStateFlow()
    private var draftId: String? = null
    private var draftWriteVersion = 0L
    private val draftWriteMutex = Mutex()
    val activeDraftId: String?
        get() = draftId
    var drafts by mutableStateOf<List<Draft>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            draftsRepository.observe(DraftType.Inventory).collect { drafts = it }
        }
    }

    fun load() = refresh()
    fun onRetry() = refresh()

    fun refreshForPull() {
        if (_state.value.hasDraftableProgress) {
            _state.update { it.copy(notice = "Сохраните или завершите инвентаризацию перед обновлением данных") }
        } else {
            refresh()
        }
    }

    fun onAction(action: InventoryAction) = viewModelScope.launch {
        when (action) {
            is InventoryAction.QueryChanged -> _state.update { it.copy(query = action.value) }
            is InventoryAction.QuantityChanged -> _state.update { state ->
                state.copy(lines = state.lines.map { line ->
                    if (line.product.batchId == action.batchId) line.copy(actualQuantity = action.value.coerceAtLeast(0.0), isChecked = true) else line
                })
            }
            is InventoryAction.ProductChecked -> _state.update { state ->
                state.copy(lines = state.lines.map { line -> if (line.product.batchId == action.batchId) line.copy(isChecked = true) else line })
            }
            InventoryAction.ProceedToConfirmation -> _state.update { state ->
                state.copy(step = InventoryStep.Confirmation, lines = state.lines.map { it.copy(isChecked = true) }, notice = null)
            }
            InventoryAction.BackToChecking -> _state.update { it.copy(step = InventoryStep.Checking, isConfirmed = false, notice = null) }
            is InventoryAction.ConfirmationChanged -> _state.update { it.copy(isConfirmed = action.value) }
            InventoryAction.ApplyChanges -> applyChanges()
            InventoryAction.StartNew -> refresh()
            InventoryAction.SaveDraft -> saveDraft(markClean = true)
            is InventoryAction.ReportSaved -> _state.update { it.copy(notice = "Отчёт сохранён в формате ${action.format}") }
            InventoryAction.NoticeDismissed -> _state.update { it.copy(notice = null) }
        }
        if (action.shouldMarkDirty) _state.update { it.copy(hasUnsavedChanges = true) }
        if (action.shouldAutosave) saveDraft(showNotice = false)
    }

    fun restoreDraft(id: String) = viewModelScope.launch {
        draftWriteVersion++
        val draft = draftsRepository.find(id) ?: return@launch
        val payload = runCatching { Json.decodeFromString<InventoryDraftPayload>(draft.payloadJson) }.getOrNull() ?: return@launch
        val valuesByBatch = payload.lines.associateBy(InventoryDraftLine::batchId)
        _state.update { state ->
            state.copy(
                step = runCatching { InventoryStep.valueOf(payload.step) }.getOrDefault(InventoryStep.Checking),
                isConfirmed = payload.isConfirmed,
                lines = state.lines.map { line ->
                    valuesByBatch[line.product.batchId]?.let { saved ->
                        line.copy(actualQuantity = saved.actualQuantity, isChecked = saved.isChecked)
                    } ?: line
                },
                notice = "Черновик восстановлен",
                hasUnsavedChanges = false
            )
        }
        draftId = draft.id
    }

    fun saveDraft(onSaved: (() -> Unit)? = null, showNotice: Boolean = true, markClean: Boolean = false) {
        val state = _state.value
        if (!state.hasDraftableProgress) {
            onSaved?.invoke()
            return
        }
        val currentDraftId = draftId ?: UUID.randomUUID().toString().also { draftId = it }
        val writeVersion = ++draftWriteVersion
        viewModelScope.launch {
            draftWriteMutex.withLock {
                if (writeVersion != draftWriteVersion) return@withLock
                val payload = InventoryDraftPayload(
                    step = state.step.name,
                    isConfirmed = state.isConfirmed,
                    lines = state.lines.map { line ->
                        InventoryDraftLine(line.product.batchId, line.actualQuantity, line.isChecked)
                    }
                )
                val draft = draftsRepository.save(
                    type = DraftType.Inventory,
                    title = "Инвентаризация",
                    summary = "${state.changedLines.size} изм. • ${state.checkedLines}/${state.lines.size} проверено",
                    payloadJson = Json.encodeToString(payload),
                    id = currentDraftId
                )
                draftId = draft.id
                _state.update {
                    it.copy(
                        notice = if (showNotice) "Черновик сохранён" else it.notice,
                        hasUnsavedChanges = if (markClean) false else it.hasUnsavedChanges
                    )
                }
                onSaved?.invoke()
            }
        }
    }

    fun discardDraft(onDiscarded: () -> Unit) {
        val currentDraftId = draftId
        draftWriteVersion++
        draftId = null
        viewModelScope.launch {
            draftWriteMutex.withLock { currentDraftId?.let { draftsRepository.delete(it) } }
            onDiscarded()
        }
    }

    private fun refresh() = viewModelScope.launch {
        draftWriteVersion++
        draftId = null
        val previous = _state.value
        val hasData = previous.lines.isNotEmpty()
        if (hasData) _state.update { it.copy(isRefreshing = true) }
        else _state.value = InventoryState(loadingState = UiState.Loading)
        when (val result = repository.products()) {
            is UiState.Success -> _state.value = InventoryState(lines = result.data.map(::InventoryLine), loadingState = UiState.Success(Unit))
            is UiState.Error -> {
                if (hasData) _state.update { it.copy(isRefreshing = false, notice = "${result.title}: ${result.text}") }
                else _state.value = InventoryState(loadingState = result)
            }
            UiState.Loading -> Unit
        }
    }

    private suspend fun applyChanges() {
        val changes = _state.value.changedLines.map { it.product.batchId to it.actualQuantity }
        _state.update { it.copy(isApplying = true, notice = null) }
        if (changes.isEmpty()) {
            val currentDraftId = draftId
            draftWriteVersion++
            _state.update { it.copy(step = InventoryStep.Report, completedAt = LocalDateTime.now(), isApplying = false) }
            draftWriteMutex.withLock { currentDraftId?.let { draftsRepository.delete(it) } }
            draftId = null
            return
        }
        when (val result = repository.updateQuantities(changes)) {
            is UiState.Success -> {
                val currentDraftId = draftId
                draftWriteVersion++
                _state.update { it.copy(step = InventoryStep.Report, completedAt = LocalDateTime.now(), notice = null, isApplying = false) }
                draftWriteMutex.withLock { currentDraftId?.let { draftsRepository.delete(it) } }
                draftId = null
            }
            is UiState.Error -> _state.update { it.copy(isApplying = false, notice = "${result.title}: ${result.text}") }
            UiState.Loading -> Unit
        }
    }
}

private val InventoryAction.shouldAutosave: Boolean
    get() = when (this) {
        is InventoryAction.QueryChanged, InventoryAction.SaveDraft, InventoryAction.ApplyChanges,
        InventoryAction.StartNew, is InventoryAction.ReportSaved, InventoryAction.NoticeDismissed -> false
        else -> true
    }

private val InventoryAction.shouldMarkDirty: Boolean
    get() = when (this) {
        is InventoryAction.QueryChanged, InventoryAction.SaveDraft, InventoryAction.ApplyChanges,
        InventoryAction.StartNew, is InventoryAction.ReportSaved, InventoryAction.NoticeDismissed -> false
        else -> true
    }
