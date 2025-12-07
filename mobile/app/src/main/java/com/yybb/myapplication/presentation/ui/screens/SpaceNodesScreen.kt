package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.presentation.ui.viewmodel.NodeSortOrder
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceNodesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNodeDetails: (String, String, String?) -> Unit,
    onNavigateToAddNode: () -> Unit,
    viewModel: SpaceNodesViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredNodes by viewModel.filteredNodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

    var showSortMenu by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // Reset scroll position when sort order changes
    LaunchedEffect(sortOrder) {
        lazyListState.animateScrollToItem(0)
    }

    // Refresh data when screen becomes visible again (e.g., after navigating back)
    // This handles the case when navigating back from node details after deleting a node
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onScreenResumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.space_nodes_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onNavigateToAddNode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.button_join),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_node_title),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.add_node_title),
                            color = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    SpaceNodesError(
                        message = errorMessage ?: stringResource(R.string.space_nodes_error_message),
                        onRetry = viewModel::retry
                    )
                }

                else -> {
                    SearchNode(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        enabled = !isLoading
                    )
                    
                    // Sort button and dropdown - positioned below search, aligned to right
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.filter_icon),
                                    contentDescription = "Sort options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Sort dropdown menu
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date (Oldest First)") },
                                    onClick = {
                                        viewModel.setSortOrder(NodeSortOrder.DATE_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date (Newest First)") },
                                    onClick = {
                                        viewModel.setSortOrder(NodeSortOrder.DATE_DESC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Connections (Lowest First)") },
                                    onClick = {
                                        viewModel.setSortOrder(NodeSortOrder.CONNECTION_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Connections (Highest First)") },
                                    onClick = {
                                        viewModel.setSortOrder(NodeSortOrder.CONNECTION_DESC)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (filteredNodes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = stringResource(R.string.space_nodes_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredNodes,
                                key = { node -> node.id }
                            ) { node ->
                                SpaceNodeCard(
                                    node = node,
                                    onSeeDetails = { onNavigateToNodeDetails(node.id.toString(), node.label, node.wikidataId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NodeCardVisualProperties(
    val elevation: Float,
    val borderWidth: Float,
    val borderColor: Color,
    val backgroundColor: Color,
    val titleFontWeight: FontWeight,
    val titleAlpha: Float
)

private fun darkenColor(color: Color, factor: Float): Color {
    val clampedFactor = factor.coerceIn(0f, 1.5f) // Allow up to 1.5 for more darkness range
    return Color(
        red = color.red * (1f - clampedFactor * 0.25f), // Darken by max 37.5% when factor is 1.5
        green = color.green * (1f - clampedFactor * 0.25f),
        blue = color.blue * (1f - clampedFactor * 0.25f),
        alpha = color.alpha
    )
}

private fun blendColors(start: Color, end: Color, fraction: Float): Color {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * clampedFraction,
        green = start.green + (end.green - start.green) * clampedFraction,
        blue = start.blue + (end.blue - start.blue) * clampedFraction,
        alpha = start.alpha + (end.alpha - start.alpha) * clampedFraction
    )
}

@Composable
private fun calculateCardVisualProperties(connectionCount: Int): NodeCardVisualProperties {
    val threshold = 25
    val clampedCount = connectionCount.coerceAtMost(threshold)
    
    val normalized = if (threshold > 0) clampedCount.toFloat() / threshold else 0f
    
    val elevation = 2f + (normalized * 18f)
    
    val borderWidth = normalized * 8f
    
    val borderOpacity = 0.2f + (normalized * 0.8f)
    val borderColor = Color.Black.copy(alpha = borderOpacity)

    val baseSurfaceColor = MaterialTheme.colorScheme.surface
    val maxDarkBackground = darkenColor(baseSurfaceColor, 1.4f)
    val backgroundColor = blendColors(baseSurfaceColor, maxDarkBackground, normalized)

    val fontWeightValue = (400 + normalized * 300).toInt()
    val fontWeight = when {
        fontWeightValue < 500 -> FontWeight.Medium
        fontWeightValue < 600 -> FontWeight.SemiBold
        else -> FontWeight.Bold
    }
    
    val titleAlpha = 0.87f + (normalized * 0.13f)
    
    return NodeCardVisualProperties(
        elevation = elevation,
        borderWidth = borderWidth,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        titleFontWeight = fontWeight,
        titleAlpha = titleAlpha
    )
}

@Composable
private fun SpaceNodeCard(
    node: SpaceNode,
    onSeeDetails: () -> Unit
) {
    val visualProperties = calculateCardVisualProperties(node.connectionCount)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (visualProperties.borderWidth > 0) {
                    Modifier.border(
                        width = visualProperties.borderWidth.dp,
                        color = visualProperties.borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = visualProperties.elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = visualProperties.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = visualProperties.titleFontWeight,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = visualProperties.titleAlpha)
                    )
                    Text(
                        text = "${node.connectionCount} connection${if (node.connectionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Button(
                    onClick = onSeeDetails,
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.see_details_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceNodesError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry) {
            Text(
                text = stringResource(R.string.retry_button_label),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchNode(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        enabled = enabled,
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_node_hint_msg),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        },
        leadingIcon = null,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

