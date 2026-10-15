package bob.colbaskin.stoq.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.feature.admin.domain.AdminRepository
import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeInvitation
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile
import bob.colbaskin.stoq.common.core.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job

enum class AdminSection { Venue, Employees, Invitations }

data class AdminState(
    val section: AdminSection = AdminSection.Venue,
    val snapshot: AdminSnapshot? = null,
    val employeeQuery: String = "",
    val inviteRole: EmployeeRole = EmployeeRole.Manager,
    val inviteDurationHours: Int = 48,
    val generatedInvitation: EmployeeInvitation? = null,
    val notice: String? = null,
    val loadState: UiState<Unit> = UiState.Loading,
    val isMutating: Boolean = false
)

sealed interface AdminAction {
    data class SectionSelected(val value: AdminSection) : AdminAction
    data class VenueSaved(val venue: VenueProfile) : AdminAction
    data class EmployeeQueryChanged(val value: String) : AdminAction
    data class EmployeeRoleChanged(val employeeId: String, val role: EmployeeRole) : AdminAction
    data class EmployeeDeleted(val employeeId: String) : AdminAction
    data class EmployeeBlocked(val employeeId: String, val blocked: Boolean) : AdminAction
    data class InviteRoleChanged(val value: EmployeeRole) : AdminAction
    data class InviteDurationChanged(val hours: Int) : AdminAction
    data object InviteCreated : AdminAction
    data class InvitationDeactivated(val invitationId: String) : AdminAction
    data class InvitationDeleted(val invitationId: String) : AdminAction
    data object NoticeShown : AdminAction
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminState())
    val state = _state.asStateFlow()
    private var refreshJob: Job? = null

    init { refresh() }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(loadState = UiState.Loading) }
            update(repository.snapshot(), null)
        }
    }

    fun onAction(action: AdminAction) = viewModelScope.launch {
        val mutation = action is AdminAction.VenueSaved || action is AdminAction.EmployeeRoleChanged || action is AdminAction.EmployeeDeleted || action is AdminAction.EmployeeBlocked || action == AdminAction.InviteCreated || action is AdminAction.InvitationDeactivated || action is AdminAction.InvitationDeleted
        if (mutation && _state.value.isMutating) return@launch
        if (mutation) _state.update { it.copy(isMutating = true, notice = null) }
        try {
        when (action) {
            is AdminAction.SectionSelected -> _state.update { it.copy(section = action.value, notice = null) }
            is AdminAction.VenueSaved -> update(repository.updateVenue(action.venue), "Данные заведения сохранены")
            is AdminAction.EmployeeQueryChanged -> _state.update { it.copy(employeeQuery = action.value) }
            is AdminAction.EmployeeRoleChanged -> update(repository.updateEmployeeRole(action.employeeId, action.role), "Роль сотрудника обновлена")
            is AdminAction.EmployeeDeleted -> update(repository.deleteEmployee(action.employeeId), "Сотрудник удалён")
            is AdminAction.EmployeeBlocked -> update(repository.setEmployeeBlocked(action.employeeId, action.blocked), if (action.blocked) "Сотрудник заблокирован" else "Доступ сотрудника восстановлен")
            is AdminAction.InviteRoleChanged -> _state.update { it.copy(inviteRole = action.value) }
            is AdminAction.InviteDurationChanged -> _state.update { it.copy(inviteDurationHours = action.hours) }
            AdminAction.InviteCreated -> {
                when (val result = repository.createInvitation(_state.value.inviteRole, _state.value.inviteDurationHours)) {
                    is UiState.Success -> {
                        _state.update { it.copy(generatedInvitation = result.data) }
                        update(repository.snapshot(), "Приглашение создано")
                    }
                    is UiState.Error -> _state.update { it.copy(notice = result.text) }
                    UiState.Loading -> Unit
                }
            }
            is AdminAction.InvitationDeactivated -> {
                val result = repository.revokeInvitation(action.invitationId)
                if (result is UiState.Success) _state.update { it.copy(generatedInvitation = it.generatedInvitation?.takeUnless { invitation -> invitation.id == action.invitationId }) }
                update(result, "Приглашение отключено")
            }
            is AdminAction.InvitationDeleted -> {
                val result = repository.deleteInvitation(action.invitationId)
                if (result is UiState.Success) _state.update { it.copy(generatedInvitation = it.generatedInvitation?.takeUnless { invitation -> invitation.id == action.invitationId }) }
                update(result, "Приглашение удалено")
            }
            AdminAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
        } finally { if (mutation) _state.update { it.copy(isMutating = false) } }
    }

    private fun update(result: UiState<AdminSnapshot>, notice: String?) {
        when (result) {
            is UiState.Success -> _state.update { it.copy(snapshot = result.data, notice = notice, loadState = UiState.Success(Unit)) }
            is UiState.Error -> _state.update { it.copy(notice = result.text, loadState = if (it.snapshot == null) result else UiState.Success(Unit)) }
            UiState.Loading -> Unit
        }
    }
}
