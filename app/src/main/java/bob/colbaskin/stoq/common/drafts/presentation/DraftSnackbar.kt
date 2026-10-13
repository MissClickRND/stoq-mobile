package bob.colbaskin.stoq.common.drafts.presentation

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme

@Composable
fun DraftSnackbar(message: String?, onMessageShown: () -> Unit, modifier: Modifier = Modifier) {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message ?: return@LaunchedEffect
        hostState.showSnackbar(message)
        onMessageShown()
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        SnackbarHost(hostState = hostState, modifier = Modifier.padding(20.dp)) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = CustomTheme.colors.primaryDark,
                contentColor = CustomTheme.colors.textOnPrimary
            )
        }
    }
}
