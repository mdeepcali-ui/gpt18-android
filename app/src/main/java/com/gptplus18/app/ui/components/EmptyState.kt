package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.LocalAppColors

/**
 * ═══════════════════════════════════════════════════════════════
 *  EmptyState — Universal Empty State Component
 *  UXRD-2026-10-16-001 · Phase 6 · D-6.1
 * ═══════════════════════════════════════════════════════════════
 *
 *  شاشة فارغة موحّدة — تُستخدم في:
 *    • الشات الفارغ
 *    • المشاريع الفارغة
 *    • السيرفرات الفارغة
 *    • نتائج البحث الفارغة
 *
 *  ⚠️ internal — يُستخدم داخل كل الشاشات
 */

@Composable
internal fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null,
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ── الأيقونة داخل دائرة ──
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Accent.copy(alpha = 0.12f),
                            Accent.copy(alpha = 0.02f),
                        )
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent.copy(alpha = 0.85f),
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── العنوان ──
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        // ── الوصف ──
        Text(
            text = description,
            color = colors.textSecondary,
            fontSize = 11.2.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
        )

        // ── زر CTA اختياري ──
        if (ctaText != null && onCtaClick != null) {
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onCtaClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Color.White,
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp),
            ) {
                Text(
                    text = ctaText,
                    fontSize = 11.2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
