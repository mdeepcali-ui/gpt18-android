package com.gptplus18.app.ui.screens.disclaimer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
) {
    val isArabic = Locale.getDefault().language == "ar"

    val title = if (isArabic) "⚠️ تنبيه مهم" else "⚠️ Important Notice"
    val body = if (isArabic) {
        """استخدامك لهذا التطبيق يكون على مسؤوليتك الشخصية.

هذا التطبيق مخصص للأغراض التعليمية والمفيدة فقط.

نحن غير مسؤولين عن أي استخدام غير مسؤول أو غير قانوني قد يصدر من المستخدم.

بالمتابعة، أنت توافق على:
• استخدام التطبيق بمسؤولية
• عدم استخدامه في أي نشاط ضار أو مخالف للقانون
• تحمّل كامل المسؤولية عن أي نتائج تنتج عن استخدامك
"""
    } else {
        """Your use of this application is at your own responsibility.

This app is intended for educational and beneficial purposes only.

We are not responsible for any irresponsible or illegal use by the user.

By continuing, you agree to:
• Use the app responsibly
• Not use it for any harmful or illegal activity
• Take full responsibility for any consequences of your use
"""
    }

    val btnText = if (isArabic) "أوافق وأتحمل المسؤولية" else "I Agree & Accept Responsibility"
    val smallNote = if (isArabic) "بالمتابعة أنت تقرّ بموافقتك على الشروط أعلاه" else "By continuing you acknowledge the terms above"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BgSecondary)
                .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = body,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = smallNote,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Color(0xFF0A0A0A),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = btnText,
                    color = Color(0xFF0A0A0A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
