package com.gptplus18.app.ui.screens.agecheck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import java.util.Calendar
import java.util.Locale
import com.gptplus18.app.ui.theme.NotoNaskhFontFamily

@Composable
fun AgeCheckScreen(
    onVerified: (String) -> Unit,
    onRejected: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isArabic = Locale.getDefault().language == "ar"

    // ═══ State ═══
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showReject by remember { mutableStateOf(false) }

    // ═══ نصوص ═══
    val title = if (isArabic) "التحقق من العمر" else "Age Verification"
    val subtitle = if (isArabic)
        "هذا التطبيق مخصص للبالغين (18+).\nيرجى إدخال تاريخ ميلادك للمتابعة."
    else
        "This app is for adults (18+).\nPlease enter your birth date to continue."
    val dayLbl = if (isArabic) "اليوم" else "Day"
    val monthLbl = if (isArabic) "الشهر" else "Month"
    val yearLbl = if (isArabic) "السنة" else "Year"
    val btnText = if (isArabic) "تحقق من عمري" else "Verify Age"
    val privacyNote = if (isArabic)
        "🔒 تاريخ ميلادك يُستخدم للتحقق فقط ولا يُخزّن مع معرّف شخصي."
    else
        "🔒 Your birth date is used for verification only."

    // ═══ الحالات ═══
    if (showReject) {
        RejectScreen(
            isArabic = isArabic,
            onExit = onRejected,
        )
        return
    }

    // ═══ التحقق ═══
    fun doVerify() {
        errorMsg = null
        val d = day.trim().toIntOrNull()
        val m = month.trim().toIntOrNull()
        val y = year.trim().toIntOrNull()

        if (d == null || m == null || y == null) {
            errorMsg = if (isArabic) "⚠️ الرجاء إدخال التاريخ كاملاً" else "⚠️ Please fill all fields"
            return
        }
        if (d !in 1..31 || m !in 1..12 || y !in 1900..Calendar.getInstance().get(Calendar.YEAR)) {
            errorMsg = if (isArabic) "⚠️ التاريخ غير صحيح" else "⚠️ Invalid date"
            return
        }

        // حساب العمر
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, d)
        }
        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        val beforeBirthday = today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)
        if (beforeBirthday) age--

        if (age >= 18) {
            val formatted = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
            onVerified(formatted)
        } else {
            showReject = true
        }
    }

    // ═══ الواجهة ═══
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.15f))

            // ═══ أيقونة ═══
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Accent.copy(alpha = 0.12f))
                    .border(1.5.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ═══ العنوان ═══
            Text(
                title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
            )

            Spacer(Modifier.height(36.dp))

            // ═══ الحقول الثلاثة ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DateField(
                    value = day,
                    onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) day = it },
                    label = dayLbl,
                    modifier = Modifier.weight(1f),
                )
                DateField(
                    value = month,
                    onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) month = it },
                    label = monthLbl,
                    modifier = Modifier.weight(1f),
                )
                DateField(
                    value = year,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) year = it },
                    label = yearLbl,
                    modifier = Modifier.weight(1.5f),
                )
            }

            // ═══ رسالة الخطأ ═══
            if (errorMsg != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        errorMsg!!,
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.weight(0.2f))

            // ═══ ملاحظة الخصوصية ═══
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    privacyNote,
                    color = colors.textTertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ═══ الزر ═══
            Button(
                onClick = { doVerify() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Color(0xFF0F0F0F),
                ),
            ) {
                Text(
                    btnText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier) {
        Text(
            label,
            color = colors.textTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = NotoNaskhFontFamily,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = colors.surfaceVariant,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = Accent,
            ),
        )
    }
}

@Composable
private fun RejectScreen(isArabic: Boolean, onExit: () -> Unit) {
    val colors = LocalAppColors.current
    val title = if (isArabic) "غير مسموح" else "Not Allowed"
    val msg = if (isArabic)
        "عذراً، عمرك أقل من 18 سنة.\nلا يمكنك استخدام هذا التطبيق."
    else
        "Sorry, you are under 18.\nYou cannot use this application."
    val btn = if (isArabic) "خروج" else "Exit"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                title,
                color = Color(0xFFEF4444),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                msg,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                ),
            ) {
                Text(btn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
