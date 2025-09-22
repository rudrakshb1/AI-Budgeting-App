package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun BankTransactionCategoryDialog(
    transaction: Transaction,
    onCategorySelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    val categories = listOf("Rent", "Salary", "Utilities", "Savings", "Other")

    AlertDialog(
        onDismissRequest = { },
        title = { Text("Categorize Transaction") },
        text = {
            Column {
                Text("Description: ${transaction.description}")
                Text("Debit: ${transaction.debit ?: ""}  Credit: ${transaction.credit ?: ""}")
                Text("Balance: ${transaction.balance ?: ""}")
                categories.forEach { cat ->
                    Button(
                        onClick = { selectedCategory = cat },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(cat) }
                }
                if (selectedCategory == "Other") {
                    TextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Custom Category") }
                    )
                }
            }
        },
        confirmButton = {
            val finalCategory = if (selectedCategory == "Other") customCategory else selectedCategory
            Button(
                onClick = { onCategorySelected(finalCategory) },
                enabled = finalCategory.isNotBlank()
            ) { Text("Save") }
        }
    )
}
