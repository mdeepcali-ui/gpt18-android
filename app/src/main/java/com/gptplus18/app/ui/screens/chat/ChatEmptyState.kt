package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.gptplus18.app.R

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatEmptyState — Empty State Components
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.6
 * ═══════════════════════════════════════════════════════════════
 *
 *  مكونات الحالة الفارغة للشات:
 *    • EmptyLogoState   — شعار + رسالة ترحيب (الافتراضي)
 *    • EmptyChatState   — مقترحات + بطاقات (احتياطي للمستقبل)
 *
 *  ⚠️ EmptyLogoState مستخدم حالياً في ChatScreen السطر 501
 *  ⚠️ EmptyChatState محفوظ للاستخدام المستقبلي
 */

@Composable
internal fun EmptyLogoState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shineTransition = rememberInfiniteTransition(label = "chat_logo_shine")
        val shineX by shineTransition.animateFloat(
            initialValue = -0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(2100),
            ),
            label = "chatShineX",
        )
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_transparent),
            contentDescription = "GPT+18",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier
                .size(85.dp)
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
        Spacer(Modifier.height(16.dp))
        Text(
            "\u0645\u0631\u062d\u0628\u0627 \u0628\u0643 \u0641\u064a GPT+18",
            color = TextPrimary,
            fontSize = 11.3.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}


@Composable
internal fun EmptyChatState(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "\u0627\u0634\u0631\u062d \u0644\u064a \u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064a \ud83d\udca1",
        "\u0627\u0643\u062a\u0628 \u0644\u064a \u062f\u0627\u0644\u0629 Python \ud83d\udcbb",
        "\u0623\u062e\u0628\u0627\u0631 \u0627\u0644\u062a\u0642\u0646\u064a\u0629 \u0627\u0644\u064a\u0648\u0645 \ud83d\udcf0",
        "\u0627\u0635\u0646\u0639 \u0644\u064a \u0635\u0648\u0631\u0629 \u0642\u0637\u0629 \ud83c\udfa8",
        "\u0644\u062e\u0651\u0635 \u0644\u064a \u0645\u0642\u0627\u0644\u0629 \ud83d\udcdd",
        "\u0627\u0642\u062a\u0631\u062d \u0639\u0644\u064a\u0651 \u0641\u0643\u0631\u0629 \u0645\u0634\u0631\u0648\u0639 \ud83e\udd14",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Accent.copy(alpha = 0.08f))
                .border(1.5.dp, Accent.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("\u2728", fontSize = 29.6.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "\u0645\u0631\u062d\u0628\u0627 \u0628\u0643 \u0641\u064a GPT+18",
            color = TextPrimary,
            fontSize = 14.1.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "\u0627\u0628\u062f\u0623 \u0628\u0627\u0644\u0643\u062a\u0627\u0628\u0629 \u0641\u064a \u0627\u0644\u0623\u0633\u0641\u0644 \u0623\u0648 \u0627\u062e\u062a\u0631 \u0627\u0642\u062a\u0631\u0627\u062d\u0627\u064b:",
            color = TextSecondary,
            fontSize = 9.1.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(22.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { s ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0E0E12))
                        .border(1.dp, Color(0xFF1F1F26), RoundedCornerShape(12.dp))
                        .clickable {
                            // نشيل الإيموجي من النهاية
                            onSuggestionClick(s.substringBeforeLast(" "))
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(s, color = TextPrimary, fontSize = 9.1.sp)
                }
            }
        }
    }
}
