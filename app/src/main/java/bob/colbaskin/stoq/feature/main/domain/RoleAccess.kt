package bob.colbaskin.stoq.feature.main.domain

import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

enum class AppSection { Dashboard, Storage, Supplies, Orders, Dishes, Admin }

fun UserRole?.accessibleSections(): Set<AppSection> = when (this) {
    UserRole.Administrator -> AppSection.entries.toSet()
    UserRole.Manager -> AppSection.entries.filterNot { it == AppSection.Admin }.toSet()
    UserRole.Waiter -> setOf(AppSection.Storage, AppSection.Supplies, AppSection.Orders, AppSection.Dishes)
    UserRole.Storekeeper -> setOf(AppSection.Supplies)
    UserRole.Pending, null -> emptySet()
}
