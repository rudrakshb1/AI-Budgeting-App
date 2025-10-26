package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aibudgetapp.ui.screens.transaction.Transaction

@Composable
fun UploadedReceiptsScreen(transactions: List<Transaction>) {
    val allReceipts = transactions.mapNotNull { it.receiptUrl }.distinct()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Uploaded Receipts", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(allReceipts) { receipt ->
                AsyncImage(
                    model = receipt,
                    contentDescription = "Uploaded Receipt",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}
