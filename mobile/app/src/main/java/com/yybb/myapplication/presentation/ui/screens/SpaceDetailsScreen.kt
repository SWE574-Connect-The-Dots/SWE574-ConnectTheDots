package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.ui.screens.components.CollaboratorDialog
import com.yybb.myapplication.presentation.ui.screens.components.DiscussionCard
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceDetailsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.yybb.myapplication.data.Constants.COMMENTS_PER_PAGE
import com.yybb.myapplication.data.Constants.DISCUSSION_SECTION_HEIGHT
import com.yybb.myapplication.data.Constants.MAX_COMMENT_LENGTH


data class Collaborator(
    val id: String,
    val name: String,
    val email: String? = null,
    val avatar: String? = null
)

data class Comment(
    val id: String,
    val commenterName: String,
    val comment: String,
    val date: Date
) {
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return "Sent: ${formatter.format(date)}"
    }
}

data class Space(
    val id: String,
    val title: String,
    val description: String,
    val collaboratorCount: Int
)

val space = Space(
    id = "1",
    title = "Climate Change Factors",
    description = "Visual Mapping of causes, effects, and interconnections of climate change factors.",
    collaboratorCount = 15
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceDetailsScreen(
    viewModel: SpaceDetailsViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    var comments by remember { mutableStateOf(getSampleComments()) }
    var newComment by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    var showCollaboratorDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var discussionSectionOffset by remember { mutableStateOf(0f) }
    var isInitialLoad by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Pagination calculations
    val paginationInfo = remember(comments.size, currentPage) {
        calculatePagination(comments, currentPage)
    }

    // Auto-scroll to discussion section when page changes (but not on initial load)
    LaunchedEffect(currentPage) {
        if (!isInitialLoad && discussionSectionOffset > 0) {
            coroutineScope.launch {
                scrollState.animateScrollTo(discussionSectionOffset.toInt())
            }
        }
        // Mark that initial load is complete after first page change
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.space_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Space Title
            Text(
                text = space.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Space Description
            Text(
                text = space.description,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                        text = context.getString(R.string.see_collab, getSampleCollaborators().size),
                        fontSize = 14.sp
                    )
                }
            }

            // See Space Graph Button
            Button(
                onClick = {
                    android.widget.Toast.makeText(
                        context,
                        "Clicked on see space graph button",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
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
                    paginationInfo.currentPageComments.forEach { comment ->
                        DiscussionCard(comment = comment)
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
                        val comment = Comment(
                            id = UUID.randomUUID().toString(),
                            commenterName = "You", // In a real app, this would be the current user
                            comment = newComment,
                            date = Date()
                        )
                        comments = listOf(comment) + comments // Add to top of list
                        currentPage = 1 // Reset to first page to show new comment
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
        }
    }

    // Collaborator Dialog
    if (showCollaboratorDialog) {
        CollaboratorDialog(
            collaborators = getSampleCollaborators(),
            onDismiss = { showCollaboratorDialog = false },
            onCollaboratorClick = { collaboratorName ->
                // Show toast message
                Toast.makeText(
                    context,
                    "Clicked on: $collaboratorName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

// Data class for pagination info
private data class PaginationInfo(
    val currentPageComments: List<Comment>,
    val totalPages: Int
)

// Pagination calculation function
private fun calculatePagination(comments: List<Comment>, currentPage: Int): PaginationInfo {
    val totalPages = (comments.size + COMMENTS_PER_PAGE - 1) / COMMENTS_PER_PAGE
    val startIndex = (currentPage - 1) * COMMENTS_PER_PAGE
    val endIndex = minOf(startIndex + COMMENTS_PER_PAGE, comments.size)
    val currentPageComments = comments.subList(startIndex, endIndex)
    return PaginationInfo(currentPageComments, totalPages)
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

// Sample data for testing
private fun getSampleComments(): List<Comment> {
    return listOf(
        Comment(
            id = "1",
            commenterName = "esranrzm",
            comment = "It's interesting how visual maps make the complexity of climate change easier to understand. Seeing the links between deforestation, carbon emissions, and rising temperatures really puts things into perspective. This approach helps us see the bigger picture and understand how different environmental factors are interconnected.",
            date = Date(System.currentTimeMillis() - 86400000) // 1 day ago
        ),
        Comment(
            id = "2",
            commenterName = "Melisaaaa",
            comment = "I'd love to see how local factors, like urbanization or industrial zones, fit into the bigger global map of climate causes and effects.",
            date = Date(System.currentTimeMillis() - 172800000) // 2 days ago
        ),
        Comment(
            id = "3",
            commenterName = "AhmetTaha06",
            comment = "It's amazing how much more people understand when they see data rather than just read about it. Maybe schools should use these visual maps to teach climate science. What do you think @esranrzm? This could revolutionize how we teach environmental science and help students grasp complex concepts more easily.",
            date = Date(System.currentTimeMillis() - 259200000) // 3 days ago
        ),
        Comment(
            id = "4",
            commenterName = "ClimateExpert",
            comment = "The interconnected nature of climate systems is fascinating. Each factor influences multiple others, creating a complex web of cause and effect relationships.",
            date = Date(System.currentTimeMillis() - 345600000) // 4 days ago
        ),
        Comment(
            id = "5",
            commenterName = "DataVizFan",
            comment = "Visual representations help break down complex scientific concepts into digestible pieces. This approach makes climate science more accessible to everyone.",
            date = Date(System.currentTimeMillis() - 432000000) // 5 days ago
        ),
        Comment(
            id = "6",
            commenterName = "EcoWarrior",
            comment = "Understanding these connections is crucial for developing effective climate action strategies. We need to address root causes, not just symptoms.",
            date = Date(System.currentTimeMillis() - 518400000) // 6 days ago
        ),
        Comment(
            id = "7",
            commenterName = "ScienceTeacher",
            comment = "This visual approach would be perfect for my students. It makes abstract concepts concrete and helps them see the bigger picture.",
            date = Date(System.currentTimeMillis() - 604800000) // 7 days ago
        ),
        Comment(
            id = "8",
            commenterName = "PolicyMaker",
            comment = "These visualizations could be incredibly valuable for policy development. They help identify key leverage points for intervention.",
            date = Date(System.currentTimeMillis() - 691200000) // 8 days ago
        ),
        Comment(
            id = "9",
            commenterName = "Researcher",
            comment = "The methodology behind these visualizations is impressive. Combining scientific data with intuitive design creates powerful communication tools.",
            date = Date(System.currentTimeMillis() - 777600000) // 9 days ago
        ),
        Comment(
            id = "10",
            commenterName = "CommunityLeader",
            comment = "This approach helps communities understand their role in the larger climate system. Local action becomes part of global solutions.",
            date = Date(System.currentTimeMillis() - 864000000) // 10 days ago
        ),
        Comment(
            id = "11",
            commenterName = "ShortCommenter",
            comment = "Great!",
            date = Date(System.currentTimeMillis() - 950400000) // 11 days ago
        ),
        Comment(
            id = "12",
            commenterName = "LongCommenter",
            comment = "This is an extremely long comment that demonstrates how the dynamic height system works. When comments are very long, the discussion area will automatically expand to accommodate all the text without cutting off any content. This ensures that users can read the complete comment regardless of its length, while still maintaining the pagination system that shows exactly 3 comments per page. The system is designed to be flexible and responsive to different content lengths, providing an optimal user experience for all types of discussions.",
            date = Date(System.currentTimeMillis() - 1036800000) // 12 days ago
        ),
        Comment(
            id = "13",
            commenterName = "TestUser13",
            comment = "This is comment number 13 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1123200000) // 13 days ago
        ),
        Comment(
            id = "14",
            commenterName = "TestUser14",
            comment = "This is comment number 14 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1209600000) // 14 days ago
        ),
        Comment(
            id = "15",
            commenterName = "TestUser15",
            comment = "This is comment number 15 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1296000000) // 15 days ago
        ),
        Comment(
            id = "16",
            commenterName = "TestUser16",
            comment = "This is comment number 16 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1382400000) // 16 days ago
        ),
        Comment(
            id = "17",
            commenterName = "TestUser17",
            comment = "This is comment number 17 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1468800000) // 17 days ago
        ),
        Comment(
            id = "18",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "18",
            commenterName = "TestUser17",
            comment = "This is comment number 17 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1468800000) // 17 days ago
        ),
        Comment(
            id = "19",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "20",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "21",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        ),
        Comment(
            id = "22",
            commenterName = "TestUser18",
            comment = "This is comment number 18 to test pagination with more pages.",
            date = Date(System.currentTimeMillis() - 1555200000) // 18 days ago
        )
    )
}

// Sample collaborators data
private fun getSampleCollaborators(): List<Collaborator> {
    return listOf(
        Collaborator(id = "1", name = "SkylineThinker"),
        Collaborator(id = "2", name = "EchoWanderer"),
        Collaborator(id = "3", name = "MidnightCoder"),
        Collaborator(id = "4", name = "LunaWriter"),
        Collaborator(id = "5", name = "UrbanLeaf"),
        Collaborator(id = "6", name = "EmmaReed"),
        Collaborator(id = "7", name = "OliviaScott"),
        Collaborator(id = "8", name = "EthanWard"),
        Collaborator(id = "9", name = "SophieMiller"),
        Collaborator(id = "10", name = "DanielBrooks"),
        Collaborator(id = "11", name = "JacobHunt"),
        Collaborator(id = "12", name = "AvaJohnson"),
        Collaborator(id = "13", name = "NoahWilliams")
    )
}