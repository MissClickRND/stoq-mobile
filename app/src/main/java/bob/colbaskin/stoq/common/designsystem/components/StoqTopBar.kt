package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

private const val DefaultAvatarUrl =
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=144&q=80"

@Composable
fun StoqTopBar(avatarUrl: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = StoqSpacing.ScreenHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/stoq_topbar_logo.svg")
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "STOQ",
                modifier = Modifier.size(width = 96.dp, height = 34.dp),
                contentScale = ContentScale.Fit
            )
            Box(modifier = Modifier.weight(1f))
            AsyncImage(
                model = avatarUrl ?: DefaultAvatarUrl,
                contentDescription = "Профиль пользователя",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
