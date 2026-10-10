package bob.colbaskin.stoq.common.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun StoqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColors else lightColors
    val typography = remember { MultiTypography(inter = createTypography(InterFontFamily)) }
    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.textOnPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceSecondary,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.outline,
            error = colors.error,
            onError = colors.textOnPrimary
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.textOnPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceSecondary,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.outline,
            error = colors.error,
            onError = colors.surface
        )
    }
    CompositionLocalProvider(LocalColors provides colors, localMultiTypography provides typography) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = StoqTypography,
            shapes = StoqShapes,
            content = content
        )
    }
}

object CustomTheme {
    val colors: AppColors
        @Composable get() = LocalColors.current
    val typography: MultiTypography
        @Composable get() = localMultiTypography.current
}
