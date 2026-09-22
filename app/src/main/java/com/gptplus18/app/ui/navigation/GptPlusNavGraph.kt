package com.gptplus18.app.ui.navigation

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
import com.gptplus18.app.ui.screens.code.CodeScreen
import com.gptplus18.app.ui.screens.media.MediaScreen
import com.gptplus18.app.ui.screens.onboarding.OnboardingScreen
import com.gptplus18.app.ui.screens.profile.ProfileScreen
import com.gptplus18.app.ui.screens.splash.SplashScreen
import com.gptplus18.app.ui.screens.settings.SettingsScreen
import com.gptplus18.app.ui.screens.subscription.SubscriptionScreen
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextSecondary

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val CHAT = "chat"
    const val CODE = "code"
    const val MEDIA = "media"
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
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Deep link handling
    androidx.compose.runtime.LaunchedEffect(deepLinkRoute) {
        if (deepLinkRoute != null && currentRoute != deepLinkRoute) {
            navController.navigate(deepLinkRoute)
        }
    }

    val showBottomBar = currentRoute in setOf(
        Routes.CHAT, Routes.CODE, Routes.MEDIA, Routes.SUBSCRIPTION, Routes.PROFILE
    )

    Scaffold(
        containerColor = BgPrimary,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgSecondary) {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, item.label,
                                tint = if (currentRoute == item.route) Accent else TextSecondary) },
                            label = { Text(item.label,
                                color = if (currentRoute == item.route) Accent else TextSecondary,
                                fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToChat = { navController.navigate(Routes.CHAT) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                    onNavigateToLogin = { navController.navigate(Routes.AUTH) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                    onNavigateToOnboarding = { navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } } },
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
                    onNavigateToCode = {
                        navController.navigate(Routes.CODE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMedia = {
                        navController.navigate(Routes.MEDIA) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
            composable(Routes.CODE) { CodeScreen() }
            composable(Routes.MEDIA) { MediaScreen() }
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
