package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: AddTransactionViewModel,
    placeholder: @Composable () -> Unit = { Text("Search") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    trailingIcon: @Composable (() -> Unit)? = null,) {

    LaunchedEffect(viewModel) {
        viewModel.fetchTransactions()
    }

    val loading = viewModel.isLoading
    val transactionError by remember { derivedStateOf { viewModel.transactionError } }
    var currentmonth by remember { mutableStateOf(LocalDate.now()) }
    var showFullReceiptUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        viewModel.filterTransaction(query)
    }
    val txList = if (query.isNotBlank()) viewModel.filteredTransaction else viewModel.transactions

    val calTransaction = txList.filter { tx ->
        if (tx.date.isNullOrBlank()) return@filter false
        try {
            val txDate = LocalDate.parse(tx.date)
            txDate.year == currentmonth.year && txDate.monthValue == currentmonth.monthValue
        } catch (e: Exception) {
            false
        }
    }

    Column {
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .semantics{traversalIndex = 0f},
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                    },
                    onSearch = { },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            },
            expanded = false,
            onExpandedChange = { },
        ) { }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentmonth = currentmonth.minusMonths(1) }
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
                text = "No transactions found for ${
                    currentmonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy")
                    )
                }",
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (loading) {
            Text("Loading...")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                items(calTransaction) { tx ->
                    val date = tx.date?.takeIf { it.isNotBlank() }?.plus(" : ") ?: ""
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {

                        if (!tx.receiptUrl.isNullOrBlank()) {
                            val imageModel = remember(tx.receiptUrl) {
                                val file = File(tx.receiptUrl!!)
                                if (file.exists()) ImageRequest.Builder(context)
                                    .data(file)
                                    .build()
                                else ImageRequest.Builder(context)
                                    .data(tx.receiptUrl)
                                    .build()
                            }

                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Receipt",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 12.dp)
                                    .clickable { showFullReceiptUrl = tx.receiptUrl }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("${date}${tx.description} - ${tx.amount} (${tx.category})")
                        }
                        TextButton(onClick = { viewModel.deleteTransaction(tx.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }


        showFullReceiptUrl?.let { url ->
            val fullPath = remember(url) {
                val file = File(url)
                if (file.exists()) ImageRequest.Builder(context).data(file).build()
                else ImageRequest.Builder(context).data(url).build()
            }

            AlertDialog(
                onDismissRequest = { showFullReceiptUrl = null },
                confirmButton = {},
                text = {
                    AsyncImage(
                        model = fullPath,
                        contentDescription = "Full Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            )
        }
    }
}
