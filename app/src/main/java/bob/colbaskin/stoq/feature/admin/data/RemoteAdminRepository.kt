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

class RemoteAdminRepository @Inject constructor(
    private val api: AuthApiService,
    private val preferences: UserPreferencesRepository
) : AdminRepository {
    override suspend fun snapshot(): UiState<AdminSnapshot> = safeApiCall(apiCall = {
        val pending = api.pendingUsers()
        val user = preferences.getUserPreferences().first().user
        AdminSnapshot(
            venue = VenueProfile(user?.restaurantName.orEmpty(), user?.restaurantAddress.orEmpty(), user?.restaurantEmail.orEmpty(), user?.restaurantPhone.orEmpty()),
            employees = pending.map {
                ManagedEmployee(it.id.toString(), it.fullName, it.email, EmployeeRole.Waiter, EmployeeStatus.Pending, it.createdAt?.take(10) ?: "24")
            },
            invitations = emptyList()
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
            EmployeeRole.Manager -> "MANAGER"
            EmployeeRole.Waiter -> "WAITER"
            EmployeeRole.Storekeeper -> "STOREKEEPER"
            else -> return UiState.Error("Роль недоступна", "Сервер поддерживает назначение ролей менеджера, официанта и кладовщика.")
        }
        val result = safeApiCall(apiCall = { api.approveUser(employeeId.toLong(), ApproveUserDto(serverRole)) }, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

    override suspend fun deleteEmployee(employeeId: String): UiState<AdminSnapshot> {
        val result = safeApiCall(apiCall = { api.deleteUser(employeeId.toLong()) }, successHandler = { Unit }).toUiState()
        return if (result is UiState.Error) result else snapshot()
    }

}
