package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri                                   //  NEW
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.ui.components.UploadPhotoButton   // NEW
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onAddTransaction: (Double, String) -> Unit,
    addTransactionError: Boolean   // camelCase
) {
    val categories = listOf("Food & Drink", "Rent", "Gas", "Other")
    var selected by remember { mutableStateOf(categories[0]) }
    var amount by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    // hold the chosen image locally (no DB / VM needed yet)
    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = "Add Transaction",
            style = MaterialTheme.typography.titleLarge,
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
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                categories.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = { selected = text; isExpanded = false },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text("Currently selected: $selected")

        Button(
            onClick = {
                // teammate’s callback stays the same (amount + category)
                // (Later, when team is ready, extend callback to include receiptUri?.toString())
                val amt = amount.toDoubleOrNull() ?: 0.0
                onAddTransaction(amt, selected)
                amount = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) { Text("Save") }

        if (addTransactionError) {
            Text(
                text = "Failed to add transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    vm: AddTransactionViewModel = viewModel()
) {
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.startListeningTransactions() }

    val txs by vm.transactions.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    Column(modifier = Modifier.fillMaxSize()) {

        // for debug
        Surface(color = Color(0x1100AAFF)) {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("DEBUG", style = MaterialTheme.typography.labelLarge)
                Text("uid=${uid ?: "null"}")
                Text("tx_count=${txs.size}")
            }
        }

        AddTransactionScreen(
            onAddTransaction = { amount, category ->
                vm.saveTransaction(amount, category, vm.receiptUri) { ok ->
                    error = !ok
                }
            },
            addTransactionError = error
        )

        HorizontalDivider()
        Text(
            text = "Recent transactions (${txs.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(txs, key = { it.id }) { tx ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            vm.deleteTransaction(tx.id)
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val bg =
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.errorContainer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(bg)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    },
                    content = {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.category, fontWeight = FontWeight.SemiBold)
                                    tx.createdAt?.let {
                                        Text("at ${it.toDate()}", color = Color.Gray)
                                    }
                                }
                                Text("${tx.amount}")
                            }
                        }
                    }
                )
            }
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
        addTransactionError = false
    )
}
