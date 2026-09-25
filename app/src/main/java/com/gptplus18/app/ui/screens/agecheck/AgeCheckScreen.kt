package com.gptplus18.app.ui.screens.agecheck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import java.util.Calendar
import java.util.Locale

@Composable
fun AgeCheckScreen(
    onVerified: () -> Unit,
    onRejected: () -> Unit,
) {
    val isArabic = Locale.getDefault().language == "ar"

    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val title = if (isArabic) "🔞 التحقق من العمر" else "🔞 Age Verification"
    val subtitle = if (isArabic) {
        "هذا التطبيق مخصص للمستخدمين من عمر 18 سنة أو أكثر.\nيرجى إدخال تاريخ ميلادك للمتابعة."
    } else {
        "This app is intended for users 18 years or older.\nPlease enter your birth date to continue."
    }
    val dayLbl = if (isArabic) "اليوم" else "Day"
    val monthLbl = if (isArabic) "الشهر" else "Month"
    val yearLbl = if (isArabic) "السنة" else "Year"
    val btnText = if (isArabic) "تحقق من العمر" else "Verify Age"
    val rejectTitle = if (isArabic) "❌ غير مسموح" else "❌ Not Allowed"
    val rejectMsg = if (isArabic) {
        "عذراً، عمرك أقل من 18 سنة.\nلا يمكنك استخدام هذا التطبيق."
    } else {
        "Sorry, you are under 18.\nYou cannot use this application."
    }
    val rejectBtn = if (isArabic) "خروج" else "Exit"
    var showReject by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showReject) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(BgSecondary)
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = rejectTitle,
                    color = Color(0xFFEF4444),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = rejectMsg,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onRejected,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(rejectBtn, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BgSecondary)
                .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) day = it },
                    label = { Text(dayLbl, color = TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Accent,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) month = it },
                    label = { Text(monthLbl, color = TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Accent,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) year = it },
                    label = { Text(yearLbl, color = TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Accent,
                    ),
                    modifier = Modifier.weight(1.3f),
                )
            }

            errorMsg?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Text(msg, color = Color(0xFFEF4444), fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMsg = null
                    val d = day.toIntOrNull()
                    val m = month.toIntOrNull()
                    val y = year.toIntOrNull()
                    val invalidMsg = if (isArabic) "يرجى إدخال تاريخ صحيح" else "Please enter a valid date"
                    if (d == null || m == null || y == null || d !in 1..31 || m !in 1..12 || y !in 1900..2100) {
                        errorMsg = invalidMsg
                        return@Button
                    }
                    val today = Calendar.getInstance()
                    val birth = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, d)
                    }
                    var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--

                    if (age >= 18) {
                        onVerified()
                    } else {
                        showReject = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = btnText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
