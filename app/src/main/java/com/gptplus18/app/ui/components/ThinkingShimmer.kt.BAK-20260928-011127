package com.gptplus18.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary

/**
 * ☁️ سحابة التفكير — مع تأثير Shimmer يمر فوق النص
 * الشريط المضيء يتحرك من اليسار لليمين بشكل مستمر
 */
@Composable
fun ThinkingShimmer(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    maxLines: Int = 8,
) {
    // 🌊 تحريك الشريط من -1 إلى 2 (يمر على كل العرض)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    // الخلفية السودا
    val bgColor = Color(0xFF0A0A0A)
    val borderColor = Color(0xFF1F1F1F)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(1.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            // ─── رأس السحابة (بدون أيقونة) ───
            Text(
                text = "أُفكّر...",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            // ─── نص التفكير ───
            if (text.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                ShimmerText(
                    text = text,
                    translateX = translateAnim,
                    maxLines = maxLines,
                )
            }
        }

        // ─── طبقة الشريط المضيء (فوق الكل) ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        start = Offset(translateAnim * 500f, 0f),
                        end = Offset(translateAnim * 500f + 300f, 400f),
                    )
                ),
        )
    }
}

@Composable
private fun ShimmerText(
    text: String,
    translateX: Float,
    maxLines: Int,
) {
    // ⭐ نص رمادي غامق هادئ (طلب المستخدم)
    Text(
        text = text,
        color = androidx.compose.ui.graphics.Color(0xFF6E6E75),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
    )
}
