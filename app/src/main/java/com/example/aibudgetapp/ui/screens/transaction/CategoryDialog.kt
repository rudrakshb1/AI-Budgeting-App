package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import android.net.Uri
import com.example.aibudgetapp.constants.DATE_FORMAT

@Composable
fun CategoryDialog(
    merchant: String,
    total: Double,
    date: Long,
    addTransactionViewModel: AddTransactionViewModel = viewModel(),
    onSaveComplete: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")
    // format date from millis
    val formattedDate = remember(date) {
        val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    }
    AlertDialog(
        onDismissRequest = { /* keep open until user confirms */ },
        title = { Text("Select Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Merchant: $merchant")
                Text("Amount: $total")
                Text("Date: $formattedDate")
                categories.forEach { cat ->
                    Button(
                        onClick = { selectedCategory = cat },
                        colors = if (selectedCategory == cat) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(cat)
                    }
                }
                if (selectedCategory == "Other") {
                    TextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Custom Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val finalCategory = if (selectedCategory == "Other") customCategory else selectedCategory
            Button(
                onClick = {
                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        description = merchant,
                        amount = total,
                        category = finalCategory,
                        date = formattedDate
                    )
                    addTransactionViewModel.onSaveTransaction(
                        category = finalCategory,
                        imageUri = Uri.EMPTY // or the real receipt Uri if available
                    )
                    onSaveComplete(finalCategory)
                },
                enabled = finalCategory.isNotBlank()
            ) {
                Text("Save")
            }
        }
    )
}