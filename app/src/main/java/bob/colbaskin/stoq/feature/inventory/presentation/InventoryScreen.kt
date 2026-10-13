package bob.colbaskin.stoq.feature.inventory.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqAsyncImage
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricCard
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.common.drafts.presentation.DraftsBanner
import bob.colbaskin.stoq.common.drafts.presentation.UnsavedChangesDialog
import bob.colbaskin.stoq.common.drafts.presentation.DraftSnackbar

@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    onStorageClick: () -> Unit,
    onDraftsClick: (String?) -> Unit,
    draftId: String? = null,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = LocalColors.current
    var restoredDraftId by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val hasChanges = state.hasDraftableProgress && state.hasUnsavedChanges && state.step != InventoryStep.Report
    val savedDraftsCount = viewModel.drafts.count { it.id != viewModel.activeDraftId }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(draftId, state.lines) {
        if (draftId != null && restoredDraftId != draftId && state.lines.isNotEmpty()) {
            restoredDraftId = draftId
            viewModel.restoreDraft(draftId)
        }
    }
    BackHandler(enabled = hasChanges) { showExitDialog = true }

    Box(Modifier.fillMaxSize().background(colors.background)) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = StoqSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item { InventoryHeader(state.step) { if (hasChanges) showExitDialog = true else onBack() } }
        item { InventoryProgress(state.step) }
        item { DraftsBanner(savedDraftsCount, "инвентаризация") { onDraftsClick(viewModel.activeDraftId) } }
        when (state.step) {
            InventoryStep.Checking -> checkingContent(
                state = state,
                onAction = viewModel::onAction,
                onSaveAndReturn = { viewModel.saveDraft(onStorageClick, showNotice = false, markClean = true) }
            )
            InventoryStep.Confirmation -> confirmationContent(state, viewModel::onAction)
            InventoryStep.Report -> reportContent(state, viewModel::onAction)
        }
    }
    when (val loadState = state.loadingState) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(loadState.title, loadState.text, viewModel::onRetry, onBack = onBack)
        is UiState.Success -> Unit
    }
    if (state.isApplying) LoadingScreen()
    }
    UnsavedChangesDialog(
        visible = showExitDialog,
        entityName = "инвентаризацию",
        onDismiss = { showExitDialog = false },
        onSaveAndExit = { showExitDialog = false; viewModel.saveDraft(onStorageClick, showNotice = false, markClean = true) },
        onDiscardAndExit = { showExitDialog = false; viewModel.discardDraft(onBack) }
    )
    DraftSnackbar(state.notice, { viewModel.onAction(InventoryAction.NoticeDismissed) }, Modifier.padding(bottom = 20.dp))
}

@Composable
private fun InventoryHeader(step: InventoryStep, onBack: () -> Unit) {
    val colors = LocalColors.current
    val subtitle = when (step) {
        InventoryStep.Checking -> "Проверьте фактические остатки на складе"
        InventoryStep.Confirmation -> "Подтвердите изменения перед применением"
        InventoryStep.Report -> "Результаты проверки остатков сохранены"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
            shape = CircleShape,
            color = colors.surfaceSecondary
        ) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", tint = colors.textPrimary) }
        }
        Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
            Text(if (step == InventoryStep.Report) "Отчёт инвентаризации" else "Инвентаризация", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
private fun InventoryProgress(step: InventoryStep) {
    val colors = LocalColors.current
    val stages = listOf("Проверка" to InventoryStep.Checking, "Подтверждение" to InventoryStep.Confirmation, "Отчёт" to InventoryStep.Report)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        stages.forEachIndexed { index, (label, stage) ->
            val completed = step.ordinal > stage.ordinal
            val selected = step == stage
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = if (completed || selected) colors.primary else colors.surfaceSecondary,
                    border = if (completed || selected) null else androidx.compose.foundation.BorderStroke(1.dp, colors.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (completed) Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp), tint = colors.textOnPrimary)
                        else Text((index + 1).toString(), style = MaterialTheme.typography.labelLarge, color = if (selected) colors.textOnPrimary else colors.textSecondary)
                    }
                }
                Text(label, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) colors.textPrimary else colors.textSecondary, maxLines = 1)
            }
            if (index < stages.lastIndex) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp).height(1.dp).background(if (step.ordinal > stage.ordinal) colors.primary else colors.outline))
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.checkingContent(
    state: InventoryState,
    onAction: (InventoryAction) -> Unit,
    onSaveAndReturn: () -> Unit
) {
    val products = state.lines.filter { line ->
        state.query.isBlank() || listOf(line.product.name, line.product.article, line.product.category).any { it.contains(state.query, ignoreCase = true) }
    }
    item {
        Column {
            Text("Проверка товаров", style = MaterialTheme.typography.titleLarge)
            Text("Укажите фактический остаток и подтвердите каждую позицию.", style = MaterialTheme.typography.bodyMedium, color = LocalColors.current.textSecondary)
        }
    }
    item { InventorySearch(state.query) { onAction(InventoryAction.QueryChanged(it)) } }
    item {
        Surface(color = LocalColors.current.surfaceSecondary, shape = MaterialTheme.shapes.small) {
            Row(Modifier.padding(StoqSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, tint = LocalColors.current.primaryDark)
                Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
                    Text("Проверка от 15 октября 2026", style = MaterialTheme.typography.labelLarge)
                    Text("Изменения можно подтвердить на следующем шаге", style = MaterialTheme.typography.bodySmall, color = LocalColors.current.textSecondary)
                }
            }
        }
    }
    if (products.isEmpty()) {
        item { InventoryEmptyState() }
    } else {
        items(products, key = { it.product.batchId }) { line ->
            InventoryProductCard(
                line = line,
                onQuantityChanged = { onAction(InventoryAction.QuantityChanged(line.product.batchId, it)) },
                onChecked = { onAction(InventoryAction.ProductChecked(line.product.batchId)) },
                onWriteOff = { onAction(InventoryAction.QuantityChanged(line.product.batchId, 0.0)) }
            )
        }
    }
    item {
        InventoryActions(
            checked = state.checkedLines,
            total = state.lines.size,
            onSave = onSaveAndReturn,
            onProceed = { onAction(InventoryAction.ProceedToConfirmation) }
        )
    }
}

