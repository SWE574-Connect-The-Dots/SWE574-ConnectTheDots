package com.yybb.myapplication.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
    object Spaces : BottomNavItem("spaces", Icons.Default.Home, "Spaces")
    object ActivityStream : BottomNavItem("activity_stream", Icons.Default.List, "Activity")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
