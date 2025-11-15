package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.Constants.TAG_ICON_SIZE
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodeDetailsViewModel
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodeDetailsViewModel.NodeOption
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceNodeDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNodeDetails: (String, String, String?) -> Unit,
    onNavigateToEdgeDetails: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onNavigateToWebView: (String) -> Unit = { },
    viewModel: SpaceNodeDetailsViewModel = hiltViewModel()
) {
    val nodeName by viewModel.nodeName.collectAsState()
    val wikidataId by viewModel.wikidataId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProperties by viewModel.filteredOptions.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val availableNodes by viewModel.availableConnectionNodes.collectAsState()
    val nodeConnections by viewModel.nodeConnections.collectAsState()
    val connectionSearchQuery by viewModel.connectionSearchQuery.collectAsState()
    val filteredConnections by viewModel.filteredConnections.collectAsState()
    val apiNodeProperties by viewModel.apiNodeProperties.collectAsState()
    val isNodePropertiesLoading by viewModel.isNodePropertiesLoading.collectAsState()
    val isWikidataPropertiesLoading by viewModel.isWikidataPropertiesLoading.collectAsState()
    val wikidataPropertiesError by viewModel.wikidataPropertiesError.collectAsState()
    val isUpdatingNodeProperties by viewModel.isUpdatingNodeProperties.collectAsState()
    val isDeletingNodeProperty by viewModel.isDeletingNodeProperty.collectAsState()
    val nodePropertiesError by viewModel.nodePropertiesError.collectAsState()
    val nodePropertyDeletionMessage by viewModel.nodePropertyDeletionMessage.collectAsState()
    val nodePropertyDeletionError by viewModel.nodePropertyDeletionError.collectAsState()
    val edgeLabelSearchResults by viewModel.edgeLabelSearchResults.collectAsState()
    val isEdgeLabelSearching by viewModel.isEdgeLabelSearching.collectAsState()
    val edgeLabelSearchError by viewModel.edgeLabelSearchError.collectAsState()
    val isCreatingEdge by viewModel.isCreatingEdge.collectAsState()
    val edgeCreationError by viewModel.edgeCreationError.collectAsState()
    val edgeCreationSuccess by viewModel.edgeCreationSuccess.collectAsState()
    val criticalError by viewModel.criticalError.collectAsState()
    val isDeletingNode by viewModel.isDeletingNode.collectAsState()
    val deleteNodeError by viewModel.deleteNodeError.collectAsState()
    val deleteNodeSuccess by viewModel.deleteNodeSuccess.collectAsState()
    val reportReasons = viewModel.reportReasons

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEdgeDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }


    // Refresh connections when the Connections tab is selected or visible
    // This ensures we have the latest data when:
    // 1. Tab is first selected
    // 2. User navigates back from EdgeDetailsScreen (composable recomposes with Connections tab visible)
    // The ViewModel prevents duplicate concurrent calls via job cancellation
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) { // Connections tab
            viewModel.refreshNodeConnections()
        }
    }
    
    // Additional refresh when composable is composed/recomposed and Connections tab is visible
    // This handles the case when user navigates back while already on Connections tab
    LaunchedEffect(Unit) {
        // Refresh connections when the screen becomes visible and Connections tab is active
        // This runs when the composable is composed (including after navigation back)
        if (selectedTabIndex == 1) {
            viewModel.refreshNodeConnections()
        }
    }

    LaunchedEffect(criticalError) {
        if (criticalError != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && criticalError != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearCriticalError()
                onNavigateBack()
            },
            title = { Text(text = stringResource(id = R.string.node_details_error_title)) },
            text = {
                Text(
                    text = criticalError ?: stringResource(id = R.string.node_details_error_message)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearCriticalError()
                        onNavigateBack()
                    }
                ) {
                    Text(text = stringResource(id = R.string.ok_button))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDeletingNode) {
                    showDeleteDialog = false
                }
            },
            title = { Text(text = stringResource(id = R.string.delete_node_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.delete_node_confirmation,
                        nodeName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNode()
                    },
                    enabled = !isDeletingNode
                ) {
                    Text(text = stringResource(id = R.string.yes_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeletingNode
                ) {
                    Text(text = stringResource(id = R.string.no_button))
                }
            }
        )
    }

    if (showAddEdgeDialog) {
        AddEdgeDialog(
            currentNodeName = nodeName,
            availableNodes = availableNodes,
            edgeLabelSearchResults = edgeLabelSearchResults,
            isEdgeLabelSearching = isEdgeLabelSearching,
            isSavingEdge = isCreatingEdge,
            edgeLabelSearchError = edgeLabelSearchError,
            onDismiss = {
                viewModel.resetEdgeLabelSearch()
                showAddEdgeDialog = false
            },
            onAddEdge = { selectedNode, isForward, description, propertyId ->
                viewModel.addEdge(
                    selectedNode = selectedNode,
                    isForwardDirection = isForward,
                    label = description,
                    wikidataPropertyId = propertyId
                )
            },
            onEdgeLabelQueryChange = viewModel::searchEdgeLabelOptions,
            onEdgeLabelSearchCleared = viewModel::resetEdgeLabelSearch,
            onEdgeLabelSearchErrorConsumed = viewModel::clearEdgeLabelSearchError
        )
    }

    if (showReportDialog) {
        ReportNodeDialog(
            nodeName = nodeName,
            reasons = reportReasons,
            onDismiss = { showReportDialog = false },
            onSubmit = { reason ->
                showReportDialog = false
                Toast.makeText(
                    context,
                    context.getString(R.string.report_node_submitted, nodeName, reason),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (isNodePropertiesLoading) {
        LoadingDialog(message = stringResource(id = R.string.loading_properties_message))
    }

    if (isDeletingNodeProperty) {
        LoadingDialog(message = stringResource(id = R.string.deleting_node_property_message))
    }

    if (isDeletingNode) {
        LoadingDialog(message = stringResource(id = R.string.deleting_node_message))
    }

    LaunchedEffect(deleteNodeSuccess) {
        if (deleteNodeSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.delete_node_success_message),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.clearDeleteNodeSuccess()
            showDeleteDialog = false
            onNavigateBack()
        }
    }

    LaunchedEffect(deleteNodeError) {
        deleteNodeError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearDeleteNodeError()
            showDeleteDialog = false
        }
    }

    LaunchedEffect(nodePropertyDeletionMessage) {
        nodePropertyDeletionMessage?.let { message ->
            Toast.makeText(
                context,
                context.getString(R.string.node_property_removed_message, message),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.clearNodePropertyDeletionMessage()
        }
    }

    LaunchedEffect(nodePropertyDeletionError) {
        nodePropertyDeletionError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearNodePropertyDeletionError()
        }
    }

    LaunchedEffect(edgeCreationError) {
        edgeCreationError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearEdgeCreationError()
        }
    }

    LaunchedEffect(edgeCreationSuccess) {
        edgeCreationSuccess?.let { result ->
            val directionText = if (result.isForwardDirection) {
                "$nodeName -> ${result.connectedNodeName}"
            } else {
                "${result.connectedNodeName} -> $nodeName"
            }
            Toast.makeText(
                context,
                context.getString(
                    R.string.edge_added_toast,
                    directionText,
                    result.label
                ),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetEdgeLabelSearch()
            viewModel.clearEdgeCreationSuccess()
            showAddEdgeDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = nodeName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    when (selectedTabIndex) {
                        0 -> {
                            // Details tab - show menu with Delete and Report options
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.delete_node_title)) },
                                        onClick = {
                                            showMenu = false
                                            showDeleteDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.report_node_title)) },
                                        onClick = {
                                            showMenu = false
                                            showReportDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Flag,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            // Connections tab - show Add connection button
                            IconButton(onClick = { showAddEdgeDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.add_connection_title)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding() - 48.dp
        val tabLabels = listOf(
            stringResource(id = R.string.details_tab_label),
            stringResource(id = R.string.connections_tab_label)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection) + 8.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection) + 8.dp,
                    top = innerPadding.calculateTopPadding() -5.dp,
                    bottom = bottomPadding
                )
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = label) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> DetailsContent(
                    apiNodeProperties = apiNodeProperties,
                    isLoadingProperties = isNodePropertiesLoading,
                    propertiesError = nodePropertiesError,
                    onRetryProperties = viewModel::retryNodeProperties,
                    onPropertyValueClick = { property ->
                        // Navigate to WebView with Wikidata URL if entity has an ID
                        property.entityId?.let { entityId ->
                            val wikidataUrl = "https://www.wikidata.org/wiki/$entityId"
                            onNavigateToWebView(wikidataUrl)
                        } ?: run {
                            // Fallback to toast if no entity ID
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.node_property_value_click_message,
                                    property.valueText
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onPropertyRemove = viewModel::deleteNodeProperty,
                    wikidataId = wikidataId,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onToggleProperty = viewModel::togglePropertySelection,
                    onSaveProperties = viewModel::saveSelectedProperties,
                    filteredOptions = filteredProperties,
                    isSavingProperties = isUpdatingNodeProperties,
                    isWikidataPropertiesLoading = isWikidataPropertiesLoading,
                    wikidataPropertiesError = wikidataPropertiesError,
                    onNavigateToWebView = onNavigateToWebView
                )

                else -> ConnectionsContent(
                    connections = filteredConnections,
                    searchQuery = connectionSearchQuery,
                    onSearchQueryChange = viewModel::updateConnectionSearchQuery,
                    hasAnyConnections = nodeConnections.isNotEmpty(),
                    currentNodeName = nodeName,
                    currentNodeId = viewModel.nodeId,
                    availableNodes = availableNodes,
                    onSeeDetailsClick = { connection ->
                        // Get node names from available nodes or use IDs as fallback
                        val sourceNode = availableNodes.find { it.id == connection.sourceId }
                        val targetNode = availableNodes.find { it.id == connection.targetId }
                        val sourceName = sourceNode?.name ?: if (connection.sourceId == viewModel.nodeId) nodeName else connection.sourceId
                        val targetName = targetNode?.name ?: if (connection.targetId == viewModel.nodeId) nodeName else connection.targetId
                        onNavigateToEdgeDetails(
                            connection.edgeId,
                            connection.label,
                            connection.sourceId,
                            sourceName,
                            connection.targetId,
                            targetName
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    apiNodeProperties: List<NodeProperty>,
    isLoadingProperties: Boolean,
    propertiesError: String?,
    onRetryProperties: () -> Unit,
    onPropertyValueClick: (NodeProperty) -> Unit,
    onPropertyRemove: (NodeProperty) -> Unit,
    wikidataId: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleProperty: (String) -> Unit,
    onSaveProperties: () -> Unit,
    filteredOptions: List<SpaceNodeDetailsViewModel.PropertyOption>,
    isSavingProperties: Boolean,
    isWikidataPropertiesLoading: Boolean,
    wikidataPropertiesError: String?,
    onNavigateToWebView: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Make Wikidata ID clickable
                val wikidataIdText = stringResource(id = R.string.node_wikidata_id, wikidataId)
                val colonIndex = wikidataIdText.indexOf(':')
                val prefix = if (colonIndex != -1) wikidataIdText.substring(0, colonIndex + 1) else wikidataIdText
                val idPart = if (colonIndex != -1) wikidataIdText.substring(colonIndex + 1).trim() else ""
                
                val annotatedWikidataText = buildAnnotatedString {
                    append(prefix)
                    if (idPart.isNotEmpty()) {
                        append(" ")
                        val start = length
                        append(idPart)
                        addStyle(
                            style = SpanStyle(color = Color(0xFF436FED)),
                            start = start,
                            end = length
                        )
                        addStringAnnotation(
                            tag = "WIKIDATA_ID",
                            annotation = wikidataId,
                            start = start,
                            end = length
                        )
                    }
                }
                
                ClickableText(
                    text = annotatedWikidataText,
                    style = MaterialTheme.typography.bodyMedium,
                    onClick = { offset ->
                        val annotations = annotatedWikidataText.getStringAnnotations(
                            tag = "WIKIDATA_ID",
                            start = offset,
                            end = offset
                        )
                        if (annotations.isNotEmpty() && wikidataId.isNotBlank()) {
                            val wikidataUrl = "https://www.wikidata.org/wiki/$wikidataId"
                            onNavigateToWebView(wikidataUrl)
                        }
                    }
                )
                Text(
                    text = stringResource(id = R.string.node_properties_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Divider()
            }
        }

        item {
            when {
                isLoadingProperties -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                propertiesError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = propertiesError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onRetryProperties) {
                            Text(text = stringResource(id = R.string.retry_button_label))
                        }
                    }
                }

                apiNodeProperties.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.node_properties_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        apiNodeProperties.forEach { property ->
                            NodePropertyDisplayRow(
                                property = property,
                                onValueClick = onPropertyValueClick,
                                onRemoveClick = onPropertyRemove
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.edit_node_properties_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Divider()
                Text(
                    text = stringResource(id = R.string.node_properties_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(id = R.string.search_property_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true
                )
                PropertySelectionList(
                    options = filteredOptions,
                    onToggleProperty = onToggleProperty,
                    onValueClick = onPropertyValueClick,
                    isLoading = isWikidataPropertiesLoading,
                    error = wikidataPropertiesError
                )
                Button(
                    onClick = onSaveProperties,
                    enabled = !isSavingProperties,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavingProperties) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = stringResource(id = R.string.save_properties_button))
                }
            }
        }
    }
}

@Composable
private fun NodePropertyDisplayRow(
    property: NodeProperty,
    onValueClick: (NodeProperty) -> Unit,
    onRemoveClick: (NodeProperty) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PropertyDisplayText(
            property = property,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            onValueClick = onValueClick
        )
        IconButton(
            onClick = { onRemoveClick(property) },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_bin),
                contentDescription = "Remove property",
                modifier = Modifier.size(TAG_ICON_SIZE.dp)
            )
        }
    }
}

