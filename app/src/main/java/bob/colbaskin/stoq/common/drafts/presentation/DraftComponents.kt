package bob.colbaskin.stoq.common.drafts.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.drafts.data.Draft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DraftsBanner(count: Int, entityName: String, onClick: () -> Unit) {
    if (count == 0) return
    Surface(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = CustomTheme.colors.primary.copy(alpha = .14f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .24f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.EditNote, null, tint = CustomTheme.colors.primaryDark) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Есть сохранённые черновики", style = MaterialTheme.typography.titleSmall)
                Text("$count ${entityName.declension(count)} - продолжите работу в любой момент", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.EditNote, "Открыть черновики", tint = CustomTheme.colors.primaryDark)
        }
    }
}

@Composable
fun ExitWithDraftHandler(
    hasChanges: Boolean,
    entityName: String,
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit
) {
    var showDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    BackHandler(enabled = hasChanges) { showDialog.value = true }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            icon = { Icon(Icons.Outlined.Save, null, tint = CustomTheme.colors.primaryDark) },
            title = { Text("Сохранить изменения?") },
            text = { Text("Вы не завершили $entityName. Сохраните черновик, чтобы продолжить позже, или выйдите без сохранения.") },
            confirmButton = {
                Button(onClick = { showDialog.value = false; onSaveAndExit() }) { Text("Сохранить и выйти") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog.value = false; onDiscardAndExit() }) { Text("Выйти без сохранения") }
            }
        )
    }
}

@Composable
fun UnsavedChangesDialog(
    visible: Boolean,
    entityName: String,
    onDismiss: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Save, null, tint = CustomTheme.colors.primaryDark) },
        title = { Text("Сохранить изменения?") },
        text = { Text("Вы не завершили $entityName. Сохраните черновик, чтобы продолжить позже, или выйдите без сохранения.") },
        confirmButton = { Button(onClick = onSaveAndExit) { Text("Сохранить и выйти") } },
        dismissButton = { OutlinedButton(onClick = onDiscardAndExit) { Text("Выйти без сохранения") } }
    )
}

@Composable
fun DraftsListScreen(
    title: String,
    drafts: List<Draft>,
    onBack: () -> Unit,
    onOpen: (Draft) -> Unit,
    onDelete: (Draft) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(32.dp).clickable(onClick = onBack).padding(4.dp))
                Spacer(Modifier.width(6.dp))
                Column { Text(title, style = MaterialTheme.typography.headlineMedium); Text("Продолжите работу или удалите ненужный черновик", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if (drafts.isEmpty()) item { Text("Черновиков пока нет", Modifier.padding(vertical = 32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(drafts, key = Draft::id) { draft ->
            StoqCard(Modifier.fillMaxWidth().clickable { onOpen(draft) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(CustomTheme.colors.primary.copy(alpha = .16f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.EditNote, null, tint = CustomTheme.colors.primaryDark) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(draft.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (draft.summary.isNotBlank()) Text(draft.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Изменён ${formatDraftTime(draft.updatedAtEpochMillis)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.DeleteOutline, "Удалить черновик", Modifier.size(38.dp).clip(CircleShape).clickable { onDelete(draft) }.padding(8.dp), tint = CustomTheme.colors.error)
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

private fun String.declension(count: Int): String = when {
    this == "заказ" && count % 10 == 1 && count % 100 != 11 -> "черновик заказа"
    this == "заказ" && count % 10 in 2..4 && count % 100 !in 12..14 -> "черновика заказа"
    this == "поставка" && count % 10 == 1 && count % 100 != 11 -> "черновик поставки"
    this == "поставка" && count % 10 in 2..4 && count % 100 !in 12..14 -> "черновика поставки"
    this == "инвентаризация" && count % 10 == 1 && count % 100 != 11 -> "черновик инвентаризации"
    this == "инвентаризация" && count % 10 in 2..4 && count % 100 !in 12..14 -> "черновика инвентаризации"
    this == "заказ" -> "черновиков заказов"
    this == "поставка" -> "черновиков поставок"
    else -> "черновиков инвентаризации"
}

private fun formatDraftTime(epochMillis: Long): String = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
