package com.gptplus18.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gptplus18.app.ui.screens.auth.AuthScreen
import com.gptplus18.app.ui.screens.chat.ChatScreen
import com.gptplus18.app.ui.screens.profile.ProfileScreen
import com.gptplus18.app.ui.screens.splash.SplashScreen
import com.gptplus18.app.ui.screens.subscription.SubscriptionScreen
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextSecondary

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val CHAT = "chat"
    const val CODE = "code"
    const val SUBSCRIPTION = "subscription"
    const val PROFILE = "profile"
}

@Composable
fun GptPlusNavGraph(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Routes.CHAT, Routes.CODE, Routes.SUBSCRIPTION, Routes.PROFILE
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
                            icon = {
                                Icon(item.icon, item.label,
                                    tint = if (currentRoute == item.route) Accent else TextSecondary)
                            },
                            label = {
                                Text(item.label,
                                    color = if (currentRoute == item.route) Accent else TextSecondary)
                            },
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
                )
            }
            composable(Routes.AUTH) {
                AuthScreen(onAuthSuccess = {
                    navController.navigate(Routes.CHAT) { popUpTo(Routes.AUTH) { inclusive = true } }
                })
            }
            composable(Routes.CHAT) { ChatScreen() }
            composable(Routes.CODE) { PlaceholderScreen("Code Agent — قريباً") }
            composable(Routes.SUBSCRIPTION) { SubscriptionScreen() }
            composable(Routes.PROFILE) { ProfileScreen() }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = TextSecondary)
    }
}
