package de.idrinth.habitevaluator.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SleepGraphView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> labels = new ArrayList<>();
    private List<Float> values = new ArrayList<>();
    private float averageValue = 0f;
    private int barColor = 0xFF4CAF50;
    private int averageColor = 0xFF1B5E20;
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
        invalidate();
    }

    public void setAverageColor(int color) {
        this.averageColor = color;
        invalidate();
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

    private int getTextColor() {
        int[] attrs = {android.R.attr.textColorPrimary};
        android.content.res.TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return color;
    }
}
