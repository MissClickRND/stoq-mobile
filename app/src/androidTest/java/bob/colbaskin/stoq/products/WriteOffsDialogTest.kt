package bob.colbaskin.stoq.products

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.theme.StoqTheme
import bob.colbaskin.stoq.feature.dashboard.data.model.*
import bob.colbaskin.stoq.feature.products.presentation.WriteOffsDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WriteOffsDialogTest {
    @get:Rule val compose = createComposeRule()
    private val item = WriteOffDto(59, 1, StatisticsProductDto(1, "Мука"), 20.0,
        "2026-10-15T10:00:00Z", "2026-10-14T10:00:00Z", "EXPIRED")

    @Test fun disposalRequiresConfirmationAndCancellationDoesNotSubmit() {
        var target by mutableStateOf<WriteOffDto?>(null)
        var submitted = 0
        compose.setContent {
            StoqTheme {
                WriteOffsDialog(UiState.Success(listOf(item)), {}, {}, disposalTarget = target,
                    onDispose = { target = it }, onDismissDisposal = { target = null },
                    onConfirmDisposal = { submitted++ })
            }
        }
        compose.onNodeWithText("Утилизировать").performScrollTo().performClick()
        assertEquals(0, submitted)
        compose.onNodeWithText("Подтвердить утилизацию?").assertExists()
        compose.onNodeWithText("Отмена").performClick()
        compose.onNodeWithText("Подтвердить утилизацию?").assertDoesNotExist()
        compose.onNodeWithText("Утилизировать").performScrollTo().performClick()
        compose.onNodeWithText("Подтвердить").performClick()
        assertEquals(1, submitted)
    }

    @Test fun errorAllowsRetryAndLoadingBlocksRepeatedConfirmation() {
        var state by mutableStateOf<UiState<Unit>?>(UiState.Error("Ошибка", "Нет подключения"))
        compose.setContent {
            StoqTheme {
                WriteOffsDialog(UiState.Success(listOf(item)), {}, {}, disposalTarget = item,
                    disposalState = state, onConfirmDisposal = { state = UiState.Loading })
            }
        }
        compose.onNodeWithText("Нет подключения").assertExists()
        compose.onNodeWithText("Подтвердить").performClick()
        compose.onNodeWithText("Подтверждение…").assertIsNotEnabled()
        compose.onNodeWithText("Отмена").assertIsNotEnabled()
    }
}
