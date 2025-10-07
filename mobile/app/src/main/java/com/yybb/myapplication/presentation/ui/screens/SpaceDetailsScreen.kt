package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceDetailsViewModel

@Composable
fun SpaceDetailsScreen(
    viewModel: SpaceDetailsViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Space Details for ID: ${viewModel.spaceId}")
    }
}
