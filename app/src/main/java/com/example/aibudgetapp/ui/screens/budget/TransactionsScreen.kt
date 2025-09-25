package com.example.aibudgetapp.ui.screens.budget



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel

@Composable
fun TransactionsScreen(viewModel: AddTransactionViewModel) { // <-- Passed from parent!

    LaunchedEffect(viewModel) {
        viewModel.fetchTransactions()
    }

    val txList = viewModel.transactions // <-- Just reference, it's a Compose state
    val loading = viewModel.isLoading
    val transactionError by remember { derivedStateOf { viewModel.transactionError } }

    Column {
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (transactionError) {
            Text(
                text = "Failed to fetch transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (!transactionError && txList.isEmpty()) {
            Text(
                text = "No transaction found",
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (loading) {
            Text("Loading...")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                items(txList) { tx ->
                    val date = tx.date?.takeIf { it.isNotBlank() }?.plus(" : ") ?: ""
                    Text("${date}${tx.description} - ${tx.amount} (${tx.category})")
                    TextButton(onClick = { viewModel.deleteTransaction(tx.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
