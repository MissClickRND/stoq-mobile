package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.feature.auth.data.model.InvitationDto
import bob.colbaskin.stoq.feature.admin.data.toInvitation
import bob.colbaskin.stoq.feature.admin.domain.model.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Test

class InvitationContractTest {
    private val invitation = InvitationDto("invitation-test", 6, "WAITER", "2026-10-15T17:00:00Z", "2070-10-17T17:00:00Z")

    @Test fun linkTargetsRealRegistrationWithRestaurantAndInvitation() {
        val mapped = invitation.toInvitation()
        val url = mapped.link.toHttpUrl()
        assertEquals("/register/employee", url.encodedPath)
        assertEquals("6", url.queryParameter("restaurantId"))
        assertEquals("invitation-test", url.queryParameter("invitationId"))
        assertEquals(EmployeeRole.Waiter, mapped.role)
        assertEquals(InvitationStatus.Active, mapped.status)
    }

    @Test fun invitationLifecycleIsMappedWithoutInventedActiveStates() {
        assertEquals(InvitationStatus.Disabled, invitation.copy(revokedAt = "2026-10-15T18:00:00Z").toInvitation().status)
        assertEquals(InvitationStatus.Used, invitation.copy(usedAt = "2026-10-15T18:00:00Z").toInvitation().status)
        assertEquals(InvitationStatus.Expired, invitation.copy(expiresAt = "2020-01-01T00:00:00Z").toInvitation().status)
    }

    @Test fun serverInvitationResponseIsDecoded() {
        val dto = Json { ignoreUnknownKeys = true }.decodeFromString<InvitationDto>("""{"id":"id","restaurantId":6,"role":"ADMIN","expiresAt":"2070-10-17T17:24:33Z","revokedAt":null,"usedAt":null,"createdAt":"2026-10-15T17:24:33Z","status":"active"}""")
        assertEquals(EmployeeRole.Administrator, dto.toInvitation().role)
    }
}
