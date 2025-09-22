package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel


enum class BudgetTab { OVERVIEW, SPENDING, TRANSACTIONS }

@Composable
fun BudgetOverviewScreen(onAddBudgetClick: () -> Unit = {}) {
    val repository = TransactionRepository()
    val viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(repository)
    )
    var selectedTab by remember { mutableStateOf(BudgetTab.OVERVIEW) }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == BudgetTab.OVERVIEW,
                    onClick = { selectedTab = BudgetTab.OVERVIEW },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == BudgetTab.SPENDING,
                    onClick = { selectedTab = BudgetTab.SPENDING },
                    text = { Text("Spending") }
                )
                Tab(
                    selected = selectedTab == BudgetTab.TRANSACTIONS,
                    onClick = { selectedTab = BudgetTab.TRANSACTIONS },
                    text = { Text("Transactions") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudgetClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                BudgetTab.OVERVIEW -> {
                    OverviewScreen()
                }

                BudgetTab.SPENDING -> {
                    SpendingScreen()
                }

                BudgetTab.TRANSACTIONS -> {
                    TransactionsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetOverviewScreenPreview() {
    BudgetOverviewScreen()
}
