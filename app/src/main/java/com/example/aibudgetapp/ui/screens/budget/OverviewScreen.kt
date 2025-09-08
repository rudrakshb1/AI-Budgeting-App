package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
    // Dummy Overview
    val totalBudget = 500
    val totalSpent = 300
    val remaining = totalBudget - totalSpent
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }

    Text("📊 Budget Overview", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))

    Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Budget: $$totalBudget")
            Text("Total Spent: $$totalSpent")
            Text("Remaining: $$remaining")
        }
    }

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
    val list = budgetViewModel.budgets
    val loading = budgetViewModel.isLoading

    if (loading) {
        Text("Loading...")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            items(list) { b ->
                Text("${b.name} - ${b.amount} (${b.chosencategory})")
                Text("Type: ${b.selecteddate} ${b.chosentype}, Include savings: ${b.checked}")
            }
        }
    }
}