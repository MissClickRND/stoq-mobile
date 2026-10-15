package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthTab

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccessRequestScreen(
    onRequestSent: () -> Unit,
    onSignIn: () -> Unit,
    onRestaurantRegistration: () -> Unit,
    viewModel: AccessRequestViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val restaurants = (state.restaurantsState as? UiState.Success)?.data.orEmpty()
    val selectedRestaurant = restaurants.firstOrNull { it.id == state.restaurantId }
    var restaurantsExpanded by remember { mutableStateOf(false) }
    val hasFilledFields = state.fullName.isNotBlank() && state.email.isNotBlank() && selectedRestaurant != null && state.password.length >= 4
    LaunchedEffect(state.requestState) { if (state.requestState is UiState.Success) onRequestSent() }
    PullToRefreshContainer(state.restaurantsState == UiState.Loading, { viewModel.onAction(AccessRequestAction.ReloadRestaurants) }, Modifier.fillMaxSize()) {
    AuthScreenContainer(
        selectedTab = AuthTab.EmployeeRegistration,
        title = "Регистрация сотрудника",
        subtitle = "Отправьте запрос на присоединение к существующему\nзаведению. После одобрения администратора вы получите\nдоступ к системе.",
        onTabSelected = { tab -> when (tab) {
            AuthTab.SignIn -> onSignIn()
            AuthTab.RestaurantRegistration -> onRestaurantRegistration()
            AuthTab.EmployeeRegistration -> Unit
        } }
    ) {
        StoqTextField(
            value = state.fullName,
            onValueChange = { viewModel.onAction(AccessRequestAction.FullNameChanged(it)) },
            label = "Имя и фамилия",
            isError = state.fieldError.fullName != null,
            supportingText = state.fieldError.fullName,
            leadingIcon = Icons.Outlined.Person
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = state.email,
            onValueChange = { viewModel.onAction(AccessRequestAction.EmailChanged(it)) },
            label = "Почта",
            isError = state.fieldError.email != null,
            supportingText = state.fieldError.email,
            leadingIcon = Icons.Outlined.Email
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        ExposedDropdownMenuBox(
            expanded = restaurantsExpanded,
            onExpandedChange = { restaurantsExpanded = it && restaurants.isNotEmpty() },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRestaurant?.name.orEmpty(),
                onValueChange = {},
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                label = { Text("Ресторан") },
                placeholder = { Text(if (state.restaurantsState == UiState.Loading) "Загрузка ресторанов…" else "Выберите ресторан") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = restaurantsExpanded) },
                isError = state.fieldError.companyName != null,
                supportingText = state.fieldError.companyName?.let { { Text(it) } },
                shape = MaterialTheme.shapes.small
            )
            ExposedDropdownMenu(
                expanded = restaurantsExpanded,
                onDismissRequest = { restaurantsExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                restaurants.forEach { restaurant ->
                    DropdownMenuItem(
                        text = {
                            androidx.compose.foundation.layout.Column {
                                Text(restaurant.name, style = MaterialTheme.typography.bodyLarge)
                                restaurant.address?.takeIf(String::isNotBlank)?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = { viewModel.onAction(AccessRequestAction.RestaurantSelected(restaurant.id)); restaurantsExpanded = false }
                    )
                }
            }
        }
        (state.restaurantsState as? UiState.Error)?.let { error ->
            Text(error.text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { viewModel.onAction(AccessRequestAction.ReloadRestaurants) }) { Text("Повторить загрузку ресторанов") }
        }
        if (state.restaurantsState is UiState.Success && restaurants.isEmpty()) {
            Text("Рестораны не найдены", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = state.password,
            onValueChange = { viewModel.onAction(AccessRequestAction.PasswordChanged(it)) },
            label = "Пароль",
            isError = state.fieldError.password != null,
            supportingText = state.fieldError.password,
            leadingIcon = Icons.Outlined.Lock,
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (state.passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            onTrailingIconClick = { viewModel.onAction(AccessRequestAction.PasswordVisibilityClicked) }
        )
        StoqPrimaryButton(
            text = if (state.requestState is UiState.Loading) "Отправляем..." else "Отправить запрос",
            onClick = { viewModel.onAction(AccessRequestAction.SubmitClicked) },
            modifier = Modifier.padding(top = StoqSpacing.Large),
            enabled = hasFilledFields && state.requestState !is UiState.Loading
        )
        if (state.requestState is UiState.Error) {
            Text(state.requestState.text, Modifier.padding(top = StoqSpacing.Small))
        }
    }
    }
}
