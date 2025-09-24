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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.aibudgetapp.ui.screens.transaction.Period
import com.github.mikephil.charting.formatter.ValueFormatter




@Composable
fun SpendingScreen(
    addTransactionViewModel: AddTransactionViewModel,
    selectedBudget: Budget
) {
    val spending = addTransactionViewModel.spendingByCategory.collectAsState(initial = emptyMap())

    // ===== NEW: Period Toggle =====
  //  val periods = listOf("Monthly", "Weekly")
  //  var selectedPeriod by remember { mutableStateOf(periods[0]) }




   // Row(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
    //    periods.forEach { period ->
   //         Button(
    //            onClick = {
    //                selectedPeriod = period
     //               addTransactionViewModel.setPeriod(
     //                   if (period == "Weekly") Period.WEEK else Period.MONTH
      //              )
     //              // addTransactionViewModel.fetchTransactions() // Fetch transactions after period change
      //          },
       //         colors = ButtonDefaults.buttonColors(
        //            containerColor = if (selectedPeriod == period) MaterialTheme.colorScheme.primary
         //           else MaterialTheme.colorScheme.surface
         //       ),
         //       modifier = Modifier.padding(end = 8.dp)
         //   ) { Text(period) }
       // }
   // }



    val today = java.time.LocalDate.now()
    val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val endOfWeek = startOfWeek.plusDays(6)


    // --- Helper for safe date parsing ---
    val safeParse: (String?) -> LocalDate? = { dateStr ->
        try { dateStr?.replace("/", "-")?.let { LocalDate.parse(it) } }
        catch (e: Exception) { null }
    }

    // ===== Filtered Transactions by Period =====
    val filteredTxns = when (selectedBudget.chosenType) {
        "Monthly" -> addTransactionViewModel.transactions.filter { tx ->
            val txDate = safeParse(tx.date)
            val start = safeParse(selectedBudget.startDate)
            val end = safeParse(selectedBudget.endDate)
            txDate != null && start != null && end != null && (txDate >= start && txDate <= end)
        }
        "Weekly" -> addTransactionViewModel.transactions.filter { tx ->
            val txDate = safeParse(tx.date)
            val start = safeParse(selectedBudget.startDate)
            val end = safeParse(selectedBudget.endDate)
            txDate != null && start != null && end != null && (txDate >= start && txDate <= end)
        }
        else -> addTransactionViewModel.transactions
    }



    // --- Calculate budget + totals ---
    val budgetAmount = if (selectedBudget.chosenType == "Weekly") {
        selectedBudget.amount / 4
    } else {
        selectedBudget.amount
    }


    val totalSpent = filteredTxns.sumOf { it.amount ?: 0.0 }
    val overspent = if (totalSpent > budgetAmount) totalSpent - budgetAmount else 0.0
    val saving = if (totalSpent < budgetAmount) budgetAmount - totalSpent else 0.0

    // --- Pie chart slices ---
    val pieSlices = mutableListOf<Pair<String, Float>>().apply {
        add("Budget" to budgetAmount.toFloat())
        add("Spending" to totalSpent.toFloat())
        if (overspent > 0) {
            add("Overspent" to overspent.toFloat())
        } else {
            add("Remaining" to saving.toFloat())
        }
    }

    val colorPalette = listOf(
        android.graphics.Color.parseColor("#90CAF9"), // light blue (Budget)
        android.graphics.Color.parseColor("#A5D6A7"), // soft green (Spending)
        android.graphics.Color.parseColor("#FFF59D"), // pastel yellow (Remaining)
        android.graphics.Color.parseColor("#FFAB91")  // peach (Overspent)
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
                setUsePercentValues(false) // show actual numbers
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
    if (overspent > 0) {
        Text("Overspent by $overspent", color = MaterialTheme.colorScheme.error)
    } else {
        Text("Remaining to save: $saving", color = MaterialTheme.colorScheme.primary)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ---- Bar Chart: Per-category spending (ALL transactions, not filtered) ----

    val allCategories = listOf("Food", "Drink", "Groceries", "Transport", "Shopping", "Bills", "Other")
    val maxCategoriesToShow = 6

// Use ALL transactions, not filtered by budget/date
    val allCategorySpending = allCategories.map { cat ->
        cat to addTransactionViewModel.transactions.filter { it.category == cat }.sumOf { it.amount ?: 0.0 }
    }
    val sortedAllEntries = allCategorySpending.sortedByDescending { it.second }
    val topSpendingAll = sortedAllEntries.take(maxCategoriesToShow)
    val otherTotalAll = sortedAllEntries.drop(maxCategoriesToShow).sumOf { it.second }
    val displaySpending = if (otherTotalAll > 0)
        topSpendingAll + listOf("Other" to otherTotalAll)
    else
        topSpendingAll

    /* // --- Filtering by budget/date range (use if needed for other cases) ---
    val categorySpending = allCategories.map { cat ->
        cat to filteredTxns.filter { it.category == cat }.sumOf { it.amount ?: 0.0 }
    }
    val sortedEntries = categorySpending.sortedByDescending { it.second }
    val topSpending = sortedEntries.take(maxCategoriesToShow)
    val otherTotal = sortedEntries.drop(maxCategoriesToShow).sumOf { it.second }
    val displaySpending = if (otherTotal > 0)
        topSpending + listOf("Other" to otherTotal)
    else
        topSpending
    */

// ---- Bar Chart AndroidView ----
    Spacer(modifier = Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                val entries = displaySpending.mapIndexed { idx, entry ->
                    BarEntry(idx.toFloat(), entry.second.toFloat())
                }
                val dataSet = BarDataSet(entries, "Categories").apply {
                    colors = colorPalette
                    valueTextSize = 16f
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
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(displaySpending.map { it.first })
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
