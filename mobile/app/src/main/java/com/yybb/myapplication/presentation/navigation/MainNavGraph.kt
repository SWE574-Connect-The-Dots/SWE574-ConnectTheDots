package com.yybb.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yybb.myapplication.data.enums.SpaceType
import com.yybb.myapplication.presentation.ui.screens.ActivityStreamScreen
import com.yybb.myapplication.presentation.ui.screens.AllSpacesScreen
import com.yybb.myapplication.presentation.ui.screens.EditProfileScreen
import com.yybb.myapplication.presentation.ui.screens.ProfileScreen
import com.yybb.myapplication.presentation.ui.screens.SettingsScreen
import com.yybb.myapplication.presentation.ui.screens.SpaceDetailsScreen
import com.yybb.myapplication.presentation.ui.screens.SpacesScreen
import com.yybb.myapplication.presentation.ui.viewmodel.EditProfileViewModel

@Composable
fun MainNavGraph(navController: NavHostController, rootNavController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Spaces.route) {
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(navController = rootNavController)
        }
        composable(BottomNavItem.Spaces.route) {
            SpacesScreen(navController = navController)
        }
        composable(BottomNavItem.ActivityStream.route) {
            ActivityStreamScreen()
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = Screen.SpaceDetails.route,
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) {
            SpaceDetailsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.EditProfile.route) {
            val viewModel: EditProfileViewModel = hiltViewModel()
            EditProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSave = { profession, bio ->
                    viewModel.saveProfile(profession, bio)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AllSpaces.route,
            arguments = listOf(navArgument("spaceType") { type = NavType.StringType })
        ) { backStackEntry ->
            val spaceTypeString = backStackEntry.arguments?.getString("spaceType")
            val spaceType = spaceTypeString?.let {
                try {
                    SpaceType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            spaceType?.let { AllSpacesScreen(spaceType = it) }
        }
    }
}
