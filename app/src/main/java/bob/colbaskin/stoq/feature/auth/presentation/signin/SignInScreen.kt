package bob.colbaskin.stoq.feature.auth.presentation.signin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer

@Composable
fun SignInScreen(
    onRequestAccess: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state = viewModel.state

    LaunchedEffect(state.signInState) {
        if (state.signInState is UiState.Success) onSignedIn()
    }

    AuthScreenContainer(
        title = "Войти в систему",
        subtitle = "Войдите с помощью рабочей почты, чтобы управлять рестораном"
    ) {
        StoqCard(modifier = Modifier.padding(top = StoqSpacing.Small)) {
            StoqTextField(
                value = state.email,
                onValueChange = { viewModel.onAction(SignInAction.EmailChanged(it)) },
                label = "Рабочая почта",
                placeholder = "name@restaurant.ru",
                isError = state.fieldError.email != null,
                supportingText = state.fieldError.email,
                leadingIcon = Icons.Outlined.Email
            )
            Spacer(modifier = Modifier.height(StoqSpacing.Medium))
            StoqTextField(
                value = state.password,
                onValueChange = { viewModel.onAction(SignInAction.PasswordChanged(it)) },
                label = "Пароль",
                isError = state.fieldError.password != null,
                supportingText = state.fieldError.password,
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = Icons.Outlined.Lock
            )
            StoqPrimaryButton(
                text = if (state.signInState is UiState.Loading) "Входим..." else "Войти",
                onClick = { viewModel.onAction(SignInAction.SignInClicked) },
                modifier = Modifier.padding(top = StoqSpacing.Large),
                enabled = state.signInState !is UiState.Loading
            )
        }
        if (state.signInState is UiState.Error) {
            Text(
                text = state.signInState.text,
                modifier = Modifier.padding(top = StoqSpacing.Medium),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = StoqSpacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Нет доступа к системе?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.TextButton(onClick = onRequestAccess) {
                Text("Запросить доступ", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
