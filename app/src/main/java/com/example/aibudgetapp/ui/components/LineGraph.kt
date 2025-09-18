package com.example.aibudgetapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart as MpLineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LineChart(
    values: List<Double>,
    xLabels: List<String>,
    limitY: Int? = null,
    title: String? = null,
    trackLineColor: Int = Color.rgb(59,130,246),
    showLastDot: Boolean = true,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth())
    {
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
            modifier = Modifier.fillMaxWidth().height(250.dp),
            factory = { context ->
                MpLineChart(context).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    setNoDataText("Loading…")
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

                val n = minOf(values.size, if (xLabels.isNotEmpty()) xLabels.size else values.size)
                val trimmedVals = values.take(n)

                val mainEntries = trimmedVals.mapIndexed { i, v ->
                    Entry(i.toFloat(), v.toFloat())
                }

                val mainSet = LineDataSet(mainEntries, "").apply {
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
                if (showLastDot && trimmedVals.isNotEmpty()) {
                    val lastIdx = n - 1
                    val lastVal = trimmedVals[lastIdx].toFloat()
                    val lastSet = LineDataSet(listOf(Entry(lastIdx.toFloat(), lastVal)), "").apply {
                        color = Color.TRANSPARENT
                        lineWidth = 0f
                        setDrawValues(false)
                        setDrawCircles(true)
                        setCircleColor(trackLineColor)
                        circleRadius = 5f
                        setCircleHoleColor(Color.WHITE)
                        circleHoleRadius = 2.5f
                        isHighlightEnabled = false
                        setDrawFilled(false)
                    }
                    data.addDataSet(lastSet)
                }

                chart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels.take(n))

                val maxYData = (trimmedVals.maxOrNull() ?: 0.0).toFloat()
                val limitVal = limitY?.toFloat() ?: 0f
                val top = maxOf(maxYData, limitVal)
                chart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = if (top == 0f) 1f else top * 1.1f
                    removeAllLimitLines()
                    if (limitY != null) {
                        addLimitLine(
                            LimitLine(limitVal, "Budget Limit").apply {
                                lineWidth = 2f
                                lineColor = Color.RED
                                textColor = Color.RED
                                enableDashedLine(10f, 6f, 0f)
                                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                            }
                        )
                    }
                }

                chart.data = data
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

private fun verticalFadeDrawable(baseColor: Int): GradientDrawable {
    val top = Color.argb(60, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)) // 연하게
    val bottom = Color.argb(0, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom))
}
