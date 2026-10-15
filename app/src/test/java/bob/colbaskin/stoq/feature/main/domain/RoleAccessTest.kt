package bob.colbaskin.stoq.feature.main.domain

import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class RoleAccessTest {
    @Test fun administratorHasEverySection() {
        assertEquals(AppSection.entries.toSet(), UserRole.Administrator.accessibleSections())
    }

    @Test fun managerHasAnalyticsAndAllBusinessSections() {
        assertEquals(setOf(AppSection.Dashboard, AppSection.Storage, AppSection.Supplies, AppSection.Orders, AppSection.Dishes, AppSection.Logs), UserRole.Manager.accessibleSections())
    }

    @Test fun waiterCannotOpenAnalyticsOrAdministration() {
        assertEquals(setOf(AppSection.Storage, AppSection.Supplies, AppSection.Orders, AppSection.Dishes), UserRole.Waiter.accessibleSections())
    }

    @Test fun storekeeperHasOnlySuppliesAndPendingHasNoBusinessAccess() {
        assertEquals(setOf(AppSection.Supplies), UserRole.Storekeeper.accessibleSections())
        assertTrue(UserRole.Pending.accessibleSections().isEmpty())
        assertTrue(null.accessibleSections().isEmpty())
    }
}
