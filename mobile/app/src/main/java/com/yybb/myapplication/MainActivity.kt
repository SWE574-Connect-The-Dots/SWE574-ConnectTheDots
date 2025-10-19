package com.yybb.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.navigation.authNavGraph
import com.yybb.myapplication.presentation.ui.screens.MainScreen
import com.yybb.myapplication.presentation.ui.screens.SplashScreen
import com.yybb.myapplication.presentation.ui.theme.MyApplicationTheme
import com.yybb.myapplication.presentation.ui.viewmodel.SettingsViewModel
import com.yybb.myapplication.util.NavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val isColorBlindTheme by settingsViewModel.isColorBlindTheme.collectAsState()
            MyApplicationTheme(
                colorBlindMode = isColorBlindTheme
            ) {
                RootNavGraph()
            }
        }
    }
}

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        NavigationEvent.events.collectLatest { event ->
            if (event is NavigationEvent.Event.NavigateToLogin) {
                navController.navigate(Screen.AuthGraph.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController)
        }
        authNavGraph(navController)
        composable(route = Screen.MainGraph.route) {
            MainScreen(rootNavController = navController)
        }
    }
}
