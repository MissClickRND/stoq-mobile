package bob.colbaskin.stoq.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing

@Composable
fun SectionPlaceholderScreen(
    title: String,
    description: String,
    icon: ImageVector
) {
    val colors = CustomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(StoqSpacing.ScreenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color(0xFFE5F9D8), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Color(0xFF005047)
            )
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = StoqSpacing.ExtraLarge),
            style = CustomTheme.typography.inter.headlineMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = StoqSpacing.Small),
            style = CustomTheme.typography.inter.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}
