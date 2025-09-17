package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen()
{
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val list = budgetViewModel.budgets
    val loading = budgetViewModel.isLoading

    Button(
    onClick = { budgetViewModel.fetchBudgets() },
    modifier = Modifier
    .fillMaxWidth()
    .padding(top = 16.dp)
    ) {
        Text("read budgets")
    }

    if (budgetError) {
        Text(
            text = "Failed to fetch transaction",
            color = androidx.compose.ui.graphics.Color.Red,
            modifier = Modifier.padding(top = 16.dp),
        )
    }

    if (loading) {
        Text("Loading...")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            items(list) { b ->
                BudgetItemCard(b)
            }
        }
    }
}