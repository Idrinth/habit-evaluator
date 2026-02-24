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

data class ScatterEntry(val hourOfDay: Float, val strength: Float)
data class ScatterPair(val pairLabel: String, val entries: List<ScatterEntry>)

private val PAIR_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63),
    Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFF5722), Color(0xFF795548),
    Color(0xFF607D8B), Color(0xFF8BC34A)
)

@Composable
fun EmotionScatterChart(
    pairs: List<ScatterPair>,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        if (pairs.isEmpty()) return@Canvas

        val textPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }

        // Legend sizing
        val legendCols = 2
        val legendRowH = 24.dp.toPx()
        val legendRows = ceil(pairs.size.toFloat() / legendCols).toInt()
        val legendHeight = legendRows * legendRowH + 16.dp.toPx()

        val leftPad = 50.dp.toPx()
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
                pathEffect = if (isZero) null else null
            )
            val label = "${abs(v.toInt()) * 10}%"
            drawContext.canvas.nativeCanvas.drawText(label, chartLeft - 4.dp.toPx(), y + 4.dp.toPx(), textPaint)
        }

        // Scatter dots
        pairs.forEachIndexed { pairIdx, pair ->
            val color = PAIR_COLORS[pairIdx % PAIR_COLORS.size].copy(alpha = 0.7f)
            pair.entries.forEach { entry ->
                if (entry.hourOfDay in 0f..24f) {
                    val px = chartLeft + (entry.hourOfDay / 24f) * chartWidth
                    val py = yCenter - (entry.strength / 10f) * (chartHeight / 2f)
                    drawCircle(color, radius = 8f, center = Offset(px, py))
                }
            }
        }

        // X-axis labels (00:00, 06:00, 12:00, 18:00, 24:00)
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.textSize = 9.sp.toPx()
        listOf(0, 6, 12, 18, 24).forEach { hour ->
            val x = chartLeft + (hour / 24f) * chartWidth
            val label = String.format("%02d:00", hour % 24)
            val canvas = drawContext.canvas.nativeCanvas
            canvas.save()
            canvas.rotate(-45f, x, chartBottom + 8.dp.toPx())
            canvas.drawText(label, x, chartBottom + 16.dp.toPx(), textPaint)
            canvas.restore()
        }

        // Legend
        val legendTop = chartBottom + 48.dp.toPx()
        val colWidth = chartWidth / legendCols
        textPaint.textAlign = android.graphics.Paint.Align.LEFT
        textPaint.textSize = 10.sp.toPx()
        pairs.forEachIndexed { idx, pair ->
            val col = idx % legendCols
            val row = idx / legendCols
            val x = chartLeft + col * colWidth
            val y = legendTop + row * legendRowH
            val color = PAIR_COLORS[idx % PAIR_COLORS.size]
            val truncated = if (pair.pairLabel.length > 30) pair.pairLabel.take(27) + "..." else pair.pairLabel

            drawRect(color, Offset(x, y), androidx.compose.ui.geometry.Size(16.dp.toPx(), 12.dp.toPx()))
            drawContext.canvas.nativeCanvas.drawText(truncated, x + 20.dp.toPx(), y + 10.dp.toPx(), textPaint)
        }
    }
}
