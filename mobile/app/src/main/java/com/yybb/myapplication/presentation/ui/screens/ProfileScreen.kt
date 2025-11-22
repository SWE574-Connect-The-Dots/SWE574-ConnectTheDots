package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.ui.screens.LoadingDialog
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
                    viewModel = viewModel,
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
    viewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onNavigateToAllSpaces: (SpaceType) -> Unit,
    onSpaceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Report-related state
    val reportReasons by viewModel.reportReasons.collectAsState()
    val isLoadingReportReasons by viewModel.isLoadingReportReasons.collectAsState()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsState()
    val reportSubmitSuccess by viewModel.reportSubmitSuccess.collectAsState()
    val reportError by viewModel.reportError.collectAsState()
    
    var showReportDialog by remember { mutableStateOf(false) }
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.clickable {
                            viewModel.prepareReport("profile", user.id.toIntOrNull() ?: 0)
                            showReportDialog = true
                        }
                    )
                }
                if (isCurrentUser) {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Create, contentDescription = "Edit Profile")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ProfileInfo("Profession", user.profession)
        ProfileInfo("Bio", user.bio ?: "-")
        ProfileInfo("Date Of Birth", user.dateOfBirth?.let { formatDisplayDate(it) } ?: "-")
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

    // Loading dialogs
    if (isLoadingReportReasons) {
        LoadingDialog(message = "Loading report reasons...")
    }

    if (isSubmittingReport) {
        LoadingDialog(message = "Submitting report...")
    }

    // Success handling
    LaunchedEffect(reportSubmitSuccess) {
        if (reportSubmitSuccess) {
            Toast.makeText(
                context,
                "Report submitted successfully for profile: ${user.username}",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetReportSubmitSuccess()
            showReportDialog = false
        }
    }

    // Error handling
    if (reportError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearReportError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(reportError!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearReportError() }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }

    // Report Dialog - only show when reasons are loaded
    if (showReportDialog && !isLoadingReportReasons && reportReasons.isNotEmpty() && !isSubmittingReport) {
        ReportProfileDialog(
            username = user.username,
            reasons = reportReasons.map { it.label },
            reasonCodes = reportReasons.map { it.code },
            onDismiss = { showReportDialog = false },
            onSubmit = { reasonLabel, reasonCode ->
                viewModel.submitReport(reasonCode)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportProfileDialog(
    username: String,
    reasons: List<String>,
    reasonCodes: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedReasonIndex by remember { mutableStateOf<Int?>(null) }
    val defaultPlaceholder = "--select a reason--"
    val isReportEnabled = selectedReasonIndex != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title: Report Profile
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Report Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Divider()
                }

                // Profile name with grayish color
                Text(
                    text = "Profile: $username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Reason for report section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Reason for report",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedReasonIndex?.let { reasons[it] } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            placeholder = {
                                Text(text = defaultPlaceholder)
                            },
                            trailingIcon = {
                                androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = dropdownExpanded
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            reasons.forEachIndexed { index, reason ->
                                DropdownMenuItem(
                                    text = { Text(reason) },
                                    onClick = {
                                        selectedReasonIndex = index
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBDBDBD),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = {
                            if (isReportEnabled && selectedReasonIndex != null) {
                                val reasonLabel = reasons[selectedReasonIndex!!]
                                val reasonCode = reasonCodes[selectedReasonIndex!!]
                                onSubmit(reasonLabel, reasonCode)
                            }
                        },
                        enabled = isReportEnabled,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.button_leave),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Report",
                            maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}
