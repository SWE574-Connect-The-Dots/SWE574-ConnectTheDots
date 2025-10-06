package com.yybb.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yybb.myapplication.presentation.ui.screens.ActivityStreamScreen
import com.yybb.myapplication.presentation.ui.screens.ProfileScreen
import com.yybb.myapplication.presentation.ui.screens.SettingsScreen
import com.yybb.myapplication.presentation.ui.screens.SpaceDetailsScreen
import com.yybb.myapplication.presentation.ui.screens.SpacesScreen

@Composable
fun MainNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Spaces.route) {
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }
        composable(BottomNavItem.Spaces.route) {
            SpacesScreen(navController = navController)
        }
        composable(BottomNavItem.ActivityStream.route) {
            ActivityStreamScreen()
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = Screen.SpaceDetails.route,
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) {
            SpaceDetailsScreen()
        }
    }
}
