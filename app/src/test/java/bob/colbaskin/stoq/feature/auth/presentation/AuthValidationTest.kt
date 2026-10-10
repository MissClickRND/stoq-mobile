package bob.colbaskin.stoq.feature.auth.presentation

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
    fun accessRequestRequiresEveryVisibleMandatoryField() {
        val result = AuthValidation.accessRequest(
            fullName = "",
            email = "invalid",
            companyName = ""
        )

        assertTrue(result.hasErrors())
    }

    @Test
    fun accessRequestAcceptsCompleteForm() {
        val result = AuthValidation.accessRequest(
            fullName = "Анна Петрова",
            email = "anna@restaurant.ru",
            companyName = "Ресторан Вкус"
        )

        assertFalse(result.hasErrors())
    }
}
