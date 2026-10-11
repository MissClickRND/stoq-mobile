package bob.colbaskin.stoq.feature.admin.data

import bob.colbaskin.stoq.feature.admin.domain.AdminRepository
import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeInvitation
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeStatus
import bob.colbaskin.stoq.feature.admin.domain.model.InvitationStatus
import bob.colbaskin.stoq.feature.admin.domain.model.ManagedEmployee
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAdminRepository @Inject constructor() : AdminRepository {
    private var venue = VenueProfile(
        name = "Ресторан Ромашка",
        city = "Москва",
        notificationEmail = "admin@romashka.ru",
        phone = "+7 (999) 123-45-67"
    )
    private var employees = listOf(
        employee("1", "Алиса Завеса", "alisa.zavesa@romashka.ru", EmployeeRole.Administrator, EmployeeStatus.Active, "12.08.2024"),
        employee("2", "Иван Петров", "ivan.petrov@romashka.ru", EmployeeRole.Manager, EmployeeStatus.Active, "03.09.2024"),
        employee("3", "Мария Волкова", "maria.volkova@romashka.ru", EmployeeRole.Chef, EmployeeStatus.Active, "21.07.2024"),
        employee("4", "Артём Смирнов", "artem.smirnov@romashka.ru", EmployeeRole.Storekeeper, EmployeeStatus.Pending, "10.10.2024"),
        employee("5", "Анна Крылова", "anna.krylova@romashka.ru", EmployeeRole.Waiter, EmployeeStatus.Active, "18.06.2024"),
        employee("6", "Дмитрий Орлов", "dmitriy.orlov@romashka.ru", EmployeeRole.Chef, EmployeeStatus.Blocked, "05.09.2024"),
        employee("7", "Полина Соколова", "polina.sokolova@romashka.ru", EmployeeRole.Manager, EmployeeStatus.Active, "27.08.2024"),
        employee("8", "Егор Кузнецов", "egor.kuznetsov@romashka.ru", EmployeeRole.Waiter, EmployeeStatus.Pending, "14.10.2024")
    )
    private var invitations = listOf(
        invitation("1", "abC128xQ", EmployeeRole.Manager, "10.10.2026 14:32", "12.10.2026 14:32", InvitationStatus.Active),
        invitation("2", "9kLmP7d2", EmployeeRole.Waiter, "09.10.2026 11:15", "11.10.2026 11:15", InvitationStatus.Used, "Иван Петров"),
        invitation("3", "Tq8vN4mZ", EmployeeRole.Chef, "08.10.2026 16:20", "10.10.2026 16:20", InvitationStatus.Expired),
        invitation("4", "Xp3kR9wE", EmployeeRole.Manager, "07.10.2026 09:45", "09.10.2026 09:45", InvitationStatus.Active),
        invitation("5", "Df6mQ2aL", EmployeeRole.Waiter, "06.10.2026 13:10", "08.10.2026 13:10", InvitationStatus.Used, "Анна Смирнова")
    )

    override fun snapshot() = AdminSnapshot(venue, employees, invitations)

    override fun updateVenue(venue: VenueProfile): AdminSnapshot {
        this.venue = venue
        return snapshot()
    }

    override fun updateEmployeeRole(employeeId: String, role: EmployeeRole): AdminSnapshot {
        employees = employees.map { if (it.id == employeeId) it.copy(role = role) else it }
        return snapshot()
    }

    override fun deleteEmployee(employeeId: String): AdminSnapshot {
        employees = employees.filterNot { it.id == employeeId }
        return snapshot()
    }

    override fun createInvitation(role: EmployeeRole, durationHours: Int): AdminSnapshot {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val id = (invitations.maxOfOrNull { it.id.toInt() } ?: 0).plus(1).toString()
        val code = "Stoq${now.minute.toString().padStart(2, '0')}${id}X"
        invitations = listOf(
            invitation(id, code, role, now.format(formatter), now.plusHours(durationHours.toLong()).format(formatter), InvitationStatus.Active)
        ) + invitations
        return snapshot()
    }

    override fun deactivateInvitation(invitationId: String): AdminSnapshot {
        invitations = invitations.map {
            if (it.id == invitationId && it.status == InvitationStatus.Active) it.copy(status = InvitationStatus.Disabled) else it
        }
        return snapshot()
    }

    override fun deleteInvitation(invitationId: String): AdminSnapshot {
        invitations = invitations.filterNot { it.id == invitationId }
        return snapshot()
    }

    private fun employee(
        id: String,
        name: String,
        email: String,
        role: EmployeeRole,
        status: EmployeeStatus,
        joinedAt: String
    ) = ManagedEmployee(id, name, email, role, status, joinedAt)

    private fun invitation(
        id: String,
        code: String,
        role: EmployeeRole,
        createdAt: String,
        expiresAt: String,
        status: InvitationStatus,
        acceptedBy: String? = null
    ) = EmployeeInvitation(id, code, role, createdAt, expiresAt, status, acceptedBy)
}
