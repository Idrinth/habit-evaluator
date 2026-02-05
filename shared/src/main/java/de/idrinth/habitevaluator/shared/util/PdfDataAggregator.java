package de.idrinth.habitevaluator.shared.util;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Centralized data aggregation utilities for PDF export functionality.
 * Eliminates duplicated aggregation logic across webserver, desktop, and android modules.
 */
public final class PdfDataAggregator {

    /**
     * Standard date format for display (yyyy-MM-dd).
     */
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Short date format for chart labels (MM/dd).
     */
    public static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    /**
     * Color palette for emotion pair charts (as ARGB integers for Android compatibility).
     * Colors: Green, Blue, Orange, Pink, Purple, Cyan, Deep Orange, Brown, Blue Grey, Light Green
     */
    public static final int[] CHART_COLORS = {
            0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0,
            0xFF00BCD4, 0xFFFF5722, 0xFF795548, 0xFF607D8B, 0xFF8BC34A
    };

    private PdfDataAggregator() {
        // Utility class
    }

    /**
     * Result record for daily habit points aggregation.
     */
    public static class DailyHabitPoints {
        private final List<String> labels;
        private final List<Float> values;
        private final float totalPoints;
        private final int daysWithData;
        private final float avgPoints;

        public DailyHabitPoints(List<String> labels, List<Float> values,
                                float totalPoints, int daysWithData, float avgPoints) {
            this.labels = labels;
            this.values = values;
            this.totalPoints = totalPoints;
            this.daysWithData = daysWithData;
            this.avgPoints = avgPoints;
        }

        public List<String> getLabels() { return labels; }
        public List<Float> getValues() { return values; }
        public float getTotalPoints() { return totalPoints; }
        public int getDaysWithData() { return daysWithData; }
        public float getAvgPoints() { return avgPoints; }
    }

    /**
     * Result record for per-habit breakdown.
     */
    public static class HabitBreakdown {
        private final String habitName;
        private final int totalPoints;
        private final int activeDays;

        public HabitBreakdown(String habitName, int totalPoints, int activeDays) {
            this.habitName = habitName;
            this.totalPoints = totalPoints;
            this.activeDays = activeDays;
        }

        public String getHabitName() { return habitName; }
        public int getTotalPoints() { return totalPoints; }
        public int getActiveDays() { return activeDays; }
    }

    /**
     * Result record for daily sleep duration aggregation.
     */
    public static class DailySleepDurations {
        private final List<String> labels;
        private final List<Float> durations;
        private final float totalDuration;
        private final int daysWithData;
        private final float avgDuration;
        private final float minDuration;
        private final float maxDuration;

        public DailySleepDurations(List<String> labels, List<Float> durations,
                                    float totalDuration, int daysWithData,
                                    float avgDuration, float minDuration, float maxDuration) {
            this.labels = labels;
            this.durations = durations;
            this.totalDuration = totalDuration;
            this.daysWithData = daysWithData;
            this.avgDuration = avgDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        public List<String> getLabels() { return labels; }
        public List<Float> getDurations() { return durations; }
        public float getTotalDuration() { return totalDuration; }
        public int getDaysWithData() { return daysWithData; }
        public float getAvgDuration() { return avgDuration; }
        public float getMinDuration() { return minDuration; }
        public float getMaxDuration() { return maxDuration; }
    }

    /**
     * Result record for daily diary points aggregation.
     */
    public static class DailyDiaryPoints {
        private final List<String> labels;
        private final List<Float> points;
        private final float totalPoints;
        private final int daysWithData;
        private final float avgPoints;

        public DailyDiaryPoints(List<String> labels, List<Float> points,
                                float totalPoints, int daysWithData, float avgPoints) {
            this.labels = labels;
            this.points = points;
            this.totalPoints = totalPoints;
            this.daysWithData = daysWithData;
            this.avgPoints = avgPoints;
        }

        public List<String> getLabels() { return labels; }
        public List<Float> getPoints() { return points; }
        public float getTotalPoints() { return totalPoints; }
        public int getDaysWithData() { return daysWithData; }
        public float getAvgPoints() { return avgPoints; }
    }

    /**
     * Result record for emotion pair daily averages.
     */
    public static class EmotionDailyAverages {
        private final List<String> labels;
        private final List<LocalDate> dates;
        private final List<String> pairIds;
        private final Map<String, String> pairLabels;
        private final List<List<Float>> pairDailyValues;
        private final Map<String, List<EmotionEntry>> entriesByPair;

        public EmotionDailyAverages(List<String> labels, List<LocalDate> dates,
                                     List<String> pairIds, Map<String, String> pairLabels,
                                     List<List<Float>> pairDailyValues,
                                     Map<String, List<EmotionEntry>> entriesByPair) {
            this.labels = labels;
            this.dates = dates;
            this.pairIds = pairIds;
            this.pairLabels = pairLabels;
            this.pairDailyValues = pairDailyValues;
            this.entriesByPair = entriesByPair;
        }

