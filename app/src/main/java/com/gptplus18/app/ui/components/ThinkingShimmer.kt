package com.gptplus18.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ☁️ سحابة التفكير — نص رمادي + وهج يمر فوق الحروف مباشرة
 * بدون مستطيل، بدون حدود، بدون خلفية
 */
@Composable
fun ThinkingShimmer(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    maxLines: Int = 8,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )

    val isDark = isSystemInDarkTheme()
    val baseColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6E6E73)
    val shimmerColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)

    val gradient = Brush.horizontalGradient(
        colors = listOf(baseColor, baseColor, shimmerColor, shimmerColor, baseColor, baseColor),
        startX = shimmerX * 400f,
        endX = shimmerX * 400f + 120f,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        BasicText(
            text = "أُفكّر...",
            style = TextStyle(
                fontSize = 11.4.sp,
                fontWeight = FontWeight.SemiBold,
                brush = gradient,
            ),
        )
        if (text.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            BasicText(
                text = text,
                maxLines = maxLines,
                style = TextStyle(
                    fontSize = 10.6.sp,
                    lineHeight = 17.sp,
                    brush = gradient,
                ),
            )
        }
    }
}
