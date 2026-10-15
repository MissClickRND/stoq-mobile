package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.feature.auth.data.model.AccessRequestDto
import bob.colbaskin.stoq.feature.auth.data.model.AuthUserDto
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

fun AuthUserDto.toDomain(): AuthUser = AuthUser(
    id = id.toString(),
    fullName = fullName,
    email = email,
    role = when (role) {
        "ADMIN" -> UserRole.Administrator
        "MANAGER" -> UserRole.Manager
        "WAITER" -> UserRole.Waiter
        "STOREKEEPER" -> UserRole.Storekeeper
        "WAIT_CONFIRM" -> UserRole.Pending
        else -> error("Unknown user role")
    },
    avatarUrl = avatarUrl,
    restaurantId = restaurantId
)

fun AccessRequest.toDto(): AccessRequestDto = AccessRequestDto(
    fullName = fullName,
    email = email,
    password = password,
    restaurantId = restaurantId
)
