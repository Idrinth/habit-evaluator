package de.idrinth.habitevaluator.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.annotation.SuppressLint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SleepGraphView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> labels = new ArrayList<>();
    private List<Float> values = new ArrayList<>();
    private float averageValue = 0f;
    private int barColor = 0xFF4CAF50;
    private int averageColor = 0xFFA5D6A7;
    private String valueFormat = "%.1f";

    private static final float PADDING_LEFT = 60f;
    private static final float PADDING_RIGHT = 16f;
    private static final float PADDING_TOP = 24f;
    private static final float PADDING_BOTTOM = 48f;

    public SleepGraphView(Context context) {
        super(context);
        init();
    }

    public SleepGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SleepGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);

        averagePaint.setStyle(Paint.Style.STROKE);
        averagePaint.setStrokeWidth(3f);
        averagePaint.setPathEffect(new DashPathEffect(new float[]{10f, 6f}, 0));

        trendPaint.setColor(0xFFE91E63);
        trendPaint.setStyle(Paint.Style.STROKE);
        trendPaint.setStrokeWidth(3f);
        trendPaint.setPathEffect(new DashPathEffect(new float[]{8f, 4f}, 0));

        textPaint.setTextSize(24f);

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(60);
    }

    public void setData(List<String> labels, List<Float> values, float averageValue) {
        this.labels = labels != null ? labels : new ArrayList<>();
        this.values = values != null ? values : new ArrayList<>();
        this.averageValue = averageValue;
        invalidate();
    }

    public void setBarColor(int color) {
        this.barColor = color;
        this.averageColor = lightenColor(color, 0.45f);
        invalidate();
    }

    public void setAverageColor(int color) {
        this.averageColor = color;
        invalidate();
    }

    private static int lightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        r = r + (int) ((255 - r) * factor);
        g = g + (int) ((255 - g) * factor);
        b = b + (int) ((255 - b) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void setValueFormat(String format) {
        this.valueFormat = format;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values.isEmpty()) {
            textPaint.setColor(getTextColor());
            textPaint.setTextSize(28f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float chartLeft = PADDING_LEFT;
        float chartRight = width - PADDING_RIGHT;
        float chartTop = PADDING_TOP;
        float chartBottom = height - PADDING_BOTTOM;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        float maxValue = 0;
        for (float v : values) {
            if (v > maxValue) maxValue = v;
        }
        if (averageValue > maxValue) maxValue = averageValue;
        maxValue = Math.max(maxValue * 1.15f, 1f);

        // Draw axes
        axisPaint.setColor(getTextColor());
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

        // Draw grid lines and Y-axis labels
        int gridLines = 4;
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        gridPaint.setColor(getTextColor());
        gridPaint.setAlpha(40);
        for (int i = 0; i <= gridLines; i++) {
            float y = chartBottom - (chartHeight * i / gridLines);
            float val = maxValue * i / gridLines;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            canvas.drawText(String.format(valueFormat, val), chartLeft - 6, y + 6, textPaint);
        }

        // Draw bars
        int count = values.size();
        float barSpacing = chartWidth / count;
        float barWidth = Math.max(barSpacing * 0.65f, 4f);
        barPaint.setColor(barColor);

        for (int i = 0; i < count; i++) {
            float val = values.get(i);
            float barHeight = (val / maxValue) * chartHeight;
            float centerX = chartLeft + barSpacing * i + barSpacing / 2;
            float left = centerX - barWidth / 2;
            float right = centerX + barWidth / 2;
            float top = chartBottom - barHeight;

            RectF rect = new RectF(left, top, right, chartBottom);
            canvas.drawRoundRect(rect, 4f, 4f, barPaint);
        }

        // Draw average line
        if (averageValue > 0) {
            averagePaint.setColor(averageColor);
            float avgY = chartBottom - (averageValue / maxValue) * chartHeight;
            canvas.drawLine(chartLeft, avgY, chartRight, avgY, averagePaint);

            textPaint.setTextSize(20f);
            textPaint.setColor(averageColor);
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Avg: " + String.format(valueFormat, averageValue),
                    chartRight - 120, avgY - 6, textPaint);
        }

        // Draw trend line (only between first and last days with data)
        if (count >= 2) {
            // Find first and last indices with non-zero values
            int firstDataIndex = -1;
            int lastDataIndex = -1;
            for (int i = 0; i < count; i++) {
                if (values.get(i) > 0) {
                    if (firstDataIndex == -1) {
                        firstDataIndex = i;
                    }
                    lastDataIndex = i;
                }
            }

            // Only draw trend if we have at least 2 data points
            if (firstDataIndex != -1 && lastDataIndex != -1 && firstDataIndex != lastDataIndex) {
                double[] trend = calculateTrendLine();
                double startY = getTrendY(trend, firstDataIndex);
                double endY = getTrendY(trend, lastDataIndex);

                float trendStartY = chartBottom - (float) (startY / maxValue) * chartHeight;
                float trendEndY = chartBottom - (float) (endY / maxValue) * chartHeight;

                // Clamp to chart bounds
                trendStartY = Math.max(chartTop, Math.min(chartBottom, trendStartY));
                trendEndY = Math.max(chartTop, Math.min(chartBottom, trendEndY));

                float startX = chartLeft + barSpacing * firstDataIndex + barSpacing / 2;
                float endX = chartLeft + barSpacing * lastDataIndex + barSpacing / 2;
                canvas.drawLine(startX, trendStartY, endX, trendEndY, trendPaint);
            }
        }

        // Draw X-axis labels
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int labelStep = Math.max(1, count / 10);
        for (int i = 0; i < count; i += labelStep) {
            float centerX = chartLeft + barSpacing * i + barSpacing / 2;
            if (i < labels.size()) {
                canvas.save();
                canvas.rotate(-45, centerX, chartBottom + 12);
                canvas.drawText(labels.get(i), centerX, chartBottom + 28, textPaint);
                canvas.restore();
            }
        }
        // Always draw last label
        if (count > 1) {
            int lastIdx = count - 1;
            float centerX = chartLeft + barSpacing * lastIdx + barSpacing / 2;
            if (lastIdx < labels.size() && lastIdx % labelStep != 0) {
                canvas.save();
                canvas.rotate(-45, centerX, chartBottom + 12);
                canvas.drawText(labels.get(lastIdx), centerX, chartBottom + 28, textPaint);
                canvas.restore();
            }
        }
    }

    @SuppressLint("ResourceType")
    private int getTextColor() {
        int[] attrs = {android.R.attr.textColorPrimary};
        android.content.res.TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return color;
    }

    private double[] calculateTrendLine() {
        int totalValues = values.size();
        if (totalValues < 2) {
            double intercept = totalValues > 0 ? values.get(0) : 0;
            return new double[]{0, intercept};
        }

        // Only include non-zero data points in the regression (0 means no data)
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        int n = 0;

        for (int i = 0; i < totalValues; i++) {
            float value = values.get(i);
            if (value > 0) {
                sumX += i;
                sumY += value;
                sumXY += i * value;
                sumXX += i * i;
                n++;
            }
        }

        if (n < 2) {
            // Not enough data points with values, return flat line at average
            double avg = n > 0 ? sumY / n : 0;
            return new double[]{0, avg};
        }

        double denom = n * sumXX - sumX * sumX;
        if (denom == 0) {
            return new double[]{0, sumY / n};
        }

        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        return new double[]{slope, intercept};
    }

    private double getTrendY(double[] trend, int x) {
        return trend[0] * x + trend[1];
    }
}
