package com.yybb.myapplication.presentation.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.components.DatePickerModal
import com.yybb.myapplication.presentation.ui.utils.ViewState
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import com.yybb.myapplication.presentation.ui.viewmodel.AuthEvent
import com.yybb.myapplication.presentation.ui.viewmodel.RegisterViewModel
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource

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
    var agreeToShareLocation by remember { mutableStateOf(false) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    val state by viewModel.viewState.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFB7B5B5),
        focusedBorderColor = Color(0xFF000000),
        cursorColor = Color(0xFF000000),
        unfocusedPlaceholderColor = Color(0xFF9E9E9E),
        focusedPlaceholderColor = Color(0xFFB0B0B0)
    )

    viewModel.eventFlow.CollectAsEffect { event ->
        if (event is AuthEvent.NavigateToLogin) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.AuthGraph.route) { inclusive = true }
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
                true,
                false,
                textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.username_title),
                username,
                { username = it },
                "johnDoe",
                false,
                false,
                textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.password_title),
                password,
                { password = it },
                "********",
                false,
                false,
                textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.profession_title),
                profession,
                { input ->
                    profession = input.filter { it.isLetter() || it.isWhitespace() }
                },
                "Teacher",
                false,
                true,
                textFieldColors
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
                        .height(52.dp)
                        .clickable { isDatePickerOpen = true }
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
                                    .clickable { isDatePickerOpen = true }
                            )
                        },
                        colors = textFieldColors,
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
                                    Locale.US,
                                    "%02d/%02d/%04d",
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.DAY_OF_MONTH),
                                    cal.get(Calendar.YEAR)
                                )
                            dateOfBirth = formatted
                        }
                    },
                    onDismiss = { isDatePickerOpen = false }
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = agreeToShareLocation,
                    onCheckedChange = { agreeToShareLocation = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Black,
                        uncheckedColor = Color.Gray
                    )
                )
                Text(
                    text = stringResource(id = R.string.location_consent_text),
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 24.dp)
                )
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
                        agreeToShareLocation
                    )
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Register Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(id = R.string.register_button), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "${stringResource(id = R.string.have_account_text)} ",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Text(
                    text = stringResource(id = R.string.sign_in_option),
                    fontSize = 14.sp,
                    color = Color(0xFF007BFF),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.onBackToLoginClicked() }
                )
            }
        }

        if (state is ViewState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
    colors: TextFieldColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(8.dp)),
            colors = colors,
            keyboardOptions = when {
                isEmail -> KeyboardOptions(keyboardType = KeyboardType.Email)
                isProfession -> KeyboardOptions(keyboardType = KeyboardType.Text)
                else -> KeyboardOptions.Default
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}