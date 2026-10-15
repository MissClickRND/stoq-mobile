package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.feature.dashboard.data.model.WriteOffDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WriteOffHistoryEntry(onClick: () -> Unit) {
    StoqCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Список списанных продуктов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Продукты из партий, списанные автоматически по сроку годности", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, "Открыть список списанных продуктов")
        }
    }
}

@Composable
fun WriteOffsDialog(
    state: UiState<List<WriteOffDto>>, onDismiss: () -> Unit, onRetry: () -> Unit,
    isRefreshing: Boolean = false, refreshError: String? = null,
    disposalTarget: WriteOffDto? = null, disposalState: UiState<Unit>? = null,
    onDispose: (WriteOffDto) -> Unit = {}, onDismissDisposal: () -> Unit = {}, onConfirmDisposal: () -> Unit = {}
) {
    val disposing = disposalState == UiState.Loading
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().heightIn(max = 620.dp).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Список списанных продуктов", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss, enabled = !disposing) { Icon(Icons.Outlined.Close, "Закрыть") }
                }
                Text("При истечении срока годности автоматически списываются продукты из партии.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                if (refreshError != null) Text(refreshError, color = MaterialTheme.colorScheme.error)
                PullToRefreshContainer(isRefreshing && state is UiState.Success, onRetry, Modifier.fillMaxWidth().heightIn(min = 100.dp).weight(1f, fill = false)) {
                when (state) {
                    UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { Text(state.text, color = MaterialTheme.colorScheme.error) }
                            item { TextButton(onClick = onRetry) { Text("Повторить") } }
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (state.data.isEmpty()) item { Text("Продуктов, ожидающих утилизации, нет.", Modifier.padding(vertical = 16.dp)) }
                            if (state.data.isNotEmpty()) item { Text("Записей о списании: ${state.data.size}", style = MaterialTheme.typography.labelLarge) }
                            items(state.data, key = { "${it.batchId}:${it.writtenOffAt}" }) { item ->
                                StoqCard {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.product.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    }
                                    Text("Партия № ${item.batchId} · Арт. ${item.productId}")
                                    Text("Списано: ${NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU")).format(item.writtenOffQuantity)} ${item.product.unit?.shortName.orEmpty()}", color = MaterialTheme.colorScheme.error)
                                    Text("Срок годности: ${writeOffDate(item.expiresAt)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Списано: ${writeOffDate(item.writtenOffAt)}", style = MaterialTheme.typography.bodySmall)
                                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                                        Text(if (item.reason == "EXPIRED") "Списано · истёк срок годности" else "Списано · ${item.reason}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(onClick = { onDispose(item) }, enabled = !disposing, modifier = Modifier.fillMaxWidth()) {
                                        Text("Утилизировать")
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
    disposalTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { if (!disposing) onDismissDisposal() },
            title = { Text("Подтвердить утилизацию?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${item.product.name} · партия № ${item.batchId}")
                    Text("Подтвердите, что списанные продукты из этой партии уже выброшены. Они исчезнут из списка, история списания сохранится. Остатки повторно списываться не будут.")
                    if (disposalState is UiState.Error) Text(disposalState.text, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = onConfirmDisposal, enabled = !disposing) {
                    if (disposing) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (disposing) "Подтверждение…" else "Подтвердить")
                }
            },
            dismissButton = { TextButton(onClick = onDismissDisposal, enabled = !disposing) { Text("Отмена") } }
        )
    }
}

private fun writeOffDate(value: String): String = runCatching {
    Instant.parse(value).atZone(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"))
}.getOrDefault(value)
