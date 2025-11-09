package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.Constants.TAG_ICON_SIZE
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodeDetailsViewModel
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodeDetailsViewModel.NodeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceNodeDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNodeDetails: (String) -> Unit,
    viewModel: SpaceNodeDetailsViewModel = hiltViewModel()
) {
    val nodeName by viewModel.nodeName.collectAsState()
    val wikidataId by viewModel.wikidataId.collectAsState()
    val nodeProperties by viewModel.nodeProperties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProperties by viewModel.filteredOptions.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val availableNodes by viewModel.availableConnectionNodes.collectAsState()
    val nodeConnections by viewModel.nodeConnections.collectAsState()
    val connectionSearchQuery by viewModel.connectionSearchQuery.collectAsState()
    val filteredConnections by viewModel.filteredConnections.collectAsState()
    val reportReasons = viewModel.reportReasons

    var isFabExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEdgeDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                TextButton(onClick = {
                    showDeleteDialog = false
                    isFabExpanded = false
                    onNavigateBack()
                }) {
                    Text(text = stringResource(id = R.string.yes_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.no_button))
                }
            }
        )
    }

    if (showAddEdgeDialog) {
        AddEdgeDialog(
            currentNodeName = nodeName,
            availableNodes = availableNodes,
            onDismiss = { showAddEdgeDialog = false },
            onAddEdge = { selectedNode, isForward, description ->
                showAddEdgeDialog = false
                val directionText = if (isForward) {
                    "$nodeName -> ${selectedNode.name}"
                } else {
                    "${selectedNode.name} -> $nodeName"
                }
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.edge_added_toast,
                        directionText,
                        description
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onSearchEdgeLabel = viewModel::searchEdgeLabelOptions
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
                }
            )
        },
        floatingActionButton = {
            NodeDetailsFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onDelete = {
                    isFabExpanded = false
                    showDeleteDialog = true
                },
                onAddConnection = {
                    isFabExpanded = false
                    showAddEdgeDialog = true
                },
                onReport = {
                    isFabExpanded = false
                    showReportDialog = true
                }
            )
        }
    ) { innerPadding ->
        val bottomPadding = (innerPadding.calculateBottomPadding() - 16.dp).coerceAtLeast(0.dp)
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val tabLabels = listOf(
            stringResource(id = R.string.details_tab_label),
            stringResource(id = R.string.connections_tab_label)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
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
                    nodeProperties = nodeProperties,
                    wikidataId = wikidataId,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onToggleProperty = viewModel::togglePropertySelection,
                    onRemoveProperty = viewModel::removeProperty,
                    onSaveProperties = viewModel::saveSelectedProperties,
                    filteredOptions = filteredProperties
                )

                else -> ConnectionsContent(
                    connections = filteredConnections,
                    searchQuery = connectionSearchQuery,
                    onSearchQueryChange = viewModel::updateConnectionSearchQuery,
                    hasAnyConnections = nodeConnections.isNotEmpty(),
                    onConnectionClick = { nodeId ->
                        viewModel.resetConnectionSearchQuery()
                        selectedTabIndex = 1
                        onNavigateToNodeDetails(nodeId)
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    nodeProperties: List<String>,
    wikidataId: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleProperty: (String) -> Unit,
    onRemoveProperty: (String) -> Unit,
    onSaveProperties: () -> Unit,
    filteredOptions: List<SpaceNodeDetailsViewModel.PropertyOption>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.node_wikidata_id, wikidataId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.node_properties_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Divider()
            }
        }

        items(nodeProperties) { property ->
            NodePropertyRow(
                property = property,
                onRemove = { onRemoveProperty(property) }
            )
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
                    onToggleProperty = onToggleProperty
                )
                Button(
                    onClick = onSaveProperties,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.save_properties_button))
                }
            }
        }
    }
}

@Composable
private fun NodePropertyRow(
    property: String,
    onRemove: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = property,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_bin),
                contentDescription = "Remove tag",
                modifier = Modifier.size(TAG_ICON_SIZE.dp)
            )
        }
    }
}

@Composable
private fun PropertySelectionList(
    options: List<SpaceNodeDetailsViewModel.PropertyOption>,
    onToggleProperty: (String) -> Unit
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(options) { option ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = option.isChecked,
                        onCheckedChange = { onToggleProperty(option.label) }
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionsContent(
    connections: List<SpaceNodeDetailsViewModel.NodeConnection>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    hasAnyConnections: Boolean,
    onConnectionClick: (String) -> Unit
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
                    key = { it.targetNodeId }
                ) { connection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConnectionClick(connection.targetNodeId) },
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.space_node_title_format,
                                    connection.targetNodeName
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.edge_description_label,
                                    connection.edgeDescription
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
    onDismiss: () -> Unit,
    onAddEdge: (NodeOption, Boolean, String) -> Unit,
    onSearchEdgeLabel: (String) -> List<String>
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<NodeOption?>(null) }
    var selectedNodeError by remember { mutableStateOf(false) }

    var edgeLabel by remember { mutableStateOf("") }
    var edgeLabelError by remember { mutableStateOf(false) }

    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResults by remember { mutableStateOf(false) }

    var isForwardDirection by remember { mutableStateOf(true) }

    val directionText = when {
        selectedNode == null && isForwardDirection -> stringResource(id = R.string.edge_direction_placeholder_forward)
        selectedNode == null && !isForwardDirection -> stringResource(id = R.string.edge_direction_placeholder_reverse)
        isForwardDirection -> "$currentNodeName -> ${selectedNode?.name}"
        else -> "${selectedNode?.name} -> $currentNodeName"
    }

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
                        onValueChange = {
                            edgeLabel = it
                            edgeLabelError = false
                            showResults = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(text = stringResource(id = R.string.add_edge_label_placeholder))
                        },
                        isError = edgeLabelError
                    )
                    Button(
                        onClick = {
                            val input = edgeLabel.trim()
                            if (input.isEmpty()) {
                                edgeLabelError = true
                                showResults = false
                            } else {
                                searchResults = onSearchEdgeLabel(input)
                                showResults = true
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.search_button))
                    }
                    if (edgeLabelError) {
                        Text(
                            text = stringResource(id = R.string.add_edge_label_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (showResults) {
                        if (searchResults.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.add_edge_no_results),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.add_edge_search_results_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    searchResults.forEach { result ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .clickable {
                                                    edgeLabel = result
                                                    showResults = false
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Text(
                                                text = result,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .padding(12.dp)
                                            )
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

                Button(
                    onClick = {
                        val node = selectedNode
                        val label = edgeLabel.trim()
                        val hasNodeError = node == null
                        val hasLabelError = label.isEmpty()

                        selectedNodeError = hasNodeError
                        edgeLabelError = hasLabelError

                        if (!hasNodeError && !hasLabelError && node != null) {
                            onAddEdge(node, isForwardDirection, label)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(id = R.string.add_edge_button))
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
private fun NodeDetailsFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onAddConnection: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isExpanded) {
            NodeActionFab(
                icon = Icons.Default.Delete,
                label = stringResource(id = R.string.delete_node_title),
                containerColor = colorResource(id = R.color.button_leave),
                onClick = onDelete
            )
            NodeActionFab(
                icon = Icons.Default.Add,
                label = stringResource(id = R.string.add_connection_title),
                containerColor = colorResource(id = R.color.button_join),
                onClick = onAddConnection
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
        text = { Text(text = label, color = Color.White) },
        containerColor = containerColor,
        contentColor = Color.White
    )
}

