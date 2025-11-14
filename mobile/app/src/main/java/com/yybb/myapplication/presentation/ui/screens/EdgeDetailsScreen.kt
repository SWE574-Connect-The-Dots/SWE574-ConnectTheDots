package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeDetailsScreen(
    edgeLabel: String,
    sourceId: String,
    sourceName: String,
    targetId: String,
    targetName: String,
    onNavigateBack: () -> Unit,
    onUpdateEdge: () -> Unit
) {
    var edgeLabelText by remember { mutableStateOf(edgeLabel) }
    var edgeLabelSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isForwardDirection by remember { mutableStateOf(true) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.delete_edge_title)) },
            text = {
                Text(
                    text = stringResource(id = R.string.delete_edge_confirmation)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // TODO: Handle delete edge action
                    }
                ) {
                    Text(text = stringResource(id = R.string.yes_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(text = stringResource(id = R.string.no_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = edgeLabel, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.close_button)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            EdgeActionsFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onDelete = {
                    isFabExpanded = false
                    showDeleteDialog = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 8.dp, top = 16.dp, bottom = 2.dp, end=8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Source and Target Node Information
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val sourceText = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = R.string.edge_source_label) + " ")
                    }
                    append("$sourceName + $sourceId")
                }
                Text(
                    text = sourceText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val targetText = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = R.string.edge_target_label) + " ")
                    }
                    append("$targetName + $targetId")
                }
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider()

            // Edit Edge Label & Direction Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.edit_edge_label_direction_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Edge Label Input
                OutlinedTextField(
                    value = edgeLabelText,
                    onValueChange = { newValue ->
                        edgeLabelText = newValue
                        // For now, just simulate search results based on input
                        if (newValue.length >= 3) {
                            edgeLabelSearchQuery = newValue
                            searchResults = listOf(
                                "$newValue - Property 1",
                                "$newValue - Property 2",
                                "$newValue - Property 3"
                            )
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = edgeLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Result Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (searchResults.isEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(id = R.string.edge_location_label) + " Istanbul",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(id = R.string.edge_founded_in_label) + " 2010 January",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(id = R.string.edge_size_label) + " 100000 m2",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(id = R.string.edge_flight_capacity_label) + "500",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(searchResults) { result ->
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Direction Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.edge_direction_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { isForwardDirection = !isForwardDirection },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isForwardDirection) {
                                colorResource(id = R.color.button_join)
                            } else {
                                colorResource(id = R.color.button_leave)
                            },
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isForwardDirection) {
                                "$sourceName → $targetName"
                            } else {
                                "$targetName → $sourceName"
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            }

            Button(
                onClick = {
                    onUpdateEdge()
                    onNavigateBack()
                },
                modifier = Modifier
                    .width(280.dp)
                    .height(60.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 8.dp, top = 16.dp, bottom = 2.dp, end=8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.update_edge_details_button),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EdgeActionsFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 1.dp)
    ) {
        if (isExpanded) {
            EdgeActionFab(
                icon = Icons.Default.Delete,
                label = stringResource(id = R.string.delete_edge_button),
                containerColor = colorResource(id = R.color.button_leave),
                onClick = onDelete
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
private fun EdgeActionFab(
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

