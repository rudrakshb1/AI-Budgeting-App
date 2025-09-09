package com.example.aibudgetapp.ui.screens.budget

import android.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingScreen() {
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