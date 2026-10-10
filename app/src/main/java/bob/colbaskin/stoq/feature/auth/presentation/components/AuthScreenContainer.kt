package bob.colbaskin.stoq.feature.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing

@Composable
fun AuthScreenContainer(
    title: String,
    subtitle: String,
    onBackClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CustomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-42).dp, y = 18.dp)
                .width(176.dp)
                .height(176.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.18f))
                .blur(34.dp)
                .alpha(0.8f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 52.dp, y = 26.dp)
                .width(206.dp)
                .height(206.dp)
                .clip(CircleShape)
                .background(colors.primaryDark.copy(alpha = 0.15f))
                .blur(42.dp)
        )
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(StoqSpacing.Large)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = colors.textPrimary
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = StoqSpacing.ScreenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = "file:///android_asset/stoq_logo.svg",
                contentDescription = "STOQ",
                modifier = Modifier
                    .fillMaxWidth(0.46f)
                    .height(78.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp),
                style = CustomTheme.typography.inter.headlineLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = StoqSpacing.Small),
                style = CustomTheme.typography.inter.bodyLarge,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.padding(top = 34.dp),
                content = content
            )
        }
    }
}
