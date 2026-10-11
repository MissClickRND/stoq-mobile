package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors

enum class StoqStatusTone {
    Success,
    Warning,
    Error,
    Info
}

@Composable
fun StoqStatusPill(
    text: String,
    tone: StoqStatusTone,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val (background, content) = when (tone) {
        StoqStatusTone.Success -> colors.success.copy(alpha = 0.12f) to colors.success
        StoqStatusTone.Warning -> colors.warning.copy(alpha = 0.12f) to colors.warning
        StoqStatusTone.Error -> colors.error.copy(alpha = 0.12f) to colors.error
        StoqStatusTone.Info -> colors.primary.copy(alpha = 0.16f) to colors.primaryDark
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(background)
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(content)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}
