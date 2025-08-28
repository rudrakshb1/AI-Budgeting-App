package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri                                   // ✅ NEW
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.aibudgetapp.ui.components.UploadPhotoButton   // ✅ NEW

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onAddTransaction: (Int, String) -> Unit,
    AddTransactionError: Boolean
) {
    val list = listOf("Food & Drink", "Rent", "Gas", "Other")
    var selected by remember { mutableStateOf(list[0]) }
    var amount by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }

    // ✅ NEW: hold the chosen image locally (no DB / VM needed yet)
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

        // ✅ NEW: Upload button (shows Camera or Gallery dialog)
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
            onValueChange = { amount = it.toIntOrNull() ?: 0 },
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
                onAddTransaction(amount, selected)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save")
        }

        if (AddTransactionError) {
            Text(
                text = "Failed to add transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen(
        onAddTransaction = { amount, category ->
            println("Transaction added: Amount = $amount, Category = $category")
        },
        AddTransactionError = false
    )
}
