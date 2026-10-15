package bob.colbaskin.stoq.feature.auth.presentation.restaurantregistration

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthTab

@Composable
fun RestaurantRegistrationScreen(
    onSignIn: () -> Unit,
    onEmployeeRegistration: () -> Unit,
    onRestaurantCreated: () -> Unit,
    viewModel: RestaurantRegistrationViewModel = hiltViewModel()
) {
    var restaurantName by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val hasFilledFields = restaurantName.trim().length >= 2 && fullName.trim().length >= 2 && email.isNotBlank() && password.length >= 4
    LaunchedEffect(viewModel.state) { if (viewModel.state is UiState.Success) onRestaurantCreated() }

    AuthScreenContainer(
        selectedTab = AuthTab.RestaurantRegistration,
        title = "Регистрация ресторана",
        subtitle = "Создайте новое заведение и получите доступ\nк аккаунту администратора.",
        onTabSelected = { tab -> when (tab) {
            AuthTab.SignIn -> onSignIn()
            AuthTab.EmployeeRegistration -> onEmployeeRegistration()
            AuthTab.RestaurantRegistration -> Unit
        } }
    ) {
        StoqTextField(fullName, { fullName = it }, "Имя и фамилия администратора", leadingIcon = Icons.Outlined.Person, isRequired = true)
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = restaurantName,
            onValueChange = { restaurantName = it },
            label = "Название заведения",
            leadingIcon = Icons.Outlined.Apartment
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(city, { city = it }, "Адрес ресторана", leadingIcon = Icons.Outlined.LocationOn)
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = "Почта",
            isError = emailError != null,
            supportingText = emailError,
            leadingIcon = Icons.Outlined.Email
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = "Пароль",
            isError = passwordError != null,
            supportingText = passwordError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            onTrailingIconClick = { passwordVisible = !passwordVisible }
        )
        StoqPrimaryButton(
            text = if (viewModel.state == UiState.Loading) "Создаём…" else "Создать ресторан",
            onClick = {
                emailError = bob.colbaskin.stoq.feature.auth.presentation.AuthValidation.emailError(email)
                passwordError = if (password.length >= 4) null else "Пароль должен содержать не менее 4 символов"
                if (emailError == null && passwordError == null) viewModel.register(fullName, email, password, restaurantName, city)
            },
            modifier = Modifier.padding(top = StoqSpacing.Large),
            enabled = hasFilledFields && viewModel.state != UiState.Loading
        )
        (viewModel.state as? UiState.Error)?.let { error ->
            Text(error.text, Modifier.padding(top = StoqSpacing.Small), color = MaterialTheme.colorScheme.error)
        }
        if (emailError != null || passwordError != null) {
            Text(
                text = "Проверьте выделенные поля.",
                modifier = Modifier.padding(top = StoqSpacing.Small),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
