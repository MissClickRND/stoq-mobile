package bob.colbaskin.stoq.common.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import bob.colbaskin.stoq.R

data class AppTypography(
    val displayLarge: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle
)

data class MultiTypography(val inter: AppTypography)

val InterFontFamily = FontFamily(
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_thin_italic, FontWeight.Thin, style = FontStyle.Italic)
)

fun createTypography(fontFamily: FontFamily) = AppTypography(
    displayLarge = textStyle(fontFamily, FontWeight.Bold, 36.sp, 44.sp),
    headlineLarge = textStyle(fontFamily, FontWeight.Bold, 32.sp, 40.sp),
    headlineMedium = textStyle(fontFamily, FontWeight.Bold, 28.sp, 36.sp),
    titleLarge = textStyle(fontFamily, FontWeight.Bold, 22.sp, 28.sp),
    titleMedium = textStyle(fontFamily, FontWeight.Medium, 16.sp, 24.sp),
    bodyLarge = textStyle(fontFamily, FontWeight.Normal, 16.sp, 24.sp),
    bodyMedium = textStyle(fontFamily, FontWeight.Normal, 14.sp, 20.sp),
    bodySmall = textStyle(fontFamily, FontWeight.Normal, 12.sp, 16.sp),
    labelLarge = textStyle(fontFamily, FontWeight.Medium, 14.sp, 20.sp),
    labelMedium = textStyle(fontFamily, FontWeight.Medium, 12.sp, 16.sp)
)

private fun textStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight
)

val localMultiTypography = staticCompositionLocalOf<MultiTypography> {
    error("No typography provided")
}

val StoqTypography = Typography(
    displayLarge = textStyle(InterFontFamily, FontWeight.Bold, 36.sp, 44.sp),
    headlineLarge = textStyle(InterFontFamily, FontWeight.Bold, 32.sp, 40.sp),
    headlineMedium = textStyle(InterFontFamily, FontWeight.Bold, 28.sp, 36.sp),
    titleLarge = textStyle(InterFontFamily, FontWeight.Bold, 22.sp, 28.sp),
    titleMedium = textStyle(InterFontFamily, FontWeight.Medium, 16.sp, 24.sp),
    bodyLarge = textStyle(InterFontFamily, FontWeight.Normal, 16.sp, 24.sp),
    bodyMedium = textStyle(InterFontFamily, FontWeight.Normal, 14.sp, 20.sp),
    bodySmall = textStyle(InterFontFamily, FontWeight.Normal, 12.sp, 16.sp),
    labelLarge = textStyle(InterFontFamily, FontWeight.Medium, 14.sp, 20.sp),
    labelMedium = textStyle(InterFontFamily, FontWeight.Medium, 12.sp, 16.sp)
)
