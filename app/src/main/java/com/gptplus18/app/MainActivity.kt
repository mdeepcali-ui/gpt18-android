package com.gptplus18.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.repository.UpdateInfo
import com.gptplus18.app.data.repository.UpdateRepository
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.ui.navigation.GptPlusNavGraph
import com.gptplus18.app.ui.navigation.Routes
import com.gptplus18.app.ui.screens.update.UpdateDialog
import com.gptplus18.app.ui.theme.FontScaleProvider
import com.gptplus18.app.ui.theme.GptPlus18Theme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ctx = LocalContext.current

            var darkMode by remember { mutableStateOf(true) }
            var fontScale by remember { mutableStateOf(1.0f) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

            val entry = remember {
                EntryPointAccessors.fromApplication(
                    ctx.applicationContext,
                    MainEntryPoint::class.java,
                )
            }
            val prefs = entry.prefs()
            val tokenStorage = entry.tokenStorage()

            // ═══ معالجة Deep Link: gptplus18://auth?token=X&name=Y ═══
            var deepLinkRoute by remember { mutableStateOf(parseDeepLink(intent)) }

            LaunchedEffect(intent) {
                val data = intent?.data
                if (data?.scheme == "gptplus18" && data.host == "auth") {
                    val tok = data.getQueryParameter("token")
                    val name = data.getQueryParameter("name") ?: "User"
                    if (!tok.isNullOrBlank()) {
                        tokenStorage.save(tok, name, "", 0)
                        deepLinkRoute = Routes.CHAT
                    }
                }
            }

            LaunchedEffect(Unit) {
                darkMode = prefs.darkModeFlow.first()
                fontScale = prefs.fontScaleFlow.first()

                // فحص التحديثات
                val updateRepo = UpdateRepository()
                updateInfo = updateRepo.checkUpdate()
            }
            LaunchedEffect(prefs) {
                prefs.darkModeFlow.collect { darkMode = it }
            }
            LaunchedEffect(prefs) {
                prefs.fontScaleFlow.collect { fontScale = it }
            }

            GptPlus18Theme(darkTheme = darkMode) {
                FontScaleProvider(fontScale = fontScale) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        GptPlusApp(deepLink = deepLinkRoute)

                        updateInfo?.let { info ->
                            UpdateDialog(
                                info = info,
                                onDismiss = { updateInfo = null },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Deep Link من Google
        val data = intent.data
        if (data?.scheme == "gptplus18" && data.host == "auth") {
            val tok = data.getQueryParameter("token")
            val name = data.getQueryParameter("name") ?: "User"
            if (!tok.isNullOrBlank()) {
                // نحفظ التوكن
                lifecycleScope.launch {
                    val entry = EntryPointAccessors.fromApplication(
                        applicationContext,
                        MainEntryPoint::class.java,
                    )
                    entry.tokenStorage().save(tok, name, "", 0)
                }
                // نعيد تشغيل الـ Activity
                recreate()
            }
        }
    }

    private fun parseDeepLink(intent: Intent?): String? {
        val data = intent?.data ?: return null
        return when {
            data.scheme == "gptplus18" && data.host == "auth" -> Routes.CHAT
            data.path?.startsWith("/chat") == true -> Routes.CHAT
            data.path?.startsWith("/subscription") == true -> Routes.SUBSCRIPTION
            data.path?.startsWith("/admin") == true -> Routes.ADMIN
            else -> null
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface MainEntryPoint {
    fun prefs(): PreferencesRepository
    fun tokenStorage(): TokenStorage
}

@Composable
fun GptPlusApp(deepLink: String? = null) {
    val navController = rememberNavController()
    GptPlusNavGraph(
        navController = navController,
        deepLinkRoute = deepLink,
    )
}
