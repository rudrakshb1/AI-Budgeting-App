package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri                                   //  NEW
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.aibudgetapp.ui.components.UploadPhotoButton   // NEW
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.budget.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen() {
    val addTransactionViewModel = remember { AddTransactionViewModel(TransactionRepository()) }
    val transactionError by remember { derivedStateOf { addTransactionViewModel.transactionError } }
    val list = listOf("Food & Drink", "Rent", "Gas", "Other")
    var selected by remember { mutableStateOf(list[0]) }
    var amount by remember { mutableDoubleStateOf(0.00) }
    var isExpanded by remember { mutableStateOf(false) }

    // hold the chosen image locally (no DB / VM needed yet)
    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Add Transaction",
            style = MaterialTheme.typography.bodyLarge,
        )

        //Upload button (shows Camera or Gallery dialog)
        UploadPhotoButton { uri ->
            receiptUri = uri
        }

        // Optional: let the user know something is attached
        if (receiptUri != null) {
            Text(
                text = "Receipt attached",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { amount = (it.toDoubleOrNull() ?: 0) as Double },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                list.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selected = list[index]
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $selected")

        Button(
            onClick = {
                // teammate’s callback stays the same (amount + category)
                // (Later, when team is ready, extend callback to include receiptUri?.toString())
                addTransactionViewModel.onAddTransaction("", "", amount, selected)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save")
        }

        if (transactionError) {
            Text(
                text = "Failed to add transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Button(
            onClick = {
                addTransactionViewModel.fetchTransactions()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("read data")
        }
        if (transactionError) {
            Text(
                text = "Failed to fetch transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        val list = addTransactionViewModel.transactions
        val loading = addTransactionViewModel.isLoading

        if (loading) {
            Text("Loading...")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                items(list) { tx ->
                    Text("${tx.description} - ${tx.amount} (${tx.category})")
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen()
}
