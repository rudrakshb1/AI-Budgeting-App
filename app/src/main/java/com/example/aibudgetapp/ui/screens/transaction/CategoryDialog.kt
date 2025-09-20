package com.example.aibudgetapp.ui.screens.transaction


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

@Composable
fun CategoryDialog(
    merchant: String,
    total: Double,
    date: Long,
    addTransactionViewModel: AddTransactionViewModel = viewModel(),
    onSaveComplete: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* keep open until user picks */ },
        title = { Text("Select Category") },
        text = {
            Column {
                Text("Merchant: $merchant")
                Text("Amount: $total")

                val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")
                categories.forEach { cat ->
                    TextButton(onClick = { selectedCategory = cat }) {
                        Text(cat)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(), // generate unique ID
                    description = merchant,
                    amount = total,
                    category = selectedCategory,
                    date = date.toString() // String because your model expects String
                )
                addTransactionViewModel.addTransaction(transaction)
                onSaveComplete()
            }) {
                Text("Save")
            }
        }
    )
}
