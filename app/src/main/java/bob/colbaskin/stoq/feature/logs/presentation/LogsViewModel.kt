package bob.colbaskin.stoq.feature.logs.presentation

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.logs.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(private val repository: LogsRepository) : ViewModel() {
    var state by mutableStateOf<UiState<List<LogEntryDto>>>(UiState.Loading)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var isLoadingMore by mutableStateOf(false)
        private set
    var pageError by mutableStateOf<String?>(null)
        private set
    var hasMore by mutableStateOf(false)
        private set
    private var cursor: String? = null
    private var request: Job? = null
    private var generation = 0

    fun refresh() {
        if (isRefreshing) return
        val version = ++generation
        request?.cancel()
        request = viewModelScope.launch {
            val previous = state
            isRefreshing = true
            isLoadingMore = false
            pageError = null
            if (previous !is UiState.Success) state = UiState.Loading
            try {
                when (val result = repository.page()) {
                    is UiState.Success -> {
                        state = UiState.Success(mergeLogEntries(emptyList(), result.data.items))
                        cursor = result.data.nextCursor
                        hasMore = cursor != null
                    }
                    is UiState.Error -> if (previous is UiState.Success) pageError = result.text else state = result
                    UiState.Loading -> Unit
                }
            } finally { if (generation == version) isRefreshing = false }
        }
    }

    fun loadMore() {
        if (isRefreshing || isLoadingMore) return
        val next = cursor ?: return
        val version = generation
        request = viewModelScope.launch {
            isLoadingMore = true
            pageError = null
            try {
                when (val result = repository.page(next)) {
                    is UiState.Success -> {
                        val current = (state as? UiState.Success)?.data.orEmpty()
                        state = UiState.Success(mergeLogEntries(current, result.data.items))
                        cursor = result.data.nextCursor?.takeUnless { it == next }
                        hasMore = cursor != null
                    }
                    is UiState.Error -> pageError = result.text
                    UiState.Loading -> Unit
                }
            } finally { if (generation == version) isLoadingMore = false }
        }
    }
}
