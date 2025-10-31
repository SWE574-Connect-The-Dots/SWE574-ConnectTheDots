package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yybb.myapplication.data.enums.SpaceType
import com.yybb.myapplication.data.model.Space
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.utils.formatDisplayDate
import com.yybb.myapplication.presentation.ui.viewmodel.ProfileUiState
import com.yybb.myapplication.presentation.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(), 
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Success -> {
            val showBackButton = !state.isCurrentUser
            Scaffold(
                topBar = {
                    if (showBackButton) {
                        TopAppBar(
                            title = { Text("") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                ProfileContent(
                    user = state.user,
                    isCurrentUser = state.isCurrentUser,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToAllSpaces = { spaceType ->
                        navController.navigate(Screen.AllSpaces.createRoute(spaceType))
                    },
                    onSpaceClick = { spaceId ->
                        navController.navigate(Screen.SpaceDetails.createRoute(spaceId.toInt()))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
        is ProfileUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(), 
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.message)
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    isCurrentUser: Boolean,
    onEditProfile: () -> Unit,
    onNavigateToAllSpaces: (SpaceType) -> Unit,
    onSpaceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${user.username}'s Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (isCurrentUser) {
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Create, contentDescription = "Edit Profile")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ProfileInfo("Profession", user.profession)
        ProfileInfo("Bio", user.bio ?: "-")
        ProfileInfo("Date Of Birth", formatDisplayDate(user.dateOfBirth))
        ProfileInfo("Joined", formatDisplayDate(user.joinedDate))

        Spacer(modifier = Modifier.height(24.dp))

        SpaceSection(
            title = "Owned Spaces",
            spaces = user.ownedSpaces,
            onSeeMore = { onNavigateToAllSpaces(SpaceType.OWNED) },
            onSpaceClick = onSpaceClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        SpaceSection(
            title = "Joined Spaces",
            spaces = user.joinedSpaces,
            onSeeMore = { onNavigateToAllSpaces(SpaceType.JOINED) },
            onSpaceClick = onSpaceClick
        )
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
        )
        Text(
            text = value, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
fun SpaceSection(
    title: String, spaces: List<Space>, onSeeMore: () -> Unit, onSpaceClick: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (spaces.isNotEmpty()) {
            spaces.take(3).forEach { space ->
                SpaceItem(space, onSpaceClick = { onSpaceClick(space.id) })
            }
            if (spaces.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSeeMore, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("See More")
                }
            }
        } else {
            Text("No spaces to show.")
        }
    }
}

@Composable
fun SpaceItem(space: Space, onSpaceClick: () -> Unit) {
    Card(
        onClick = onSpaceClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = space.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = space.description, color = Color.Gray, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to space",
                tint = Color.Gray
            )
        }
    }
}
