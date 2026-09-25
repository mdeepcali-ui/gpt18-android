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
    onNavigateToDisclaimer: () -> Unit = {},
    onNavigateToAgeCheck: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1200)

        var hasToken = false
        var disclaimerOk = false
        var ageOk = false

        try {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SplashEntryPoint::class.java,
            )
            hasToken = entry.tokenStorage().getToken() != null
            val prefs = entry.prefs()
            disclaimerOk = prefs.disclaimerAcceptedFlow.first()
            ageOk = prefs.ageVerifiedFlow.first()
        } catch (_: Exception) {
            try {
                val entry = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    SplashEntryPoint::class.java,
                )
                hasToken = entry.tokenStorage().getToken() != null
                val prefs = entry.prefs()
                disclaimerOk = prefs.disclaimerAcceptedFlow.first()
                ageOk = prefs.ageVerifiedFlow.first()
            } catch (_: Exception) {
                hasToken = false
            }
        }

        // 🔞 الترتيب: Disclaimer → AgeCheck → Chat/Login
        when {
            !disclaimerOk -> onNavigateToDisclaimer()
            !ageOk -> onNavigateToAgeCheck()
            hasToken -> onNavigateToChat()
            else -> onNavigateToLogin()
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
