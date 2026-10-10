package bob.colbaskin.stoq.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import bob.colbaskin.stoq.R
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing

enum class AuthTab { SignIn, RestaurantRegistration, EmployeeRegistration }

@Composable
fun AuthScreenContainer(
    selectedTab: AuthTab,
    title: String,
    subtitle: String,
    onTabSelected: (AuthTab) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CustomTheme.colors
    val logoResource = if (isSystemInDarkTheme()) R.drawable.stoq_logo_dark else R.drawable.stoq_logo_light
    Box(Modifier.fillMaxSize().background(colors.background).imePadding()) {
        Image(
            painter = painterResource(R.drawable.auth_cosmic_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = StoqSpacing.ScreenHorizontal, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 430.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(PaddingValues(horizontal = 20.dp, vertical = 24.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(logoResource),
                        contentDescription = "STOQ",
                        modifier = Modifier.height(58.dp).fillMaxWidth(0.45f),
                        contentScale = ContentScale.Fit
                    )
                    AuthModeTabs(selectedTab, onTabSelected, Modifier.padding(top = 18.dp))
                    BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 24.dp)) {
                        val titleSize = if (maxWidth < 340.dp) 20.sp else 24.sp
                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth(),
                            style = CustomTheme.typography.inter.headlineLarge.copy(
                                fontSize = titleSize,
                                lineHeight = (titleSize.value + 6).sp
                            ),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = subtitle,
                        modifier = Modifier.padding(top = StoqSpacing.Small),
                        style = CustomTheme.typography.inter.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Column(Modifier.fillMaxWidth().padding(top = 24.dp), content = content)
                }
            }
        }
    }
}

@Composable
private fun AuthModeTabs(selectedTab: AuthTab, onTabSelected: (AuthTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = CustomTheme.colors
    val tabs = listOf(
        AuthTab.SignIn to "Вход в аккаунт",
        AuthTab.RestaurantRegistration to "Регистрация\nресторана",
        AuthTab.EmployeeRegistration to "Регистрация\nсотрудника"
    )
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceSecondary.copy(alpha = 0.82f)).padding(4.dp)
    ) {
        tabs.forEach { (tab, label) ->
            val active = tab == selectedTab
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp))
                    .background(if (active) colors.primary.copy(alpha = 0.46f) else Color.Transparent)
                    .clickable { onTabSelected(tab) }.padding(horizontal = 3.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = CustomTheme.typography.inter.labelMedium.copy(fontSize = 10.sp, lineHeight = 13.sp),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
