package bob.colbaskin.stoq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import bob.colbaskin.stoq.common.designsystem.theme.StoqTheme
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.MainViewModel
import bob.colbaskin.stoq.common.navigation.AppNavHost
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoqTheme {
                StoqApp()
            }
        }
    }
}

@Composable
fun StoqApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is bob.colbaskin.stoq.common.core.UiState.Loading -> LoadingScreen()
            is bob.colbaskin.stoq.common.core.UiState.Success -> AppNavHost(state)
            is bob.colbaskin.stoq.common.core.UiState.Error -> LoadingScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoqAppPreview() {
    StoqTheme {
        StoqApp()
    }
}
