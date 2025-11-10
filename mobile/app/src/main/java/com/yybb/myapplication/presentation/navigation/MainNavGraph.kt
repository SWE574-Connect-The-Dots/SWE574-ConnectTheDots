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
import com.yybb.myapplication.presentation.ui.screens.CreateSpaceScreen
import com.yybb.myapplication.presentation.ui.screens.EditProfileScreen
import com.yybb.myapplication.presentation.ui.screens.ProfileScreen
import com.yybb.myapplication.presentation.ui.screens.SettingsScreen
import com.yybb.myapplication.presentation.ui.screens.SpaceDetailsScreen
import com.yybb.myapplication.presentation.ui.screens.SpaceNodeDetailsScreen
import com.yybb.myapplication.presentation.ui.screens.SpaceNodesScreen
import com.yybb.myapplication.presentation.ui.screens.SpacesScreen
import com.yybb.myapplication.presentation.ui.viewmodel.CreateSpaceViewModel
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
            route = Screen.Profile.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = Screen.SpaceDetails.route,
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) {
            SpaceDetailsScreen(
                onNavigateBack = {
                    if (navController.previousBackStackEntry?.destination?.route == Screen.CreateSpace.route) {
                        navController.navigate(BottomNavItem.Spaces.route) {
                            popUpTo(Screen.SpaceDetails.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onNavigateToSpaceNodes = { spaceId ->
                    navController.navigate(Screen.SpaceNodes.createRoute(spaceId))
                },
                onNavigateToProfile = { username ->
                    navController.navigate(Screen.Profile.createRoute(username))
                }
            )
        }
        composable(
            route = Screen.SpaceNodes.route,
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            SpaceNodesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNodeDetails = { nodeId, nodeLabel, wikidataId ->
                    navController.navigate(Screen.SpaceNodeDetails.createRoute(spaceId, nodeId, nodeLabel, wikidataId))
                }
            )
        }
        composable(
            route = Screen.SpaceNodeDetails.route,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.StringType },
                navArgument("nodeId") { type = NavType.StringType },
                navArgument("nodeLabel") { type = NavType.StringType },
                navArgument("nodeWikidataId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currentSpaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            SpaceNodeDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNodeDetails = { nodeId, nodeLabel, wikidataId ->
                    navController.navigate(Screen.SpaceNodeDetails.createRoute(currentSpaceId, nodeId, nodeLabel, wikidataId))
                }
            )
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
        composable(Screen.CreateSpace.route) {
            val viewModel: CreateSpaceViewModel = hiltViewModel()
            CreateSpaceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { spaceId ->
                    navController.navigate(Screen.SpaceDetails.createRoute(spaceId)) {
                        popUpTo(Screen.CreateSpace.route) { inclusive = true } // remove create from stack
                    }
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
