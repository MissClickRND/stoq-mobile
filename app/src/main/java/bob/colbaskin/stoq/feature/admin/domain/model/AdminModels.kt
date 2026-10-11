package bob.colbaskin.stoq.feature.admin.domain.model

data class VenueProfile(
    val name: String,
    val city: String,
    val notificationEmail: String,
    val phone: String
)

enum class EmployeeRole(val title: String) {
    Administrator("Администратор"),
    Manager("Менеджер"),
    Chef("Повар"),
    Storekeeper("Кладовщик"),
    Waiter("Официант")
}

enum class EmployeeStatus(val title: String) {
    Active("Активен"),
    Pending("Ожидает"),
    Blocked("Заблокирован")
}

data class ManagedEmployee(
    val id: String,
    val name: String,
    val email: String,
    val role: EmployeeRole,
    val status: EmployeeStatus,
    val joinedAt: String
) {
    val initials: String
        get() = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
}

enum class InvitationStatus(val title: String) {
    Active("Активна"),
    Used("Использована"),
    Expired("Истекла"),
    Disabled("Отключена")
}

data class EmployeeInvitation(
    val id: String,
    val code: String,
    val role: EmployeeRole,
    val createdAt: String,
    val expiresAt: String,
    val status: InvitationStatus,
    val acceptedBy: String? = null
) {
    val link: String
        get() = "https://stoq.app/invite/$code"
}

data class AdminSnapshot(
    val venue: VenueProfile,
    val employees: List<ManagedEmployee>,
    val invitations: List<EmployeeInvitation>
)
