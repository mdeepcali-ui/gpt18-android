package com.gptplus18.app.ui.screens.disclaimer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
// ⭐ v2.0: شاشة الشروط + سياسة الخصوصية — تصميم احترافي
// ═══════════════════════════════════════════════════════════════

private val BLUE_PRIMARY = Color(0xFF3B82F6)
private val GREEN_SUCCESS = Color(0xFF10B981)

@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isArabic = Locale.getDefault().language == "ar"

    // ═══ النصوص ═══
    val title = if (isArabic) "الشروط والخصوصية" else "Terms & Privacy"
    val subtitle = if (isArabic)
        "يرجى قراءة الشروط قبل المتابعة"
    else
        "Please review the terms before continuing"

    val section1Title = if (isArabic) "تنبيه مهم" else "Important Notice"
    val section1Body = if (isArabic) """
• التطبيق مخصص للبالغين (18+).
• استخدامك للتطبيق على مسؤوليتك الشخصية.
• لا نتحمل مسؤولية أي استخدام غير قانوني.
• لا تستخدم التطبيق لإيذاء الآخرين أو انتهاك حقوقهم.
""".trim() else """
• This app is for adults (18+).
• Use is at your own responsibility.
• We are not liable for any illegal use.
• Do not use it to harm others or violate their rights.
""".trim()

    val section2Title = if (isArabic) "سياسة الخصوصية" else "Privacy Policy"
    val section2Body = if (isArabic) """
• نحفظ بريدك الإلكتروني واسمك لتسجيل الدخول فقط.
• صورتك الشخصية اختيارية ويمكنك حذفها في أي وقت.
• محادثاتك مشفّرة ولا تُشارك مع أي طرف ثالث.
• لا نبيع بياناتك ولا نستخدمها للإعلانات.
• لك الحق في حذف حسابك وبياناتك نهائياً في أي وقت.
""".trim() else """
• We store your email and name for login only.
• Profile picture is optional; you can delete it anytime.
• Your chats are encrypted and never shared with third parties.
• We never sell your data or use it for ads.
• You can permanently delete your account and data anytime.
""".trim()

    val section3Title = if (isArabic) "شروط الاستخدام" else "Terms of Use"
    val section3Body = if (isArabic) """
• يجب أن يكون عمرك 18+ لاستخدام التطبيق.
• يُمنع استخدام التطبيق في أي نشاط ضار أو مخالف للقانون.
• يُمنع إنشاء محتوى يتعلق بالقاصرين أو الأذى الحقيقي.
• نحتفظ بحق إغلاق الحساب عند انتهاك الشروط.
• قد نحدّث الشروط مستقبلاً، وسيتم إشعارك بالتغييرات.
""".trim() else """
• You must be 18+ to use the app.
• Harmful or illegal activity is prohibited.
• Content involving minors or real harm is strictly banned.
• We reserve the right to close accounts that violate terms.
• Terms may be updated; you'll be notified of changes.
""".trim()

    val buttonText = if (isArabic) "أوافق على الشروط" else "I Agree"
    val footerText = if (isArabic)
        "بالمتابعة، أنت تؤكد أنك قرأت وفهمت ووافقت على كل ما ورد أعلاه"
    else
        "By continuing, you confirm you've read and agreed to all the above"

    // ═══ الواجهة ═══
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(28.dp))

            // ═══ العنوان ═══
            Text(
                title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ═══ الأقسام (Scrollable) ═══
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                SectionCard(
                    icon = Icons.Default.Warning,
                    iconColor = BLUE_PRIMARY,
                    title = section1Title,
                    body = section1Body,
                )
                Spacer(Modifier.height(12.dp))
                SectionCard(
                    icon = Icons.Default.Lock,
                    iconColor = GREEN_SUCCESS,
                    title = section2Title,
                    body = section2Body,
                )
                Spacer(Modifier.height(12.dp))
                SectionCard(
                    icon = Icons.Default.CheckCircle,
                    iconColor = BLUE_PRIMARY,
                    title = section3Title,
                    body = section3Body,
                )
                Spacer(Modifier.height(20.dp))
            }

            // ═══ Footer ═══
            Text(
                footerText,
                color = colors.textTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )

            Spacer(Modifier.height(10.dp))

            // ═══ زر الموافقة ═══
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BLUE_PRIMARY,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    body: String,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Column {
            // ═══ أيقونة + عنوان ═══
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(10.dp))

            // ═══ المحتوى ═══
            Text(
                body,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 21.sp,
            )
        }
    }
}
