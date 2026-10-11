package bob.colbaskin.stoq.feature.admin.domain

import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile

interface AdminRepository {
    fun snapshot(): AdminSnapshot
    fun updateVenue(venue: VenueProfile): AdminSnapshot
    fun updateEmployeeRole(employeeId: String, role: EmployeeRole): AdminSnapshot
    fun deleteEmployee(employeeId: String): AdminSnapshot
    fun createInvitation(role: EmployeeRole, durationHours: Int): AdminSnapshot
    fun deactivateInvitation(invitationId: String): AdminSnapshot
    fun deleteInvitation(invitationId: String): AdminSnapshot
}