        public List<String> getLabels() { return labels; }
        public List<LocalDate> getDates() { return dates; }
        public List<String> getPairIds() { return pairIds; }
        public Map<String, String> getPairLabels() { return pairLabels; }
        public List<List<Float>> getPairDailyValues() { return pairDailyValues; }
        public Map<String, List<EmotionEntry>> getEntriesByPair() { return entriesByPair; }
    }

    /**
     * Aggregates habit points by day for a date range.
     *
     * @param habits         list of habits to aggregate
     * @param scoringService the scoring service to calculate points
     * @param fromDate       start date (inclusive)
     * @param toDate         end date (inclusive)
     * @return aggregated daily habit points data
     */
    public static DailyHabitPoints aggregateHabitPoints(List<Habit> habits,
                                                         HabitScoringService scoringService,
                                                         LocalDate fromDate,
                                                         LocalDate toDate) {
        List<String> labels = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            int dayTotal = 0;
            for (Habit habit : habits) {
                dayTotal += scoringService.calculateHabitScore(habit, d, d).getScore();
            }
            values.add((float) dayTotal);
            if (dayTotal > 0) {
                totalPoints += dayTotal;
                daysWithData++;
            }
        }
        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        return new DailyHabitPoints(labels, values, totalPoints, daysWithData, avgPoints);
    }

