package com.gptplus18.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.AccentDark
import com.gptplus18.app.ui.theme.BgPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    // محاكاة فحص التوكن
    LaunchedEffect(Unit) {
        delay(1500)
        // حالياً: نوجه لـ Login
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "GPT+18",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
            Text(
                text = "ذكاء اصطناعي بلا قيود",
                fontSize = 16.sp,
                color = Color(0xFF9A9AA0),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
