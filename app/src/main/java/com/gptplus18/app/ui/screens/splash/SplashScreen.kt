package com.gptplus18.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun SplashScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDisclaimer: () -> Unit = {},
    onNavigateToAgeCheck: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1200)

        var hasToken = false
        var disclaimerOk = false
        var ageOk = false

        try {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SplashEntryPoint::class.java,
            )
            hasToken = entry.tokenStorage().getToken() != null
            val prefs = entry.prefs()
            disclaimerOk = prefs.disclaimerAcceptedFlow.first()
            ageOk = prefs.ageVerifiedFlow.first()
        } catch (_: Exception) {
            try {
                val entry = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    SplashEntryPoint::class.java,
                )
                hasToken = entry.tokenStorage().getToken() != null
                val prefs = entry.prefs()
                disclaimerOk = prefs.disclaimerAcceptedFlow.first()
                ageOk = prefs.ageVerifiedFlow.first()
            } catch (_: Exception) {
                hasToken = false
            }
        }

        // 🔞 الترتيب: Disclaimer → AgeCheck → Chat/Login
        when {
            !disclaimerOk -> onNavigateToDisclaimer()
            !ageOk -> onNavigateToAgeCheck()
            hasToken -> onNavigateToChat()
            else -> onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding().background(BgPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val shineTransition = rememberInfiniteTransition(label = "splash_shine")
            val shineX by shineTransition.animateFloat(
                initialValue = -0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(2100),
                ),
                label = "shineX",
            )
            Image(
                painter = painterResource(id = R.drawable.logo_transparent),
                contentDescription = "GPT+18",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        val w = size.width
                        val cx = w * shineX
                        val bandW = w * 0.55f
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0f),
                                ),
                                startX = cx - bandW / 2f,
                                endX = cx + bandW / 2f,
                            ),
                            blendMode = BlendMode.SrcAtop,
                        )
                    },
            )
            Text(
                text = stringResource(R.string.t_009),
                fontSize = 13.2.sp,
                color = Color(0xFF9A9AA0),
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
