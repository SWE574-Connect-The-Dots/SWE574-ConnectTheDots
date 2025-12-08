package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.yybb.myapplication.data.Constants.DESCRIPTION_FIELD_HEIGHT
import com.yybb.myapplication.data.Constants.MAX_TITLE_LENGTH
import com.yybb.myapplication.data.Constants.TAG_CARD_HEIGHT
import com.yybb.myapplication.data.Constants.TAG_CARD_PADDING
import com.yybb.myapplication.data.Constants.TAG_ICON_SIZE
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.presentation.ui.viewmodel.CreateSpaceFormState
import com.yybb.myapplication.presentation.ui.viewmodel.CreateSpaceUiState
import com.yybb.myapplication.presentation.ui.viewmodel.CreateSpaceViewModel
import com.yybb.myapplication.presentation.ui.viewmodel.RetrieveTagWikidataUiState

@Composable
fun CreateSpaceScreen(
    viewModel: CreateSpaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Int) -> Unit // new parameter
) {
    val tagWikidataUiState by viewModel.tagWikidataUiState.collectAsState()
    val createSpaceUiState by viewModel.createSpaceUiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val searchResults = when (val tagState = tagWikidataUiState) {
        is RetrieveTagWikidataUiState.Success -> tagState.tags
        else -> emptyList()
    }

    if (tagWikidataUiState is RetrieveTagWikidataUiState.Loading) {
        LoadingDialog(message = stringResource(R.string.loading_tags_message))
    }

    if (createSpaceUiState is CreateSpaceUiState.Loading) {
        LoadingDialog(message = stringResource(R.string.creating_space_message))
    }

    if (createSpaceUiState is CreateSpaceUiState.Success) {
        val spaceId = (createSpaceUiState as CreateSpaceUiState.Success).spaceId
        LaunchedEffect(spaceId) {
            onNavigateToDetails(spaceId)
            viewModel.resetCreateSpaceState()
        }
    }

    // Handle errors
    when (val state = createSpaceUiState) {
        is CreateSpaceUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetCreateSpaceState() },
                title = { Text(text = stringResource(R.string.error)) },
                text = { Text(text = state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetCreateSpaceState() }) {
                        Text(stringResource(R.string.ok_button))
                    }
                }
            )
        }
        else -> { /* Nothing to do */ }
    }

    when (val tagState = tagWikidataUiState) {
        is RetrieveTagWikidataUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetTagWikidataState() },
                title = { Text(text = stringResource(R.string.error)) },
                text = { Text(text = tagState.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetTagWikidataState() }) {
                        Text(stringResource(R.string.ok_button))
                    }
                }
            )
        }
        else -> { /* Nothing to do */ }
    }

    val showLocationDialog by viewModel.showLocationDialog.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()

    if (isLoadingCities && showLocationDialog) {
        LoadingDialog(message = "Loading cities...")
    }

    if (showLocationDialog) {
        AddSpaceLocationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideLocationDialog() }
        )
    }

    CreateSpaceContent(
        formState = formState,
        onFormStateChange = { newState -> viewModel.updateFormState(newState) },
        onNavigateBack = onNavigateBack,
        onGetWikiTags = { query -> viewModel.getTags(query) },
        onCreateClicked = { form -> viewModel.createSpace(formState) },
        searchResults = searchResults,
        viewModel = viewModel
    )
}

