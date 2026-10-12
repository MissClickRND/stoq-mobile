package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.R

@Composable
fun StoqTopBar(avatarUrl: String?, showAvatar: Boolean, onProfileClick: () -> Unit) {
    val logoResource = if (isSystemInDarkTheme()) R.drawable.stoq_logo_dark else R.drawable.stoq_logo_light
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(logoResource),
                contentDescription = "STOQ",
                modifier = Modifier.size(width = 96.dp, height = 36.dp),
                contentScale = ContentScale.Fit
            )
            Box(modifier = Modifier.weight(1f))
            if (showAvatar) {
                if (avatarUrl.isNullOrBlank()) {
                    AvatarPlaceholder(onProfileClick)
                } else {
                    StoqAsyncImage(
                        imageUrl = avatarUrl,
                        contentDescription = "Профиль пользователя",
                        modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onProfileClick),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Person, contentDescription = "Профиль пользователя", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
