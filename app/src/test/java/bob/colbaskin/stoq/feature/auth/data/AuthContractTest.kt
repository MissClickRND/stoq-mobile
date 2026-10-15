package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.feature.auth.data.model.AuthResponseDto
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class AuthContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun parsesActualStatusWithNumericIds() {
        val response = json.decodeFromString<AuthResponseDto>("""{"authenticated":true,"user":{"id":5,"email":"owner@example.com","fullName":"Тест Администратор","role":"ADMIN","restaurantId":4}}""")
        val user = response.user.toDomain()
        assertEquals("5", user.id)
        assertEquals(4L, user.restaurantId)
        assertEquals(UserRole.Administrator, user.role)
    }

    @Test fun mapsAllSupportedRolesWithoutElevatingUnknownRoles() {
        val response = json.decodeFromString<AuthResponseDto>("""{"user":{"id":6,"email":"user@example.com","fullName":"Тест Пользователь","role":"WAITER","restaurantId":4}}""")
        mapOf("MANAGER" to UserRole.Manager, "WAITER" to UserRole.Waiter, "STOREKEEPER" to UserRole.Storekeeper, "WAIT_CONFIRM" to UserRole.Pending).forEach { (role, expected) ->
            assertEquals(expected, response.user.copy(role = role).toDomain().role)
        }
        assertTrue(runCatching { response.user.copy(role = "UNKNOWN").toDomain() }.isFailure)
    }
}
