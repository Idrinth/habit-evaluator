package de.idrinth.habitevaluator.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.annotation.SuppressLint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmotionLineChartView extends View {

    private static final int[] PAIR_COLORS = {
            0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0,
            0xFF00BCD4, 0xFFFF5722, 0xFF795548, 0xFF607D8B, 0xFF8BC34A
    };

    private static final float PADDING_LEFT = 60f;
    private static final float PADDING_RIGHT = 16f;
    private static final float PADDING_TOP = 24f;
    private static final float PADDING_BOTTOM = 48f;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint zeroLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> labels = new ArrayList<>();
    private List<String> pairNames = new ArrayList<>();
    private List<List<Float>> pairDailyValues = new ArrayList<>();

    public EmotionLineChartView(Context context) {
        super(context);
        init();
    }

    public EmotionLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmotionLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);

        dotPaint.setStyle(Paint.Style.FILL);

        textPaint.setTextSize(24f);

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(60);

        zeroLinePaint.setStyle(Paint.Style.STROKE);
        zeroLinePaint.setStrokeWidth(1.5f);
        zeroLinePaint.setPathEffect(new DashPathEffect(new float[]{10f, 6f}, 0));

        legendBoxPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<String> labels, List<String> pairNames, List<List<Float>> pairDailyValues) {
        this.labels = labels != null ? labels : new ArrayList<>();
        this.pairNames = pairNames != null ? pairNames : new ArrayList<>();
        this.pairDailyValues = pairDailyValues != null ? pairDailyValues : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pairDailyValues.isEmpty()) {
            textPaint.setColor(getTextColor());
            textPaint.setTextSize(28f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float legendHeight = getLegendHeight();
        float chartLeft = PADDING_LEFT;
        float chartRight = width - PADDING_RIGHT;
        float chartTop = PADDING_TOP;
        float chartBottom = height - PADDING_BOTTOM - legendHeight;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;
        float yCenter = chartTop + chartHeight / 2f;

        // Draw axes
        axisPaint.setColor(getTextColor());
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

        // Draw grid lines at -10, -5, 0, 5, 10
        int[] gridValues = {-10, -5, 0, 5, 10};
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int val : gridValues) {
            float gridY = yCenter - (val / 10f) * (chartHeight / 2f);
            if (val == 0) {
                zeroLinePaint.setColor(getTextColor());
                zeroLinePaint.setAlpha(120);
                canvas.drawLine(chartLeft, gridY, chartRight, gridY, zeroLinePaint);
            } else {
                gridPaint.setColor(getTextColor());
                gridPaint.setAlpha(40);
                canvas.drawLine(chartLeft, gridY, chartRight, gridY, gridPaint);
            }
            canvas.drawText(String.valueOf(val), chartLeft - 6, gridY + 6, textPaint);
        }

        int count = labels.size();
        if (count == 0) {
            return;
        }
        float pointSpacing = count > 1 ? chartWidth / (count - 1) : chartWidth;

        // Draw lines and dots for each emotion pair
        for (int p = 0; p < pairDailyValues.size(); p++) {
            int lineColor = PAIR_COLORS[p % PAIR_COLORS.length];
            List<Float> dailyValues = pairDailyValues.get(p);

            linePaint.setColor(lineColor);
            dotPaint.setColor(lineColor);

            // Draw connecting lines
            Float prevVal = null;
            float prevX = 0;
            for (int i = 0; i < count && i < dailyValues.size(); i++) {
                Float val = dailyValues.get(i);
                if (val == null) {
                    prevVal = null;
                    continue;
                }
                float px = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
                float py = yCenter - (val / 10f) * (chartHeight / 2f);
                if (prevVal != null) {
                    float prevY = yCenter - (prevVal / 10f) * (chartHeight / 2f);
                    canvas.drawLine(prevX, prevY, px, py, linePaint);
                }
                prevVal = val;
                prevX = px;
            }

            // Draw dots
            for (int i = 0; i < count && i < dailyValues.size(); i++) {
                Float val = dailyValues.get(i);
                if (val == null) {
                    continue;
                }
                float px = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
                float py = yCenter - (val / 10f) * (chartHeight / 2f);
                canvas.drawCircle(px, py, 5f, dotPaint);
            }
        }

        // X-axis labels
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int labelStep = Math.max(1, count / 10);
        for (int i = 0; i < count; i += labelStep) {
            float centerX = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
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
            float centerX = chartLeft + pointSpacing * lastIdx;
            if (lastIdx < labels.size() && lastIdx % labelStep != 0) {
                canvas.save();
                canvas.rotate(-45, centerX, chartBottom + 12);
                canvas.drawText(labels.get(lastIdx), centerX, chartBottom + 28, textPaint);
                canvas.restore();
            }
        }

        // Legend below chart
        float legendY = chartBottom + PADDING_BOTTOM + 8;
        float colWidth = chartWidth / 3f;
        Paint legendTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendTextPaint.setColor(getTextColor());
        legendTextPaint.setTextSize(20f);

        for (int p = 0; p < pairNames.size(); p++) {
            int lineColor = PAIR_COLORS[p % PAIR_COLORS.length];
            int col = p % 3;
            int row = p / 3;
            float lx = chartLeft + col * colWidth;
            float ly = legendY + row * 24f;

            legendBoxPaint.setColor(lineColor);
            canvas.drawRect(lx, ly - 6, lx + 16, ly + 6, legendBoxPaint);

            String label = pairNames.get(p);
            if (label != null && label.length() > 20) {
                label = label.substring(0, 17) + "...";
            }
            canvas.drawText(label != null ? label : "", lx + 20, ly + 6, legendTextPaint);
        }
    }

    private float getLegendHeight() {
        if (pairNames.isEmpty()) {
            return 0f;
        }
        int legendLines = (pairNames.size() + 2) / 3;
        return legendLines * 24f + 16f;
    }

    @SuppressLint("ResourceType")
    private int getTextColor() {
        int[] attrs = {android.R.attr.textColorPrimary};
        android.content.res.TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return color;
    }
}