@Composable
private fun PropertySelectionList(
    options: List<SpaceNodeDetailsViewModel.PropertyOption>,
    onToggleProperty: (String) -> Unit,
    onValueClick: (NodeProperty) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            options.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.node_properties_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(options) { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = option.isChecked,
                                onCheckedChange = { onToggleProperty(option.property.statementId) }
                            )
                            PropertyDisplayText(
                                property = option.property,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onValueClick = onValueClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyDisplayText(
    property: NodeProperty,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    onValueClick: ((NodeProperty) -> Unit)? = null
) {
    val displayText = property.display.ifBlank { "${property.propertyLabel}: ${property.valueText}" }
    val colonIndex = displayText.indexOf(':')
    val isClickable = property.isEntity && colonIndex != -1 && onValueClick != null

    if (!isClickable) {
        Text(
            text = displayText,
            style = textStyle,
            modifier = modifier
        )
        return
    }

    val prefix = displayText.substring(0, colonIndex + 1)
    val suffix = displayText.substring(colonIndex + 1)
    val firstNonSpaceIndex = suffix.indexOfFirst { !it.isWhitespace() }
    val leadingSpaces = if (firstNonSpaceIndex == -1) suffix else suffix.substring(0, firstNonSpaceIndex)
    val valuePart = if (firstNonSpaceIndex == -1) "" else suffix.substring(firstNonSpaceIndex)

    val annotatedText = buildAnnotatedString {
        append(prefix)
        if (suffix.isNotEmpty()) {
            append(leadingSpaces)
            if (valuePart.isNotEmpty()) {
                val start = length
                append(valuePart)
                addStyle(
                    style = SpanStyle(color = Color(0xFF436FED)),
                    start = start,
                    end = length
                )
                addStringAnnotation(
                    tag = "ENTITY_VALUE",
                    annotation = property.entityId ?: property.valueText,
                    start = start,
                    end = length
                )
            }
        }
    }

    ClickableText(
        text = annotatedText,
        style = textStyle,
        modifier = modifier,
        onClick = { offset ->
            val annotations = annotatedText.getStringAnnotations(
                tag = "ENTITY_VALUE",
                start = offset,
                end = offset
            )
            if (annotations.isNotEmpty()) {
                onValueClick?.invoke(property)
            }
        }
    )
}

@Composable
private fun ConnectionsContent(
    connections: List<SpaceNodeDetailsViewModel.NodeConnection>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    hasAnyConnections: Boolean,
    currentNodeName: String,
    currentNodeId: String,
    availableNodes: List<SpaceNodeDetailsViewModel.NodeOption>,
    onSeeDetailsClick: (SpaceNodeDetailsViewModel.NodeConnection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = stringResource(id = R.string.search_connections_hint))
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true
        )

        if (connections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val message = if (hasAnyConnections) {
                    stringResource(id = R.string.space_node_connections_no_results)
                } else {
                    stringResource(id = R.string.space_node_no_connections)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = connections,
                    key = { it.edgeId }
                ) { connection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.edge_description_label,
                                        connection.label
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (connection.isSource) {
                                        stringResource(id = R.string.edge_role_source)
                                    } else {
                                        stringResource(id = R.string.edge_role_target)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onSeeDetailsClick(connection) },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(text = stringResource(id = R.string.see_details_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEdgeDialog(
    currentNodeName: String,
    availableNodes: List<NodeOption>,
    edgeLabelSearchResults: List<WikidataProperty>,
    isEdgeLabelSearching: Boolean,
    isSavingEdge: Boolean,
    edgeLabelSearchError: String?,
    onDismiss: () -> Unit,
    onAddEdge: (NodeOption, Boolean, String, String) -> Unit,
    onEdgeLabelQueryChange: (String) -> Unit,
    onEdgeLabelSearchCleared: () -> Unit,
    onEdgeLabelSearchErrorConsumed: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<NodeOption?>(null) }
    var selectedNodeError by remember { mutableStateOf(false) }

    var edgeLabel by remember { mutableStateOf("") }
    var edgeLabelError by remember { mutableStateOf(false) }
    var selectedProperty by remember { mutableStateOf<WikidataProperty?>(null) }
    var showResults by remember { mutableStateOf(false) }

    var isForwardDirection by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clearFocusRequester = remember { FocusRequester() }

    LaunchedEffect(edgeLabelSearchError) {
        if (edgeLabelSearchError != null) {
            showResults = true
        }
    }

    val directionText = when {
        selectedNode == null && isForwardDirection -> stringResource(id = R.string.edge_direction_placeholder_forward)
        selectedNode == null && !isForwardDirection -> stringResource(id = R.string.edge_direction_placeholder_reverse)
        isForwardDirection -> "$currentNodeName -> ${selectedNode?.name}"
        else -> "${selectedNode?.name} -> $currentNodeName"
    }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth(0.95f),
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 420.dp, max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.add_edge_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Divider()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.add_edge_select_node_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedNode?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            placeholder = {
                                Text(text = stringResource(id = R.string.add_edge_select_node_placeholder))
                            },
                            isError = selectedNodeError,
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
                            availableNodes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = {
                                        selectedNode = option
                                        selectedNodeError = false
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (selectedNodeError) {
                        Text(
                            text = stringResource(id = R.string.add_edge_select_node_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.add_edge_label_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = edgeLabel,
                        onValueChange = { input ->
                            edgeLabel = input
                            edgeLabelError = false
                            selectedProperty = null
                            val normalized = input.trim()
                            if (normalized.length >= 3) {
                                showResults = true
                                onEdgeLabelQueryChange(normalized)
                            } else {
                                showResults = false
                                onEdgeLabelSearchCleared()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isEdgeLabelSearching && !isSavingEdge,
                        placeholder = {
                            Text(text = stringResource(id = R.string.add_edge_label_placeholder))
                        },
                        isError = edgeLabelError,
                        trailingIcon = {
                            if (isEdgeLabelSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                    Spacer(
                        modifier = Modifier
                            .size(0.dp)
                            .focusRequester(clearFocusRequester)
                            .focusable()
                    )
                    if (edgeLabelError) {
                        Text(
                            text = stringResource(id = R.string.add_edge_label_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (showResults) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            when {
                                isEdgeLabelSearching -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = stringResource(id = R.string.searching_label),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                edgeLabelSearchError != null -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = edgeLabelSearchError,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(
                                            onClick = {
                                                onEdgeLabelSearchErrorConsumed()
                                                onEdgeLabelQueryChange(edgeLabel.trim())
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.retry_button_label))
                                        }
                                    }
                                }

                                edgeLabelSearchResults.isEmpty() -> {
                                    Text(
                                        text = stringResource(id = R.string.add_edge_no_results),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }

                                else -> {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.add_edge_search_results_title),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.heightIn(max = 220.dp)
                                        ) {
                                            items(
                                                items = edgeLabelSearchResults,
                                                key = { it.id }
                                            ) { result ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(
                                                            width = 1.dp,
                                                            color = MaterialTheme.colorScheme.outline,
                                                            shape = MaterialTheme.shapes.small
                                                        )
                                                        .clickable {
                                                            edgeLabel = result.label
                                                            selectedProperty = result
                                                            edgeLabelError = false
                                                            showResults = false
                                                            onEdgeLabelSearchCleared()
                                                            keyboardController?.hide()
                                                            focusManager.clearFocus(force = true)
                                                            clearFocusRequester.requestFocus()
                                                        },
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${result.label} (${result.id})",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (result.description.isNotBlank()) {
                                                            Text(
                                                                text = result.description,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isForwardDirection = !isForwardDirection },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isForwardDirection) colorResource(id = R.color.button_join) else colorResource(id = R.color.button_leave),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = directionText)
                    }
                }

                val canSave = selectedNode != null &&
                    selectedProperty != null &&
                    edgeLabel.isNotBlank() &&
                    !isSavingEdge

                Button(
                    onClick = {
                        if (isSavingEdge) return@Button
                        val node = selectedNode
                        val property = selectedProperty
                        val label = edgeLabel.trim()
                        val hasNodeError = node == null
                        val hasLabelError = label.isEmpty() || property == null

                        selectedNodeError = hasNodeError
                        edgeLabelError = hasLabelError

                        if (!hasNodeError && !hasLabelError && node != null && property != null) {
                            onAddEdge(node, isForwardDirection, label, property.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = canSave
                ) {
                    if (isSavingEdge) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(text = stringResource(id = R.string.add_edge_button))
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportNodeDialog(
    nodeName: String,
    reasons: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var reasonError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.report_node_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Divider()
                }

                Text(
                    text = stringResource(id = R.string.report_node_dialog_node_label, nodeName),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.report_node_dialog_reason_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedReason ?: "",
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            placeholder = {
                                Text(text = stringResource(id = R.string.report_node_dialog_reason_placeholder))
                            },
                            isError = reasonError,
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
                            reasons.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason) },
                                    onClick = {
                                        selectedReason = reason
                                        reasonError = false
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (reasonError) {
                        Text(
                            text = stringResource(id = R.string.report_node_dialog_reason_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBDBDBD),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.report_node_dialog_cancel),
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = {
                            val reason = selectedReason
                            val hasError = reason.isNullOrBlank()
                            reasonError = hasError
                            if (!hasError && reason != null) {
                                onSubmit(reason)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.button_leave),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.report_node_dialog_submit),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ConnectionFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = colorResource(id = R.color.button_join),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.add_connection_title),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.add_connection_title),
            color = Color.White
        )
    }
}

@Composable
private fun NodeActionsFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 80.dp, end = 16.dp)
    ) {
        if (isExpanded) {
            NodeActionFab(
                icon = Icons.Default.Delete,
                label = stringResource(id = R.string.delete_node_title),
                containerColor = colorResource(id = R.color.button_leave),
                onClick = onDelete
            )
            NodeActionFab(
                icon = Icons.Default.Flag,
                label = stringResource(id = R.string.report_node_title),
                containerColor = Color.Black,
                onClick = onReport
            )
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.MoreVert,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun NodeActionFab(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
        },
        text = {
            Text(text = label, color = Color.White)
        },
        containerColor = containerColor,
        contentColor = Color.White,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

