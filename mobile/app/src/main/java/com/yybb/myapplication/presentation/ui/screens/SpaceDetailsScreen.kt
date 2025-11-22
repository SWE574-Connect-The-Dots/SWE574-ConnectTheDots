package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.Constants.COMMENTS_PER_PAGE
import com.yybb.myapplication.data.Constants.DISCUSSION_SECTION_HEIGHT
import com.yybb.myapplication.data.Constants.MAX_COMMENT_LENGTH
import com.yybb.myapplication.data.model.Discussion
import com.yybb.myapplication.data.model.VoteType
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.screens.components.CollaboratorDialog
import com.yybb.myapplication.presentation.ui.screens.components.DiscussionCard
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceDetailsViewModel
import com.yybb.myapplication.presentation.ui.screens.LoadingDialog
import kotlinx.coroutines.launch

data class Collaborator(
    val id: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceDetailsScreen(
    viewModel: SpaceDetailsViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit = {},
    onNavigateToSpaceNodes: (Int) -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
) {
    val spaceDetails by viewModel.spaceDetails.collectAsState()
    val discussions by viewModel.discussions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingDiscussions by viewModel.isLoadingDiscussions.collectAsState()
    val isAddingDiscussion by viewModel.isAddingDiscussion.collectAsState()
    val isJoiningLeavingSpace by viewModel.isJoiningLeavingSpace.collectAsState()
    val isDeletingSpace by viewModel.isDeletingSpace.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val isLoadingProfile by viewModel.isLoadingProfile.collectAsState()
    val profileLoadSuccess by viewModel.profileLoadSuccess.collectAsState()
    val voteRequiresCollaboratorError by viewModel.voteRequiresCollaboratorError.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoadingReportReasons by viewModel.isLoadingReportReasons.collectAsState()
    val reportReasons by viewModel.reportReasons.collectAsState()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsState()
    val reportSubmitSuccess by viewModel.reportSubmitSuccess.collectAsState()
    
    var newComment by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    var showCollaboratorDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportDialogTitle by remember { mutableStateOf("") }
    var reportDialogContentType by remember { mutableStateOf("space") }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var discussionSectionOffset by remember { mutableStateOf(0f) }
    var isInitialLoad by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    
    val paginationInfo = remember(discussions, currentPage) {
        calculatePagination(discussions, currentPage)
    }

    LaunchedEffect(currentPage) {
        if (!isInitialLoad && discussionSectionOffset > 0) {
            coroutineScope.launch {
                scrollState.animateScrollTo(discussionSectionOffset.toInt())
            }
        }
        if (isInitialLoad) {
            isInitialLoad = false
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.comment_added_msg))
            showSuccessMessage = false
        }
    }

    if (isAddingDiscussion) {
        LoadingDialog(message = stringResource(R.string.adding_discussions_message))
    }

    if (isJoiningLeavingSpace) {
        LoadingDialog(message = if (viewModel.isUserCollaborator())
            stringResource(R.string.leaving_space_message)
        else
            stringResource(R.string.joining_space_message))
    }

    if (isDeletingSpace) {
        LoadingDialog(message = stringResource(R.string.deleting_space_message))
    }

    if (isLoadingProfile) {
        LoadingDialog(message = stringResource(R.string.loading_message))
    }

    if (isLoadingReportReasons) {
        LoadingDialog(message = "Loading report reasons...")
    }

    if (isSubmittingReport) {
        LoadingDialog(message = "Submitting report...")
    }

    LaunchedEffect(profileLoadSuccess) {
        profileLoadSuccess?.let { username ->
            viewModel.resetProfileLoadSuccess()
            onNavigateToProfile(username)
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.space_deleted_successfully_message),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetDeleteSuccess()
            onNavigateBack()
        }
    }

    // Vote requires collaborator error dialog
    if (voteRequiresCollaboratorError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearVoteRequiresCollaboratorError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.vote_requires_collaborator_message)) },
            confirmButton = {
                Button(onClick = { viewModel.clearVoteRequiresCollaboratorError() }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }

    if (error != null && !isAddingDiscussion && !isJoiningLeavingSpace && !isDeletingSpace && !isLoading && !voteRequiresCollaboratorError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(error!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.space_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.space_actions),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Delete Space Menu Item (only visible to creator)
                            if (viewModel.isUserCreator()) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = stringResource(R.string.delete_space_button),
                                            color = colorResource(id = R.color.button_leave)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = colorResource(id = R.color.button_leave)
                                        )
                                    }
                                )
                            }
                            // Report Space Menu Item
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = stringResource(R.string.report_space_button),
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    reportDialogTitle = spaceDetails?.title ?: ""
                                    reportDialogContentType = "space"
                                    viewModel.prepareReport("space", spaceDetails?.id ?: 0)
                                    showReportDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)

        ) {
            when {
                isLoading && !isAddingDiscussion && !isJoiningLeavingSpace -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.loading_message))
                    }
                }
                error != null && !isAddingDiscussion && !isJoiningLeavingSpace -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: $error")
                    }
                }
                spaceDetails != null -> {
                    val currentSpace = spaceDetails!!
                    
                    // Action Buttons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Leave/Join Button
                        Button(
                            onClick = {
                                if (viewModel.isUserCollaborator()) {
                                    viewModel.leaveSpace()
                                } else {
                                    viewModel.joinSpace()
                                }
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.isUserCollaborator())
                                    colorResource(id = R.color.button_leave)
                                else
                                    colorResource(id = R.color.button_join)
                            )
                        ) {
                            Text(
                                text = if (viewModel.isUserCollaborator())
                                    stringResource(R.string.leave_space_button)
                                else
                                    stringResource(R.string.join_space_button),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                    
                    // Space Title
                    Text(
                        text = currentSpace.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Space Description
                    Text(
                        text = currentSpace.description,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Tags Display
                    if (currentSpace.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentSpace.tags.forEach { tag ->
                                Card(
                                    modifier = Modifier.wrapContentWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.LightGray
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Text(
                                        text = tag.name,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // See Collaborators Button
                    TextButton(
                        onClick = { showCollaboratorDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Blue
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = context.getString(R.string.see_collab, currentSpace.collaborators.size),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // See Space Graph Button
                    Button(
                        onClick = { onNavigateToSpaceNodes(currentSpace.id) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = context.getString(R.string.see_space_graph),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

            // Discussion Section
            Text(
                text = context.getString(R.string.discussion_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Discussion Cards (Scrollable with fixed height)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DISCUSSION_SECTION_HEIGHT.dp)
                    .padding(bottom = 8.dp)
                    .onGloballyPositioned { coordinates ->
                        discussionSectionOffset = with(density) { coordinates.positionInParent().y }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Show current page comments
                    val currentUsername = viewModel.getCurrentUsername()
                    paginationInfo.currentPageDiscussions.forEach { discussion ->
                        key(discussion.id) {
                            DiscussionCard(
                                discussion = discussion,
                                currentUsername = currentUsername,
                                onVoteClick = { discussionId, voteType ->
                                    val voteValue = when (voteType) {
                                        VoteType.UP -> "up"
                                        VoteType.DOWN -> "down"
                                        VoteType.NONE -> return@DiscussionCard
                                    }
                                    viewModel.voteDiscussion(discussionId, voteValue)
                                },
                                onReportClick = { discussionId ->
                                    reportDialogTitle = "Discussion by ${discussion.username}"
                                    reportDialogContentType = "discussion"
                                    viewModel.prepareReport("discussion", discussionId.toIntOrNull() ?: 0)
                                    showReportDialog = true
                                }
                        )
                        }
                    }
                }
            }

            // Pagination Controls
            if (paginationInfo.totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Generate pagination numbers
                    val paginationNumbers = generatePaginationNumbers(currentPage, paginationInfo.totalPages)

                    paginationNumbers.forEach { pageNumber ->
                        if (pageNumber == "...") {
                            Text(
                                text = "...",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            // Page number text button
                            TextButton(
                                onClick = { currentPage = pageNumber as Int },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (currentPage == pageNumber) Color.Black else Color.Gray
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = pageNumber.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (currentPage == pageNumber) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Join Discussion Section
            if (viewModel.isUserCollaborator()) {
                // Show discussion input for collaborators
                Text(
                    text = context.getString(R.string.join_discussion_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Comment Input
                OutlinedTextField(
                    value = newComment,
                    onValueChange = {
                        if (it.length <= MAX_COMMENT_LENGTH) {
                            newComment = it
                        }
                    },
                    placeholder = { Text(context.getString(R.string.write_comment_hint_msg)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = MaterialTheme.shapes.small.copy(all = CornerSize(8.dp)),
                    maxLines = 4
                )

                // Character count
                Text(
                    text = "${newComment.length}/${MAX_COMMENT_LENGTH}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp)
                )

                // Share Button
                Button(
                    onClick = {
                        if (newComment.isNotBlank()) {
                            viewModel.addDiscussion(newComment)
                            newComment = ""
                            showSuccessMessage = true
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newComment.isNotBlank()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.login_icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = context.getString(R.string.share_button),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Show join message for non-collaborators
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.join_collaborator_message),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
                }
            }
        }
    }

    // Collaborator Dialog
    if (showCollaboratorDialog && spaceDetails != null) {
        val collaborators = spaceDetails!!.collaborators.map { username ->
            Collaborator(id = username, name = username)
        }
        CollaboratorDialog(
            collaborators = collaborators,
            onDismiss = { showCollaboratorDialog = false },
            onCollaboratorClick = { collaboratorName ->
                showCollaboratorDialog = false
                viewModel.getProfileByUsername(collaboratorName)
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && spaceDetails != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_space_button)) },
            text = {
                Text(
                    stringResource(R.string.delete_space_confirmation, spaceDetails!!.title)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSpace()
                    }
                ) {
                    Text(stringResource(R.string.delete_space_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    LaunchedEffect(reportSubmitSuccess) {
        if (reportSubmitSuccess) {
            val contentTypeText = if (reportDialogContentType == "space") "space" else "discussion"
            Toast.makeText(
                context,
                "Report submitted successfully for $contentTypeText: $reportDialogTitle",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetReportSubmitSuccess()
            showReportDialog = false
        }
    }

    // Report Dialog - only show when reasons are loaded
    if (showReportDialog && !isLoadingReportReasons && reportReasons.isNotEmpty() && !isSubmittingReport) {
        ReportSpaceDialog(
            spaceTitle = reportDialogTitle,
            contentType = reportDialogContentType,
            reasons = reportReasons.map { it.label },
            reasonCodes = reportReasons.map { it.code },
            onDismiss = { showReportDialog = false },
            onSubmit = { reasonLabel, reasonCode ->
                viewModel.submitReport(reasonCode)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSpaceDialog(
    spaceTitle: String,
    contentType: String,
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
                // Title: Report Space
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.report_space_button),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Divider()
                }

                // Content name with grayish color
                Text(
                    text = "${contentType.replaceFirstChar { it.uppercase() }}: $spaceTitle",
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
                            text = stringResource(R.string.cancel_button),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
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
                            text = stringResource(R.string.report_space_button),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

// Data class for pagination info
private data class PaginationInfo(
    val currentPageDiscussions: List<Discussion>,
    val totalPages: Int
)

// Pagination calculation function
private fun calculatePagination(discussions: List<Discussion>, currentPage: Int): PaginationInfo {
    val totalPages = (discussions.size + COMMENTS_PER_PAGE - 1) / COMMENTS_PER_PAGE
    val startIndex = (currentPage - 1) * COMMENTS_PER_PAGE
    val endIndex = minOf(startIndex + COMMENTS_PER_PAGE, discussions.size)
    val currentPageDiscussions = discussions.subList(startIndex, endIndex)
    return PaginationInfo(currentPageDiscussions, totalPages)
}

// Pagination helper function
private fun generatePaginationNumbers(currentPage: Int, totalPages: Int): List<Any> {
    val numbers = mutableListOf<Any>()

    when {
        totalPages <= 5 -> {
            // Show all pages if 5 or fewer
            for (i in 1..totalPages) {
                numbers.add(i)
            }
        }
        currentPage <= 2 -> {
            // Show first 3 pages, ellipsis, last page
            for (i in 1..3) {
                numbers.add(i)
            }
            if (totalPages > 3) {
                numbers.add("...")
                numbers.add(totalPages)
            }
        }
        currentPage >= totalPages - 1 -> {
            // Show first page, ellipsis, last 3 pages
            numbers.add(1)
            if (totalPages > 3) {
                numbers.add("...")
            }
            for (i in totalPages - 2..totalPages) {
                numbers.add(i)
            }
        }
        else -> {
            // Show first page, ellipsis, current-1, current, current+1, ellipsis, last page
            numbers.add(1)
            numbers.add("...")
            for (i in currentPage - 1..currentPage + 1) {
                numbers.add(i)
            }
            numbers.add("...")
            numbers.add(totalPages)
        }
    }

    return numbers
}