@Composable
private fun InventorySearch(value: String, onValueChange: (String) -> Unit) {
    val colors = LocalColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Поиск товара по названию или артикулу", maxLines = 1) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = colors.textSecondary) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryDark,
            unfocusedBorderColor = colors.outline,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface
        )
    )
}

@Composable
private fun InventoryProductCard(
    line: InventoryLine,
    onQuantityChanged: (Double) -> Unit,
    onChecked: () -> Unit,
    onWriteOff: () -> Unit
) {
    val colors = LocalColors.current
    StoqCard(contentPadding = PaddingValues(StoqSpacing.Large)) {
        Row(verticalAlignment = Alignment.Top) {
            StoqAsyncImage(null, line.product.name, Modifier.size(64.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.product.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Арт. ${line.product.article}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        InventoryCategory(line.product.category)
                    }
                    StoqStatusPill(line.statusTitle(), line.statusTone())
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = StoqSpacing.Medium), color = colors.outline.copy(alpha = 0.55f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            InventoryFact(Icons.Outlined.Inventory2, "По системе", "${line.product.systemQuantity.formatQuantity()} ${line.product.unit}", Modifier.weight(1f))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Фактически · ${line.product.unit}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                EditableActualQuantity(
                    quantity = line.actualQuantity,
                    onQuantityChanged = onQuantityChanged,
                    onConfirmed = onChecked,
                    onWriteOff = onWriteOff
                )
            }
        }
    }
}

@Composable
private fun EditableActualQuantity(
    quantity: Double,
    onQuantityChanged: (Double) -> Unit,
    onConfirmed: () -> Unit,
    onWriteOff: () -> Unit
) {
    val colors = LocalColors.current
    var editing by remember { mutableStateOf(false) }
    var value by remember(quantity) { mutableStateOf(quantity.formatQuantity()) }
    val parsedValue = value.replace(',', '.').toDoubleOrNull()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints {
        val maximumFieldWidth = (maxWidth - 38.dp).coerceAtLeast(0.dp)
        val defaultFieldWidth = minOf(126.dp, maximumFieldWidth)
        val numberWidth = with(density) {
            textMeasurer.measure(AnnotatedString(value), style = MaterialTheme.typography.bodyLarge).size.width.toDp()
        }
        // 16.dp — левое внутреннее поле Material TextField, 56.dp — место под кнопку подтверждения.
        val editingFieldWidth = (numberWidth + 72.dp).coerceIn(defaultFieldWidth, maximumFieldWidth)

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (editing) {
                Box(modifier = Modifier.width(editingFieldWidth)) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = value.isNotBlank() && parsedValue == null,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    parsedValue?.let {
                                        onQuantityChanged(it.coerceAtLeast(0.0))
                                        onConfirmed()
                                        editing = false
                                    }
                                },
                                enabled = parsedValue != null
                            ) { Icon(Icons.Outlined.Check, "Подтвердить количество", tint = colors.success) }
                        },
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Transparent,
                            unfocusedTextColor = Color.Transparent,
                            focusedBorderColor = colors.primaryDark,
                            unfocusedBorderColor = colors.outline
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text(
                        value,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp, end = 56.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.width(defaultFieldWidth).clip(MaterialTheme.shapes.small).clickable { editing = true },
                    shape = MaterialTheme.shapes.small,
                    color = colors.surfaceSecondary
                ) {
                    Text(
                        quantity.formatQuantity(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onWriteOff, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.DeleteOutline, "Списать продукт", tint = colors.error)
            }
        }
    }
}

