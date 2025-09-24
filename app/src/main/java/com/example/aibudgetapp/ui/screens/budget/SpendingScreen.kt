package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import java.time.LocalDate

@Composable
fun SpendingScreen(
    addTransactionViewModel: AddTransactionViewModel,
    selectedBudget: Budget
) {
    val spending = addTransactionViewModel.spendingByCategory.collectAsState(initial = emptyMap())

    // --- Helper for safe date parsing ---
    val safeParse: (String?) -> LocalDate? = { dateStr ->
        try { dateStr?.replace("/", "-")?.let { LocalDate.parse(it) } }
        catch (e: Exception) { null }
    }

    // --- Filter transactions by budget date range ---
    val filteredTxns = addTransactionViewModel.transactions.filter { tx ->
        val txDate = safeParse(tx.date)
        val start = safeParse(selectedBudget.startDate)
        val end = safeParse(selectedBudget.endDate)
        txDate != null && start != null && end != null && (txDate >= start && txDate <= end)
    }

    // --- Calculate totals ---
    val totalSpent = filteredTxns.sumOf { it.amount ?: 0.0 }
    val budgetAmount = selectedBudget.amount
    val overspent = if (totalSpent > budgetAmount) totalSpent - budgetAmount else 0.0
    val saving = if (totalSpent < budgetAmount) budgetAmount - totalSpent else 0.0

    // --- Pie chart slices ---
    val pieSlices = mutableListOf<Pair<String, Float>>().apply {
        add("Budget" to budgetAmount.toFloat())
        add("Spending" to totalSpent.toFloat())
        if (overspent > 0) {
            add("Overspent" to overspent.toFloat())
        } else {
            add("Saving" to saving.toFloat())
        }
    }

    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.error.toArgb() // red for overspent
    )

    Spacer(modifier = Modifier.height(32.dp))
    Text("Budget vs Spending", style = MaterialTheme.typography.titleMedium)

    // ---- Pie Chart ----
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                val entries = pieSlices.map { (label, value) -> PieEntry(value, label) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = colorPalette
                    valueTextSize = 16f
                    sliceSpace = 4f
                }
                this.data = PieData(dataSet)
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 14f
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    xEntrySpace = 20f
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    // Show remaining/overspent text
    Spacer(modifier = Modifier.height(8.dp))
    if (overspent > 0) {
        Text(
            text = "Overspent by $overspent",
            color = MaterialTheme.colorScheme.error
        )
    } else {
        Text(
            text = "Remaining to save: $saving",
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    


    // ---- Bar Chart: Per-category spending ----
    val maxCategoriesToShow = 6
    val sortedEntries = spending.value.entries.sortedByDescending { it.value }
    val topSpending = sortedEntries.take(maxCategoriesToShow)
    val otherTotal = sortedEntries.drop(maxCategoriesToShow).sumOf { it.value }
    val displaySpending = if (otherTotal > 0)
        topSpending.map { it.key to it.value } + listOf("Other" to otherTotal)
    else
        topSpending.map { it.key to it.value }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                val entries = displaySpending.mapIndexed { idx, entry ->
                    BarEntry(idx.toFloat(), entry.second.toFloat())
                }
                val dataSet = BarDataSet(entries, "Categories").apply {
                    colors = colorPalette
                    valueTextSize = 16f
                }
                val data = BarData(dataSet)
                this.data = data
                description.isEnabled = false
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(displaySpending.map { it.first })
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 12f
                    labelRotationAngle = -45f
                }
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}
