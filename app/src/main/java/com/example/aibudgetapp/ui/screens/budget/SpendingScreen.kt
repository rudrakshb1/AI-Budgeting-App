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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel



@Composable
fun SpendingScreen(addTransactionViewModel: AddTransactionViewModel) {
    val spending = addTransactionViewModel.spendingByCategory.collectAsState(initial = emptyMap())
    val maxCategoriesToShow = 6
    val sortedEntries = spending.value.entries.sortedByDescending { it.value }
    val topSpending = sortedEntries.take(maxCategoriesToShow)
    val otherTotal = sortedEntries.drop(maxCategoriesToShow).sumOf { it.value }
    val displaySpending = if (otherTotal > 0)
        topSpending.map { it.key to it.value } + listOf("Other" to otherTotal)
    else
        topSpending.map { it.key to it.value }

    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.surface.toArgb(),
        MaterialTheme.colorScheme.onPrimary.toArgb(),
        MaterialTheme.colorScheme.onSecondary.toArgb(),
        MaterialTheme.colorScheme.onTertiary.toArgb()
    )

    Spacer(modifier = Modifier.height(32.dp))
    Text("Live Spending by Category", style = MaterialTheme.typography.titleMedium)
    if (displaySpending.isEmpty()) {
        Text("No spending records yet.")
    } else {
        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    val entries = displaySpending.map { (cat, amt) -> PieEntry(amt.toFloat(), cat) }
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
}
