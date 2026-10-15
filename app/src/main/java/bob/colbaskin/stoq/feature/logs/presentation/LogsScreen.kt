package bob.colbaskin.stoq.feature.logs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.presentation.RefreshOnResume
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.feature.logs.data.LogEntryDto
import kotlinx.serialization.json.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LogsScreen(viewModel: LogsViewModel = hiltViewModel()) {
    val colors = LocalColors.current
    RefreshOnResume(remember(viewModel) { viewModel::refresh })
    val entries = (viewModel.state as? UiState.Success)?.data.orEmpty()
    val groups = entries.groupBy { logDate(it.timestamp, "d MMMM yyyy") }
    PullToRefreshContainer(viewModel.isRefreshing && viewModel.state is UiState.Success, viewModel::refresh, Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().background(colors.background).testTag("logs-feed"),
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item("header") {
                Column {
                    Text("Логи", style = MaterialTheme.typography.headlineMedium)
                    Text("История действий и изменений", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("Время — Москва. Новые события сверху.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            when (val state = viewModel.state) {
                UiState.Loading -> item { Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                is UiState.Error -> item {
                    StoqCard {
                        Text("Не удалось загрузить журнал", style = MaterialTheme.typography.titleMedium)
                        Text(state.text, color = colors.error)
                        TextButton(onClick = viewModel::refresh) { Text("Повторить") }
                    }
                }
                is UiState.Success -> {
                    if (entries.isEmpty()) item { StoqCard { Text("Действий пока нет"); Text("Новые события будут появляться здесь.", color = colors.textSecondary) } }
                    groups.forEach { (date, dayEntries) ->
                        item("date:$date") {
                            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(date, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Записей: ${dayEntries.size}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        }
                        items(dayEntries, key = { "entry:${it.id}" }) { LogEntryCard(it) }
                    }
                    viewModel.pageError?.let { message -> item("page-error") { Text(message, color = colors.error) } }
                    if (viewModel.hasMore) item("more") {
                        OutlinedButton(onClick = viewModel::loadMore, enabled = !viewModel.isLoadingMore && !viewModel.isRefreshing, modifier = Modifier.fillMaxWidth()) {
                            if (viewModel.isLoadingMore) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Text(if (viewModel.pageError == null) "Загрузить ещё" else "Повторить подгрузку")
                        }
                    } else if (viewModel.pageError != null) item { TextButton(onClick = viewModel::refresh) { Text("Повторить обновление") } }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogEntryCard(entry: LogEntryDto) {
    val colors = LocalColors.current
    val (label, color, icon) = when (entry.action) {
        "create" -> Triple("Создание", colors.success, Icons.Outlined.Add)
        "update" -> Triple("Обновление", Color(0xFF319BF4), Icons.Outlined.Edit)
        "delete" -> Triple("Удаление", colors.error, Icons.Outlined.DeleteOutline)
        "role" -> Triple("Изменение роли", colors.secondary, Icons.Outlined.ManageAccounts)
        "invite" -> Triple("Приглашение", colors.success, Icons.Outlined.PersonAdd)
        "automatic" -> Triple("Автоматически", colors.textSecondary, Icons.Outlined.Settings)
        else -> Triple("Действие", colors.textSecondary, Icons.Outlined.History)
    }
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.width(28.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = color.copy(alpha = .16f)) { Icon(icon, null, Modifier.padding(5.dp).size(18.dp), tint = color) }
            Box(Modifier.width(2.dp).weight(1f).background(color.copy(alpha = .22f)))
        }
        StoqCard(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color.copy(alpha = .12f)) {
                    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                        if (entry.author.role == "SYSTEM") Icon(Icons.Outlined.Settings, null, Modifier.size(18.dp), tint = color)
                        else Text(entry.author.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""), color = color, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.author.name, style = MaterialTheme.typography.titleSmall)
                    Text(logRole(entry.author.role), color = colors.textSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Text(logDate(entry.timestamp, "HH:mm"), color = colors.textSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LogTag(label, color)
                LogTag(entry.category, colors.textSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(entry.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (entry.description.isNotBlank()) Text(entry.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            entry.change?.let { change ->
                Surface(color = colors.surfaceSecondary, shape = MaterialTheme.shapes.small, modifier = Modifier.padding(top = 8.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Было", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary); Text(logValue(change.before), style = MaterialTheme.typography.bodySmall) }
                        Icon(Icons.Outlined.ArrowForward, null, Modifier.padding(horizontal = 8.dp).size(16.dp), tint = color)
                        Column(Modifier.weight(1f)) { Text("Стало", style = MaterialTheme.typography.labelSmall, color = color); Text(logValue(change.after), style = MaterialTheme.typography.bodySmall) }
                    }
                }
            } ?: Column(Modifier.padding(top = 6.dp)) {
                entry.target?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium) }
                entry.detail?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
            }
        }
    }
}

@Composable private fun LogTag(text: String, color: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = .12f)) { Text(text, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color) }
}

internal fun logDate(value: String, pattern: String): String = runCatching {
    Instant.parse(value).atZone(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("ru-RU")))
}.getOrDefault(value)

private fun logValue(value: JsonElement?): String = when (value) { null, JsonNull -> "—"; is JsonPrimitive -> value.content; else -> value.toString() }
private fun logRole(role: String): String = when (role) {
    "ADMIN" -> "Администратор"; "MANAGER" -> "Менеджер"; "WAITER" -> "Официант"; "STOREKEEPER" -> "Кладовщик"
    "WAIT_CONFIRM" -> "Ожидает подтверждения"; "GUEST" -> "Гость"; "SYSTEM" -> "Автоматическое действие"; else -> role
}
