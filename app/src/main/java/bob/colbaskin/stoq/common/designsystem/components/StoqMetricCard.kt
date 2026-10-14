package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon

data class StoqMetric(
    val label: String,
    val value: String,
    val change: String = "",
    val icon: ImageVector,
    val tint: Color,
    val supportingText: String = "",
    val onClick: (() -> Unit)? = null
)

@Composable
fun StoqMetricCard(
    label: String,
    value: String,
    change: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    supportingText: String = "",
    onClick: (() -> Unit)? = null
) {
    val colors = LocalColors.current
    Card(
        modifier = modifier
            .heightIn(min = 140.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.fillMaxHeight().padding(StoqSpacing.Medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(tint.copy(alpha = 0.14f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = tint)
                }
                Spacer(Modifier.width(StoqSpacing.Small))
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                if (change.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text("↑ $change", style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
                }
            }
            if (supportingText.isNotBlank()) {
                Text(
                    supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StoqMetricGrid(
    metrics: List<StoqMetric>,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    require(columns > 0) { "Количество колонок должно быть больше нуля" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
    ) {
        metrics.chunked(columns).forEach { row ->
            if (row.size < columns) {
                val metric = row.single()
                StoqMetricCard(
                    label = metric.label,
                    value = metric.value,
                    change = metric.change,
                    icon = metric.icon,
                    tint = metric.tint,
                    supportingText = metric.supportingText,
                    onClick = metric.onClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
                ) {
                    row.forEach { metric ->
                        StoqMetricCard(
                            label = metric.label,
                            value = metric.value,
                            change = metric.change,
                            icon = metric.icon,
                            tint = metric.tint,
                            supportingText = metric.supportingText,
                            onClick = metric.onClick,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}
