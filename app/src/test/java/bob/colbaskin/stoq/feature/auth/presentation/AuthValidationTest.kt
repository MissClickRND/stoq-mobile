package bob.colbaskin.stoq.feature.auth.presentation

import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {
    @Test
    fun signInAcceptsValidCredentialsFormat() {
        val result = AuthValidation.signIn(
            email = "waiter@stoq.demo",
            password = "stoq2026"
        )

        assertFalse(result.hasErrors())
    }

    @Test
    fun signInRejectsInvalidEmailAndShortPassword() {
        val result = AuthValidation.signIn(
            email = "not-an-email",
            password = "123"
        )

        assertTrue(result.email != null)
        assertTrue(result.password != null)
    }

    @Test
    fun accessRequestRequiresEveryMandatoryField() {
        val result = AuthValidation.accessRequest(
            fullName = "",
            email = "invalid",
            companyName = "",
            role = null,
            reason = "short",
            consentAccepted = false
        )

        assertTrue(result.hasErrors())
    }

    @Test
    fun accessRequestAcceptsCompleteForm() {
        val result = AuthValidation.accessRequest(
            fullName = "Анна Петрова",
            email = "anna@restaurant.ru",
            companyName = "Ресторан Вкус",
            role = UserRole.Administrator,
            reason = "Нужен доступ для управления меню ресторана",
            consentAccepted = true
        )

        assertFalse(result.hasErrors())
    }
}
