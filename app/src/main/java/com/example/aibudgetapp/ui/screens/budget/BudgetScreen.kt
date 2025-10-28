package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

enum class BudgetTab { OVERVIEW, SPENDING, TRANSACTIONS }

@Composable
fun BudgetOverviewScreen(onAddBudgetClick: () -> Unit = {}) {
    // Transaction ViewModel
    val repository = TransactionRepository()
    val transactionViewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(repository)
    )

    //  Shared Budget ViewModel (using the Factory inside BudgetViewModel.kt)
    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(BudgetRepository())
    )

    // Always fetch budgets when screen opens
    LaunchedEffect(Unit) { budgetViewModel.fetchBudgets() }

    // Tab selection
    var selectedTab by remember { mutableStateOf(BudgetTab.OVERVIEW) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var showEditScreen by remember { mutableStateOf(false) }

    if (showEditScreen && budgetToEdit != null) {
        BudgetScreen(
            onBackClick = {
                showEditScreen = false
                budgetToEdit = null
            },
            budgetToEdit = budgetToEdit
        )
        return
    }

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
            ) { Text("+") }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            //  Tab contents
            when (selectedTab) {
                BudgetTab.OVERVIEW -> {
                    OverviewScreen(
                        onEditBudget = { budget ->
                            budgetToEdit = budget
                            showEditScreen = true
                        }
                    )
                }
                BudgetTab.SPENDING -> {
                    SpendingScreen(
                        addTransactionViewModel = transactionViewModel,
                        budgetViewModel = budgetViewModel
                    )
                }
                BudgetTab.TRANSACTIONS -> {
                    TransactionsScreen(viewModel = transactionViewModel)
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
