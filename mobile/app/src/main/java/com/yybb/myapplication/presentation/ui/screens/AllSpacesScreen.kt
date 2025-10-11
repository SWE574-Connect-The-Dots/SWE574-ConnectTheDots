package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yybb.myapplication.data.enums.SpaceType

@Composable
fun AllSpacesScreen(spaceType: SpaceType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "All ${spaceType.name.lowercase().replaceFirstChar { it.uppercase() }} Spaces Screen")
    }
}