@Composable
private fun InventoryFact(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    val colors = LocalColors.current
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(17.dp), tint = colors.textSecondary)
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
    }
}

@Composable
private fun InventoryCategory(category: String) {
    val colors = LocalColors.current
    val tint = when (category) {
        "Рыба и морепродукты" -> colors.primaryDark
        "Бакалея" -> colors.warning
        "Молочные продукты" -> colors.secondary
        else -> colors.success
    }
    Surface(modifier = Modifier.padding(top = StoqSpacing.ExtraSmall), color = tint.copy(alpha = 0.13f), shape = MaterialTheme.shapes.small) {
        Text(category, Modifier.padding(horizontal = StoqSpacing.Small, vertical = StoqSpacing.ExtraSmall), style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun InventoryActions(checked: Int, total: Int, onSave: () -> Unit, onProceed: () -> Unit) {
    val colors = LocalColors.current
    Column {
        Text("Проверено $checked из $total", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        if (checked < total) {
            Text(
                "Остальные позиции будут отмечены без изменений при переходе.",
                modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Small), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f).height(52.dp)) { Text("Сохранить") }
            Button(
                onClick = onProceed,
                modifier = Modifier.weight(1.35f).height(52.dp),
                enabled = total > 0,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)
            ) { Text("К подтверждению") }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.confirmationContent(
    state: InventoryState,
    onAction: (InventoryAction) -> Unit
) {
    item { ConfirmationMetrics(state) }
    item {
        Column {
            Text("Изменения по итогам проверки", style = MaterialTheme.typography.titleLarge)
            Text("Будут применены только позиции с расхождениями.", style = MaterialTheme.typography.bodyMedium, color = LocalColors.current.textSecondary)
        }
    }
    if (state.changedLines.isEmpty()) {
        item { InventoryEmptyState("Расхождений нет", "Все фактические остатки совпадают с системными.") }
    } else {
        items(state.changedLines, key = { it.product.batchId }) { line -> ChangeCard(line) }
    }
    item {
        ConfirmationActions(
            isConfirmed = state.isConfirmed,
            onConfirmed = { onAction(InventoryAction.ConfirmationChanged(it)) },
            onBack = { onAction(InventoryAction.BackToChecking) },
            onApply = { onAction(InventoryAction.ApplyChanges) }
        )
    }
}

@Composable
private fun ConfirmationMetrics(state: InventoryState) {
    val colors = LocalColors.current
    val metrics = listOf(
        Triple("Изменится", state.changedLines.count { !it.isDeleted }.toString(), colors.warning),
        Triple("К удалению", state.deletedLines.size.toString(), colors.error),
        Triple("Без изменений", (state.lines.size - state.changedLines.size).toString(), colors.secondary)
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                row.forEach { metric ->
                    StoqMetricCard(metric.first, metric.second, "", if (metric.first == "К удалению") Icons.Outlined.DeleteOutline else Icons.Outlined.Refresh, metric.third, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChangeCard(line: InventoryLine) {
    val colors = LocalColors.current
    val deleted = line.isDeleted
    StoqCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = if (deleted) colors.error.copy(alpha = 0.12f) else colors.warning.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) { Icon(if (deleted) Icons.Outlined.DeleteOutline else Icons.Outlined.WarningAmber, null, tint = if (deleted) colors.error else colors.warning) }
            }
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(line.product.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    StoqStatusPill(if (deleted) "Удаление" else "Корректировка", if (deleted) StoqStatusTone.Error else StoqStatusTone.Warning)
                }
                Text("Арт. ${line.product.article}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Row(modifier = Modifier.padding(top = StoqSpacing.Medium)) {
                    InventoryFact(Icons.Outlined.Inventory2, "Было", "${line.product.systemQuantity.formatQuantity()} ${line.product.unit}", Modifier.weight(1f))
                    InventoryFact(Icons.Outlined.CheckCircle, "Стало", "${line.actualQuantity.formatQuantity()} ${line.product.unit}", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ConfirmationActions(isConfirmed: Boolean, onConfirmed: (Boolean) -> Unit, onBack: () -> Unit, onApply: () -> Unit) {
    val colors = LocalColors.current
    StoqCard {
        Row(modifier = Modifier.fillMaxWidth().clickable { onConfirmed(!isConfirmed) }, verticalAlignment = Alignment.Top) {
            Checkbox(checked = isConfirmed, onCheckedChange = onConfirmed)
            Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
                Text("Подтверждаю корректность инвентаризации", style = MaterialTheme.typography.labelLarge)
                Text("Я проверил(а) изменения и подтверждаю, что они соответствуют результатам пересчёта.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium), horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp)) { Text("Назад") }
            Button(onClick = onApply, enabled = isConfirmed, modifier = Modifier.weight(1.35f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)) { Text("Применить") }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.reportContent(
    state: InventoryState,
    onAction: (InventoryAction) -> Unit
) {
    item { ReportMetrics(state) }
    item { ReportSuccessBanner() }
    item {
        Text("В отчёте отражено", style = MaterialTheme.typography.titleLarge)
    }
    if (state.changedLines.isEmpty()) {
        item { InventoryEmptyState("Изменений нет", "Все товары были проверены без расхождений.") }
    } else {
        items(state.changedLines, key = { it.product.batchId }) { line -> ReportLineCard(line) }
    }
    item {
        ReportActions(state, onAction)
    }
}

@Composable
private fun ReportActions(state: InventoryState, onAction: (InventoryAction) -> Unit) {
    val context = LocalContext.current
    val downloadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        saveInventoryCsv(context.contentResolver, uri, state)
        onAction(InventoryAction.ReportSaved("CSV"))
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        saveInventoryPdf(context.contentResolver, uri, state)
        onAction(InventoryAction.ReportSaved("PDF"))
    }
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
            OutlinedButton(
                onClick = { downloadLauncher.launch("inventory-report.csv") },
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Outlined.FileDownload, null, modifier = Modifier.size(19.dp))
                Text("Скачать", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
            }
            OutlinedButton(
                onClick = { pdfLauncher.launch("inventory-report.pdf") },
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Outlined.PictureAsPdf, null, modifier = Modifier.size(19.dp))
                Text("В PDF", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
            }
        }
        Button(
            onClick = { onAction(InventoryAction.StartNew) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LocalColors.current.primary, contentColor = LocalColors.current.textOnPrimary)
        ) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(19.dp))
            Text("Новая инвентаризация", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
        }
    }
}

@Composable
private fun ReportMetrics(state: InventoryState) {
    val colors = LocalColors.current
    val metrics = listOf(
        ReportMetric("Проверено", state.lines.size.toString(), colors.success, Icons.Outlined.CheckCircle),
        ReportMetric("Обновлено", state.changedLines.count { !it.isDeleted }.toString(), colors.primaryDark, Icons.Outlined.Refresh),
        ReportMetric("Удалено", state.deletedLines.size.toString(), colors.error, Icons.Outlined.DeleteOutline),
        ReportMetric("Завершено", state.reportTime(), colors.secondary, Icons.Outlined.CalendarToday, state.reportDateTime().substringBefore(","))
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                row.forEach { metric ->
                    StoqMetricCard(
                        label = metric.label,
                        value = metric.value,
                        change = "",
                        icon = metric.icon,
                        tint = metric.tint,
                        modifier = Modifier.weight(1f),
                        supportingText = metric.supportingText
                    )
                }
            }
        }
    }
}

private data class ReportMetric(
    val label: String,
    val value: String,
    val tint: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val supportingText: String = ""
)

@Composable
private fun ReportSuccessBanner() {
    val colors = LocalColors.current
    Surface(color = colors.success.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(StoqSpacing.Large), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = colors.success) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, null, tint = colors.surface) }
            }
            Column(modifier = Modifier.padding(start = StoqSpacing.Medium)) {
                Text("Инвентаризация завершена", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text("Подтверждённые изменения применены к остаткам.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun ReportLineCard(line: InventoryLine) {
    val colors = LocalColors.current
    StoqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (line.isDeleted) Icons.Outlined.DeleteOutline else Icons.Outlined.Refresh, null, tint = if (line.isDeleted) colors.error else colors.success)
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Small)) {
                Text(line.product.name, style = MaterialTheme.typography.titleMedium)
                Text("${line.product.systemQuantity.formatQuantity()} ${line.product.unit} → ${line.actualQuantity.formatQuantity()} ${line.product.unit}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            StoqStatusPill(if (line.isDeleted) "Удалено" else "Обновлено", if (line.isDeleted) StoqStatusTone.Error else StoqStatusTone.Success)
        }
    }
}

@Composable
private fun InventoryEmptyState(title: String = "Товары не найдены", subtitle: String = "Измените поисковый запрос и попробуйте ещё раз.") {
    val colors = LocalColors.current
    StoqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = colors.textSecondary)
            Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

private fun InventoryLine.statusTitle() = when {
    !isChecked -> "К проверке"
    isDeleted -> "К удалению"
    difference != 0.0 -> "Расхождение"
    else -> "Проверено"
}

private fun InventoryLine.statusTone() = when {
    !isChecked -> StoqStatusTone.Info
    isDeleted -> StoqStatusTone.Error
    difference != 0.0 -> StoqStatusTone.Warning
    else -> StoqStatusTone.Success
}

private fun Double.formatQuantity() = if (this % 1.0 == 0.0) toLong().toString() else toString()
