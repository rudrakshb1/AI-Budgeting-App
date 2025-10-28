package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    placeholder: @Composable () -> Unit = { Text("Search") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    trailingIcon: @Composable (() -> Unit)? = null,
    onEditBudget: (Budget) -> Unit = {}
)
{
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val filteredBudget = budgetViewModel.filteredBudget
    val loading = budgetViewModel.isLoading
    var query by remember { mutableStateOf("") }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }


    LaunchedEffect(budgetViewModel) {
        budgetViewModel.fetchBudgets()
    }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .semantics{traversalIndex = 0f},
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = {
                    query = it
                    budgetViewModel.filterBudgetByCategory(query)
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

    if (budgetError) {
        Text(
            text = "Failed to fetch budget",
            color = androidx.compose.ui.graphics.Color.Red,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
    if (!loading && !budgetError && filteredBudget.isEmpty()) {
        Text(
            text = "No budget found",
            color = androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier.padding(top = 16.dp),
        )
    }

    if (loading) {
        Text("Loading...")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            items(filteredBudget) { b ->
                BudgetItemCard(b,
                    onEdit = { budget ->
                        onEditBudget(budget)
                    },
                    onDelete = { id ->
                        budgetViewModel.deleteBudget(id)
                    }
                )
            }
        }
    }
}