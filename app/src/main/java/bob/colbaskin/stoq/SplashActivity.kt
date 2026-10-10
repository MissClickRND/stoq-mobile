package bob.colbaskin.stoq

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bob.colbaskin.stoq.common.designsystem.theme.StoqTheme
import kotlin.math.max
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoqTheme(darkTheme = true) {
                SplashScreen(
                    onFinished = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val logoSize = 184.dp
    var isLogoMoving by remember { mutableStateOf(false) }
    val logoTransition = updateTransition(targetState = isLogoMoving, label = "logo entrance")
    val logoScale = remember { Animatable(1f) }

    val logoXOffset by logoTransition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "logo position"
    ) { isMoving ->
        if (isMoving) screenWidth / 2 - logoSize / 2 else -screenWidth
    }
    val logoRotation by logoTransition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "logo rotation"
    ) { isMoving -> if (isMoving) 360f else 0f }

    LaunchedEffect(Unit) {
        delay(500)
        isLogoMoving = true
        delay(1_500)
        logoScale.animateTo(
            targetValue = max(screenWidth.value, screenHeight.value) / logoSize.value,
            animationSpec = tween(500, easing = LinearEasing)
        )
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF172D29))) {
        Image(
            painter = painterResource(R.drawable.stoq_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = -logoXOffset)
                .size(logoSize)
                .scale(logoScale.value)
                .rotate(-logoRotation)
        )
    }
}