@Composable
fun LoadingDialog(message: String) {
    AlertDialog(
        onDismissRequest = { },
        title = {},
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message)
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceContent(
    formState: CreateSpaceFormState,
    onFormStateChange: (CreateSpaceFormState) -> Unit,
    onNavigateBack: () -> Unit,
    onGetWikiTags: (tagQuery: String) -> Unit,
    onCreateClicked: (CreateSpaceFormState) -> Unit,
    searchResults: List<TagDto> = emptyList(),
    viewModel: CreateSpaceViewModel
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val resetForm = {
        onFormStateChange(CreateSpaceFormState())
    }

    // Add tag function
    val addTag = { tag: TagDto ->
        val exists = formState.selectedTags.any { it.id == tag.id  && it.url == tag.url}
        if (!exists) {
            onFormStateChange(formState.copy(
                selectedTags = formState.selectedTags + tag,
                tagSearchQuery = "",
                showSearchResults = false,
                showTagSearch = true,
            ))
        }
    }

    // Remove tag function
    val removeTag = { tag: TagDto ->
        onFormStateChange(formState.copy(
            selectedTags = formState.selectedTags.filter { it.id != tag.id && it.url != tag.url }
        ))
    }

    // Toggle tag search function
    val toggleTagSearch = {
        val showSearch = if (formState.showTagSearch == false) true else false
        onFormStateChange(formState.copy(
            showTagSearch = showSearch
        ))
    }

    // Create space function
    val createSpace = {
        if (formState.isFormValid) {
            onCreateClicked(formState)
            resetForm()
        } else {
            Toast.makeText(context, context.getString(R.string.fill_inputs_error_msg), Toast.LENGTH_SHORT).show()
        }
    }

    // Handle search click with service call
    val handleSearchClick = {
        if (formState.isSearchEnabled) {
            onFormStateChange(formState.copy(
                showTagSearch = true,
                showSearchResults = true
            ))
            onGetWikiTags(formState.tagSearchQuery)
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
                        onFormStateChange(formState.copy(spaceTitle = newTitle))
                    }
                }
            )

            // Space Description Section
            SpaceDescriptionSection(
                description = formState.spaceDescription,
                onDescriptionChange = { newDescription ->
                    onFormStateChange(formState.copy(spaceDescription = newDescription))
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
                    onFormStateChange(formState.copy(
                        tagSearchQuery = newQuery,
                        showSearchResults = false
                    ))
                },
                onSearchClick = handleSearchClick
            )

            // Location Section
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location:",
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
    searchResults: List<TagDto>,
    onAddTag: (TagDto) -> Unit,
    onRemoveTag: (TagDto) -> Unit,
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
                Text(
                    text = stringResource(R.string.selected_tags_title, formState.selectedTags.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Blue
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    formState.selectedTags.forEach { tag ->
                        SelectedTagCard(
                            tag = tag.description,
                            onRemoveClick = { onRemoveTag(tag) }
                        )
                    }
                }
            } else {
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
        }

        // Search Results (shown when showSearchResults is true)
        if (formState.showSearchResults && searchResults.isNotEmpty()) {
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
                            result = result.description,
                            onAddClick = { onAddTag(result) }
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSpaceLocationDialog(
    viewModel: CreateSpaceViewModel,
    onDismiss: () -> Unit
) {
    val countries by viewModel.countries.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoadingCountries by viewModel.isLoadingCountries.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    var tempSelectedCountry by remember { mutableStateOf<String?>(selectedCountry) }
    var tempSelectedCity by remember { mutableStateOf<String?>(selectedCity) }
    var countrySearchQuery by remember { mutableStateOf(tempSelectedCountry ?: "") }
    var citySearchQuery by remember { mutableStateOf(tempSelectedCity ?: "") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    val countryFocusRequester = remember { FocusRequester() }
    val cityFocusRequester = remember { FocusRequester() }

    // Load cities when country is selected
    LaunchedEffect(tempSelectedCountry) {
        if (tempSelectedCountry != null) {
            if (tempSelectedCity == null || tempSelectedCity != selectedCity) {
                citySearchQuery = ""
                tempSelectedCity = null
            }
            viewModel.loadCities(tempSelectedCountry!!)
        } else {
            citySearchQuery = ""
            tempSelectedCity = null
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
                    .heightIn(min = 200.dp, max = 400.dp),
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
                        onExpandedChange = { isCountryDropdownExpanded = !isCountryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = countrySearchQuery,
                            onValueChange = { query ->
                                countrySearchQuery = query
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
                                countryFocusRequester.requestFocus()
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = isCountryDropdownExpanded && !isLoadingCountries,
                            onDismissRequest = { isCountryDropdownExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            if (filteredCountries.isEmpty() && countrySearchQuery.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No countries found") },
                                    onClick = { }
                                )
                            } else {
                                filteredCountries.take(100).forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(country.name) },
                                        onClick = {
                                            countrySearchQuery = country.name
                                            tempSelectedCountry = country.name
                                            isCountryDropdownExpanded = false
                                            viewModel.loadCities(country.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // City Dropdown (only visible when country is selected)
                if (tempSelectedCountry != null) {
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
                                                citySearchQuery = city
                                                tempSelectedCity = city
                                                isCityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveLocationData(tempSelectedCountry, tempSelectedCity)
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