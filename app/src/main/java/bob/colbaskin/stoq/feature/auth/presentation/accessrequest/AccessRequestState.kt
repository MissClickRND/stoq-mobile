package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.auth.domain.model.RestaurantOption
import bob.colbaskin.stoq.feature.auth.presentation.FieldError

data class AccessRequestState(
    val fullName: String = "",
    val email: String = "",
    val restaurantId: Long? = null,
    val restaurantsState: UiState<List<RestaurantOption>> = UiState.Loading,
    val password: String = "",
    val passwordVisible: Boolean = false,
    val fieldError: FieldError = FieldError(),
    val requestState: UiState<Unit>? = null
)
