package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.BottomNavItem
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.screens.components.ActivityCard
import com.yybb.myapplication.presentation.ui.utils.navigateFromActivity
import com.yybb.myapplication.presentation.ui.viewmodel.ActivityStreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityStreamScreen(
    navController: NavHostController? = null,
    viewModel: ActivityStreamViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUsername = remember { viewModel.getCurrentUsername() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.activity_stream_title)) },
                actions = {
                    // Refresh button
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(
                                color = Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.refresh() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.refresh_button),
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        color = Color.Red
                    )
                }
            }
            else -> {
                if (activities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No activities found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = activities,
                            key = { activity -> activity.id }
                        ) { activity ->
                            ActivityCard(
                                activity = activity,
                                onCardClick = {
                                    navController?.let { nav ->
                                        navigateFromActivity(activity, nav)
                                    }
                                },
                                onActorClick = { actorName ->
                                    navController?.let { nav ->
                                        // Navigate to profile page
                                        if (actorName == currentUsername) {
                                            // Navigate to current user's profile
                                            nav.navigate(BottomNavItem.Profile.route)
                                        } else {
                                            // Navigate to other user's profile
                                            nav.navigate(Screen.Profile.createRoute(actorName))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}