package com.example.aibudgetapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart as MpLineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.toArgb

@Composable
fun LineChart(
    values: List<Double>,
    xLabels: List<String>,
    compareValues: List<Int>? = null,
    title: String? = null,
    trackLineColor: Int = Color.rgb(59,130,246), // main
    compareLineColor: Int = Color.RED,          // compare
    label: String = "Spending",
    compareLabel: String = "Budget",
    dashedCompare: Boolean = true,
    showLegend: Boolean = true,
    showLastDot: Boolean = true,
) {

    val cSurface = MaterialTheme.colorScheme.surface.toArgb()
    val cOnSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    val cOnSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val cError = MaterialTheme.colorScheme.error.toArgb()

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            factory = { context ->
                MpLineChart(context).apply {
                    description.isEnabled = false
                    legend.isEnabled = showLegend
                    setNoDataText("Loading…")


                    setBackgroundColor(cSurface)
                    setNoDataTextColor(cOnSurfaceVar)
                    legend.textColor = cOnSurface
                    xAxis.textColor = cOnSurfaceVar
                    axisLeft.textColor = cOnSurfaceVar

                    setDrawGridBackground(false)
                    setTouchEnabled(false)

                    axisRight.isEnabled = false
                    axisLeft.axisMinimum = 0f
                    axisLeft.setDrawLimitLinesBehindData(false)
                    setExtraBottomOffset(16f)
                    setExtraTopOffset(2f)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        setDrawGridLines(false)
                        setAvoidFirstLastClipping(true)
                        yOffset = 3f
                    }
                }
            },
            update = { chart ->
                if (values.isEmpty()) {
                    chart.clear(); chart.invalidate(); return@AndroidView
                }

                val nMain = minOf(values.size, if (xLabels.isNotEmpty()) xLabels.size else values.size)
                val mainVals = values.take(nMain)
                val labels   = xLabels.take(nMain).ifEmpty { (0 until nMain).map { it.toString() } }
                val cmpVals  = compareValues?.take(nMain)?.map { it.toFloat() }

                val mainEntries = mainVals.mapIndexed { i, v -> Entry(i.toFloat(), v.toFloat()) }
                val mainSet = LineDataSet(mainEntries, if (showLegend) label else "").apply {
                    color = trackLineColor
                    setCircleColor(trackLineColor)
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                    setDrawFilled(true)
                    fillDrawable = verticalFadeDrawable(trackLineColor)
                    fillFormatter = IFillFormatter { _, _ -> 0f }
                }
                val data = LineData(mainSet)

                if (showLastDot && mainVals.isNotEmpty()) {
                    val lastIdx = nMain - 1
                    val lastVal = mainVals[lastIdx].toFloat()
                    val lastSet = LineDataSet(listOf(Entry(lastIdx.toFloat(), lastVal)), "").apply {
                        color = android.graphics.Color.TRANSPARENT
                        lineWidth = 0f
                        setDrawValues(false)
                        setDrawCircles(true)
                        setCircleColor(trackLineColor)
                        circleRadius = 5f
                        setCircleHoleColor(android.graphics.Color.WHITE)
                        circleHoleRadius = 2.5f
                        isHighlightEnabled = false
                        setDrawFilled(false)
                    }
                    data.addDataSet(lastSet)
                }

                if (!cmpVals.isNullOrEmpty()) {
                    val cmpEntries = cmpVals.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                    val cmpSet = LineDataSet(cmpEntries, if (showLegend) compareLabel else "").apply {
                        color = compareLineColor
                        mode = LineDataSet.Mode.LINEAR
                        setDrawFilled(false)
                        setDrawValues(false)
                        setDrawCircles(false)
                        if (dashedCompare) {
                            enableDashedLine(28f, 14f, 0f)
                            lineWidth = 2f
                        } else {
                            lineWidth = 2f
                        }
                    }
                    data.addDataSet(cmpSet)
                }

                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                val maxMain = (mainVals.maxOrNull() ?: 0.0).toFloat()
                val maxCmp  = (cmpVals?.maxOrNull() ?: 0f)
                val top = maxOf(maxMain, maxCmp)
                chart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = if (top == 0f) 1f else top * 1.1f
                    removeAllLimitLines()
                }
                chart.data = data
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

private fun verticalFadeDrawable(baseColor: Int): GradientDrawable {
    val top = Color.argb(120, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val bottom = Color.argb(0, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom))
}
