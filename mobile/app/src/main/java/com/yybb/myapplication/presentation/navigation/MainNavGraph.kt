package com.yybb.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.yybb.myapplication.presentation.ui.screens.WebViewScreen
import com.yybb.myapplication.presentation.ui.viewmodel.EdgeDetailsViewModel
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
            ActivityStreamScreen(navController = navController)
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
                    if (username.isEmpty()) {
                        // Navigate to current user's profile via bottom nav
                        navController.navigate(BottomNavItem.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.navigate(Screen.Profile.createRoute(username))
                    }
                },
                onNavigateFromActivity = { activity ->
                    com.yybb.myapplication.presentation.ui.utils.navigateFromActivity(activity, navController)
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
                    val currentNodeId = backStackEntry.arguments?.getString("nodeId") ?: ""
                    navController.navigate(Screen.EdgeDetails.createRoute(currentSpaceId, edgeId, edgeLabel, sourceId, sourceName, targetId, targetName, currentNodeId))
                },
                onNavigateToWebView = { url ->
                    navController.navigate(Screen.WebView.createRoute(url))
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
                navArgument("targetName") { type = NavType.StringType },
                navArgument("currentNodeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currentSpaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            val viewModel: EdgeDetailsViewModel = hiltViewModel()
            EdgeDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNodeDetails = { nodeId, nodeLabel, wikidataId ->
                    navController.navigate(Screen.SpaceNodeDetails.createRoute(currentSpaceId, nodeId, nodeLabel, wikidataId))
                },
                onUpdateEdge = {
                    // This is handled internally by EdgeDetailsScreen
                },
                viewModel = viewModel
            )
        }
        composable(
            route = Screen.WebView.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: return@composable
            WebViewScreen(
                url = android.net.Uri.decode(url),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            val viewModel: EditProfileViewModel = hiltViewModel()
            EditProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSave = { profession, bio, city, country ->
                    viewModel.saveProfile(profession, bio, city, country)
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
