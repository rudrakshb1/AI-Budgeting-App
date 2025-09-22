package com.example.aibudgetapp.ui.screens.transaction


import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.components.UploadPhotoButton
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onReceiptPicked: (Uri) -> Unit = {}     // new parameter
) {
    val addTransactionViewModel = remember { AddTransactionViewModel(TransactionRepository()) }
    val transactionError by remember { derivedStateOf { addTransactionViewModel.transactionError } }

    val categories = listOf("Food & Drink", "Rent", "Gas", "Other")
    var selected by remember { mutableStateOf(categories[0]) }
    var amount by remember { mutableStateOf(0.0) }
    var isExpanded by remember { mutableStateOf(false) }
    var transactionDate by remember { mutableStateOf(LocalDate.now().toString()) }

    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Add Transaction", style = MaterialTheme.typography.bodyLarge)

        UploadPhotoButton(
            onImagePicked = { uri ->
                // addTransactionViewModel.onReceiptSelected(uri)
                onReceiptPicked(uri)                               // ✅ navigate to ReceiptFlow
                receiptUri = uri
            },
            addTxViewModel = addTransactionViewModel
        )

        if (receiptUri != null) {
            Text(
                text = "Receipt attached",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // everything below stays the same (manual transaction form, list, etc.)
        OutlinedTextField(
            value = transactionDate,
            onValueChange = { transactionDate = it },
            label = { Text("Date (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { amount = it.toDoubleOrNull() ?: 0.0 },
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
                categories.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selected = text
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
                addTransactionViewModel.onAddTransaction(
                    description = "",
                    amount = amount,
                    category = selected,
                    date = transactionDate
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
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
            onClick = { addTransactionViewModel.fetchTransactions() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Read data")
        }

        if (transactionError) {
            Text(
                text = "Failed to fetch transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        val txList = addTransactionViewModel.transactions
        val loading = addTransactionViewModel.isLoading

        if (loading) {
            Text("Loading...")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                items(txList) { tx ->
                    val date = tx.date?.takeIf { it.isNotBlank() }?.plus(" : ") ?: ""
                    Text("${date}${tx.description} - ${tx.amount} (${tx.category})")
                    TextButton(onClick = { addTransactionViewModel.deleteTransaction(tx.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
