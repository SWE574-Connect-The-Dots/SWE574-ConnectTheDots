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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var agreeToShareLocation by remember { mutableStateOf(false) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    val state by viewModel.viewState.collectAsState()

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
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.username_title),
                username,
                { username = it },
                "johnDoe",
                false,
                false
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.password_title),
                password,
                { password = it },
                "********",
                false,
                false
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
                true
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
                shape = MaterialTheme.shapes.medium
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
    isProfession: Boolean
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