    /**
     * Calculates per-habit breakdown for a date range.
     *
     * @param habits         list of habits
     * @param scoringService the scoring service
     * @param fromDate       start date (inclusive)
     * @param toDate         end date (inclusive)
     * @return list of habit breakdowns
     */
    public static List<HabitBreakdown> calculateHabitBreakdowns(List<Habit> habits,
                                                                 HabitScoringService scoringService,
                                                                 LocalDate fromDate,
                                                                 LocalDate toDate) {
        List<HabitBreakdown> breakdowns = new ArrayList<>();
        for (Habit habit : habits) {
            int habitTotal = 0;
            int habitDays = 0;
            for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
                int score = scoringService.calculateHabitScore(habit, d, d).getScore();
                habitTotal += score;
                if (score > 0) {
                    habitDays++;
                }
            }
            breakdowns.add(new HabitBreakdown(habit.getName(), habitTotal, habitDays));
        }
        return breakdowns;
    }

    /**
     * Groups sleep entries by date within a range.
     *
     * @param allEntries all sleep entries
     * @param fromDate   start date (inclusive)
     * @param toDate     end date (inclusive)
     * @return map of date to list of sleep entries for that date
     */
    public static Map<LocalDate, List<SleepEntry>> groupSleepEntriesByDate(List<SleepEntry> allEntries,
                                                                            LocalDate fromDate,
                                                                            LocalDate toDate) {
        Map<LocalDate, List<SleepEntry>> entriesByDate = new TreeMap<>();
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            entriesByDate.put(d, new ArrayList<>());
        }
        for (SleepEntry entry : allEntries) {
            if (DateRangeUtils.isInRange(entry.getDate(), fromDate, toDate)) {
                List<SleepEntry> dayList = entriesByDate.get(entry.getDate());
                if (dayList != null) {
                    dayList.add(entry);
                }
            }
        }
        return entriesByDate;
    }

    /**
     * Aggregates sleep durations by day for a date range.
     *
     * @param allEntries all sleep entries
     * @param fromDate   start date (inclusive)
     * @param toDate     end date (inclusive)
     * @return aggregated daily sleep duration data
     */
    public static DailySleepDurations aggregateSleepDurations(List<SleepEntry> allEntries,
                                                               LocalDate fromDate,
                                                               LocalDate toDate) {
        Map<LocalDate, List<SleepEntry>> entriesByDate = groupSleepEntriesByDate(allEntries, fromDate, toDate);

        List<String> labels = new ArrayList<>();
        List<Float> durations = new ArrayList<>();
        float totalDuration = 0f;
        int daysWithData = 0;
        float minDuration = Float.MAX_VALUE;
        float maxDuration = 0f;

        for (Map.Entry<LocalDate, List<SleepEntry>> mapEntry : entriesByDate.entrySet()) {
            labels.add(mapEntry.getKey().format(LABEL_FORMAT));
            List<SleepEntry> dayEntries = mapEntry.getValue();
            float dayDuration = 0f;
            for (SleepEntry entry : dayEntries) {
                dayDuration += (float) entry.getHours();
            }
            durations.add(dayDuration);
            if (!dayEntries.isEmpty()) {
                totalDuration += dayDuration;
                daysWithData++;
                minDuration = Math.min(minDuration, dayDuration);
                maxDuration = Math.max(maxDuration, dayDuration);
            }
        }

        float avgDuration = daysWithData > 0 ? totalDuration / daysWithData : 0f;
        if (minDuration == Float.MAX_VALUE) {
            minDuration = 0f;
        }

        return new DailySleepDurations(labels, durations, totalDuration, daysWithData,
                avgDuration, minDuration, maxDuration);
    }

    /**
     * Aggregates diary points by day for a date range.
     *
     * @param allEntries   all diary entries
     * @param diaryService the diary service for point calculation
     * @param fromDate     start date (inclusive)
     * @param toDate       end date (inclusive)
     * @return aggregated daily diary points data
     */
    public static DailyDiaryPoints aggregateDiaryPoints(List<DiaryEntry> allEntries,
                                                         DiaryService diaryService,
                                                         LocalDate fromDate,
                                                         LocalDate toDate) {
        List<String> labels = new ArrayList<>();
        List<Float> points = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            int dayPoints = diaryService.getDayPoints(allEntries, d);
            points.add((float) dayPoints);
            if (dayPoints > 0) {
                totalPoints += dayPoints;
                daysWithData++;
            }
        }
        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        return new DailyDiaryPoints(labels, points, totalPoints, daysWithData, avgPoints);
    }

    /**
     * Computes daily averages for emotion entries grouped by emotion pair.
     *
     * @param rangeEntries emotion entries within the date range
     * @param fromDate     start date (inclusive)
     * @param toDate       end date (inclusive)
     * @return emotion daily averages data
     */
    public static EmotionDailyAverages computeEmotionDailyAverages(List<EmotionEntry> rangeEntries,
                                                                    LocalDate fromDate,
                                                                    LocalDate toDate) {
        // Build date labels
        List<String> labels = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            dates.add(d);
        }

        // Group entries by emotion pair
        Map<String, List<EmotionEntry>> entriesByPair = new TreeMap<>();
        Map<String, String> pairLabels = new TreeMap<>();
        for (EmotionEntry entry : rangeEntries) {
            if (entry.getEmotionPair() != null) {
                String pairId = entry.getEmotionPair().getId();
                entriesByPair.computeIfAbsent(pairId, k -> new ArrayList<>()).add(entry);
                pairLabels.put(pairId, entry.getEmotionPair().toString());
            }
        }

        // For each pair, compute daily average strength
        List<String> pairIds = new ArrayList<>(entriesByPair.keySet());
        List<List<Float>> pairDailyValues = new ArrayList<>();
        for (String pairId : pairIds) {
            List<EmotionEntry> pairEntries = entriesByPair.get(pairId);
            Map<LocalDate, List<Integer>> strengthsByDate = new TreeMap<>();
            for (EmotionEntry entry : pairEntries) {
                LocalDate d = entry.getRecordedAt().toLocalDate();
                strengthsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(entry.getStrength());
            }
            List<Float> dailyAvgs = new ArrayList<>();
            for (LocalDate d : dates) {
                List<Integer> strengths = strengthsByDate.get(d);
                if (strengths != null && !strengths.isEmpty()) {
                    float sum = 0;
                    for (int s : strengths) {
                        sum += s;
                    }
                    dailyAvgs.add(sum / strengths.size());
                } else {
                    dailyAvgs.add(null);
                }
            }
            pairDailyValues.add(dailyAvgs);
        }

        return new EmotionDailyAverages(labels, dates, pairIds, pairLabels, pairDailyValues, entriesByPair);
    }

    /**
     * Filters emotion entries to only include those within the date range.
     *
     * @param allEntries all emotion entries
     * @param fromDate   start date (inclusive)
     * @param toDate     end date (inclusive)
     * @return filtered list of emotion entries
     */
    public static List<EmotionEntry> filterEmotionEntriesByDateRange(List<EmotionEntry> allEntries,
                                                                      LocalDate fromDate,
                                                                      LocalDate toDate) {
        List<EmotionEntry> rangeEntries = new ArrayList<>();
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (DateRangeUtils.isInRange(entryDate, fromDate, toDate)) {
                rangeEntries.add(entry);
            }
        }
        return rangeEntries;
    }

    /**
     * Calculates a linear trend line using least squares regression.
     * Excludes zero values from the regression (treats them as missing data).
     *
     * @param values the data points
     * @return array of [slope, intercept]
     */
    public static double[] calculateTrendLine(List<Float> values) {
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
            if (value != 0) {
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

    /**
     * Gets the Y value on a trend line for a given X.
     *
     * @param trend the trend line coefficients [slope, intercept]
     * @param x     the X value
     * @return the Y value
     */
    public static double getTrendY(double[] trend, int x) {
        return trend[0] * x + trend[1];
    }

    /**
     * Truncates a string to the specified maximum length, adding ellipsis if truncated.
     *
     * @param text      the text to truncate
     * @param maxLength the maximum length (including ellipsis)
     * @return the truncated string
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Gets a color from the chart color palette by index (wraps around).
     *
     * @param index the index
     * @return the color as an ARGB integer
     */
    public static int getChartColor(int index) {
        return CHART_COLORS[index % CHART_COLORS.length];
    }
}
