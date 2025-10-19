package com.yybb.myapplication.presentation.ui.screens.auth

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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.navigation.Screen
import com.yybb.myapplication.presentation.ui.utils.CollectAsEffect
import com.yybb.myapplication.presentation.ui.utils.ViewState
import com.yybb.myapplication.presentation.ui.viewmodel.AuthEvent
import com.yybb.myapplication.presentation.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by viewModel.viewState.collectAsState()

    viewModel.eventFlow.CollectAsEffect { event ->
        when (event) {
            is AuthEvent.NavigateToMain -> {
                navController.navigate(Screen.MainGraph.route) {
                    popUpTo(Screen.AuthGraph.route) {
                        inclusive = true
                    }
                }
            }
            is AuthEvent.NavigateToRegister -> {
                navController.navigate(Screen.Register.route) {
                    popUpTo(Screen.AuthGraph.route) {
                        inclusive = true
                    }
                }
            }
            else -> {

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
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.headline),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            InputField(
                stringResource(id = R.string.username_title),
                username,
                { username = it },
                "johnDoe",
                isPassword = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                stringResource(id = R.string.password_title),
                password,
                { password = it },
                "********",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.onLoginClicked(
                        username,
                        password
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
                        painter = painterResource(id = R.drawable.login_icon),
                        contentDescription = "Login Icon",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(id = R.string.login_button), fontSize = 14.sp, color=Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SignUpText(
                onClick = { viewModel.onRegisterClicked() }
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
    isPassword: Boolean
) {
    var passwordVisible by remember { mutableStateOf(false) }

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
            shape = MaterialTheme.shapes.small.copy(all = CornerSize(8.dp)),
//            colors = colors,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            singleLine = true
        )
    }
}

@Composable
fun SignUpText(onClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("${stringResource(R.string.dont_have_account)} ")

        pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up")
        withStyle(
            style = SpanStyle(
                color = Color(0xFF007BFF),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        ) {
            append(stringResource(R.string.sign_up_option))
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
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}
