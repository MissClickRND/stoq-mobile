package bob.colbaskin.stoq.feature.auth.presentation.restaurantregistration

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onRestaurantCreated: () -> Unit
) {
    var restaurantName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val hasFilledFields = restaurantName.isNotBlank() && city.isNotBlank() && email.isNotBlank() && password.isNotBlank()

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
        StoqTextField(
            value = restaurantName,
            onValueChange = { restaurantName = it },
            label = "Название заведения",
            leadingIcon = Icons.Outlined.Apartment
        )
        Spacer(Modifier.height(StoqSpacing.Small))
        StoqTextField(city, { city = it }, "Город", leadingIcon = Icons.Outlined.LocationOn)
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
            text = "Создать ресторан",
            onClick = {
                emailError = if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) null else "Введите корректный email"
                passwordError = if (password.length >= 8) null else "Пароль должен содержать не менее 8 символов"
                if (emailError == null && passwordError == null) onRestaurantCreated()
            },
            modifier = Modifier.padding(top = StoqSpacing.Large),
            enabled = hasFilledFields
        )
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
