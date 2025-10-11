package com.yybb.myapplication.presentation.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import com.yybb.myapplication.presentation.ui.viewmodel.AuthEvent
import com.yybb.myapplication.presentation.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    viewModel.eventFlow.CollectAsEffect { event ->
        when (event) {
            is AuthEvent.NavigateToMain -> {
                navController.navigate(Screen.MainGraph.route) {
                    popUpTo(Screen.AuthGraph.route) {
                        inclusive = true
                    }
                }
            }
            is AuthEvent.NavigateToRegister -> {
                navController.navigate(Screen.Register.route) {
                    popUpTo(Screen.AuthGraph.route) {
                        inclusive = true
                    }
                }
            }
            else -> {

            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Screen")
        Text(text = "This text is localized: ${stringResource(id = R.string.hello_world)}")
        Button(onClick = { viewModel.onLoginClicked() }) {
            Text(text = "Fake Login")
        }
        Button(onClick = { viewModel.onRegisterClicked() }) {
            Text(text = "Register")
        }
    }
}
