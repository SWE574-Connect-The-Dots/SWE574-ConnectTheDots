package com.yybb.myapplication.presentation.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.ui.screens.Collaborator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun CollaboratorDialog(
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onCollaboratorClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter collaborators based on search query
    val filteredCollaborators = remember(collaborators, searchQuery) {
        if (searchQuery.isBlank()) {
            collaborators
        } else {
            collaborators.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 30.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = stringResource(R.string.space_collaborators_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    modifier = Modifier.Companion.padding(bottom = 16.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_space_hint_msg)) },
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_button),
                        )
                    }
                )

                // Collaborators List
                LazyColumn(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCollaborators) { collaborator ->
                        CollaboratorItem(
                            collaborator = collaborator,
                            onClick = {
                                onCollaboratorClick(collaborator.name)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.Companion.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.Companion.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.close_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CollaboratorItem(
    collaborator: Collaborator,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = collaborator.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.Medium
            )
        }
    }
}