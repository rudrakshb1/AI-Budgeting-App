package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import java.time.LocalDate

@Composable
fun SpendingScreen(
    addTransactionViewModel: AddTransactionViewModel,
    budgetViewModel: BudgetViewModel
) {
    val budgets = budgetViewModel.budgets

    // Load budgets initially (if needed)
    LaunchedEffect(Unit) {
        budgetViewModel.fetchBudgets()
        addTransactionViewModel.fetchTransactions()
    }

    // Show UI depending on budgets state
    when {
        budgetViewModel.isLoading -> {
            Text("Loading budgets...", style = MaterialTheme.typography.bodyMedium)
            return
        }
        budgets.isEmpty() -> {
            Text("Please add budget", style = MaterialTheme.typography.bodyMedium)
            return
        }
    }

    // Keep selected budget state, reset to first when budgets list changes
    var selectedBudget by remember(budgets) { mutableStateOf(budgets.first()) }
    var expanded by remember { mutableStateOf(false) }

    // Safe date parser
    val safeParse: (String?) -> LocalDate? = { s ->
        try { s?.replace("/", "-")?.let(LocalDate::parse) } catch (_: Exception) { null }
    }

    // --- Budget selection dropdown ---
    Box {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("${selectedBudget.name} (${selectedBudget.chosenType})")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            budgets.forEach { b ->
                DropdownMenuItem(
                    text = { Text("${b.name} (${b.chosenType})") },
                    onClick = { selectedBudget = b; expanded = false }
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text(
        "Current:\nName: ${selectedBudget.name}\nType: ${selectedBudget.chosenType}\nCategory: ${selectedBudget.chosenCategory}\nAmount: $${selectedBudget.amount}",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(16.dp))

    // Filter transactions that match the selected budget
    val filteredTxns = addTransactionViewModel.transactions.filter { tx ->
        val txDate = safeParse(tx.date)
        val start = safeParse(selectedBudget.startDate)
        val end = safeParse(selectedBudget.endDate)
        txDate != null && start != null && end != null &&
                txDate >= start && txDate <= end &&
                tx.category == selectedBudget.chosenCategory
    }

    val budgetAmount = selectedBudget.amount.toDouble()
    val totalSpent = filteredTxns.sumOf { it.amount ?: 0.0 }
    val overspent = (totalSpent - budgetAmount).coerceAtLeast(0.0)
    val saving = (budgetAmount - totalSpent).coerceAtLeast(0.0)

    // Prepare PieChart slices
    val pieSlices = buildList {
        add("Budget" to budgetAmount.toFloat())
        if (overspent > 0) {
            add("Overspent" to overspent.toFloat())
        } else {
            add("Remaining" to saving.toFloat())
        }
        if (totalSpent > 0) {
            add("Spending" to totalSpent.toFloat())
        }
    }

    // ---- Pie Chart ----
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(false)
                holeRadius = 65f
                transparentCircleRadius = 69f
                setCenterText("Budget\nSummary")
                setCenterTextSize(18f)
                description.isEnabled = false
                legend.isEnabled = true
                legend.textSize = 14f
                legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
            }
        },
        update = { chart ->
            // Define color mapping by label
            val sliceColors = pieSlices.map { (label, _) ->
                when (label) {
                    "Budget"    -> android.graphics.Color.parseColor("#5DA5DA") // Blue
                    "Spending"  -> android.graphics.Color.parseColor("#FAA43A") // Orange
                    "Remaining" -> android.graphics.Color.parseColor("#60BD68") // Green
                    "Overspent" -> android.graphics.Color.parseColor("#F17CB0") // Pink
                    else        -> android.graphics.Color.GRAY
                }
            }
            val entries = pieSlices.map { (label, value) -> PieEntry(value, label) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = sliceColors               // ✅ assign per slice
                valueTextSize = 14f
            }
            chart.data = PieData(dataSet).apply {
                setValueTextColor(android.graphics.Color.DKGRAY)
                setValueTextSize(14f)
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        "$${String.format("%.0f", value)}"
                })
            }
            chart.setEntryLabelColor(android.graphics.Color.BLACK)
            chart.invalidate()
            chart.animateY(900)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    // --- Show Remaining / Overspent text
    Spacer(modifier = Modifier.height(8.dp))
    when {
        totalSpent == 0.0 -> Text("No spending yet", color = MaterialTheme.colorScheme.primary)
        overspent > 0     -> Text("Overspent by $overspent", color = MaterialTheme.colorScheme.error)
        else              -> Text("Remaining to save: $saving", color = MaterialTheme.colorScheme.primary)
    }

    Spacer(modifier = Modifier.height(24.dp))

    val fixedCategories = listOf(
        "Food & Drink", "Rent", "Vacation", "Groceries", "Transport", "Bills"
    )

    val barChartXAxisLabels = fixedCategories + "Other"

    // --- Group transactions globally, sum each category ---
    val transactionByCategory = addTransactionViewModel.transactions.groupBy { it.category }

    val displaySpending by remember(addTransactionViewModel.transactions, fixedCategories) {
        derivedStateOf {
            val spendingByCategory = fixedCategories.associateWith { cat ->
                transactionByCategory[cat]?.sumOf { it.amount ?: 0.0 } ?: 0.0
            }
            // --- Sum everything NOT in your fixed list under "Other" ---
            val otherCategorySpending = addTransactionViewModel.transactions
                .filter { it.category !in fixedCategories }
                .sumOf { it.amount ?: 0.0 }

            val result = fixedCategories.map { it to (spendingByCategory[it] ?: 0.0) } +
                    listOf("Other" to otherCategorySpending)
            result
        }
    }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawValueAboveBar(true)
                setFitBars(true)
                animateY(900)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    granularity = 1f
                    textSize = 14f
                    labelRotationAngle = -22f
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    textSize = 13f
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val entries = displaySpending.mapIndexed { idx, entry ->
                BarEntry(idx.toFloat(), entry.second.toFloat())
            }
            val dataSet = BarDataSet(entries, "Categories").apply {
                colors = listOf(
                    android.graphics.Color.parseColor("#90CAF9"),
                    android.graphics.Color.parseColor("#A5D6A7"),
                    android.graphics.Color.parseColor("#FFD700"),
                    android.graphics.Color.parseColor("#60BD68"),
                    android.graphics.Color.parseColor("#FFAB91"),
                    android.graphics.Color.parseColor("#CE93D8"),
                    android.graphics.Color.parseColor("#F17CB0")
                )
                valueTextSize = 14f
                valueTextColor = android.graphics.Color.DKGRAY
                setDrawValues(true)
            }
            val data = BarData(dataSet).apply {
                barWidth = 0.5f
            }

            chart.data = data
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(barChartXAxisLabels)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}
