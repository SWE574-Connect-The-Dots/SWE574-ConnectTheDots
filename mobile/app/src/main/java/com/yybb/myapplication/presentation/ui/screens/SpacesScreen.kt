package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import com.yybb.myapplication.presentation.ui.utils.StatefulContent
import com.yybb.myapplication.presentation.ui.viewmodel.SpacesEvent
import com.yybb.myapplication.presentation.ui.viewmodel.SpacesViewModel

@Composable
fun SpacesScreen(
    navController: NavController,
    viewModel: SpacesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.eventFlow.CollectAsEffect { event ->
        when (event) {
            is SpacesEvent.NavigateToSpaceDetails -> {
                navController.navigate(Screen.SpaceDetails.createRoute(event.spaceId))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        StatefulContent(
            state = uiState,
            loadingContent = { CircularProgressIndicator() },
            errorContent = { message -> Text(text = message, color = Color.Red) },
            successContent = { data -> Text(text = data) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { viewModel.fetchData(isSuccess = true) }) {
                Text(text = "Fake Success")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.fetchData(isSuccess = false) }) {
                Text(text = "Fake Error")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { viewModel.onGoToDetailsClicked(1) }) {
            Text(text = "Go to Space Details (ID: 1)")
        }
        Button(onClick = { navController.navigate(Screen.CreateSpace.route) }) {
            Text(text = "Create Space")
        }
    }
}
