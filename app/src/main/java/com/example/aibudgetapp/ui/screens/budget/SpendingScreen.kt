package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
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
    selectedBudget: Budget
) {
    // --- Helper for safe date parsing ---
    val safeParse: (String?) -> LocalDate? = { dateStr ->
        try {
            dateStr?.replace("/", "-")?.let { LocalDate.parse(it) }
        } catch (e: Exception) { null }
    }

    // ===== Filtered Transactions by Budget (Option A: selected category only) =====
    val filteredTxns = addTransactionViewModel.transactions.filter { tx ->
        val txDate = safeParse(tx.date)
        val start = safeParse(selectedBudget.startDate)
        val end = safeParse(selectedBudget.endDate)
        txDate != null && start != null && end != null &&
                txDate >= start && txDate <= end &&
                tx.category == selectedBudget.chosenCategory //  match chosen category
    }

    // --- Calculate budget + totals for the SELECTED CATEGORY only ---
    val budgetAmount = selectedBudget.amount.toDouble()
    val totalSpent = filteredTxns
        .filter { it.category == selectedBudget.chosenCategory } // only this category
        .sumOf { it.amount ?: 0.0 }

    val overspent = if (totalSpent > budgetAmount) totalSpent - budgetAmount else 0.0
    val saving = if (totalSpent < budgetAmount) budgetAmount - totalSpent else 0.0

    // --- Pie Chart slices, single declaration ---
    val pieSlices = mutableListOf<Pair<String, Float>>().apply {
        add("Budget" to budgetAmount.toFloat())
        add("Spending" to totalSpent.toFloat())
        if (overspent > 0) {
            add("Overspent" to overspent.toFloat())
        } else {
            add("Remaining" to saving.toFloat())
        }
    }

    // --- Debug print ---
    LaunchedEffect(filteredTxns) {
        println("Filtered Txns: ${filteredTxns.size}")
        println("Total Spent: $totalSpent")
        println("Pie Slices: $pieSlices")
        println("Selected Category: ${selectedBudget.chosenCategory}")
    }

    val colorPalette = listOf(
        android.graphics.Color.parseColor("#5DA5DA"), // Blue = Budget
        android.graphics.Color.parseColor("#FAA43A"), // Orange = Spending
        android.graphics.Color.parseColor("#60BD68"), // Green = Remaining
        android.graphics.Color.parseColor("#F17CB0")  // Pink = Overspent
    )

    // ---- Pie Chart ----
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                val entries = pieSlices.map { (label, value) -> PieEntry(value, label) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = colorPalette
                    valueTextSize = 14f
                }
                this.data = PieData(dataSet).apply {
                    setValueTextColor(android.graphics.Color.DKGRAY)
                    setValueTextSize(14f)
                    setValueFormatter(object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "$${String.format("%.0f", value)}"
                        }
                    })
                }

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
                animateY(900)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    // --- Show Remaining / Overspent text
    Spacer(modifier = Modifier.height(8.dp))
    when {
        totalSpent == 0.0 -> Text("No spending yet", color = MaterialTheme.colorScheme.primary)
        overspent > 0 -> Text("Overspent by $overspent", color = MaterialTheme.colorScheme.error)
        else -> Text("Remaining to save: $saving", color = MaterialTheme.colorScheme.primary)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- Define your desired categories ---
    val fixedCategories = listOf(
        "Food & Drink", "Rent", "Vacation", "Groceries", "Transport", "Bills"
    )

// --- Group transactions globally, sum each category ---
    val transactionByCategory = addTransactionViewModel.transactions.groupBy { it.category }

    val categorySpending = fixedCategories.map { cat ->
        cat to (transactionByCategory[cat]?.sumOf { it.amount ?: 0.0 } ?: 0.0)
    }

// --- Sum everything NOT in your fixed list under "Other" ---
    val otherSpending = addTransactionViewModel.transactions
        .filter { it.category !in fixedCategories }
        .sumOf { it.amount ?: 0.0 }

    val allSpending = categorySpending + listOf("Other" to otherSpending)

// --- This guarantees you always have all bars, even zeroes ---
// If you want to filter out zero bars, use .filter { it.second > 0 } instead

    val displaySpending = allSpending

    Spacer(modifier = Modifier.height(24.dp))

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                val entries = displaySpending.mapIndexed { idx, entry ->
                    BarEntry(idx.toFloat(), entry.second.toFloat())
                }
                val dataSet = BarDataSet(entries, "Categories").apply {
                    colors = listOf(
                        android.graphics.Color.parseColor("#90CAF9"), // Food & Drink
                        android.graphics.Color.parseColor("#A5D6A7"), // Rent
                        android.graphics.Color.parseColor("#FFD700"), // Vacation
                        android.graphics.Color.parseColor("#60BD68"), // Groceries
                        android.graphics.Color.parseColor("#FFAB91"), // Transport
                        android.graphics.Color.parseColor("#CE93D8"), // Bills
                        android.graphics.Color.parseColor("#F17CB0")  // Other
                    )
                    valueTextSize = 14f
                    valueTextColor = android.graphics.Color.DKGRAY
                    setDrawValues(true)
                }
                val data = BarData(dataSet)
                data.barWidth = 0.5f
                this.data = data

                description.isEnabled = false
                setDrawValueAboveBar(true)
                setFitBars(true)
                animateY(900)

                // --- Correct X-axis configuration ---
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(fixedCategories + "Other")
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
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

}
