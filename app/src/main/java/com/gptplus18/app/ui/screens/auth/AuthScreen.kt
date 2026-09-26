package com.gptplus18.app.ui.screens.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.R
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary

private val RedBrand = Color(0xFFE63946)
private val CardBg = Color(0xFF0E0E12)
private val BorderSubtle = Color(0xFF1F1F26)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().background(BgPrimary)) {
        // ═══ خلفية متدرجة أنيقة ═══
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A0D).copy(alpha = 0.6f),
                            Color(0xFF0A0A0C).copy(alpha = 0.3f),
                            Color.Transparent,
                        )
                    )
                ),
        )

        // ═══ نقاط ضوئية ناعمة ═══
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RedBrand.copy(alpha = 0.15f),
                            Color.Transparent,
                        ),
                        center = Offset(0.5f * 1080f, 100f),
                        radius = 500f,
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))

            // ═══ الشعار الشفاف ═══
            Image(
                painter = painterResource(id = R.drawable.logo_transparent),
                contentDescription = "GPT+18",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(180.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ═══ العنوان الفخم ═══
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "GPT",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp,
                )
                Text(
                    "+18",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = RedBrand,
                    letterSpacing = 0.sp,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "\u0630\u0643\u0627\u0621 \u0627\u0635\u0637\u0646\u0627\u0639\u064a \u0628\u0644\u0627 \u0642\u064a\u0648\u062f",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )

            Spacer(Modifier.height(60.dp))

            // ═══ زر Google الفخم ═══
            GoogleSignInButton(
                isLoading = state.isLoading,
                onClick = { vm.openGoogleAuth(ctx) },
            )

            // خطأ
            state.error?.let { err ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A1518))
                        .border(1.dp, RedBrand.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\u26a0\ufe0f", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            err,
                            color = Color(0xFFFF7A7A),
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ═══ شارة الثقة ═══
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("\ud83d\udd12", fontSize = 11.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "\u062f\u062e\u0648\u0644 \u0622\u0645\u0646 \u0628\u062d\u0633\u0627\u0628 Google",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ═══ الشروط ═══
            Text(
                "\u0628\u0627\u0644\u0645\u062a\u0627\u0628\u0639\u0629 \u0623\u0646\u062a \u062a\u0648\u0627\u0641\u0642 \u0639\u0644\u0649\n\u0627\u0644\u0634\u0631\u0648\u0637 \u0648\u0627\u0644\u0623\u062d\u0643\u0627\u0645 \u0648\u0633\u064a\u0627\u0633\u0629 \u0627\u0644\u062e\u0635\u0648\u0635\u064a\u0629",
                color = TextTertiary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
            )

            Spacer(Modifier.height(40.dp))

            // ═══ Footer — علامة تجارية ═══
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(RedBrand),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "GPT+18 \u00b7 \u0646\u0633\u062e\u0629 v1.2.0",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    // Shimmer
    val transition = rememberInfiniteTransition(label = "google_shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3A3A48),
                        Color(0xFF5A5A6E),
                        Color(0xFF3A3A48),
                    ),
                    start = Offset(shimmerX * 800f, 0f),
                    end = Offset(shimmerX * 800f + 400f, 200f),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(enabled = !isLoading, onClick = onClick),
    ) {
        // Shimmer ضوئي
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        start = Offset(shimmerX * 900f, 0f),
                        end = Offset(shimmerX * 900f + 350f, 200f),
                    )
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF4285F4),
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "\u062c\u0627\u0631\u064a \u0627\u0644\u062f\u062e\u0648\u0644...",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                // شعار Google رسمي
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4285F4),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    "\u0627\u0644\u0645\u062a\u0627\u0628\u0639\u0629 \u0628\u062d\u0633\u0627\u0628 Google",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                )
            }
        }
    }
}
