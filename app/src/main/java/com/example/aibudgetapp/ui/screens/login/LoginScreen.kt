package com.example.aibudgetapp.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    loginError: Boolean,
    loginErrorMessage: String?,
    onClearError: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    val loginViewModel = remember { LoginViewModel() }
    val forgotPasswordSuccess by remember { derivedStateOf { loginViewModel.forgotPasswordSuccess } }
    val forgotPasswordError by remember { derivedStateOf { loginViewModel.forgotPasswordError } }
    val forgotPasswordSuccessMessage by remember { derivedStateOf { loginViewModel.forgotPasswordSuccessMessage } }
    val forgotPasswordErrorMessage by remember { derivedStateOf { loginViewModel.forgotPasswordErrorMessage } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Budgeting App",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; if (loginError) onClearError()
                            loginViewModel.forgotPasswordSuccess = false
                            loginViewModel.forgotPasswordError = false },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; if (loginError) onClearError()
                            loginViewModel.forgotPasswordSuccess = false
                            loginViewModel.forgotPasswordError = false },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Forgot password?
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    showResetDialog = true
                }
            ) {
                Text("Forgot password?")
            }
        }


        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Login")
        }
        if (loginError) {
            Text(
                text = loginErrorMessage ?: "Login failed",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        if (forgotPasswordSuccess) {
            Text(
                text = forgotPasswordSuccessMessage,
                color = Color.Green,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        if (forgotPasswordError) {
            Text(
                text = forgotPasswordErrorMessage ?: "Failed to send password reset email.",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text("Don't have an account?")
            Spacer(Modifier.width(6.dp))
            TextButton(onClick = onRegister) {
                Text("Register")
            }
        }
    }

    if (showResetDialog) {
        var resetEmailInput by remember { mutableStateOf(email) }
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address to reset your password.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        loginViewModel.sendPasswordResetEmail(resetEmailInput)
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen(
        onLogin = { email, pw ->  },
        onRegister = {  },
        loginError = false,
        loginErrorMessage = "",
    )
}
