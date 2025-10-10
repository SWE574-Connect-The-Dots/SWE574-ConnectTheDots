package com.yybb.myapplication.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object SpaceDetails : Screen("space_details/{spaceId}") {
        fun createRoute(spaceId: Int) = "space_details/$spaceId"
    }

    // Auth graph
    object AuthGraph : Screen("auth_graph")

    // Main graph
    object MainGraph : Screen("main_graph")
}
