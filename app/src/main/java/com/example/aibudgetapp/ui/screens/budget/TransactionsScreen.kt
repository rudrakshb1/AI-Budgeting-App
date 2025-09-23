package com.example.aibudgetapp.ui.screens.budget



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel

@Composable
fun TransactionsScreen(viewModel: AddTransactionViewModel) { // <-- Passed from parent!
    val transactions = viewModel.transactions // <-- Just reference, it's a Compose state

    Column {
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { transaction ->
                Text("- ${transaction.description}: \$${transaction.amount}")
            }
        }
    }
}
