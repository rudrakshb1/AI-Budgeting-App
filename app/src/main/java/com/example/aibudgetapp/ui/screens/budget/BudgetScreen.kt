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
import androidx.compose.runtime.livedata.observeAsState
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

    // Observe budget list
    val budgets by budgetViewModel.budgetList.observeAsState(emptyList())

    // Currently selected budget
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    LaunchedEffect(budgets) {
        if (selectedBudget == null && budgets.isNotEmpty()) {
            selectedBudget = budgets.first()
        }
    }

    // Tab selection
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
            ) { Text("+") }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            //  Budget selector only on Spending tab
            if (selectedTab == BudgetTab.SPENDING && budgets.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            selectedBudget?.let { "${it.name} (${it.chosenType})" }
                                ?: "Select Budget"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        budgets.forEach { budget ->
                            DropdownMenuItem(
                                text = { Text("${budget.name} (${budget.chosenType})") },
                                onClick = {
                                    selectedBudget = budget
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                selectedBudget?.let {
                    Text(
                        "Current:\nName: ${it.name}\nType: ${it.chosenType}\nCategory: ${it.chosenCategory}\nAmount: $${it.amount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } ?: Text("No budget selected")
                Spacer(modifier = Modifier.height(16.dp))
            }

            //  Tab contents
            when (selectedTab) {
                BudgetTab.OVERVIEW -> {
                    //  if you want Overview to use the same ViewModel, pass budgetViewModel here
                    OverviewScreen()
                }
                BudgetTab.SPENDING -> {
                    selectedBudget?.let {
                        SpendingScreen(
                            addTransactionViewModel = transactionViewModel,
                            selectedBudget = it
                        )
                    } ?: Text("Please select a budget to view spending")
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
