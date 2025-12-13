package com.yybb.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.presentation.ui.viewmodel.EditProfileUiState
import com.yybb.myapplication.presentation.ui.viewmodel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit,
    onSave: (profession: String, bio: String?, city: String?, country: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is EditProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is EditProfileUiState.Success -> {
            EditProfileContent(
                user = state.user,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onSave = onSave
            )
        }

        is EditProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    user: User,
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit,
    onSave: (profession: String, bio: String?, city: String?, country: String?) -> Unit
) {
    var profession by remember { mutableStateOf(user.profession) }
    var bio by remember { mutableStateOf(user.bio) }
    val maxCharLimitForProfession = 100
    val maxCharLimitForBio = 500

    // Parse location_name to extract country and city
    val (initialCountry, initialCity) = remember(user.locationName) {
        if (user.locationName != null && user.locationName.contains(",")) {
            val parts = user.locationName.split(",").map { it.trim() }
            if (parts.size >= 2) {
                Pair(parts[1], parts[0]) // Format: "City, Country"
            } else {
                Pair(null, null)
            }
        } else {
            Pair(null, null)
        }
    }

    var selectedCountry by remember { mutableStateOf<String?>(initialCountry) }
    var selectedCity by remember { mutableStateOf<String?>(initialCity) }
    var countrySearchQuery by remember { mutableStateOf(initialCountry ?: "") }
    var citySearchQuery by remember { mutableStateOf(initialCity ?: "") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val countries by viewModel.countries.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoadingCountries by viewModel.isLoadingCountries.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()

    // Track if this is the first load
    var isInitialLoad by remember { mutableStateOf(true) }

    // Load cities when country is selected
    LaunchedEffect(selectedCountry) {
        if (selectedCountry != null) {
            if (!isInitialLoad || initialCity == null) {
                // Reset city if country changed or no initial city
                selectedCity = null
                citySearchQuery = ""
            }
            viewModel.loadCities(selectedCountry!!)
            isInitialLoad = false
        } else {
            selectedCity = null
            citySearchQuery = ""
        }
    }

    // Set initial city after cities are loaded (only on first load)
    LaunchedEffect(cities, initialCity, isInitialLoad) {
        if (isInitialLoad && cities.isNotEmpty() && initialCity != null && selectedCountry == initialCountry) {
            // Check if the initial city exists in the loaded cities
            val matchingCity = cities.find { it.equals(initialCity, ignoreCase = true) }
            if (matchingCity != null) {
                selectedCity = matchingCity
                citySearchQuery = matchingCity
            }
        }
    }

    // Filter countries based on search query
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

    // Filter cities based on search query
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editing ${user.username}'s Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = profession,
                onValueChange = {
                    if (it.length <= maxCharLimitForProfession) {
                        profession = it
                    }
                },
                label = { Text("Profession") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = bio ?: "",
                onValueChange = {
                    if (it.length <= maxCharLimitForBio) {
                        bio = it
                    }
                },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Country Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Country",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val countryFocusRequester = remember { FocusRequester() }
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
                            isCountryDropdownExpanded = true
                        },
                        placeholder = { Text("Select Country") },
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
                    ExposedDropdownMenu(
                        expanded = isCountryDropdownExpanded && !isLoadingCountries,
                        onDismissRequest = { 
                            isCountryDropdownExpanded = false
                        }
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
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // City Dropdown (only visible when country is selected)
            if (selectedCountry != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "City",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    val cityFocusRequester = remember { FocusRequester() }
                    ExposedDropdownMenuBox(
                        expanded = isCityDropdownExpanded,
                        onExpandedChange = { expanded ->
                            isCityDropdownExpanded = expanded
                        }
                    ) {
                        OutlinedTextField(
                            value = citySearchQuery,
                            onValueChange = { query ->
                                citySearchQuery = query
                                isCityDropdownExpanded = true
                            },
                            placeholder = { Text("Select City") },
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
                        // Keep focus when typing or when dropdown opens
                        LaunchedEffect(isCityDropdownExpanded, citySearchQuery) {
                            if (isCityDropdownExpanded) {
                                cityFocusRequester.requestFocus()
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = isCityDropdownExpanded && !isLoadingCities,
                            onDismissRequest = { 
                                isCityDropdownExpanded = false
                            },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            if (filteredCities.isEmpty() && citySearchQuery.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No cities found") },
                                    onClick = { }
                                )
                            } else {
                                val displayCities = filteredCities.take(100)
                                displayCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city) },
                                        onClick = {
                                            selectedCity = city
                                            citySearchQuery = city
                                            isCityDropdownExpanded = false
                                        }
                                    )
                                }
                                if (filteredCities.size > 100) {
                                    DropdownMenuItem(
                                        text = { Text("... and ${filteredCities.size - 100} more. Refine your search.") },
                                        onClick = { }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSave(profession, bio, selectedCity, selectedCountry) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = profession.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }

    // Loading dialogs
    if (isLoadingCountries) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Loading Countries") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we load the countries...")
                }
            },
            confirmButton = { }
        )
    }

    if (isLoadingCities && selectedCountry != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Loading Cities") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we load cities for $selectedCountry...")
                }
            },
            confirmButton = { }
        )
    }
}
