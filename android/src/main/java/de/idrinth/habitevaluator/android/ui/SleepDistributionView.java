package de.idrinth.habitevaluator.android.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SleepDistributionView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private double[] percentAsleep = new double[24];

    private static final float PADDING_LEFT = 60f;
    private static final float PADDING_RIGHT = 16f;
    private static final float PADDING_TOP = 24f;
    private static final float PADDING_BOTTOM = 48f;

    public SleepDistributionView(Context context) {
        super(context);
        init();
    }

    public SleepDistributionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SleepDistributionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(0xFF5B78F6);

        textPaint.setTextSize(24f);

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(60);
    }

    public void setData(double[] percentAsleep) {
        this.percentAsleep = percentAsleep != null ? percentAsleep : new double[24];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean hasData = false;
        for (double v : percentAsleep) {
            if (v > 0) {
                hasData = true;
                break;
            }
        }

        if (!hasData) {
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

        // Draw axes
        axisPaint.setColor(getTextColor());
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

        // Draw grid lines and Y-axis labels
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        gridPaint.setColor(getTextColor());
        gridPaint.setAlpha(40);
        for (int i = 0; i <= 4; i++) {
            float val = i * 25f;
            float y = chartBottom - (chartHeight * i / 4f);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            canvas.drawText(String.format("%.0f%%", val), chartLeft - 6, y + 6, textPaint);
        }

        // Draw bars
        float barSpacing = chartWidth / 24f;
        float barWidth = Math.max(barSpacing * 0.65f, 4f);

        for (int h = 0; h < 24; h++) {
            float val = (float) percentAsleep[h];
            float barHeight = (val / 100f) * chartHeight;
            float centerX = chartLeft + barSpacing * h + barSpacing / 2f;
            float left = centerX - barWidth / 2f;
            float right = centerX + barWidth / 2f;
            float top = chartBottom - barHeight;

            RectF rect = new RectF(left, top, right, chartBottom);
            canvas.drawRoundRect(rect, 4f, 4f, barPaint);
        }

        // Draw X-axis labels
        textPaint.setColor(getTextColor());
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int h = 0; h < 24; h += 3) {
            float centerX = chartLeft + barSpacing * h + barSpacing / 2f;
            canvas.save();
            canvas.rotate(-45, centerX, chartBottom + 12);
            canvas.drawText(String.format("%02d:00", h), centerX, chartBottom + 28, textPaint);
            canvas.restore();
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
}
