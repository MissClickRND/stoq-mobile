package bob.colbaskin.stoq.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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
    val loadingState: UiState<Unit> = UiState.Loading,
    val isApplying: Boolean = false
) {
    val changedLines: List<InventoryLine>
        get() = lines.filter { it.difference != 0.0 }
    val deletedLines: List<InventoryLine>
        get() = lines.filter { it.isDeleted }
    val checkedLines: Int
        get() = lines.count { it.isChecked }
}

sealed interface InventoryAction {
    data class QueryChanged(val value: String) : InventoryAction
    data class QuantityChanged(val productId: String, val value: Double) : InventoryAction
    data class ProductChecked(val productId: String) : InventoryAction
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
    private val repository: InventoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(InventoryState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun onRetry() = refresh()

    fun onAction(action: InventoryAction) = viewModelScope.launch {
        when (action) {
            is InventoryAction.QueryChanged -> _state.update { it.copy(query = action.value) }
            is InventoryAction.QuantityChanged -> _state.update { state ->
                state.copy(lines = state.lines.map { line ->
                    if (line.product.id == action.productId) line.copy(actualQuantity = action.value.coerceAtLeast(0.0), isChecked = true) else line
                })
            }
            is InventoryAction.ProductChecked -> _state.update { state ->
                state.copy(lines = state.lines.map { line -> if (line.product.id == action.productId) line.copy(isChecked = true) else line })
            }
            InventoryAction.ProceedToConfirmation -> _state.update { it.copy(step = InventoryStep.Confirmation, notice = null) }
            InventoryAction.BackToChecking -> _state.update { it.copy(step = InventoryStep.Checking, isConfirmed = false, notice = null) }
            is InventoryAction.ConfirmationChanged -> _state.update { it.copy(isConfirmed = action.value) }
            InventoryAction.ApplyChanges -> applyChanges()
            InventoryAction.StartNew -> refresh()
            InventoryAction.SaveDraft -> _state.update { it.copy(notice = "Черновик инвентаризации сохранён") }
            is InventoryAction.ReportSaved -> _state.update { it.copy(notice = "Отчёт сохранён в формате ${action.format}") }
            InventoryAction.NoticeDismissed -> _state.update { it.copy(notice = null) }
        }
    }

    private fun refresh() = viewModelScope.launch {
        _state.value = InventoryState(loadingState = UiState.Loading)
        when (val result = repository.products()) {
            is UiState.Success -> _state.value = InventoryState(lines = result.data.map(::InventoryLine), loadingState = UiState.Success(Unit))
            is UiState.Error -> _state.value = InventoryState(loadingState = result)
            UiState.Loading -> Unit
        }
    }

    private suspend fun applyChanges() {
        val changes = _state.value.changedLines.map { it.product.batchId to it.actualQuantity }
        _state.update { it.copy(isApplying = true, notice = null) }
        when (val result = repository.updateQuantities(changes)) {
            is UiState.Success -> _state.update { it.copy(step = InventoryStep.Report, completedAt = LocalDateTime.now(), notice = null, isApplying = false) }
            is UiState.Error -> _state.update { it.copy(isApplying = false, notice = "${result.title}: ${result.text}") }
            UiState.Loading -> Unit
        }
    }
}
