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
    private final Paint negativeBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averageLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint averageLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint zeroLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

        negativeBarPaint.setColor(errorColor);
        negativeBarPaint.setStyle(Paint.Style.FILL);

        zeroLinePaint.setColor(onSurfaceColor);
        zeroLinePaint.setAlpha(60);
        zeroLinePaint.setStyle(Paint.Style.STROKE);
        zeroLinePaint.setStrokeWidth(dpToPx(1f));

        averageLinePaint.setColor(errorColor);
        averageLinePaint.setStyle(Paint.Style.STROKE);
        averageLinePaint.setStrokeWidth(dpToPx(2f));
        averageLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{dpToPx(6f), dpToPx(4f)}, 0));

        trendLinePaint.setColor(0xFFE91E63);
        trendLinePaint.setStyle(Paint.Style.STROKE);
        trendLinePaint.setStrokeWidth(dpToPx(2f));
        trendLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{dpToPx(4f), dpToPx(2f)}, 0));

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
        int minPoints = 0;
        for (int p : dailyPoints) {
            maxPoints = Math.max(maxPoints, p);
            minPoints = Math.min(minPoints, p);
        }
        if (average > maxPoints) {
            maxPoints = (int) Math.ceil(average);
        }
        if (average < minPoints) {
            minPoints = (int) Math.floor(average);
        }

        // Total range from min to max
        int range = maxPoints - minPoints;
        if (range == 0) {
            range = 1;
        }

        // Calculate where zero line sits within the chart
        float zeroY = topPadding + (float) maxPoints / range * chartHeight;

        int barCount = dailyPoints.size();
        float totalBarWidth = chartWidth / barCount;
        float barWidth = totalBarWidth * (1 - BAR_GAP_FRACTION);
        float gap = totalBarWidth * BAR_GAP_FRACTION;

        // Draw zero line if there are negative values
        if (minPoints < 0) {
            canvas.drawLine(sidePadding, zeroY, width - sidePadding, zeroY, zeroLinePaint);
        }

        // Draw bars
        for (int i = 0; i < barCount; i++) {
            int points = dailyPoints.get(i);
            float left = sidePadding + i * totalBarWidth + gap / 2;
            float right = left + barWidth;

            if (points > 0) {
                float barHeight = (float) points / range * chartHeight;
                float top = zeroY - barHeight;
                RectF rect = new RectF(left, top, right, zeroY);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

                // Value on top of bar
                String valueStr = String.valueOf(points);
                canvas.drawText(valueStr, left + barWidth / 2, top - dpToPx(2f), valuePaint);
            } else if (points < 0) {
                float barHeight = (float) (-points) / range * chartHeight;
                float bottom = zeroY + barHeight;
                RectF rect = new RectF(left, zeroY, right, bottom);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, negativeBarPaint);

                // Value below bar
                String valueStr = String.valueOf(points);
                canvas.drawText(valueStr, left + barWidth / 2, bottom + dpToPx(10f), valuePaint);
            }

            // Label below
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), left + barWidth / 2, height - dpToPx(4f), labelPaint);
            }
        }

        // Draw average line
        if (average != 0) {
            float avgY = topPadding + (float) ((maxPoints - average) / range * chartHeight);
            Path path = new Path();
            path.moveTo(sidePadding, avgY);
            path.lineTo(width - sidePadding, avgY);
            canvas.drawPath(path, averageLinePaint);

            String avgLabel = String.format("%.1f", average);
            canvas.drawText(avgLabel, width - sidePadding - dpToPx(2f), avgY - dpToPx(3f), averageLabelPaint);
        }

        // Draw trend line
        if (barCount >= 2) {
            double[] trend = calculateTrendLine();
            double startY = getTrendY(trend, 0);
            double endY = getTrendY(trend, barCount - 1);

            float trendStartY = topPadding + (float) ((maxPoints - startY) / range * chartHeight);
            float trendEndY = topPadding + (float) ((maxPoints - endY) / range * chartHeight);

            // Clamp to chart bounds
            trendStartY = Math.max(topPadding, Math.min(topPadding + chartHeight, trendStartY));
            trendEndY = Math.max(topPadding, Math.min(topPadding + chartHeight, trendEndY));

            Path trendPath = new Path();
            trendPath.moveTo(sidePadding + totalBarWidth / 2, trendStartY);
            trendPath.lineTo(width - sidePadding - totalBarWidth / 2, trendEndY);
            canvas.drawPath(trendPath, trendLinePaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    private double[] calculateTrendLine() {
        int n = dailyPoints.size();
        if (n < 2) {
            double intercept = n > 0 ? dailyPoints.get(0) : 0;
            return new double[]{0, intercept};
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += dailyPoints.get(i);
            sumXY += i * dailyPoints.get(i);
            sumXX += i * i;
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
