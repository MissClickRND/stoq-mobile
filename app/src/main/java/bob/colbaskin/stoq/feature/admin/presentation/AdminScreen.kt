package bob.colbaskin.stoq.feature.admin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.admin.domain.model.AdminSnapshot
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeInvitation
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeRole
import bob.colbaskin.stoq.feature.admin.domain.model.EmployeeStatus
import bob.colbaskin.stoq.feature.admin.domain.model.InvitationStatus
import bob.colbaskin.stoq.feature.admin.domain.model.ManagedEmployee
import bob.colbaskin.stoq.feature.admin.domain.model.VenueProfile

@Composable
fun AdminScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snapshot = state.snapshot ?: return
    val colors = LocalColors.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(colors.background),
        contentPadding = PaddingValues(
            start = StoqSpacing.ScreenHorizontal,
            top = StoqSpacing.Medium,
            end = StoqSpacing.ScreenHorizontal,
            bottom = StoqSpacing.ExtraLarge
        ),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item {
            Column {
                Text("Админ-панель", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Управляйте заведением, командой и доступами",
                    modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            AdminSectionTabs(
                selected = state.section,
                onSelected = { viewModel.onAction(AdminAction.SectionSelected(it)) }
            )
        }
        state.notice?.let { notice ->
            item {
                Surface(
                    color = colors.primary.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = notice,
                        modifier = Modifier.padding(horizontal = StoqSpacing.Large, vertical = StoqSpacing.Medium),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        when (state.section) {
            AdminSection.Venue -> venueItems(snapshot.venue, viewModel::onAction)
            AdminSection.Employees -> employeeItems(snapshot, state, viewModel::onAction)
            AdminSection.Invitations -> invitationItems(snapshot, state, viewModel::onAction)
        }
    }
}

@Composable
private fun AdminSectionTabs(selected: AdminSection, onSelected: (AdminSection) -> Unit) {
    val colors = LocalColors.current
    val sections = listOf(
        AdminSection.Venue to ("Заведение" to Icons.Outlined.Storefront),
        AdminSection.Employees to ("Сотрудники" to Icons.Outlined.Groups),
        AdminSection.Invitations to ("Приглашения" to Icons.Outlined.MailOutline)
    )
    Surface(color = colors.surfaceSecondary.copy(alpha = 0.82f), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            sections.forEach { (section, content) ->
                val isSelected = section == selected
                Surface(
                    modifier = Modifier.weight(1f).height(52.dp).clip(MaterialTheme.shapes.small).clickable { onSelected(section) },
                    color = if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.surfaceSecondary.copy(alpha = 0.82f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(content.second, content.first, modifier = Modifier.size(19.dp), tint = if (isSelected) colors.primaryDark else colors.textSecondary)
                        Text(
                            content.first,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.venueItems(
    venue: VenueProfile,
    onAction: (AdminAction) -> Unit
) {
    item {
        VenueEditor(venue = venue, onSave = { onAction(AdminAction.VenueSaved(it)) })
    }
    item {
        StoqCard {
            val colors = LocalColors.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = MaterialTheme.shapes.small,
                    color = colors.primary.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Business, null, tint = colors.primaryDark)
                    }
                }
                Column(modifier = Modifier.padding(start = StoqSpacing.Medium)) {
                    Text("Данные используются в поставках", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Контактные данные будут видны в связанных документах.",
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueEditor(venue: VenueProfile, onSave: (VenueProfile) -> Unit) {
    var draft by remember(venue) { mutableStateOf(venue) }
    StoqCard {
        Text("Информация о заведении", style = MaterialTheme.typography.titleLarge)
        Text(
            "Основные данные для работы команды и уведомлений.",
            modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.padding(top = StoqSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
        ) {
            StoqTextField(draft.name, { draft = draft.copy(name = it) }, "Название заведения", isRequired = true)
            StoqTextField(draft.city, { draft = draft.copy(city = it) }, "Город", isRequired = true)
            StoqTextField(draft.notificationEmail, { draft = draft.copy(notificationEmail = it) }, "Email для уведомлений", isRequired = true)
            StoqTextField(draft.phone, { draft = draft.copy(phone = it) }, "Телефон", isRequired = true)
            StoqPrimaryButton(
                text = "Сохранить изменения",
                onClick = { onSave(draft) },
                enabled = draft.name.isNotBlank() && draft.city.isNotBlank() && draft.notificationEmail.isNotBlank() && draft.phone.isNotBlank(),
                modifier = Modifier.padding(top = StoqSpacing.Small)
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.employeeItems(
    snapshot: AdminSnapshot,
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    item { TeamMetrics(snapshot) }
    item {
        StoqTextField(
            value = state.employeeQuery,
            onValueChange = { onAction(AdminAction.EmployeeQueryChanged(it)) },
            label = "Поиск сотрудника",
            leadingIcon = Icons.Outlined.Search
        )
    }
    item {
        SectionHeading("Сотрудники", "Меняйте роль в карточке — без горизонтальной прокрутки таблицы.")
    }
    val employees = snapshot.employees.filter {
        it.name.contains(state.employeeQuery, ignoreCase = true) || it.email.contains(state.employeeQuery, ignoreCase = true)
    }
    if (employees.isEmpty()) {
        item { EmptyCard("Сотрудники не найдены", "Измените запрос и попробуйте ещё раз.") }
    } else {
        items(employees.size, key = { employees[it].id }) { index ->
            EmployeeCard(
                employee = employees[index],
                onRoleChanged = { onAction(AdminAction.EmployeeRoleChanged(employees[index].id, it)) },
                onDelete = { onAction(AdminAction.EmployeeDeleted(employees[index].id)) }
            )
        }
    }
}

@Composable
private fun TeamMetrics(snapshot: AdminSnapshot) {
    val employees = snapshot.employees
    val metrics = listOf(
        TeamMetric("Всего сотрудников", employees.size.toString(), "Все сотрудники заведения", Icons.Outlined.Groups, LocalColors.current.success),
        TeamMetric("Активных", employees.count { it.status == EmployeeStatus.Active }.toString(), "Работают в системе", Icons.Outlined.PersonOutline, LocalColors.current.success),
        TeamMetric("Ожидают подтверждения", employees.count { it.status == EmployeeStatus.Pending }.toString(), "Приглашения отправлены", Icons.Outlined.AccessTime, LocalColors.current.warning),
        TeamMetric("Администраторов", employees.count { it.role == EmployeeRole.Administrator }.toString(), "С правами администратора", Icons.Outlined.AdminPanelSettings, LocalColors.current.secondary)
    )
    Column(verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)) {
                row.forEach { metric ->
                    StoqCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(StoqSpacing.Medium)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                color = metric.tint.copy(alpha = 0.14f),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(metric.icon, null, modifier = Modifier.size(22.dp), tint = metric.tint)
                                }
                            }
                            Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
                                Text(metric.label, style = MaterialTheme.typography.bodySmall, color = LocalColors.current.textSecondary, maxLines = 2)
                                Text(metric.value, style = MaterialTheme.typography.titleLarge, color = LocalColors.current.textPrimary)
                            }
                        }
                        Text(metric.subtitle, modifier = Modifier.padding(top = StoqSpacing.Small), style = MaterialTheme.typography.bodySmall, color = LocalColors.current.textSecondary, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeCard(
    employee: ManagedEmployee,
    onRoleChanged: (EmployeeRole) -> Unit,
    onDelete: () -> Unit
) {
    StoqCard {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(employee.initials)
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(employee.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, "Удалить сотрудника", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Text(employee.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.padding(top = StoqSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
                ) {
                    RolePicker(employee.role, onRoleChanged, Modifier.weight(1f))
                    StoqStatusPill(employee.status.title, employee.status.tone())
                }
                Text(
                    "В команде с ${employee.joinedAt}",
                    modifier = Modifier.padding(top = StoqSpacing.Small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.invitationItems(
    snapshot: AdminSnapshot,
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    item {
        InvitationCreator(
            role = state.inviteRole,
            durationHours = state.inviteDurationHours,
            generatedInvitation = state.generatedInvitation,
            onRoleChanged = { onAction(AdminAction.InviteRoleChanged(it)) },
            onDurationChanged = { onAction(AdminAction.InviteDurationChanged(it)) },
            onCreate = { onAction(AdminAction.InviteCreated) }
        )
    }
    item { SectionHeading("Активность приглашений", "Ссылки можно копировать, отключать или удалять прямо из карточки.") }
    if (snapshot.invitations.isEmpty()) {
        item { EmptyCard("Приглашений пока нет", "Создайте первую ссылку для сотрудника.") }
    } else {
        items(snapshot.invitations.size, key = { snapshot.invitations[it].id }) { index ->
            InvitationCard(
                invitation = snapshot.invitations[index],
                onDeactivate = { onAction(AdminAction.InvitationDeactivated(snapshot.invitations[index].id)) },
                onDelete = { onAction(AdminAction.InvitationDeleted(snapshot.invitations[index].id)) }
            )
        }
    }
}

@Composable
private fun InvitationCreator(
    role: EmployeeRole,
    durationHours: Int,
    generatedInvitation: EmployeeInvitation?,
    onRoleChanged: (EmployeeRole) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onCreate: () -> Unit
) {
    StoqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PersonAddAlt1, null, tint = LocalColors.current.primaryDark)
            Text("Приглашение сотрудников", modifier = Modifier.padding(start = StoqSpacing.Small), style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "Создайте одноразовую ссылку с ограниченным сроком действия.",
            modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
        ) {
            RolePicker(role, onRoleChanged, Modifier.weight(1f))
            DurationPicker(durationHours, onDurationChanged, Modifier.weight(1f))
        }
        StoqPrimaryButton("Создать ссылку", onCreate, Modifier.padding(top = StoqSpacing.Medium))
        generatedInvitation?.let { invitation ->
            GeneratedInvitationLink(invitation)
        }
    }
}

@Composable
private fun GeneratedInvitationLink(invitation: EmployeeInvitation) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val colors = LocalColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium),
        color = colors.primary.copy(alpha = 0.14f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(StoqSpacing.Medium)) {
            Text("Ссылка готова", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(
                invitation.link,
                modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
            ) {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(invitation.link)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Text("Копировать", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
                }
                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, invitation.link)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Отправить приглашение"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
                    Text("Поделиться", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: EmployeeInvitation,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val colors = LocalColors.current
    StoqCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = colors.primary.copy(alpha = 0.14f),
                shape = MaterialTheme.shapes.small
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.MailOutline, null, tint = colors.primaryDark)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = StoqSpacing.Medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(invitation.role.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    StoqStatusPill(invitation.status.title, invitation.status.tone())
                }
                Text(
                    invitation.link,
                    modifier = Modifier.padding(top = StoqSpacing.Small),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    invitation.acceptedBy?.let { "Принял: $it" } ?: "До ${invitation.expiresAt}",
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
        ) {
            OutlinedButton(
                onClick = { clipboard.setText(AnnotatedString(invitation.link)) },
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp))
                Text("Копировать", modifier = Modifier.padding(start = StoqSpacing.ExtraSmall))
            }
            if (invitation.status == InvitationStatus.Active) {
                OutlinedButton(onClick = onDeactivate, modifier = Modifier.height(44.dp)) {
                    Icon(Icons.Outlined.Block, "Отключить приглашение", modifier = Modifier.size(18.dp))
                }
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error.copy(alpha = 0.12f),
                    contentColor = colors.error
                )
            ) {
                Icon(Icons.Outlined.DeleteOutline, "Удалить приглашение", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RolePicker(value: EmployeeRole, onSelected: (EmployeeRole) -> Unit, modifier: Modifier = Modifier) {
    SelectPicker(value.title, EmployeeRole.entries.map { it.title }, modifier) { title ->
        onSelected(EmployeeRole.entries.first { it.title == title })
    }
}

@Composable
private fun DurationPicker(value: Int, onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(24, 48, 72)
    SelectPicker("$value ч", options.map { "$it ч" }, modifier) { label ->
        onSelected(label.substringBefore(" ").toInt())
    }
}

@Composable
private fun SelectPicker(value: String, options: List<String>, modifier: Modifier = Modifier, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { expanded = true },
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = StoqSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Outlined.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = MaterialTheme.shapes.medium) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun Avatar(initials: String) {
    val colors = LocalColors.current
    Surface(
        modifier = Modifier.size(44.dp),
        color = colors.primary.copy(alpha = 0.16f),
        shape = MaterialTheme.shapes.large
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, style = MaterialTheme.typography.labelLarge, color = colors.primaryDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            modifier = Modifier.padding(top = 2.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    StoqCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle,
            modifier = Modifier.padding(top = StoqSpacing.ExtraSmall),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun EmployeeStatus.tone() = when (this) {
    EmployeeStatus.Active -> StoqStatusTone.Success
    EmployeeStatus.Pending -> StoqStatusTone.Warning
    EmployeeStatus.Blocked -> StoqStatusTone.Error
}

private fun InvitationStatus.tone() = when (this) {
    InvitationStatus.Active -> StoqStatusTone.Success
    InvitationStatus.Used -> StoqStatusTone.Info
    InvitationStatus.Expired, InvitationStatus.Disabled -> StoqStatusTone.Error
}

private data class TeamMetric(
    val label: String,
    val value: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color
)
