package de.idrinth.habitevaluator.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun SleepGraph(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4CAF50),
    formatLabel: String = "%.1f"
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val avgColor = lightenColor(barColor, 0.45f)
    val trendColor = Color(0xFFE91E63)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) {
            val paint = android.graphics.Paint().apply {
                color = onSurface.hashCode()
                textSize = 14.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "No data available", size.width / 2, size.height / 2, paint
            )
            return@Canvas
        }

        val values = data.map { it.second }
        val labels = data.map { it.first }
        val average = if (values.any { it > 0 }) values.filter { it > 0 }.average().toFloat() else 0f
        val maxValue = max(max(values.max(), average) * 1.15f, 1f)

        val leftPad = 60.dp.toPx()
        val rightPad = 16.dp.toPx()
        val topPad = 24.dp.toPx()
        val bottomPad = 48.dp.toPx()
        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val textPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }

        // Axes
        drawLine(onSurface, Offset(chartLeft, chartTop), Offset(chartLeft, chartBottom), 1.dp.toPx())
        drawLine(onSurface, Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1.dp.toPx())

        // Grid + Y labels
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = chartBottom - (chartHeight * i / gridLines)
            drawLine(onSurface.copy(alpha = 0.15f), Offset(chartLeft, y), Offset(chartRight, y), 1f)
            val labelVal = maxValue * i / gridLines
            textPaint.textAlign = android.graphics.Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                String.format(formatLabel, labelVal), chartLeft - 4.dp.toPx(), y + 4.dp.toPx(), textPaint
            )
        }

        // Bars
        val barSpacing = chartWidth / data.size
        val barWidth = max(barSpacing * 0.65f, 4f)
        val cornerRad = 4.dp.toPx()

        data.forEachIndexed { i, (_, value) ->
            val barH = (value / maxValue) * chartHeight
            val barX = chartLeft + barSpacing * i + (barSpacing - barWidth) / 2
            drawRoundRect(
                color = barColor,
                topLeft = Offset(barX, chartBottom - barH),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(cornerRad)
            )
        }

        // Average line
        if (average > 0) {
            val avgY = chartBottom - (average / maxValue) * chartHeight
            drawLine(
                color = avgColor,
                start = Offset(chartLeft, avgY),
                end = Offset(chartRight, avgY),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
            val avgPaint = android.graphics.Paint().apply {
                color = avgColor.hashCode()
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "Avg: ${String.format(formatLabel, average)}", chartRight - 8.dp.toPx(), avgY - 4.dp.toPx(), avgPaint
            )
        }

        // Trend line
        val nonZero = values.mapIndexedNotNull { i, v -> if (v > 0) i to v else null }
        if (nonZero.size >= 2) {
            val first = nonZero.first().first
            val last = nonZero.last().first
            if (first != last) {
                val n = nonZero.size.toFloat()
                val sumX = nonZero.sumOf { it.first.toDouble() }
                val sumY = nonZero.sumOf { it.second.toDouble() }
                val sumXY = nonZero.sumOf { it.first.toDouble() * it.second.toDouble() }
                val sumX2 = nonZero.sumOf { it.first.toDouble() * it.first.toDouble() }
                val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
                val intercept = (sumY - slope * sumX) / n

                fun xFor(i: Int) = chartLeft + barSpacing * i + barSpacing / 2
                fun yFor(v: Float) = (chartBottom - (v / maxValue) * chartHeight).coerceIn(chartTop, chartBottom)

                drawLine(
                    color = trendColor,
                    start = Offset(xFor(first), yFor((slope * first + intercept).toFloat())),
                    end = Offset(xFor(last), yFor((slope * last + intercept).toFloat())),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )
            }
        }

        // X-axis labels (rotated)
        val labelStep = max(1, data.size / 10)
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.textSize = 9.sp.toPx()
        data.forEachIndexed { i, (label, _) ->
            if (i % labelStep == 0 || i == data.size - 1) {
                val x = chartLeft + barSpacing * i + barSpacing / 2
                val canvas = drawContext.canvas.nativeCanvas
                canvas.save()
                canvas.rotate(-45f, x, chartBottom + 8.dp.toPx())
                canvas.drawText(label, x, chartBottom + 16.dp.toPx(), textPaint)
                canvas.restore()
            }
        }
    }
}

private fun lightenColor(color: Color, factor: Float): Color {
    return Color(
        red = color.red + (1f - color.red) * factor,
        green = color.green + (1f - color.green) * factor,
        blue = color.blue + (1f - color.blue) * factor,
        alpha = color.alpha
    )
}
