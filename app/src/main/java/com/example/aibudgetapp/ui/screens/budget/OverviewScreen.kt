package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    placeholder: @Composable () -> Unit = { Text("Search") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    trailingIcon: @Composable (() -> Unit)? = null,
)
{
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val list = budgetViewModel.budgets
    val loading = budgetViewModel.isLoading
    var expanded by rememberSaveable { mutableStateOf(false) }
    val budgets by budgetViewModel.budgetList.observeAsState(emptyList())
    var query by remember { mutableStateOf("") }




    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .semantics{traversalIndex = 0f},
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = {query = it},
                onSearch = {
                    budgetViewModel.fetchbudgetcategory(query)
                    expanded = false
                },
                expanded = expanded,
                onExpandedChange = {expanded = it},
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        },
        expanded = expanded,
        onExpandedChange = {expanded = it},
    ) {
        LazyColumn {
            items(budgets) {budget ->
                BudgetItemCard(budget)
            }}
    }

    Button(
        onClick = { budgetViewModel.fetchBudgets() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text("read budgets")
    }

    if (!loading && !budgetError && list.isEmpty()) {
        Text(
            text = "Failed to fetch budget",
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
                TextButton(onClick = { budgetViewModel.deleteBudget(b.id) }) {
                    Text("Delete")
                }
            }
        }
    }
}