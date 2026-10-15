package bob.colbaskin.stoq.feature.auth.domain.model

enum class UserRole(val title: String) {
    Manager("Управляющий"),
    Administrator("Администратор"),
    Waiter("Официант"),
    Storekeeper("Кладовщик"),
    Pending("Ожидает подтверждения")
}
