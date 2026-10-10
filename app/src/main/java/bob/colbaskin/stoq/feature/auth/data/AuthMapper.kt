package bob.colbaskin.stoq.feature.auth.data

import bob.colbaskin.stoq.feature.auth.data.model.AccessRequestDto
import bob.colbaskin.stoq.feature.auth.data.model.AuthUserDto
import bob.colbaskin.stoq.feature.auth.domain.model.AccessRequest
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole

fun AuthUserDto.toDomain(): AuthUser = AuthUser(
    id = id,
    fullName = fullName,
    email = email,
    role = UserRole.entries.firstOrNull { it.name == role } ?: UserRole.Waiter,
    avatarUrl = avatarUrl
)

fun AccessRequest.toDto(): AccessRequestDto = AccessRequestDto(
    fullName = fullName,
    email = email,
    companyName = companyName,
    role = requestedRole.name,
    reason = reason
)
