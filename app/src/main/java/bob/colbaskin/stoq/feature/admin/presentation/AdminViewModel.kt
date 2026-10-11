package bob.colbaskin.stoq.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.feature.admin.domain.AdminRepository
import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeInvitation
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AdminSection { Venue, Employees, Invitations }

data class AdminState(
    val section: AdminSection = AdminSection.Venue,
    val snapshot: AdminSnapshot? = null,
    val employeeQuery: String = "",
    val inviteRole: EmployeeRole = EmployeeRole.Manager,
    val inviteDurationHours: Int = 48,
    val generatedInvitation: EmployeeInvitation? = null,
    val notice: String? = null
)

sealed interface AdminAction {
    data class SectionSelected(val value: AdminSection) : AdminAction
    data class VenueSaved(val venue: VenueProfile) : AdminAction
    data class EmployeeQueryChanged(val value: String) : AdminAction
    data class EmployeeRoleChanged(val employeeId: String, val role: EmployeeRole) : AdminAction
    data class EmployeeDeleted(val employeeId: String) : AdminAction
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
    private val _state = MutableStateFlow(AdminState(snapshot = repository.snapshot()))
    val state = _state.asStateFlow()

    fun onAction(action: AdminAction) = viewModelScope.launch {
        when (action) {
            is AdminAction.SectionSelected -> _state.update { it.copy(section = action.value, notice = null) }
            is AdminAction.VenueSaved -> update(repository.updateVenue(action.venue), "Данные заведения сохранены")
            is AdminAction.EmployeeQueryChanged -> _state.update { it.copy(employeeQuery = action.value) }
            is AdminAction.EmployeeRoleChanged -> update(repository.updateEmployeeRole(action.employeeId, action.role), "Роль сотрудника обновлена")
            is AdminAction.EmployeeDeleted -> update(repository.deleteEmployee(action.employeeId), "Сотрудник удалён")
            is AdminAction.InviteRoleChanged -> _state.update { it.copy(inviteRole = action.value) }
            is AdminAction.InviteDurationChanged -> _state.update { it.copy(inviteDurationHours = action.hours) }
            AdminAction.InviteCreated -> {
                val snapshot = repository.createInvitation(state.value.inviteRole, state.value.inviteDurationHours)
                _state.update { it.copy(snapshot = snapshot, generatedInvitation = snapshot.invitations.firstOrNull(), notice = null) }
            }
            is AdminAction.InvitationDeactivated -> update(repository.deactivateInvitation(action.invitationId), "Приглашение отключено")
            is AdminAction.InvitationDeleted -> update(repository.deleteInvitation(action.invitationId), "Приглашение удалено")
            AdminAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun update(snapshot: AdminSnapshot, notice: String) {
        _state.update { it.copy(snapshot = snapshot, notice = notice) }
    }
}
