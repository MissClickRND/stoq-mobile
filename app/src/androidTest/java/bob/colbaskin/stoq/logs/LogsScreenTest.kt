package bob.colbaskin.stoq.logs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import bob.colbaskin.stoq.common.designsystem.theme.StoqTheme
import bob.colbaskin.stoq.feature.logs.data.*
import bob.colbaskin.stoq.feature.logs.presentation.*
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class LogsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val calls = AtomicInteger()
    private val event = LogEntryDto("1", "update", LogAuthorDto(null, "Система", "SYSTEM"), "Склад", "Изменены остатки", "Инвентаризация", "2026-10-15T18:00:00Z", change = LogChangeDto(JsonPrimitive(2), JsonPrimitive("3 кг")))

    private fun launch() {
        val api = object : LogsApi {
            override suspend fun page(cursor: String?): LogsPageDto {
                calls.incrementAndGet()
                return if (cursor == null) LogsPageDto(listOf(event), "next")
                else LogsPageDto(listOf(event, event.copy(id = "2", title = "Создана поставка", change = null, action = "create")))
            }
        }
        val viewModel = LogsViewModel(LogsRepository(api))
        compose.setContent { StoqTheme { LogsScreen(viewModel) } }
        compose.waitForIdle()
    }

    @Test fun rendersMobileTimelineAndBeforeAfterValues() {
        launch()
        compose.onNodeWithText("Логи").assertExists()
        compose.onNodeWithContentDescription("Обновить журнал").assertDoesNotExist()
        compose.onNodeWithTag("logs-feed").performScrollToNode(hasText("Было"))
        compose.onNodeWithText("Было").assertExists()
        compose.onNodeWithText("Стало").assertExists()
        compose.onNodeWithText("3 кг").assertExists()
        compose.onNodeWithText("21:00").assertExists()
    }

    @Test fun paginationAndPullToRefreshWork() {
        launch()
        compose.onNodeWithText("Загрузить ещё").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("logs-feed").performScrollToNode(hasText("Создана поставка"))
        compose.onNodeWithText("Создана поставка").assertExists()
        compose.onNodeWithTag("logs-feed").performScrollToIndex(0)
        val previousCalls = calls.get()
        compose.onNodeWithTag("logs-feed").performTouchInput { swipeDown(startY = top + 20, endY = bottom - 20, durationMillis = 500) }
        compose.waitUntil(5_000) { calls.get() > previousCalls }
    }
}
