package com.gptplus18.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gptplus18.app.ui.screens.admin.AdminScreen
import com.gptplus18.app.ui.screens.about.AboutScreen
import com.gptplus18.app.ui.screens.auth.AuthScreen
import com.gptplus18.app.ui.screens.chat.ChatScreen
import com.gptplus18.app.ui.screens.onboarding.OnboardingScreen
import com.gptplus18.app.ui.screens.profile.ProfileScreen
import com.gptplus18.app.ui.screens.splash.SplashScreen
import com.gptplus18.app.ui.screens.disclaimer.DisclaimerScreen
import com.gptplus18.app.ui.screens.agecheck.AgeCheckScreen
import com.gptplus18.app.ui.screens.settings.SettingsScreen
import com.gptplus18.app.ui.screens.subscription.SubscriptionScreen
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.BgSecondary
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val DISCLAIMER = "disclaimer"
    const val AGE_CHECK = "age_check"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val CHAT = "chat"
    const val SUBSCRIPTION = "subscription"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val ADMIN = "admin"
}

@Composable
fun GptPlusNavGraph(
    navController: NavHostController,
    deepLinkRoute: String? = null,
    deepLinkTick: Int = 0,
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Deep link handling — يتفعل كل مرة tick يتغير
    androidx.compose.runtime.LaunchedEffect(deepLinkTick) {
        if (deepLinkTick > 0 && deepLinkRoute != null) {
            navController.navigate(deepLinkRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        // ⭐ v2.0: Edge-to-Edge — لا نستخدم padding من Scaffold (المحتوى يمتد خلف الأشرطة)
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier,
            // ✨ Animations بين الشاشات
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(200))
            },
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToChat = { navController.navigate(Routes.CHAT) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                    onNavigateToLogin = { navController.navigate(Routes.AUTH) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                    onNavigateToDisclaimer = { navController.navigate(Routes.DISCLAIMER) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                    onNavigateToAgeCheck = { navController.navigate(Routes.AGE_CHECK) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                )
            }
            composable(Routes.DISCLAIMER) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                DisclaimerScreen(
                    onAccept = {
                        scope.launch {
                            try {
                                val entry = dagger.hilt.android.EntryPointAccessors.fromApplication(
                                    ctx.applicationContext,
                                    com.gptplus18.app.ui.screens.splash.SplashEntryPoint::class.java,
                                )
                                entry.prefs().setDisclaimerAccepted()
                            } catch (_: Exception) {}
                            navController.navigate(Routes.AGE_CHECK) {
                                popUpTo(Routes.DISCLAIMER) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(Routes.AGE_CHECK) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                AgeCheckScreen(
                    onVerified = {
                        scope.launch {
                            try {
                                val entry = dagger.hilt.android.EntryPointAccessors.fromApplication(
                                    ctx.applicationContext,
                                    com.gptplus18.app.ui.screens.splash.SplashEntryPoint::class.java,
                                )
                                entry.prefs().setAgeVerified()
                                val hasToken = entry.tokenStorage().getToken() != null
                                if (hasToken) {
                                    navController.navigate(Routes.CHAT) { popUpTo(Routes.AGE_CHECK) { inclusive = true } }
                                } else {
                                    navController.navigate(Routes.AUTH) { popUpTo(Routes.AGE_CHECK) { inclusive = true } }
                                }
                            } catch (_: Exception) {
                                navController.navigate(Routes.AUTH) { popUpTo(Routes.AGE_CHECK) { inclusive = true } }
                            }
                        }
                    },
                    onRejected = {
                        // نغلق التطبيق — أقل من 18
                        try {
                            (ctx as? android.app.Activity)?.finishAffinity()
                        } catch (_: Exception) {}
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.CHAT) {
                ChatScreen(
                    onNavigateToSubscription = {
                        navController.navigate(Routes.SUBSCRIPTION) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Routes.ADMIN)
                    },
                    onNavigateToAuth = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            // ⭐ v2.0: Code + Media دُمجا في Chat (تُستدعى تلقائياً حسب النية)
            composable(Routes.SUBSCRIPTION) { SubscriptionScreen() }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onAdminClick = { navController.navigate(Routes.ADMIN) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onLogout = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onAbout = { navController.navigate(Routes.ABOUT) },
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADMIN) { AdminScreen() }
        }
    }
}
