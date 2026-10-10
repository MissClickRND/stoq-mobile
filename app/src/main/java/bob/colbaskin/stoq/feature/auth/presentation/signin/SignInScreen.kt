package bob.colbaskin.stoq.feature.auth.presentation.signin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthTab

@Composable
fun SignInScreen(
    onRestaurantRegistration: () -> Unit,
    onEmployeeRegistration: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val hasFilledFields = state.email.isNotBlank() && state.password.isNotBlank()
    LaunchedEffect(state.signInState) { if (state.signInState is UiState.Success) onSignedIn() }
    AuthScreenContainer(
        selectedTab = AuthTab.SignIn,
        title = "Войти в аккаунт",
        subtitle = "Войдите, чтобы получить доступ к платформе\nи управлять вашим заведением.",
        onTabSelected = { tab -> when (tab) {
            AuthTab.RestaurantRegistration -> onRestaurantRegistration()
            AuthTab.EmployeeRegistration -> onEmployeeRegistration()
            AuthTab.SignIn -> Unit
        } }
    ) {
        StoqTextField(
            value = state.email,
            onValueChange = { viewModel.onAction(SignInAction.EmailChanged(it)) },
            label = "Почта",
            placeholder = "name@restaurant.ru",
            isError = state.fieldError.email != null,
            supportingText = state.fieldError.email,
            leadingIcon = Icons.Outlined.Email
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = state.password,
            onValueChange = { viewModel.onAction(SignInAction.PasswordChanged(it)) },
            label = "Пароль",
            isError = state.fieldError.password != null,
            supportingText = state.fieldError.password,
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = if (state.isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            onTrailingIconClick = { viewModel.onAction(SignInAction.PasswordVisibilityClicked) }
        )
        StoqPrimaryButton(
            text = if (state.signInState is UiState.Loading) "Входим..." else "Войти",
            onClick = { viewModel.onAction(SignInAction.SignInClicked) },
            modifier = Modifier.padding(top = StoqSpacing.Large),
            enabled = hasFilledFields && state.signInState !is UiState.Loading
        )
        TextButton(onClick = {}, modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Small)) {
            Text("Забыли пароль?", color = MaterialTheme.colorScheme.onSurface)
        }
        if (state.signInState is UiState.Error) {
            Text(state.signInState.text, Modifier.padding(top = StoqSpacing.Small), color = MaterialTheme.colorScheme.error)
        }
    }
}
