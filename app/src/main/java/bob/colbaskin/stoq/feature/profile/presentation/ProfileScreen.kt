package bob.colbaskin.stoq.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    user: AuthUser,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var signOutConfirmationVisible by remember { mutableStateOf(false) }
    val colors = LocalColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(horizontal = StoqSpacing.ScreenHorizontal, vertical = StoqSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Medium)
    ) {
        item { ProfileHeader(onBack) }
        item { ProfileHero(user) }
        item { AccountDetails(user) }
        item {
            OutlinedButton(
                onClick = { signOutConfirmationVisible = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(20.dp))
                Text("Выйти из аккаунта", modifier = Modifier.padding(start = StoqSpacing.Small))
            }
        }
    }

    if (signOutConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { signOutConfirmationVisible = false },
            title = { Text("Выйти из аккаунта") },
            text = { Text("Чтобы продолжить работу, потребуется снова войти по рабочей почте.") },
            dismissButton = {
                OutlinedButton(onClick = { signOutConfirmationVisible = false }) { Text("Отмена") }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onAction(ProfileAction.SignOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.textOnPrimary)
                ) { Text("Выйти") }
            }
        )
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit) {
    val colors = LocalColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
            shape = CircleShape,
            color = colors.surfaceSecondary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", tint = colors.textPrimary)
            }
        }
        Column(modifier = Modifier.padding(start = StoqSpacing.Small)) {
            Text("Профиль", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
            Text("Управление учётной записью", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
private fun ProfileHero(user: AuthUser) {
    val colors = LocalColors.current
    StoqCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            ProfileAvatar(user, 96.dp)
            Text(
                user.fullName,
                modifier = Modifier.padding(top = StoqSpacing.Medium),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Surface(
                modifier = Modifier.padding(top = StoqSpacing.Small),
                shape = MaterialTheme.shapes.small,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Text(
                    user.role.title,
                    modifier = Modifier.padding(horizontal = StoqSpacing.Medium, vertical = StoqSpacing.ExtraSmall),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primaryDark
                )
            }
        }
    }
}

@Composable
private fun AccountDetails(user: AuthUser) {
    StoqCard {
        Text("Учётная запись", style = MaterialTheme.typography.titleLarge)
        ProfileDetail(Icons.Outlined.PersonOutline, "Имя", user.fullName)
        HorizontalDivider(Modifier.padding(vertical = StoqSpacing.Medium), color = LocalColors.current.outline.copy(alpha = 0.55f))
        ProfileDetail(Icons.Outlined.Email, "Рабочая почта", user.email)
        HorizontalDivider(Modifier.padding(vertical = StoqSpacing.Medium), color = LocalColors.current.outline.copy(alpha = 0.55f))
        ProfileDetail(Icons.Outlined.Badge, "Роль в заведении", user.role.title)
    }
}

@Composable
private fun ProfileDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    val colors = LocalColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = colors.surfaceSecondary) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = colors.primaryDark) }
        }
        Column(modifier = Modifier.padding(start = StoqSpacing.Medium).weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileAvatar(user: AuthUser, size: androidx.compose.ui.unit.Dp) {
    val colors = LocalColors.current
    val initials = user.fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Surface(modifier = Modifier.size(size), shape = CircleShape, color = colors.primary.copy(alpha = 0.14f)) {
        if (user.avatarUrl.isNullOrBlank()) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.primaryDark)
            }
        } else {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Аватар пользователя",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
