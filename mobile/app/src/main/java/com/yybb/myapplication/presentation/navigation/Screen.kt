package com.yybb.myapplication.presentation.navigation

import com.yybb.myapplication.data.enums.SpaceType

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object SpaceDetails : Screen("space_details/{spaceId}") {
        fun createRoute(spaceId: Int) = "space_details/$spaceId"
    }
    object EditProfile : Screen("edit_profile")
    object AllSpaces : Screen("all_spaces/{spaceType}") {
        fun createRoute(spaceType: SpaceType) = "all_spaces/${spaceType.name}"
    }

    // Auth graph
    object AuthGraph : Screen("auth_graph")

    // Main graph
    object MainGraph : Screen("main_graph")
}
