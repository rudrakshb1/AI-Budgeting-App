package com.example.aibudgetapp.ui.screens.budget

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

enum class BudgetTab { OVERVIEW, SPENDING, TRANSACTIONS }

@Composable
fun BudgetOverviewScreen(onAddBudgetClick: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(BudgetTab.OVERVIEW) }
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }

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
                    // Dummy Overview
                    val totalBudget = 500
                    val totalSpent = 300
                    val remaining = totalBudget - totalSpent

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

                BudgetTab.SPENDING -> {
                    // Spending → Pie + Bar Chart
                    Text("💰 Spending Breakdown", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Pie Chart ---
                    AndroidView(
                        factory = { context ->
                            PieChart(context).apply {
                                val entries = listOf(
                                    PieEntry(35.7f, "Food & Drink"),
                                    PieEntry(21.4f, "Entertainment"),
                                    PieEntry(14.3f, "Gas"),
                                    PieEntry(28.6f, "Savings")
                                )
                                val dataSet = PieDataSet(entries, "").apply {
                                    colors = listOf(
                                        Color.RED,
                                        Color.BLUE,
                                        Color.YELLOW,
                                        Color.GREEN
                                    )
                                    valueTextSize = 14f
                                }
                                this.data = PieData(dataSet)
                                description.isEnabled = false
                                legend.isEnabled = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Bar Chart ---
                    AndroidView(
                        factory = { context ->
                            BarChart(context).apply {
                                val entries = listOf(
                                    BarEntry(0f, 35.7f),
                                    BarEntry(1f, 21.4f),
                                    BarEntry(2f, 14.3f),
                                    BarEntry(3f, 28.6f)
                                )
                                val dataSet = BarDataSet(entries, "Categories").apply {
                                    colors = listOf(
                                        Color.RED,
                                        Color.BLUE,
                                        Color.YELLOW,
                                        Color.GREEN
                                    )
                                    valueTextSize = 14f
                                }
                                val data = BarData(dataSet)
                                this.data = data

                                description.isEnabled = false
                                xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(
                                        listOf("Food & Drink", "Entertainment", "Gas", "Savings")
                                    )
                                    position = XAxis.XAxisPosition.BOTTOM
                                    setDrawGridLines(false)
                                    granularity = 1f
                                }
                                axisRight.isEnabled = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }

                BudgetTab.TRANSACTIONS -> {
                    // Dummy Transactions
                    Column {
                        Text("🧾 Recent Transactions", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("- Starbucks Coffee: $5.50")
                        Text("- Netflix Subscription: $12.99")
                        Text("- Gas Station: $40.00")
                    }
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
