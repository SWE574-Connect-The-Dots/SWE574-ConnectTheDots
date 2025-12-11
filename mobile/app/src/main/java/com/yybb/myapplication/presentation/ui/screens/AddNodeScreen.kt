package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yybb.myapplication.R
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.viewmodel.AddNodeEvent
import com.yybb.myapplication.presentation.ui.viewmodel.AddNodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeScreen(
    navController: NavController,
    spaceId: String,
    viewModel: AddNodeViewModel = hiltViewModel()
) {
    var wikidataSearchQuery by remember { mutableStateOf("") }
    val wikidataSearchResults by viewModel.wikidataSearchResults.collectAsState()
    val isSearchingWikidata by viewModel.isSearchingWikidata.collectAsState()
    val wikidataSearchError by viewModel.wikidataSearchError.collectAsState()
    val selectedEntity by viewModel.selectedEntity.collectAsState()
    val entityProperties by viewModel.entityProperties.collectAsState()
    val isLoadingProperties by viewModel.isLoadingProperties.collectAsState()
    val propertySearchQuery by viewModel.propertySearchQuery.collectAsState()
    val filteredProperties by viewModel.filteredProperties.collectAsState()
    val selectedProperties by viewModel.selectedProperties.collectAsState()
    val availableNodes by viewModel.availableNodes.collectAsState()
    val isLoadingNodes by viewModel.isLoadingNodes.collectAsState()
    val edgeLabelSearchResults by viewModel.edgeLabelSearchResults.collectAsState()
    val isEdgeLabelSearching by viewModel.isEdgeLabelSearching.collectAsState()
    val edgeLabelSearchError by viewModel.edgeLabelSearchError.collectAsState()
    val isCreatingNode by viewModel.isCreatingNode.collectAsState()
    val createNodeError by viewModel.createNodeError.collectAsState()
    val createNodeSuccess by viewModel.createNodeSuccess.collectAsState()

    var isForwardDirection by remember { mutableStateOf(true) }
    var connectToNodeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedConnectToNode by remember { mutableStateOf<SpaceNode?>(null) }
    var edgeLabel by remember { mutableStateOf("") }
    var showEdgeLabelResults by remember { mutableStateOf(false) }
    var selectedEdgeLabelProperty by remember { mutableStateOf<WikidataProperty?>(null) }

    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clearFocusRequester = remember { FocusRequester() }

    LaunchedEffect(edgeLabelSearchError) {
        if (edgeLabelSearchError != null) {
            showEdgeLabelResults = true
        }
    }

    // Loading Dialogs
    if (isSearchingWikidata) {
        LoadingDialog(message = stringResource(R.string.searching_label))
    }

    if (isLoadingProperties) {
        LoadingDialog(message = stringResource(R.string.loading_properties_message))
    }

    if (isLoadingNodes) {
        LoadingDialog(message = stringResource(R.string.loading_nodes_message))
    }

    if (isCreatingNode) {
        LoadingDialog(message = stringResource(R.string.creating_node_message))
    }

    val showLocationDialog by viewModel.showLocationDialog.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()

    if (isLoadingCities && showLocationDialog) {
        LoadingDialog(message = "Loading cities...")
    }

    if (showLocationDialog) {
        AddLocationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideLocationDialog() }
        )
    }

    // Handle create node success - show toast
    LaunchedEffect(key1 = createNodeSuccess) {
        createNodeSuccess?.let { message ->
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Handle navigation events from ViewModel
    viewModel.eventFlow.CollectAsEffect { event ->
        when (event) {
            is AddNodeEvent.NavigateBack -> {
                // Navigate back to node list screen
                navController.popBackStack()
            }
        }
    }

    // Handle create node error
    LaunchedEffect(createNodeError) {
        if (createNodeError != null) {
            Toast.makeText(
                context,
                createNodeError,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearCreateNodeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_node_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
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
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Wikidata Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = wikidataSearchQuery,
                    onValueChange = { wikidataSearchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_wikidata_hint)) },
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (wikidataSearchQuery.isNotBlank()) {
                            viewModel.searchWikidataEntities(wikidataSearchQuery)
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        }
                    },
                    enabled = wikidataSearchQuery.isNotBlank() && !isSearchingWikidata,
                    modifier = Modifier
                        .width(70.dp)
                        .height(56.dp)
                ) {
                    if (isSearchingWikidata) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search_button),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (wikidataSearchError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = wikidataSearchError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = {
                    viewModel.clearWikidataSearchError()
                    if (wikidataSearchQuery.isNotBlank()) {
                        viewModel.searchWikidataEntities(wikidataSearchQuery)
                    }
                }) {
                    Text(text = stringResource(R.string.retry_button_label))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Entity Selection
            if (selectedEntity != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "${selectedEntity!!.label} ${selectedEntity!!.description}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Selected Entity") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Entity Search Results
            if (wikidataSearchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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
                            text = "Select an entity:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 220.dp)
                        ) {
                            items(
                                items = wikidataSearchResults,
                                key = { it.id }
                            ) { entity ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable {
                                            viewModel.selectEntity(entity)
                                            keyboardController?.hide()
                                            focusManager.clearFocus(force = true)
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
                                            text = "${entity.label} (${entity.id})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (entity.description.isNotBlank()) {
                                            Text(
                                                text = entity.description,
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Selected Entity Display
            if (selectedEntity != null) {
                Text(
                    text = stringResource(R.string.selected_entity_label, selectedEntity!!.label),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.property_selection_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Property Search
            if (selectedEntity != null) {
                OutlinedTextField(
                    value = propertySearchQuery,
                    onValueChange = { query ->
                        viewModel.updatePropertySearchQuery(query)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_property_hint)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )

                // Property Results
                if (entityProperties.isNotEmpty() && !isLoadingProperties) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = if (propertySearchQuery.length > 2) filteredProperties else entityProperties,
                                key = { it.statementId }
                            ) { property ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedProperties.contains(property.statementId),
                                        onCheckedChange = {
                                            viewModel.togglePropertySelection(property.statementId)
                                        }
                                    )
                                    PropertyDisplayTextInAddNode(
                                        property = property,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        onValueClick = { property ->
                                            // Navigate to WebView with Wikidata URL if entity has an ID
                                            property.entityId?.let { entityId ->
                                                val wikidataUrl = "https://www.wikidata.org/wiki/$entityId"
                                                navController.navigate(Screen.WebView.createRoute(wikidataUrl))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLoadingProperties) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connect To Node Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.connect_to_node_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                ExposedDropdownMenuBox(
                    expanded = connectToNodeDropdownExpanded,
                    onExpandedChange = { connectToNodeDropdownExpanded = !connectToNodeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedConnectToNode?.label ?: stringResource(R.string.select_a_node_default),
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = {
                            Text(stringResource(R.string.select_a_node_default))
                        },
                        trailingIcon = {
                            androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = connectToNodeDropdownExpanded
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = connectToNodeDropdownExpanded,
                        onDismissRequest = { connectToNodeDropdownExpanded = false }
                    ) {
                        // Default option: "Select a node"
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_a_node_default)) },
                            onClick = {
                                selectedConnectToNode = null
                                connectToNodeDropdownExpanded = false
                            }
                        )
                        // Available nodes
                        availableNodes.forEach { node ->
                            DropdownMenuItem(
                                text = { Text(node.label) },
                                onClick = {
                                    selectedConnectToNode = node
                                    connectToNodeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Only show edge direction and edge label if a node is selected for connection
            if (selectedConnectToNode != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Edge Direction
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.edge_direction_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    val directionText = if (isForwardDirection) {
                        "Existing -> new"
                    } else {
                        "new -> Existing"
                    }
                    Button(
                        onClick = { isForwardDirection = !isForwardDirection },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isForwardDirection)
                                colorResource(id = R.color.button_join)
                            else
                                colorResource(id = R.color.button_leave),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = directionText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edge Label
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.edge_label_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = edgeLabel,
                    onValueChange = { input ->
                        edgeLabel = input
                        selectedEdgeLabelProperty = null
                        val normalized = input.trim()
                        if (normalized.length > 2) {
                            showEdgeLabelResults = true
                            viewModel.searchEdgeLabelOptions(normalized)
                        } else {
                            showEdgeLabelResults = false
                            viewModel.resetEdgeLabelSearch()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.add_edge_label_placeholder)) },
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
                if (edgeLabelSearchError != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = edgeLabelSearchError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(
                            onClick = {
                                viewModel.resetEdgeLabelSearch()
                                if (edgeLabel.trim().length >= 3) {
                                    viewModel.searchEdgeLabelOptions(edgeLabel.trim())
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.retry_button_label))
                        }
                    }
                }
                if (showEdgeLabelResults) {
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
                                        text = stringResource(R.string.searching_label),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            edgeLabelSearchResults.isEmpty() && edgeLabelSearchError == null -> {
                                // No results, but keep the text as written (user can use custom label)
                            }

                            else -> {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_edge_search_results_title),
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
                                                        selectedEdgeLabelProperty = result
                                                        showEdgeLabelResults = false
                                                        viewModel.resetEdgeLabelSearch()
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location (Optional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { viewModel.showLocationDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF436FED)
                    )
                ) {
                    Text("Add Location", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Node Button
            // Edge label is only required if connecting to another node
            val requiresEdgeLabel = selectedConnectToNode != null
            val canCreateNode = selectedEntity != null && 
                    (!requiresEdgeLabel || edgeLabel.isNotBlank()) && 
                    !isCreatingNode

            Button(
                onClick = {
                    viewModel.createNode(
                        relatedNodeId = selectedConnectToNode?.id?.toString(),
                        edgeLabel = edgeLabel,
                        isNewNodeSource = isForwardDirection,
                        selectedEdgeLabelProperty = selectedEdgeLabelProperty
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canCreateNode,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = stringResource(R.string.create_node_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PropertyDisplayTextInAddNode(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLocationDialog(
    viewModel: AddNodeViewModel,
    onDismiss: () -> Unit
) {
    val countries by viewModel.countries.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoadingCountries by viewModel.isLoadingCountries.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()
    val isGettingCoordinates by viewModel.isGettingCoordinates.collectAsState()
    val coordinatesResult by viewModel.coordinatesResult.collectAsState()
    val locationData by viewModel.locationData.collectAsState()

    var selectedCountry by remember { mutableStateOf<String?>(locationData?.country) }
    var selectedCity by remember { mutableStateOf<String?>(locationData?.city) }
    var locationNameText by remember { mutableStateOf(locationData?.locationName ?: "") }
    var latitudeText by remember { mutableStateOf(locationData?.latitude?.toString() ?: "") }
    var longitudeText by remember { mutableStateOf(locationData?.longitude?.toString() ?: "") }
    var countrySearchQuery by remember { mutableStateOf(locationData?.country ?: "") }
    var citySearchQuery by remember { mutableStateOf(locationData?.city ?: "") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    val countryFocusRequester = remember { FocusRequester() }
    val cityFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Load cities when country is selected
    LaunchedEffect(selectedCountry) {
        if (selectedCountry != null) {
            if (locationData?.city == null) {
                selectedCity = null
                citySearchQuery = ""
            }
            viewModel.loadCities(selectedCountry!!)
        } else {
            selectedCity = null
            citySearchQuery = ""
        }
    }

    // Filter countries and cities
    val filteredCountries = remember(countries, countrySearchQuery) {
        val query = countrySearchQuery.trim()
        if (query.isEmpty()) {
            countries
        } else {
            countries.filter { country ->
                country.name.contains(query, ignoreCase = true)
            }
        }
    }

    val filteredCities = remember(cities, citySearchQuery) {
        val query = citySearchQuery.trim()
        if (query.isEmpty()) {
            cities
        } else {
            cities.filter { city ->
                city.contains(query, ignoreCase = true)
            }
        }
    }

    // Handle coordinate response
    LaunchedEffect(coordinatesResult) {
        coordinatesResult?.let { coordinates ->
            locationNameText = coordinates.displayName
            latitudeText = coordinates.latitude.toString()
            longitudeText = coordinates.longitude.toString()
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.95f),
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 550.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Location:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Divider()
                }

                // Country Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Country:",
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = isCountryDropdownExpanded,
                        onExpandedChange = { expanded ->
                            isCountryDropdownExpanded = expanded
                            if (expanded) {
                                keyboardController?.hide()
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = countrySearchQuery,
                            onValueChange = { query ->
                                countrySearchQuery = query
                                keyboardController?.hide()
                                isCountryDropdownExpanded = true
                            },
                            placeholder = { Text("-- Select Country --") },
                            trailingIcon = {
                                if (isLoadingCountries) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isCountryDropdownExpanded
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .focusRequester(countryFocusRequester),
                            enabled = !isLoadingCountries
                        )
                        LaunchedEffect(isCountryDropdownExpanded) {
                            if (isCountryDropdownExpanded) {
                                keyboardController?.hide()
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = isCountryDropdownExpanded && !isLoadingCountries,
                            onDismissRequest = { isCountryDropdownExpanded = false }
                        ) {
                            if (filteredCountries.isEmpty() && countrySearchQuery.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No countries found") },
                                    onClick = { }
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .heightIn(max = 250.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    filteredCountries.forEach { country ->
                                        DropdownMenuItem(
                                            text = { Text(country.name) },
                                            onClick = {
                                                selectedCountry = country.name
                                                countrySearchQuery = country.name
                                                isCountryDropdownExpanded = false
                                                viewModel.loadCities(country.name)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // City Dropdown (only visible when country is selected)
                if (selectedCountry != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "City:",
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = isCityDropdownExpanded,
                            onExpandedChange = { isCityDropdownExpanded = !isCityDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = citySearchQuery,
                                onValueChange = { query ->
                                    citySearchQuery = query
                                    isCityDropdownExpanded = true
                                },
                                placeholder = { Text("-- Select City --") },
                                trailingIcon = {
                                    if (isLoadingCities) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = isCityDropdownExpanded
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .focusRequester(cityFocusRequester),
                                enabled = !isLoadingCities
                            )
                            LaunchedEffect(isCityDropdownExpanded) {
                                if (isCityDropdownExpanded) {
                                    cityFocusRequester.requestFocus()
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = isCityDropdownExpanded && !isLoadingCities,
                                onDismissRequest = { isCityDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                if (filteredCities.isEmpty() && citySearchQuery.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No cities found") },
                                        onClick = { }
                                    )
                                } else {
                                    filteredCities.take(100).forEach { city ->
                                        DropdownMenuItem(
                                            text = { Text(city) },
                                            onClick = {
                                                selectedCity = city
                                                citySearchQuery = city
                                                isCityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Get Coordinates Button
                if (selectedCountry != null && selectedCity != null) {
                    Button(
                        onClick = {
                            viewModel.getCoordinatesFromAddress(selectedCity, selectedCountry)
                        },
                        enabled = !isGettingCoordinates,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGettingCoordinates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Get Coordinates from Address", color = Color.White)
                    }
                }

                // Location Name
                OutlinedTextField(
                    value = locationNameText,
                    onValueChange = { locationNameText = it },
                    label = { Text("Location Name (optional)", fontSize = 13.sp) },
                    placeholder = { Text("Enter location name manually", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                // Latitude and Longitude
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = { Text("Latitude", fontSize = 13.sp) },
                        placeholder = { Text("e.g., 40.7128", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = { Text("Longitude", fontSize = 13.sp) },
                        placeholder = { Text("e.g., -74.0060", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val lat = latitudeText.toDoubleOrNull()
                            val lon = longitudeText.toDoubleOrNull()
                            viewModel.saveLocationData(
                                country = selectedCountry,
                                city = selectedCity,
                                locationName = locationNameText.takeIf { it.isNotBlank() },
                                latitude = lat,
                                longitude = lon
                            )
                        },
                        enabled = !isLoadingCities,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBDBDBD),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    )
}
