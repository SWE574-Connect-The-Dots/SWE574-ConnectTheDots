package com.yybb.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yybb.myapplication.presentation.ui.screens.MainScreen

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.AuthGraph.route
    ) {
        authNavGraph(navController)
        composable(route = Screen.MainGraph.route) {
            MainScreen()
        }
    }
}
