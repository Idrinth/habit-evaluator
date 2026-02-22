package de.idrinth.habitevaluator.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

private val PAIR_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63),
    Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFF5722), Color(0xFF795548),
    Color(0xFF607D8B), Color(0xFF8BC34A)
)

@Composable
fun EmotionLineChart(
    labels: List<String>,
    pairNames: List<String>,
    pairDailyValues: List<List<Float?>>,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        if (labels.isEmpty() || pairDailyValues.isEmpty()) return@Canvas

        val textPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }

        // Legend sizing
        val legendCols = 3
        val legendRowH = 24.dp.toPx()
        val legendRows = ceil(pairNames.size.toFloat() / legendCols).toInt()
        val legendHeight = legendRows * legendRowH + 16.dp.toPx()

        val leftPad = 60.dp.toPx()
        val rightPad = 16.dp.toPx()
        val topPad = 24.dp.toPx()
        val bottomPad = 48.dp.toPx() + legendHeight

        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        val yCenter = chartTop + chartHeight / 2

        // Axes
        drawLine(onSurface, Offset(chartLeft, chartTop), Offset(chartLeft, chartBottom), 1.dp.toPx())
        drawLine(onSurface, Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1.dp.toPx())

        // Grid + Y labels (-10, -5, 0, +5, +10)
        val gridValues = listOf(-10f, -5f, 0f, 5f, 10f)
        textPaint.textAlign = android.graphics.Paint.Align.RIGHT
        gridValues.forEach { v ->
            val y = yCenter - (v / 10f) * (chartHeight / 2f)
            val isZero = v == 0f
            drawLine(
                color = if (isZero) onSurface.copy(alpha = 0.5f) else onSurface.copy(alpha = 0.15f),
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = if (isZero) 1.5f else 1f,
                pathEffect = if (isZero) PathEffect.dashPathEffect(floatArrayOf(10f, 6f)) else null
            )
            val label = "${abs(v.toInt()) * 10}%"
            drawContext.canvas.nativeCanvas.drawText(label, chartLeft - 4.dp.toPx(), y + 4.dp.toPx(), textPaint)
        }

        // Lines and dots per pair
        val pointCount = labels.size
        val pointSpacing = if (pointCount > 1) chartWidth / (pointCount - 1) else chartWidth / 2

        pairDailyValues.forEachIndexed { pairIdx, dailyValues ->
            val pairColor = PAIR_COLORS[pairIdx % PAIR_COLORS.size]
            val linePaint = pairColor
            var prevOffset: Offset? = null

            dailyValues.forEachIndexed { dayIdx, value ->
                if (value != null) {
                    val px = chartLeft + pointSpacing * dayIdx
                    val py = yCenter - (value / 10f) * (chartHeight / 2f)
                    val current = Offset(px, py)

                    // Draw line from previous
                    if (prevOffset != null) {
                        drawLine(linePaint, prevOffset, current, strokeWidth = 2.dp.toPx())
                    }

                    // Draw dot
                    drawCircle(pairColor, radius = 5f, center = current)

                    prevOffset = current
                } else {
                    prevOffset = null
                }
            }
        }

        // X-axis labels
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.textSize = 9.sp.toPx()
        val labelStep = max(1, pointCount / 10)
        labels.forEachIndexed { i, label ->
            if (i % labelStep == 0 || i == labels.size - 1) {
                val x = chartLeft + pointSpacing * i
                val canvas = drawContext.canvas.nativeCanvas
                canvas.save()
                canvas.rotate(-45f, x, chartBottom + 8.dp.toPx())
                canvas.drawText(label, x, chartBottom + 16.dp.toPx(), textPaint)
                canvas.restore()
            }
        }

        // Legend
        val legendTop = chartBottom + 48.dp.toPx()
        val colWidth = chartWidth / legendCols
        textPaint.textAlign = android.graphics.Paint.Align.LEFT
        textPaint.textSize = 10.sp.toPx()
        pairNames.forEachIndexed { idx, name ->
            val col = idx % legendCols
            val row = idx / legendCols
            val x = chartLeft + col * colWidth
            val y = legendTop + row * legendRowH
            val color = PAIR_COLORS[idx % PAIR_COLORS.size]
            val truncated = if (name.length > 20) name.take(17) + "..." else name

            drawRect(color, Offset(x, y), androidx.compose.ui.geometry.Size(16.dp.toPx(), 12.dp.toPx()))
            drawContext.canvas.nativeCanvas.drawText(truncated, x + 20.dp.toPx(), y + 10.dp.toPx(), textPaint)
        }
    }
}
