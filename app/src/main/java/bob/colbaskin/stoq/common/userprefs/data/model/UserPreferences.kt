package bob.colbaskin.stoq.common.userprefs.data.model

import bob.colbaskin.stoq.datastore.AuthStatus
import bob.colbaskin.stoq.datastore.RoleStatus
import bob.colbaskin.stoq.datastore.UserPreferencesProto
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

data class UserPreferences(
    val authStatus: AuthConfig,
    val user: AuthUser?
)

fun UserPreferencesProto.toData(): UserPreferences {
    val authStatus = when (this.authStatus) {
        AuthStatus.AUTHENTICATED -> AuthConfig.Authenticated
        AuthStatus.NOT_AUTHENTICATED,
        AuthStatus.UNRECOGNIZED,
        null -> AuthConfig.NotAuthenticated
    }
    val user = if (authStatus == AuthConfig.Authenticated) {
        AuthUser(
            id = userId,
            fullName = userName,
            email = userEmail,
            role = when (roleStatus) {
                RoleStatus.MANAGER -> UserRole.Manager
                RoleStatus.ADMINISTRATOR -> UserRole.Administrator
                RoleStatus.WAITER,
                RoleStatus.GUEST,
                RoleStatus.UNRECOGNIZED,
                null -> UserRole.Waiter
            }
        )
    } else {
        null
    }
    return UserPreferences(authStatus = authStatus, user = user)
}
