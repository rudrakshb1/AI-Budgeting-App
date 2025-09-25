package com.example.aibudgetapp.ui.screens.budget


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.Legend

/**
 * Generic Pie Chart Composable to show budget vs. spending slices.
 *
 * @param dataPairs List of Pair(label, value) for chart slices, e.g. ("Spent", 7000f), ("Remaining", 3000f).
 * @param modifier Modifier for Compose UI layout.
 */
@Composable
fun BudgetPieChart(
    dataPairs: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.surface.toArgb(),
        MaterialTheme.colorScheme.onPrimary.toArgb(),
        MaterialTheme.colorScheme.onSecondary.toArgb()
    )

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                val entries = dataPairs.map { (label, value) ->
                    PieEntry(value, label)
                }
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
                setUsePercentValues(true)
                setDrawEntryLabels(true)
                setEntryLabelTextSize(14f)
                setExtraOffsets(10f, 10f, 10f, 10f)
                animateY(1200)
                invalidate()
            }
        },
        modifier = modifier
    )
}
