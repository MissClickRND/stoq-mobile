package bob.colbaskin.stoq.common.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ColorTokens {
    val LightPrimaryContainer = Color(0xFFE3FBD9)
    val LightSurfaceVariant = Color(0xFFF1F5F9)
    val LightErrorContainer = Color(0xFFFFE7E9)
    val DarkPrimary = Color(0xFF94E66C)
    val DarkPrimaryContainer = Color(0xFF204A25)
    val DarkSurfaceVariant = Color(0xFF233142)
    val DarkMutedText = Color(0xFFBAC9DA)
    val DarkError = Color(0xFFFF8D96)
    val DarkErrorContainer = Color(0xFF5E2029)
}

object StoqSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val ScreenHorizontal = 20.dp
}

val StoqShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
