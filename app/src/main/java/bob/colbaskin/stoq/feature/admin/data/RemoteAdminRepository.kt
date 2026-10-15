package bob.colbaskin.stoq.feature.admin.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import bob.colbaskin.stoq.feature.admin.domain.AdminRepository
import bob.colbaskin.stoq.feature.admin.domain.model.*
import bob.colbaskin.stoq.feature.auth.data.model.ApproveUserDto
import bob.colbaskin.stoq.feature.auth.data.model.UpdateRestaurantDto
import bob.colbaskin.stoq.feature.auth.data.remote.AuthApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import bob.colbaskin.stoq.feature.auth.data.model.CreateInvitationDto
import bob.colbaskin.stoq.feature.auth.data.model.InvitationDto
import bob.colbaskin.stoq.feature.auth.data.model.SetUserBlockDto
import okhttp3.HttpUrl.Companion.toHttpUrl
import bob.colbaskin.stoq.BuildConfig

class RemoteAdminRepository @Inject constructor(
    private val api: AuthApiService,
    private val preferences: UserPreferencesRepository,
    private val statisticsApi: bob.colbaskin.stoq.feature.dashboard.data.remote.StatisticsApiService
) : AdminRepository {
    override suspend fun snapshot(): UiState<AdminSnapshot> = safeApiCall(apiCall = {
        val users = api.users()
        val restaurant = api.restaurant()
        val statistics = statisticsApi.admin()
        val invitations = api.invitations()
        AdminSnapshot(
            venue = VenueProfile(restaurant.name, restaurant.address.orEmpty(), restaurant.email.orEmpty(), restaurant.phone.orEmpty()),
            employees = users.map {
                val role = when (it.role) { "ADMIN" -> EmployeeRole.Administrator; "MANAGER" -> EmployeeRole.Manager; "STOREKEEPER" -> EmployeeRole.Storekeeper; else -> EmployeeRole.Waiter }
                val status = when { it.blocked || it.status == "blocked" -> EmployeeStatus.Blocked; it.role == "WAIT_CONFIRM" || it.status == "pending" -> EmployeeStatus.Pending; else -> EmployeeStatus.Active }
                ManagedEmployee(it.id.toString(), it.fullName, it.email, role, status, it.createdAt.take(10))
            },
            invitations = invitations.map { it.toInvitation() },
            statistics = AdminStatistics(statistics.employees, statistics.invitations, statistics.requests, statistics.admins)
        )
    }, successHandler = { it }).toUiState()

    override suspend fun updateVenue(venue: VenueProfile): UiState<AdminSnapshot> {
        val result = safeApiCall(apiCall = {
            val restaurant = api.updateRestaurant(UpdateRestaurantDto(
                name = venue.name.trim(), address = venue.city.trim(),
                email = venue.notificationEmail.trim().takeIf(String::isNotBlank),
                phone = venue.phone.trim().takeIf(String::isNotBlank)
            ))
            preferences.getUserPreferences().first().user?.let { user ->
                preferences.saveUser(user.copy(restaurantName = restaurant.name, restaurantAddress = restaurant.address,
                    restaurantEmail = restaurant.email, restaurantPhone = restaurant.phone))
            }
        }, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

    override suspend fun updateEmployeeRole(employeeId: String, role: EmployeeRole): UiState<AdminSnapshot> {
        val serverRole = when (role) {
            EmployeeRole.Administrator -> "ADMIN"
            EmployeeRole.Manager -> "MANAGER"
            EmployeeRole.Waiter -> "WAITER"
            EmployeeRole.Storekeeper -> "STOREKEEPER"
            else -> return UiState.Error("Роль недоступна", "Сервер поддерживает назначение ролей менеджера, официанта и кладовщика.")
        }
        val result = safeApiCall(apiCall = {
            val user = api.users().first { it.id.toString() == employeeId }
            if (user.role == "WAIT_CONFIRM") api.approveUser(employeeId.toLong(), ApproveUserDto(serverRole))
            else api.changeUserRole(employeeId.toLong(), ApproveUserDto(serverRole))
        }, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

    override suspend fun deleteEmployee(employeeId: String): UiState<AdminSnapshot> {
        val result = safeApiCall(apiCall = { api.deleteUser(employeeId.toLong()) }, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

    override suspend fun createInvitation(role: EmployeeRole, hours: Int): UiState<EmployeeInvitation> = safeApiCall(apiCall = {
        require(hours in 1..720)
        api.createInvitation(CreateInvitationDto(role.apiRole(), hours))
    }, successHandler = { it.toInvitation() }).toUiState()

    override suspend fun revokeInvitation(id: String) = mutate { api.revokeInvitation(id) }
    override suspend fun deleteInvitation(id: String) = mutate { api.deleteInvitation(id) }
    override suspend fun setEmployeeBlocked(id: String, blocked: Boolean) = mutate { api.setUserBlocked(id.toLong(), SetUserBlockDto(blocked)) }

    private suspend fun mutate(action: suspend () -> Unit): UiState<AdminSnapshot> {
        val result = safeApiCall(apiCall = action, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

}

private fun EmployeeRole.apiRole(): String = when (this) {
    EmployeeRole.Administrator -> "ADMIN"; EmployeeRole.Manager -> "MANAGER"; EmployeeRole.Waiter -> "WAITER"; EmployeeRole.Storekeeper -> "STOREKEEPER"
    EmployeeRole.Chef -> error("Роль повара не поддерживается API")
}

internal fun InvitationDto.toInvitation(): EmployeeInvitation {
    val role = when (role) { "ADMIN" -> EmployeeRole.Administrator; "MANAGER" -> EmployeeRole.Manager; "STOREKEEPER" -> EmployeeRole.Storekeeper; else -> EmployeeRole.Waiter }
    val state = when {
        revokedAt != null || status == "revoked" -> InvitationStatus.Disabled
        usedAt != null || status == "used" -> InvitationStatus.Used
        status == "expired" || runCatching { java.time.Instant.parse(expiresAt).isBefore(java.time.Instant.now()) }.getOrDefault(false) -> InvitationStatus.Expired
        else -> InvitationStatus.Active
    }
    val url = BuildConfig.BASE_API_URL.toHttpUrl().newBuilder().encodedPath("/register/employee")
        .query(null).addQueryParameter("restaurantId", restaurantId.toString()).addQueryParameter("invitationId", id).build().toString()
    return EmployeeInvitation(id, id, role, createdAt, expiresAt, state, link = url)
}
