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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun PointChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val trendColor = Color(0xFFE91E63)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val points = data.map { it.second }
        val average = if (points.isNotEmpty()) points.sum() / points.size else 0f

        val maxPts = max(points.max(), max(average, 0f))
        val minPts = min(points.min(), min(average, 0f))
        val range = max(maxPts - minPts, 1f)

        val topPad = 16.dp.toPx()
        val bottomPad = 48.dp.toPx()
        val sidePad = 8.dp.toPx()
        val chartHeight = size.height - topPad - bottomPad
        val chartWidth = size.width - sidePad * 2

        val barSlotWidth = chartWidth / data.size
        val barWidth = barSlotWidth * 0.7f
        val cornerRad = 4.dp.toPx()
        val zeroY = topPad + (maxPts / range) * chartHeight

        val textPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw bars
        data.forEachIndexed { i, (label, value) ->
            val barX = sidePad + barSlotWidth * i + (barSlotWidth - barWidth) / 2
            val barColor = if (value >= 0) primaryColor else errorColor
            val barH = (kotlin.math.abs(value) / range) * chartHeight

            if (value >= 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX, zeroY - barH),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(cornerRad)
                )
            } else {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX, zeroY),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(cornerRad)
                )
            }

            // Value label
            val valueLabelY = if (value >= 0) zeroY - barH - 4.dp.toPx() else zeroY + barH + 12.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                value.toInt().toString(), barX + barWidth / 2, valueLabelY, textPaint
            )

        }

        // X-axis labels (rotated, stepped)
        val labelStep = max(1, data.size / 10)
        val labelPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        data.forEachIndexed { i, (label, _) ->
            if (i % labelStep == 0 || i == data.size - 1) {
                val x = sidePad + barSlotWidth * i + barSlotWidth / 2
                val canvas = drawContext.canvas.nativeCanvas
                canvas.save()
                canvas.rotate(-45f, x, size.height - bottomPad + 8.dp.toPx())
                canvas.drawText(label, x, size.height - bottomPad + 16.dp.toPx(), labelPaint)
                canvas.restore()
            }
        }

        // Zero line (only if negative values)
        if (minPts < 0) {
            drawLine(
                color = onSurface.copy(alpha = 0.25f),
                start = Offset(sidePad, zeroY),
                end = Offset(size.width - sidePad, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Average line
        if (average != 0f) {
            val avgY = topPad + ((maxPts - average) / range) * chartHeight
            val clampedAvgY = avgY.coerceIn(topPad, topPad + chartHeight)
            drawLine(
                color = errorColor,
                start = Offset(sidePad, clampedAvgY),
                end = Offset(size.width - sidePad, clampedAvgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
            val avgPaint = android.graphics.Paint().apply {
                color = errorColor.hashCode()
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.1f", average),
                size.width - sidePad,
                clampedAvgY - 4.dp.toPx(),
                avgPaint
            )
        }

        // Trend line
        drawTrendLine(this, points, sidePad, barSlotWidth, barWidth, topPad, maxPts, range, chartHeight, trendColor)
    }
}

private fun drawTrendLine(
    scope: DrawScope, points: List<Float>,
    sidePad: Float, barSlotWidth: Float, barWidth: Float,
    topPad: Float, maxPts: Float, range: Float, chartHeight: Float,
    color: Color
) {
    val nonZero = points.mapIndexedNotNull { i, v -> if (v != 0f) i to v else null }
    if (nonZero.size < 2) return

    val firstIdx = nonZero.first().first
    val lastIdx = nonZero.last().first
    if (firstIdx == lastIdx) return

    val n = nonZero.size.toFloat()
    val sumX = nonZero.sumOf { it.first.toDouble() }
    val sumY = nonZero.sumOf { it.second.toDouble() }
    val sumXY = nonZero.sumOf { it.first.toDouble() * it.second.toDouble() }
    val sumX2 = nonZero.sumOf { it.first.toDouble() * it.first.toDouble() }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    fun xForIndex(i: Int) = sidePad + barSlotWidth * i + barWidth / 2 + (barSlotWidth - barWidth) / 2
    fun yForValue(v: Float) = topPad + ((maxPts - v) / range) * chartHeight

    val startVal = (slope * firstIdx + intercept).toFloat()
    val endVal = (slope * lastIdx + intercept).toFloat()

    scope.drawLine(
        color = color,
        start = Offset(xForIndex(firstIdx), yForValue(startVal).coerceIn(topPad, topPad + chartHeight)),
        end = Offset(xForIndex(lastIdx), yForValue(endVal).coerceIn(topPad, topPad + chartHeight)),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
    )
}
