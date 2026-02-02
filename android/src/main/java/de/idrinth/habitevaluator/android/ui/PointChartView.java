package de.idrinth.habitevaluator.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view that draws a bar chart of daily points with an average line overlay.
 */
public class PointChartView extends View {

    private final List<Integer> dailyPoints = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private double average = 0;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averageLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averageLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float LABEL_TEXT_SIZE_SP = 10f;
    private static final float VALUE_TEXT_SIZE_SP = 9f;
    private static final float BOTTOM_PADDING_DP = 24f;
    private static final float TOP_PADDING_DP = 16f;
    private static final float SIDE_PADDING_DP = 8f;
    private static final float BAR_CORNER_RADIUS_DP = 4f;
    private static final float BAR_GAP_FRACTION = 0.3f;

    public PointChartView(Context context) {
        super(context);
        init();
    }

    public PointChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PointChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int primaryColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0xFF6200EE);
        int errorColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, 0xFFB00020);
        int onSurfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0xFF000000);

        barPaint.setColor(primaryColor);
        barPaint.setStyle(Paint.Style.FILL);

        averageLinePaint.setColor(errorColor);
        averageLinePaint.setStyle(Paint.Style.STROKE);
        averageLinePaint.setStrokeWidth(dpToPx(2f));
        averageLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{dpToPx(6f), dpToPx(4f)}, 0));

        averageLabelPaint.setColor(errorColor);
        averageLabelPaint.setTextSize(spToPx(VALUE_TEXT_SIZE_SP));
        averageLabelPaint.setTextAlign(Paint.Align.RIGHT);

        labelPaint.setColor(onSurfaceColor);
        labelPaint.setTextSize(spToPx(LABEL_TEXT_SIZE_SP));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(onSurfaceColor);
        valuePaint.setTextSize(spToPx(VALUE_TEXT_SIZE_SP));
        valuePaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setColor(onSurfaceColor);
        gridPaint.setAlpha(30);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(0.5f));
    }

    public void setData(List<Integer> points, List<String> dayLabels, double avg) {
        this.dailyPoints.clear();
        this.dailyPoints.addAll(points);
        this.labels.clear();
        this.labels.addAll(dayLabels);
        this.average = avg;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (dailyPoints.isEmpty()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float bottomPadding = dpToPx(BOTTOM_PADDING_DP);
        float topPadding = dpToPx(TOP_PADDING_DP);
        float sidePadding = dpToPx(SIDE_PADDING_DP);
        float cornerRadius = dpToPx(BAR_CORNER_RADIUS_DP);

        float chartHeight = height - bottomPadding - topPadding;
        float chartWidth = width - 2 * sidePadding;

        int maxPoints = 0;
        for (int p : dailyPoints) {
            maxPoints = Math.max(maxPoints, p);
        }
        if (average > maxPoints) {
            maxPoints = (int) Math.ceil(average);
        }
        if (maxPoints == 0) {
            maxPoints = 1;
        }

        int barCount = dailyPoints.size();
        float totalBarWidth = chartWidth / barCount;
        float barWidth = totalBarWidth * (1 - BAR_GAP_FRACTION);
        float gap = totalBarWidth * BAR_GAP_FRACTION;

        // Draw bars
        for (int i = 0; i < barCount; i++) {
            int points = dailyPoints.get(i);
            float barHeight = (float) points / maxPoints * chartHeight;
            float left = sidePadding + i * totalBarWidth + gap / 2;
            float top = topPadding + chartHeight - barHeight;
            float right = left + barWidth;
            float bottom = topPadding + chartHeight;

            if (points > 0) {
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

                // Value on top of bar
                String valueStr = String.valueOf(points);
                canvas.drawText(valueStr, left + barWidth / 2, top - dpToPx(2f), valuePaint);
            }

            // Label below
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), left + barWidth / 2, height - dpToPx(4f), labelPaint);
            }
        }

        // Draw average line
        if (average > 0) {
            float avgY = topPadding + chartHeight - (float) (average / maxPoints * chartHeight);
            Path path = new Path();
            path.moveTo(sidePadding, avgY);
            path.lineTo(width - sidePadding, avgY);
            canvas.drawPath(path, averageLinePaint);

            String avgLabel = String.format("%.1f", average);
            canvas.drawText(avgLabel, width - sidePadding - dpToPx(2f), avgY - dpToPx(3f), averageLabelPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
