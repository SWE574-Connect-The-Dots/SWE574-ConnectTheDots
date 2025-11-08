package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.Constants.TAG_ICON_SIZE
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceNodeDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceNodeDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpaceNodeDetailsViewModel = hiltViewModel()
) {
    val nodeName by viewModel.nodeName.collectAsState()
    val wikidataId by viewModel.wikidataId.collectAsState()
    val nodeProperties by viewModel.nodeProperties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProperties by viewModel.filteredOptions.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

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
        }
    ) { innerPadding ->
        val bottomPadding = (innerPadding.calculateBottomPadding() - 16.dp).coerceAtLeast(0.dp)
        var selectedTabIndex by remember { mutableIntStateOf(0) }
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

                else -> ConnectionsContent()
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
private fun ConnectionsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Will be added soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

