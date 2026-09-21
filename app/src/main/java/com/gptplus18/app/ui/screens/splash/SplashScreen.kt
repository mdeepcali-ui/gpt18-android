package com.gptplus18.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(1200)
        try {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SplashEntryPoint::class.java,
            )
            val hasToken = entry.tokenStorage().getToken() != null
            if (hasToken) onNavigateToChat() else onNavigateToLogin()
        } catch (_: Exception) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BgPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GPT+18", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Accent)
            Text("ذكاء اصطناعي بلا قيود", fontSize = 15.sp, color = Color(0xFF9A9AA0),
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}
