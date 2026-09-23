package com.gptplus18.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@Composable
fun SplashScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1200)
        try {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SplashEntryPoint::class.java,
            )
            // 1) هل اتعرض Onboarding؟
            val onboardingDone = entry.prefs().onboardingDoneFlow.first()
            if (!onboardingDone) {
                onNavigateToOnboarding()
                return@LaunchedEffect
            }
            // 2) هل في توكن؟
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
            Text(stringResource(R.string.t_009), fontSize = 15.sp, color = Color(0xFF9A9AA0),
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}
