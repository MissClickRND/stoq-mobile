package bob.colbaskin.stoq.feature.auth.presentation.accessrequest

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.components.StoqTextField
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.domain.model.UserRole
import bob.colbaskin.stoq.feature.auth.presentation.components.AuthScreenContainer

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AccessRequestScreen(
    onRequestSent: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: AccessRequestViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.requestState) {
        if (state.requestState is UiState.Success) onRequestSent()
    }

    AuthScreenContainer(
        title = "Запросить доступ",
        subtitle = "Оставьте заявку, и мы предоставим доступ к системе после проверки"
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            StoqTextField(
                value = state.fullName,
                onValueChange = { viewModel.onAction(AccessRequestAction.FullNameChanged(it)) },
                label = "Имя и фамилия",
                isError = state.fieldError.fullName != null,
                supportingText = state.fieldError.fullName,
                leadingIcon = Icons.Outlined.Person
            )
            Spacer(modifier = Modifier.height(StoqSpacing.Medium))
            StoqTextField(
                value = state.email,
                onValueChange = { viewModel.onAction(AccessRequestAction.EmailChanged(it)) },
                label = "Рабочая почта",
                isError = state.fieldError.email != null,
                supportingText = state.fieldError.email,
                leadingIcon = Icons.Outlined.Email
            )
            Spacer(modifier = Modifier.height(StoqSpacing.Medium))
            StoqTextField(
                value = state.companyName,
                onValueChange = { viewModel.onAction(AccessRequestAction.CompanyNameChanged(it)) },
                label = "Компания или ресторан",
                isError = state.fieldError.companyName != null,
                supportingText = state.fieldError.companyName,
                leadingIcon = Icons.Outlined.Apartment
            )
            Spacer(modifier = Modifier.height(StoqSpacing.Medium))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = state.selectedRole?.title.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                    label = { Text("Роль") },
                    isError = state.fieldError.role != null,
                    supportingText = state.fieldError.role?.let { { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    UserRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.title) },
                            onClick = {
                                viewModel.onAction(AccessRequestAction.RoleSelected(role))
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(StoqSpacing.Medium))
            StoqTextField(
                value = state.reason,
                onValueChange = { viewModel.onAction(AccessRequestAction.ReasonChanged(it)) },
                label = "Зачем вам доступ?",
                isError = state.fieldError.reason != null,
                supportingText = state.fieldError.reason,
                singleLine = false,
                leadingIcon = Icons.Outlined.QuestionAnswer
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(top = StoqSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.consentAccepted,
                    onCheckedChange = { viewModel.onAction(AccessRequestAction.ConsentClicked) }
                )
                Text(
                    text = "Я согласен(на) с обработкой данных",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            state.fieldError.consent?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StoqPrimaryButton(
                text = if (state.requestState is UiState.Loading) "Отправляем..." else "Отправить запрос",
                onClick = { viewModel.onAction(AccessRequestAction.SubmitClicked) },
                modifier = Modifier.padding(top = StoqSpacing.Large),
                enabled = state.requestState !is UiState.Loading
            )
        }
        if (state.requestState is UiState.Error) {
            Text(
                text = state.requestState.text,
                modifier = Modifier.padding(top = StoqSpacing.Medium),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        TextButton(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = StoqSpacing.Large)
        ) {
            Text(
                text = "Уже есть доступ? Войти",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
