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
import com.yybb.myapplication.presentation.ui.screens.AddNodeScreen
import com.yybb.myapplication.presentation.ui.screens.EdgeDetailsScreen
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
                },
                onNavigateToAddNode = {
                    navController.navigate(Screen.AddNode.createRoute(spaceId.toInt()))
                }
            )
        }
        composable(
            route = Screen.AddNode.route,
            arguments = listOf(navArgument("spaceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: ""
            AddNodeScreen(
                navController = navController,
                spaceId = spaceId
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
                },
                onNavigateToEdgeDetails = { edgeId, edgeLabel, sourceId, sourceName, targetId, targetName ->
                    navController.navigate(Screen.EdgeDetails.createRoute(currentSpaceId, edgeId, edgeLabel, sourceId, sourceName, targetId, targetName))
                }
            )
        }
        composable(
            route = Screen.EdgeDetails.route,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.StringType },
                navArgument("edgeId") { type = NavType.StringType },
                navArgument("edgeLabel") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("sourceName") { type = NavType.StringType },
                navArgument("targetId") { type = NavType.StringType },
                navArgument("targetName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val edgeLabel = backStackEntry.arguments?.getString("edgeLabel") ?: return@composable
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: return@composable
            val sourceName = backStackEntry.arguments?.getString("sourceName") ?: return@composable
            val targetId = backStackEntry.arguments?.getString("targetId") ?: return@composable
            val targetName = backStackEntry.arguments?.getString("targetName") ?: return@composable
            EdgeDetailsScreen(
                edgeLabel = edgeLabel,
                sourceId = sourceId,
                sourceName = sourceName,
                targetId = targetId,
                targetName = targetName,
                onNavigateBack = { navController.popBackStack() },
                onUpdateEdge = {
                    // For now, just navigate back - backend integration will come later
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
