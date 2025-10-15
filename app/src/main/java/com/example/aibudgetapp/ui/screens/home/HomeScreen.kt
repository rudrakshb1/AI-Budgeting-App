package com.example.aibudgetapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.components.LineChart
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.screenContainer.Screen
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainerViewModel
import com.example.aibudgetapp.ui.screens.settings.SettingsUiState
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.theme.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext


@Composable
fun HomeScreen(
    uiState: SettingsUiState,
    screenContainerViewModel: ScreenContainerViewModel,
) {
    val homeViewModel = remember { HomeViewModel(BudgetRepository(), TransactionRepository()) }

        LaunchedEffect(Unit) {
        homeViewModel.getMonthlyBudgetList()
        homeViewModel.getWeeklyBudgetList()
        homeViewModel.get12MonthlyTransaction()
        homeViewModel.get12WeeklyTransaction()
    }

    val monthlyBudget = homeViewModel.monthlyBudgetList.lastOrNull() ?: 0
    val monthlyBudgetList = homeViewModel.monthlyBudgetList
    val monthlySpent = homeViewModel.monthlyListTransaction.lastOrNull() ?: 0.0
    val monthly12Spent = homeViewModel.monthlyListTransaction
    val monthLabels = homeViewModel.monthLabels

    val weeklyBudget = homeViewModel.weeklyBudgetList.lastOrNull() ?: 0
    val weeklyBudgetList = homeViewModel.weeklyBudgetList
    val weeklySpent = homeViewModel.weeklyListTransaction.lastOrNull() ?: 0.0
    val weekly12Spent = homeViewModel.weeklyListTransaction
    val weekLabels = homeViewModel.weekLabels
    val context = LocalContext.current

    // NEW: fire threshold checks when values change
    LaunchedEffect(monthlyBudget, monthlySpent, weeklyBudget, weeklySpent) {
        com.example.aibudgetapp.notifications.ThresholdNotifier.maybeNotifyBudget(
            context, "Monthly", monthlySpent, monthlyBudget.toDouble()
        )
        com.example.aibudgetapp.notifications.ThresholdNotifier.maybeNotifyBudget(
            context, "Weekly", weeklySpent, weeklyBudget.toDouble()
        )
    }


    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .offset(y = (-32).dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 0.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
            ) {
                item {
                    Spacer(Modifier.height(48.dp))
                }
                item {
                    Greeting(
                        name = uiState.displayName,
                        modifier = Modifier.fillMaxWidth(),
                        badgeCount = 0,
                        onBellClick = {
                         //   com.example.aibudgetapp.notifications.SimpleNotifier.showTest(context)
                        }
                    )
                }


                item {
                    Text("📊 Monthly Overview",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.titleMedium)
                    val monthlyRemaining = monthlyBudget - monthlySpent
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Total Budget: $$monthlyBudget")
                            Text("Total Spent: $$monthlySpent")
                            Text("Remaining: $$monthlyRemaining")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    LineChart(
                        values = monthly12Spent,
                        compareValues = monthlyBudgetList,
                        xLabels = monthLabels,
                        title = "Monthly Spendings",
                    )
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    Text("📊 Weekly Overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    val weeklyRemaining = weeklyBudget - weeklySpent
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Total Budget: $$weeklyBudget")
                            Text("Total Spent: $$weeklySpent")
                            Text("Remaining: $$weeklyRemaining")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    LineChart(
                        values = weekly12Spent,
                        compareValues = weeklyBudgetList,
                        xLabels = weekLabels,
                        title = "Weekly Spendings"
                    )
                    Spacer(Modifier.height(48.dp))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) }
                )
                { Text("+") }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    // NEW: props for bell UI
    badgeCount: Int = 0,
    onBellClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Welcome, $name!",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleLarge
        )
        //  NEW: bell with optional badge (kept 0 for now)
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge { Text(badgeCount.coerceAtMost(9).toString()) }
                }
            }
        ) {
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications"

                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun HomePreview() {
//    HomeScreen(
//        userName = ".",
//        screenContainerViewModel = remember { ScreenContainerViewModel() },
//    )
//}
