package com.gptplus18.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    CHAT("chat", "دردشة", Icons.Default.Chat),
    CODE("code", "برمجة", Icons.Default.Code),
    MEDIA("media", "وسائط", Icons.Default.Movie),
    SUBSCRIPTION("subscription", "اشتراك", Icons.Default.CreditCard),
    PROFILE("profile", "بروفايل", Icons.Default.Person),
}
