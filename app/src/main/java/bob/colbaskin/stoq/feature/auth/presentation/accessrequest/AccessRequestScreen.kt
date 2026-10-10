package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthTab

@Composable
fun AccessRequestScreen(
    onRequestSent: () -> Unit,
    onSignIn: () -> Unit,
    onRestaurantRegistration: () -> Unit,
    viewModel: AccessRequestViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val hasFilledFields = state.fullName.isNotBlank() && state.email.isNotBlank() && state.companyName.isNotBlank()
    LaunchedEffect(state.requestState) { if (state.requestState is UiState.Success) onRequestSent() }
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
        StoqTextField(
            value = state.companyName,
            onValueChange = { viewModel.onAction(AccessRequestAction.CompanyNameChanged(it)) },
            label = "Компания / заведение",
            isError = state.fieldError.companyName != null,
            supportingText = state.fieldError.companyName,
            leadingIcon = Icons.Outlined.Apartment
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
