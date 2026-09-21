package com.gptplus18.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gptplus18.app.ui.screens.auth.AuthScreen
import com.gptplus18.app.ui.screens.chat.ChatScreen
import com.gptplus18.app.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val CHAT = "chat"
}

@Composable
fun GptPlusNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
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
            ChatScreen()
        }
    }
}
