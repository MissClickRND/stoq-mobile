package bob.colbaskin.stoq.common.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.userprefs.data.model.AuthConfig
import bob.colbaskin.stoq.common.userprefs.data.model.UserPreferences
import bob.colbaskin.stoq.feature.auth.presentation.accessrequest.AccessRequestScreen
import bob.colbaskin.stoq.feature.auth.presentation.restaurantregistration.RestaurantRegistrationScreen
import bob.colbaskin.stoq.feature.auth.presentation.signin.SignInScreen
import bob.colbaskin.stoq.feature.main.presentation.MainNavigation
import kotlinx.serialization.Serializable

@Serializable
data object SignInDestination

@Serializable
data object AccessRequestDestination

@Serializable
data object RestaurantRegistrationDestination

@Serializable
data object AccessRequestSentDestination

@Serializable
data object AuthenticatedDestination

@Composable
fun AppNavHost(uiState: UiState.Success<UserPreferences>) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = getRootStartDestination(uiState.data.authStatus)) {
        composable<SignInDestination> {
            SignInScreen(
                onRestaurantRegistration = { navController.navigate(RestaurantRegistrationDestination) },
                onEmployeeRegistration = { navController.navigate(AccessRequestDestination) },
                onSignedIn = {
                    navController.navigate(AuthenticatedDestination) {
                        popUpTo(SignInDestination) { inclusive = true }
                    }
                }
            )
        }
        composable<AccessRequestDestination> {
            AccessRequestScreen(
                onRequestSent = {
                    navController.navigate(AccessRequestSentDestination) {
                        popUpTo(AccessRequestDestination) { inclusive = true }
                    }
                },
                onSignIn = { navController.navigate(SignInDestination) { popUpTo(SignInDestination) { inclusive = true } } },
                onRestaurantRegistration = { navController.navigate(RestaurantRegistrationDestination) }
            )
        }
        composable<RestaurantRegistrationDestination> {
            RestaurantRegistrationScreen(
                onSignIn = { navController.navigate(SignInDestination) { popUpTo(SignInDestination) { inclusive = true } } },
                onEmployeeRegistration = { navController.navigate(AccessRequestDestination) },
                onRestaurantCreated = {
                    navController.navigate(AuthenticatedDestination) {
                        popUpTo(SignInDestination) { inclusive = true }
                    }
                }
            )
        }
        composable<AccessRequestSentDestination> {
            AccessRequestSentScreen(
                onBackToSignIn = {
                    navController.navigate(SignInDestination) {
                        popUpTo(SignInDestination) { inclusive = true }
                    }
                }
            )
        }
        composable<AuthenticatedDestination> {
            MainNavigation(user = uiState.data.user)
        }
    }
}
private fun getRootStartDestination(authStatus: AuthConfig): Any = when (authStatus) {
    AuthConfig.Authenticated -> AuthenticatedDestination
    AuthConfig.NotAuthenticated -> SignInDestination
}

@Composable
private fun AccessRequestSentScreen(onBackToSignIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(StoqSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Заявка отправлена", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Мы сообщим на рабочую почту, когда доступ будет предоставлен.",
            modifier = Modifier.padding(top = StoqSpacing.Small),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        StoqPrimaryButton(
            text = "Вернуться ко входу",
            onClick = onBackToSignIn,
            modifier = Modifier.padding(top = StoqSpacing.ExtraLarge)
        )
    }
}
