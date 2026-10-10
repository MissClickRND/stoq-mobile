package bob.colbaskin.stoq.common.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnPrimary: Color,
    val outline: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val buttonSecondaryBackground: Color,
    val buttonSecondaryContent: Color
)

val lightColors = AppColors(
    background = Color(0xFFF8FBFC),
    surface = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFF3F8FA),
    primary = Color(0xFF8DDE4B),
    primaryDark = Color(0xFF259A32),
    secondary = Color(0xFF005047),
    textPrimary = Color(0xFF07132D),
    textSecondary = Color(0xFF7184A4),
    textOnPrimary = Color(0xFF06132A),
    outline = Color(0xFFD6E0EB),
    success = Color(0xFF159C20),
    warning = Color(0xFFFFA700),
    error = Color(0xFFF42535),
    buttonSecondaryBackground = Color(0xFFFFFFFF),
    buttonSecondaryContent = Color(0xFF07132D)
)

val darkColors = AppColors(
    background = Color(0xFF081614),
    surface = Color(0xFF10211E),
    surfaceSecondary = Color(0xFF172D29),
    primary = Color(0xFF9BEA68),
    primaryDark = Color(0xFF64BE42),
    secondary = Color(0xFF88D4B2),
    textPrimary = Color(0xFFF4FBF8),
    textSecondary = Color(0xFFB5C9C3),
    textOnPrimary = Color(0xFF071B14),
    outline = Color(0xFF36504A),
    success = Color(0xFF7DEB72),
    warning = Color(0xFFFFC45B),
    error = Color(0xFFFF9197),
    buttonSecondaryBackground = Color(0xFF172D29),
    buttonSecondaryContent = Color(0xFFF4FBF8)
)

val LocalColors = compositionLocalOf { lightColors }
