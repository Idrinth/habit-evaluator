package de.idrinth.habitevaluator.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun SleepDistributionChart(
    percentAsleep: DoubleArray,
    modifier: Modifier = Modifier
) {
    val barColor = Color(0xFF5B78F6)
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        if (percentAsleep.all { it == 0.0 }) {
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

        val textPaint = android.graphics.Paint().apply {
            color = onSurface.hashCode()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }

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

        // Axes
        drawLine(onSurface, Offset(chartLeft, chartTop), Offset(chartLeft, chartBottom), 1.dp.toPx())
        drawLine(onSurface, Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1.dp.toPx())

        // Grid + Y labels (0%, 25%, 50%, 75%, 100%)
        textPaint.textAlign = android.graphics.Paint.Align.RIGHT
        for (i in 0..4) {
            val y = chartBottom - (chartHeight * i / 4)
            drawLine(onSurface.copy(alpha = 0.15f), Offset(chartLeft, y), Offset(chartRight, y), 1f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i * 25}%", chartLeft - 4.dp.toPx(), y + 4.dp.toPx(), textPaint
            )
        }

        // Bars (24 hours)
        val barSpacing = chartWidth / 24
        val barWidth = max(barSpacing * 0.65f, 4f)
        val cornerRad = 4.dp.toPx()

        for (h in 0 until 24.coerceAtMost(percentAsleep.size)) {
            val barH = (percentAsleep[h].toFloat() / 100f) * chartHeight
            val barX = chartLeft + barSpacing * h + (barSpacing - barWidth) / 2
            drawRoundRect(
                color = barColor,
                topLeft = Offset(barX, chartBottom - barH),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(cornerRad)
            )
        }

        // X-axis labels (every 3 hours)
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.textSize = 9.sp.toPx()
        for (h in 0 until 24 step 3) {
            val x = chartLeft + barSpacing * h + barSpacing / 2
            val label = String.format("%02d:00", h)
            val canvas = drawContext.canvas.nativeCanvas
            canvas.save()
            canvas.rotate(-45f, x, chartBottom + 8.dp.toPx())
            canvas.drawText(label, x, chartBottom + 16.dp.toPx(), textPaint)
            canvas.restore()
        }
    }
}
