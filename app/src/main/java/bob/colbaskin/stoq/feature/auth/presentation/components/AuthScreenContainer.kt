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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.R
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
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.auth_basil), contentDescription = null,
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-78).dp, y = 6.dp).width(174.dp).height(174.dp),
            contentScale = ContentScale.Fit, alpha = 0.78f
        )
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.auth_tomatoes), contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 86.dp, y = 14.dp).width(220.dp).height(220.dp),
            contentScale = ContentScale.Fit
        )
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.auth_pepper_bowl), contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).offset(x = (-86).dp, y = 76.dp).width(215.dp).height(215.dp),
            contentScale = ContentScale.Fit, alpha = 0.9f
        )
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.auth_caprese), contentDescription = null,
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 96.dp, y = 78.dp).width(256.dp).height(256.dp),
            contentScale = ContentScale.Fit, alpha = 0.92f
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
                .padding(horizontal = StoqSpacing.ScreenHorizontal)
                .padding(top = 34.dp, bottom = 86.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AsyncImage(
                model = "file:///android_asset/stoq_logo.svg",
                contentDescription = "STOQ",
                modifier = Modifier
                    .fillMaxWidth(0.46f)
                    .height(70.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 38.dp),
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
                modifier = Modifier.padding(top = 32.dp),
                content = content
            )
        }
    }
}
