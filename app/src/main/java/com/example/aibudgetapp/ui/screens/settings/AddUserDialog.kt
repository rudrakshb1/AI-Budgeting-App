package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onCreate: (email: String, password: String, firstName: String, lastName: String?) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val emailOk = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val pwMatch = password == confirm
    val canSubmit = emailOk && firstName.isNotBlank() && password.length >= 6 && pwMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add user") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                // Email
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // First name (required)
                OutlinedTextField(
                    value = firstName, onValueChange = { firstName = it },
                    label = { Text("First name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Last name (optional)
                OutlinedTextField(
                    value = lastName, onValueChange = { lastName = it },
                    label = { Text("Last name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Password
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Confirm password
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it },
                    label = { Text("Confirm password") },
                    isError = confirm.isNotEmpty() && !pwMatch,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                // Inline validation messages
                if (confirm.isNotEmpty() && !pwMatch) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (email.isNotBlank() && !emailOk) {
                    Text("Invalid email format", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            // Only enabled when validation passes
            Button(enabled = canSubmit, onClick = {
                onCreate(email, password, firstName, lastName.ifBlank { null })
            }) { Text("Create account") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
