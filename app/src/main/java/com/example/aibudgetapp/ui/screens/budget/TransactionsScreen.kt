package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.aibudgetapp.ui.screens.transaction.ReceiptLink

@Composable
fun TransactionsScreen(viewModel: AddTransactionViewModel) { // <-- Passed from parent!

    LaunchedEffect(viewModel) {
        viewModel.fetchTransactions()
    }

    val txList = viewModel.transactions // <-- Just reference, it's a Compose state
    val loading = viewModel.isLoading
    val transactionError by remember { derivedStateOf { viewModel.transactionError } }
    var currentmonth by remember { mutableStateOf(LocalDate.now()) }

    val calTransaction = txList.filter { tx ->
        if (tx.date.isNullOrBlank()) return@filter false
        try{
            val txDate = LocalDate.parse(tx.date)
            txDate.year == currentmonth.year && txDate.monthValue == currentmonth.monthValue
        } catch (e: Exception){
            false
        }
    }

    Column {
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(
                onClick = { currentmonth = currentmonth.minusMonths(1)}
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }
            Text(
                text = currentmonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = {
                currentmonth = currentmonth.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Month"
                )
            }
        }

        if (transactionError) {
            Text(
                text = "Failed to fetch transaction",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (!transactionError && txList.isEmpty()) {
            Text(
                text = "No transactions found for ${currentmonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
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
                items(calTransaction) { tx ->
                    val date = tx.date?.takeIf { it.isNotBlank() }?.plus(" : ") ?: ""
                    Text("${date}${tx.description} - ${tx.amount} (${tx.category})")
                    if (tx.receiptUrl != null && tx.receiptUrl!!.isNotBlank()) {
                        ReceiptLink(url = tx.receiptUrl!!)   // opens the image; label shows filename
                    }
                    TextButton(onClick = { viewModel.deleteTransaction(tx.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
