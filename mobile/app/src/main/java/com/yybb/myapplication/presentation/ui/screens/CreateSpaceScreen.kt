package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.R
import com.yybb.myapplication.data.Constants
import com.yybb.myapplication.data.Constants.DESCRIPTION_FIELD_HEIGHT
import com.yybb.myapplication.data.Constants.MAX_TITLE_LENGTH
import com.yybb.myapplication.data.Constants.TAG_CARD_HEIGHT
import com.yybb.myapplication.data.Constants.TAG_CARD_PADDING
import com.yybb.myapplication.data.Constants.TAG_ICON_SIZE
import com.yybb.myapplication.presentation.ui.viewmodel.CreateSpaceViewModel


// Data class for form state
@Stable
private data class CreateSpaceFormState(
    val spaceTitle: String = "",
    val spaceDescription: String = "",
    val selectedTags: List<String> = emptyList(),
    val showTagSearch: Boolean = false,
    val tagSearchQuery: String = "",
    val showSearchResults: Boolean = false
) {
    val isFormValid: Boolean
        get() = spaceTitle.isNotBlank() && spaceDescription.isNotBlank()

    val isSearchEnabled: Boolean
        get() = tagSearchQuery.isNotBlank()
}

@Composable
fun CreateSpaceScreen(
    viewModel: CreateSpaceViewModel,
    onNavigateBack: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    CreateSpaceContent(
        onNavigateBack = onNavigateBack
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceContent(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var formState by remember {
        mutableStateOf(CreateSpaceFormState())
    }

    // Memoized search results to prevent recreation
    val searchResults = remember { getDummySearchResults() }

    // Reset form function
    val resetForm = {
        formState = CreateSpaceFormState()
    }

    // Add tag function
    val addTag = { tag: String ->
        if (!formState.selectedTags.contains(tag)) {
            formState = formState.copy(
                selectedTags = formState.selectedTags + tag,
                tagSearchQuery = "",
                showSearchResults = false
            )
        }
    }

    // Remove tag function
    val removeTag = { tag: String ->
        formState = formState.copy(
            selectedTags = formState.selectedTags.filter { it != tag }
        )
    }

    // Toggle tag search function
    val toggleTagSearch = {
        formState = formState.copy(
            showTagSearch = !formState.showTagSearch,
            showSearchResults = false,
            tagSearchQuery = ""
        )
    }

    // Create space function
    val createSpace = {
        if (formState.isFormValid) {
            Toast.makeText(context, "Space created successfully!", Toast.LENGTH_SHORT).show()
            resetForm()
        } else {
            Toast.makeText(context, context.getString(R.string.fill_inputs_error_msg), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_space_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Space Title Section
            SpaceTitleSection(
                title = formState.spaceTitle,
                onTitleChange = { newTitle ->
                    if (newTitle.length <= MAX_TITLE_LENGTH) {
                        formState = formState.copy(spaceTitle = newTitle)
                    }
                }
            )

            // Space Description Section
            SpaceDescriptionSection(
                description = formState.spaceDescription,
                onDescriptionChange = { newDescription ->
                    formState = formState.copy(spaceDescription = newDescription)
                }
            )

            // Tags Section
            TagsSection(
                formState = formState,
                searchResults = searchResults,
                onAddTag = addTag,
                onRemoveTag = removeTag,
                onToggleTagSearch = toggleTagSearch,
                onSearchQueryChange = { newQuery ->
                    formState = formState.copy(
                        tagSearchQuery = newQuery,
                        showSearchResults = false
                    )
                },
                onSearchClick = {
                    if (formState.isSearchEnabled) {
                        formState = formState.copy(showSearchResults = true)
                    }
                }
            )

            // Create Space Button
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = createSpace,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.isFormValid
            ) {
                Text(
                    text = stringResource(R.string.create_space_button),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Space Title Section Component
@Composable
private fun SpaceTitleSection(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.space_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = {
                Text(
                    text = "ex: The Dyatlov Pass Incident",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        // Character count
        Text(
            text = "${title.length}/${MAX_TITLE_LENGTH}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// Space Description Section Component
@Composable
private fun SpaceDescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.space_description_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = {
                Text(
                    text = "In February 1959, nine experienced hikers from the Ural Polytechnic Institute set...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(DESCRIPTION_FIELD_HEIGHT.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            maxLines = 5
        )
    }
}

// Tags Section Component
@Composable
private fun TagsSection(
    formState: CreateSpaceFormState,
    searchResults: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onToggleTagSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.tags_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Selected Tags Display
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (formState.selectedTags.isNotEmpty()) {
                // Debug info
                Text(
                    text = stringResource(R.string.selected_tags_title, formState.selectedTags.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Blue
                )

                // Vertical list of selected tags
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    formState.selectedTags.forEach { tag ->
                        SelectedTagCard(
                            tag = tag,
                            onRemoveClick = { onRemoveTag(tag) }
                        )
                    }
                }
            } else {
                // Show placeholder when no tags are selected
                Text(
                    text = stringResource(R.string.no_tags),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        // Add/Hide Tags Button
        Button(
            onClick = onToggleTagSearch,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (formState.showTagSearch) stringResource(R.string.hide_tags_button) else stringResource(R.string.add_tags_button),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Tag Search Input (shown when showTagSearch is true)
        if (formState.showTagSearch) {
            OutlinedTextField(
                value = formState.tagSearchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search for tags...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            // Search Button
            Button(
                onClick = onSearchClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.isSearchEnabled
            ) {
                Text(
                    text = stringResource(R.string.search_button),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Search Results (shown when showSearchResults is true)
            if (formState.showSearchResults) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Select from results:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        searchResults.forEach { result ->
                            SearchResultItem(
                                result = result,
                                onAddClick = { onAddTag(result) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Selected Tag Card Component
@Composable
private fun SelectedTagCard(
    tag: String,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(TAG_CARD_HEIGHT.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TAG_CARD_PADDING.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tag,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_bin),
                    contentDescription = "Remove tag",
                    modifier = Modifier.size(TAG_ICON_SIZE.dp)
                )
            }
        }
    }
}

// Search Result Item Component
@Composable
private fun SearchResultItem(
    result: String,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddClick,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.add_button),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Dummy search results for demonstration
private fun getDummySearchResults(): List<String> {
    return listOf(
        "Ural Mountains",
        "mountain range in Russia",
        "ID: Q35600",
        "History of human settlement in the Ural Mountains",
        "chronology of creation of settles by humans in the region from the Arctic Ocean in the north to the Ural River and northwestern modern Kazakhstan",
        "ID: Q23136011",
        "Ural Mountains in Nazi planning",
        "aspect of Nazi geopolitical planning",
        "ID: Q5170733",
        "Ural Mountain Review",
        "ID: Q107327317"
    )
}

