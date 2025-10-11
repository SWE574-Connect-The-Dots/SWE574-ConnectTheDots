package com.yybb.myapplication.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.yybb.myapplication.presentation.ui.screens.auth.LoginScreen
import com.yybb.myapplication.presentation.ui.screens.auth.RegisterScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Login.route,
        route = Screen.AuthGraph.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
    }
}