package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import coil.compose.AsyncImage
import bob.colbaskin.stoq.R

private const val DefaultAvatarUrl =
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=144&q=80"

@Composable
fun StoqTopBar(avatarUrl: String?) {
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
            AsyncImage(
                model = avatarUrl ?: DefaultAvatarUrl,
                contentDescription = "Профиль пользователя",
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
