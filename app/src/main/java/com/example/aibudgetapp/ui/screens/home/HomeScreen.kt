package com.example.aibudgetapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.screenContainer.Screen
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainerViewModel
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.theme.*

@Composable
fun HomeScreen(
    userName: String,
    screenContainerViewModel: ScreenContainerViewModel,
) {
    val homeViewModel = remember { HomeViewModel(BudgetRepository(), TransactionRepository())}
    homeViewModel.getMonthlyBudget()
    homeViewModel.getMonthlyTransaction()
    val totalBudget = homeViewModel.budgetAmount
    val totalSpent = homeViewModel.totalSpent

    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

        ) { innerPadding ->
            Column {
                Greeting(
                    name = userName,
                    modifier = Modifier.padding(innerPadding)
                )

                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    val remaining = totalBudget - totalSpent
                    Text("📊 Monthly Overview", style = MaterialTheme.typography.titleMedium)
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


                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Button(
            onClick = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) },
        ) {
            Text("+")
        }
    }
}

@Composable
fun Greeting (
    name: String,
    modifier: Modifier = Modifier) {
    Text(
        text = "Welcome, $name!",
        modifier = Modifier.padding(32.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(
        userName=".",
        screenContainerViewModel= remember { ScreenContainerViewModel() },
    )
}



