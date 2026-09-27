package com.gptplus18.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.repository.NotificationsRepository
import com.gptplus18.app.data.repository.UpdateInfo
import com.gptplus18.app.data.repository.UpdateRepository
import com.gptplus18.app.ui.navigation.GptPlusNavGraph
import com.gptplus18.app.ui.navigation.Routes
import com.gptplus18.app.ui.screens.update.UpdateDialog
import com.gptplus18.app.ui.theme.FontScaleProvider
import com.gptplus18.app.ui.theme.GptPlus18Theme
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 🔗 Deep Link state — في مستوى Activity (بدل recreate)
    private var deepLinkRoute by mutableStateOf<String?>(null)
    private var deepLinkTick by mutableStateOf(0)

    // 🔔 طلب إذن الإشعارات (Android 13+)
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* النتيجة ما تهم — نتابع */ }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ⭐ v2.0: Edge-to-Edge كامل + دعم النتوء
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        // ⭐ السماح للمحتوى بالامتداد خلف النتوء/شريط الحالة
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 🔔 اطلب إذن الإشعارات فوراً
        requestNotifPermissionIfNeeded()

        // نعالج الـ deep link الأولي
        deepLinkRoute = parseDeepLink(intent)
        if (deepLinkRoute != null) deepLinkTick = 1

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
            val notificationsRepo = entry.notificationsRepo()

            // Deep Link معالجة (cold start)
            LaunchedEffect(intent) {
                val data = intent?.data
                if (data?.scheme == "gptplus18" && data.host == "auth") {
                    val tok = data.getQueryParameter("token")
                    val name = data.getQueryParameter("name") ?: "User"
                    if (!tok.isNullOrBlank()) {
                        tokenStorage.save(tok, name, "", 0)
                        deepLinkRoute = Routes.CHAT
                        deepLinkTick++
                    }
                }
            }

            LaunchedEffect(Unit) {
                darkMode = prefs.darkModeFlow.first()
                fontScale = prefs.fontScaleFlow.first()

                try { notificationsRepo.registerCurrentToken() } catch (_: Exception) {}

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
                        val navController = rememberNavController()
                        GptPlusNavGraph(
                            navController = navController,
                            deepLinkRoute = deepLinkRoute,
                            deepLinkTick = deepLinkTick,
                        )

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

        val data = intent.data
        if (data?.scheme == "gptplus18" && data.host == "auth") {
            val tok = data.getQueryParameter("token")
            val name = data.getQueryParameter("name") ?: "User"
            if (!tok.isNullOrBlank()) {
                lifecycleScope.launch {
                    val entry = EntryPointAccessors.fromApplication(
                        applicationContext,
                        MainEntryPoint::class.java,
                    )
                    entry.tokenStorage().save(tok, name, "", 0)

                    try {
                        entry.notificationsRepo().registerCurrentToken()
                    } catch (_: Exception) {}

                    // 🔥 بدل recreate() — نحدّث الـ state
                    deepLinkRoute = Routes.CHAT
                    deepLinkTick++
                }
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
    fun notificationsRepo(): NotificationsRepository
}
