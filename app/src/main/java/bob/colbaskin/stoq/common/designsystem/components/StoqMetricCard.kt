package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.LocalColors
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape

@Composable
fun StoqMetricCard(
    label: String,
    value: String,
    change: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(StoqSpacing.Medium)) {
            Box(
                Modifier.size(42.dp).background(tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint)
            }
            Spacer(Modifier.height(StoqSpacing.Small))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                if (change.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text("↑ $change", style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
