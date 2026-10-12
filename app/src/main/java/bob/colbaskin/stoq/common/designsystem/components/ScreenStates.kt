package bob.colbaskin.stoq.common.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(StoqSpacing.Medium))
        Text(
            text = "Загрузка…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ErrorScreen(
    title: String,
    text: String,
    onRetry: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(StoqSpacing.ExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(StoqSpacing.Medium),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(StoqSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    text = text,
                    modifier = Modifier.padding(top = StoqSpacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 4
                )
                if (onBack == null) {
                    StoqPrimaryButton(
                        text = "Повторить",
                        onClick = onRetry,
                        modifier = Modifier.padding(top = StoqSpacing.Large)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = StoqSpacing.Large),
                        horizontalArrangement = Arrangement.spacedBy(StoqSpacing.Small)
                    ) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Назад") }
                        StoqPrimaryButton(text = "Повторить", onClick = onRetry, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
