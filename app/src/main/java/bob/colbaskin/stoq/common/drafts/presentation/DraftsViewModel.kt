package bob.colbaskin.stoq.common.drafts.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.drafts.data.Draft
import bob.colbaskin.stoq.common.drafts.data.DraftRepository
import bob.colbaskin.stoq.common.drafts.data.DraftType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(
    private val repository: DraftRepository
) : ViewModel() {
    var drafts by mutableStateOf<List<Draft>>(emptyList())
        private set
    private var observingJob: Job? = null

    fun load(type: DraftType) {
        observingJob?.cancel()
        observingJob = viewModelScope.launch {
            repository.observe(type).collectLatest { drafts = it }
        }
    }

    fun delete(draft: Draft) = viewModelScope.launch { repository.delete(draft.id) }
}
