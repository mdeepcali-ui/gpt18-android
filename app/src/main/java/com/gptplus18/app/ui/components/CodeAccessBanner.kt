package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import java.util.Locale

/**
 * ⭐ v2.0: Banner مثبت في شاشة البرمجة
 * يظهر فقط للمستخدم المجاني (بدون اشتراك برمجة أو رصيد كافي).
 *
 * رسالة ودية + زر اشتراك.
 */
@Composable
fun CodeAccessBanner(
    userName: String,
    credits: Int,
    usdValue: Double,
    onSubscribe: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isArabic = Locale.getDefault().language == "ar"

    val title = if (isArabic)
        "وضع البرمجة يحتاج رصيد أو اشتراك"
    else
        "Code mode needs credits or subscription"

    val subtitle = if (isArabic)
        "ضيف رصيد أو اشترك بباقة البرمجة للاستفادة من كتابة الأكواد، تشغيل الملفات، والبيئة المعزولة."
    else
        "Add credits or subscribe to unlock coding, file execution, and sandboxed environment."

    val buttonText = if (isArabic)
        "اشترك أو اشحن رصيد"
    else
        "Subscribe / Top-up"

    // رصيد حالي؟
    val hasBalance = credits > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFB800).copy(alpha = 0.10f),
                        Color(0xFFFFB800).copy(alpha = 0.03f),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFFB800).copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable { onSubscribe() }
            .padding(10.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // أيقونة القفل
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFB800).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        color = TextPrimary,
                        fontSize = 11.5.sp, lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (hasBalance) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (isArabic)
                                "رصيدك الحالي: $credits credits (\$${String.format(Locale.US, "%.2f", usdValue)})"
                            else
                                "Balance: $credits credits (\$${String.format(Locale.US, "%.2f", usdValue)})",
                            color = Color(0xFF4ADE80),
                            fontSize = 9.sp, lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 9.5.sp,
                lineHeight = 16.sp,
            )

            Spacer(Modifier.height(8.dp))

            // زر اشترك
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFB800).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { onSubscribe() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    buttonText,
                    color = Color(0xFFFFB800),
                    fontSize = 10.5.sp, lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
