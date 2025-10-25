package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add user") },
        text = {
            // TODO: AddUserInputFields
        },
        confirmButton = {
            // TODO: ConfirmButton
        },
        dismissButton = {
            // TODO: CancelButton
        }
    )
}
