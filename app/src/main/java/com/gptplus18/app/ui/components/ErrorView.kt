package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.LocalAppColors

/**
 * ═══════════════════════════════════════════════════════════════
 *  ErrorView — Universal Error Component
 *  UXRD-2026-10-16-001 · Phase 6 · D-6.3
 * ═══════════════════════════════════════════════════════════════
 *
 *  شاشة خطأ موحّدة — تُستخدم عند فشل:
 *    • الاتصال
 *    • جلب البيانات
 *    • العمليات الحساسة
 *
 *  ⚠️ internal — للاستخدام في كل الشاشات
 */

@Composable
internal fun ErrorView(
    title: String = "صار في خطأ",
    description: String = "تأكد من اتصالك بالإنترنت وجرب مرة تانية",
    modifier: Modifier = Modifier,
    retryText: String = "أعد المحاولة",
    onRetry: (() -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    val errorRed = Color(0xFFE5484D)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ── الأيقونة داخل دائرة حمراء خفيفة ──
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            errorRed.copy(alpha = 0.15f),
                            errorRed.copy(alpha = 0.02f),
                        )
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = errorRed,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── العنوان ──
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        // ── الوصف ──
        Text(
            text = description,
            color = colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )

        // ── زر إعادة المحاولة ──
        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent)
                    .clickable { onRetry() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = retryText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
