package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color



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
        OutlinedTextField(
            value = amount.toString(),
            onValueChange = {amount = it.toIntOrNull() ?: 0 },
            label = { Text("Amount")},
            modifier = Modifier.fillMaxWidth(),
        )
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {isExpanded = !isExpanded},
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)}
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = {isExpanded = false}) {
                list.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = {Text(text = text)},
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
            onClick = { onAddTransaction(amount, selected) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ){
            Text("Save")
        }
        if (AddTransactionError){
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