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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    val (background, content) = when (tone) {
        StoqStatusTone.Success -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.secondary
        StoqStatusTone.Warning -> Color(0xFFFFF3DA) to Color(0xFFB87100)
        StoqStatusTone.Error -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        StoqStatusTone.Info -> Color(0xFFE4F1FF) to Color(0xFF1674D1)
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
