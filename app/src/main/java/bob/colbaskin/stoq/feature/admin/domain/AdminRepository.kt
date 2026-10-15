package bob.colbaskin.stoq.feature.admin.domain

import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile
import bob.colbaskin.stoq.common.core.UiState

interface AdminRepository {
    suspend fun createInvitation(role: EmployeeRole, hours: Int): UiState<bob.colbaskin.stoq.feature.admin.domain.model.EmployeeInvitation>
    suspend fun revokeInvitation(id: String): UiState<AdminSnapshot>
    suspend fun deleteInvitation(id: String): UiState<AdminSnapshot>
    suspend fun setEmployeeBlocked(id: String, blocked: Boolean): UiState<AdminSnapshot>
    suspend fun snapshot(): UiState<AdminSnapshot>
    suspend fun updateVenue(venue: VenueProfile): UiState<AdminSnapshot>
    suspend fun updateEmployeeRole(employeeId: String, role: EmployeeRole): UiState<AdminSnapshot>
    suspend fun deleteEmployee(employeeId: String): UiState<AdminSnapshot>
}
