package com.yybb.myapplication.presentation.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.components.DatePickerModal
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import com.yybb.myapplication.presentation.ui.utils.ViewState
import com.yybb.myapplication.presentation.ui.viewmodel.AuthEvent
import com.yybb.myapplication.presentation.ui.viewmodel.RegisterViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var countrySearchQuery by remember { mutableStateOf("") }
    var citySearchQuery by remember { mutableStateOf("") }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    val state by viewModel.viewState.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoadingCountries by viewModel.isLoadingCountries.collectAsState()
    val isLoadingCities by viewModel.isLoadingCities.collectAsState()

    viewModel.eventFlow.CollectAsEffect { event ->
        if (event is AuthEvent.NavigateToLogin) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.AuthGraph.route) { inclusive = true }
            }
        }
    }

    // Load cities when country is selected
    androidx.compose.runtime.LaunchedEffect(selectedCountry) {
        if (selectedCountry != null) {
            selectedCity = null // Reset city when country changes
            citySearchQuery = "" // Reset city search
            viewModel.loadCities(selectedCountry!!)
        } else {
            selectedCity = null
            citySearchQuery = ""
        }
    }

    // Filter countries based on search query - optimized with remember
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

    // Filter cities based on search query - optimized with remember
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

    // 🔹 Show dialog when error occurs
    if (state is ViewState.Error) {
        val errorMessage = (state as ViewState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.ok_button))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage) }
        )
    }

    // 🔹 Show loading dialog when fetching countries
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

    // 🔹 Show loading dialog when fetching cities
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 42.dp),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(top = 32.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Disable form while loading countries initially
            val isFormEnabled = !isLoadingCountries
            Text(
                text = stringResource(id = R.string.headline),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            InputField(
                stringResource(id = R.string.email_title),
                email,
                { email = it },
                "example@gmail.com",
                isEmail = true,
                isProfession = false,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.username_title),
                username,
                { username = it },
                "johnDoe",
                isEmail = false,
                isProfession = false,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.password_title),
                password,
                { password = it },
                "********",
                isEmail = false,
                isProfession = false,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.profession_title),
                profession,
                { input ->
                    profession = input.filter { it.isLetter() || it.isWhitespace() }
                },
                "Teacher",
                isEmail = false,
                isProfession = true,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.date_of_birth_title),
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clickable(enabled = isFormEnabled) { isDatePickerOpen = true }
                ) {
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = {},
                        placeholder = { Text("MM / DD / YYYY") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "Select Date",
                                modifier = Modifier
                                    .clickable(enabled = isFormEnabled) { isDatePickerOpen = true }
                            )
                        },
                        enabled = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isDatePickerOpen) {
                DatePickerModal(
                    onDateSelected = { millis ->
                        millis?.let {
                            val cal = Calendar.getInstance().apply { timeInMillis = it }
                            val formatted =
                                String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.DAY_OF_MONTH)
                                )
                            dateOfBirth = formatted
                        }
                    },
                    onDismiss = { isDatePickerOpen = false }
                )
            }

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
                    }
                ) {
                    OutlinedTextField(
                        value = countrySearchQuery,
                        onValueChange = { query ->
                            countrySearchQuery = query
                            // Always keep dropdown open while typing
                            isCountryDropdownExpanded = true
                        },
                        placeholder = { Text("Search or Select Country") },
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
                    // Keep focus when typing or when dropdown opens
                    LaunchedEffect(isCountryDropdownExpanded, countrySearchQuery) {
                        if (isCountryDropdownExpanded) {
                            countryFocusRequester.requestFocus()
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = isCountryDropdownExpanded && !isLoadingCountries,
                        onDismissRequest = { 
                            isCountryDropdownExpanded = false
                            // Don't reset search query - let user keep their search
                        },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        if (filteredCountries.isEmpty() && countrySearchQuery.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No countries found") },
                                onClick = { }
                            )
                        } else {
                            // Limit items for better performance, show first 100 matches
                            val displayCountries = filteredCountries.take(100)
                            displayCountries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country.name) },
                                    onClick = {
                                        selectedCountry = country.name
                                        countrySearchQuery = country.name
                                        isCountryDropdownExpanded = false
                                    }
                                )
                            }
                            if (filteredCountries.size > 100) {
                                DropdownMenuItem(
                                    text = { Text("... and ${filteredCountries.size - 100} more. Refine your search.") },
                                    onClick = { }
                                )
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
                                // Always keep dropdown open while typing
                                isCityDropdownExpanded = true
                            },
                            placeholder = { Text("Search or Select City") },
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
                                // Don't reset search query - let user keep their search
                            },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            if (filteredCities.isEmpty() && citySearchQuery.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No cities found") },
                                    onClick = { }
                                )
                            } else {
                                // Limit items for better performance, show first 100 matches
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.checkInputsAndNavigate(
                        email,
                        username,
                        password,
                        profession,
                        dateOfBirth,
                        selectedCountry,
                        selectedCity
                    )
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = isFormEnabled
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Register Icon",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(id = R.string.register_button), fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SignInText(
                onClick = { viewModel.onBackToLoginClicked() }
            )
        }

        if (state is ViewState.Loading) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Registering...") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Please wait while we register your account")
                    }
                },
                confirmButton = { }
            )
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isEmail: Boolean,
    isProfession: Boolean,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(8.dp)),
            singleLine = true,
            keyboardOptions = when {
                isEmail -> KeyboardOptions(keyboardType = KeyboardType.Email)
                isProfession -> KeyboardOptions(keyboardType = KeyboardType.Text)
                else -> KeyboardOptions.Default
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            enabled = enabled
        )
    }
}

@Composable
fun SignInText(onClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("${stringResource(R.string.have_account_text)} ")

        pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up")
        withStyle(
            style = SpanStyle(
                color = Color(0xFF007BFF),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        ) {
            append(stringResource(R.string.sign_in_option))
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset)
                .firstOrNull()?.let { _ -> onClick() }
        },
        style = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}