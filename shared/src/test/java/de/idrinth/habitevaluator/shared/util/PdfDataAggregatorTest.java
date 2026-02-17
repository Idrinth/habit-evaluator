package de.idrinth.habitevaluator.shared.util;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfDataAggregatorTest {

    @Test
    void testDisplayFormatConstant() {
        assertNotNull(PdfDataAggregator.DISPLAY_FORMAT);
        assertEquals("2024-01-15", LocalDate.of(2024, 1, 15).format(PdfDataAggregator.DISPLAY_FORMAT));
    }

    @Test
    void testLabelFormatConstant() {
        assertNotNull(PdfDataAggregator.LABEL_FORMAT);
        assertEquals("2024-01-15", LocalDate.of(2024, 1, 15).format(PdfDataAggregator.LABEL_FORMAT));
    }

    @Test
    void testChartColorsNotEmpty() {
        assertTrue(PdfDataAggregator.CHART_COLORS.length > 0);
    }

    @Test
    void testGetChartColorWrapsAround() {
        int firstColor = PdfDataAggregator.getChartColor(0);
        int wrappedColor = PdfDataAggregator.getChartColor(PdfDataAggregator.CHART_COLORS.length);
        assertEquals(firstColor, wrappedColor);
    }

    @Test
    void testGetChartColorValidIndex() {
        assertEquals(PdfDataAggregator.CHART_COLORS[3], PdfDataAggregator.getChartColor(3));
    }

    @Test
    void testTruncateNull() {
        assertEquals("", PdfDataAggregator.truncate(null, 10));
    }

    @Test
    void testTruncateShortString() {
        assertEquals("hello", PdfDataAggregator.truncate("hello", 10));
    }

    @Test
    void testTruncateExactLength() {
        assertEquals("hello", PdfDataAggregator.truncate("hello", 5));
    }

    @Test
    void testTruncateLongString() {
        String result = PdfDataAggregator.truncate("hello world this is long", 10);
        assertEquals("hello w...", result);
        assertEquals(10, result.length());
    }

    @Test
    void testCalculateTrendLineEmpty() {
        double[] trend = PdfDataAggregator.calculateTrendLine(Collections.emptyList());
        assertEquals(0, trend[0]);
        assertEquals(0, trend[1]);
    }

    @Test
    void testCalculateTrendLineSingleValue() {
        double[] trend = PdfDataAggregator.calculateTrendLine(List.of(5.0f));
        assertEquals(0, trend[0]);
        assertEquals(5.0, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineIncreasing() {
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);
        double[] trend = PdfDataAggregator.calculateTrendLine(values);
        assertTrue(trend[0] > 0, "Slope should be positive for increasing values");
    }

    @Test
    void testCalculateTrendLineDecreasing() {
        List<Float> values = Arrays.asList(5.0f, 4.0f, 3.0f, 2.0f, 1.0f);
        double[] trend = PdfDataAggregator.calculateTrendLine(values);
        assertTrue(trend[0] < 0, "Slope should be negative for decreasing values");
    }

    @Test
    void testCalculateTrendLineSkipsZeros() {
        List<Float> values = Arrays.asList(0.0f, 5.0f, 0.0f, 5.0f, 0.0f);
        double[] trend = PdfDataAggregator.calculateTrendLine(values);
        assertEquals(0, trend[0], 0.001, "Slope should be ~0 for constant non-zero values");
    }

    @Test
    void testCalculateTrendLineAllZeros() {
        List<Float> values = Arrays.asList(0.0f, 0.0f, 0.0f);
        double[] trend = PdfDataAggregator.calculateTrendLine(values);
        assertEquals(0, trend[0]);
        assertEquals(0, trend[1]);
    }

    @Test
    void testGetTrendY() {
        double[] trend = new double[]{2.0, 3.0};
        assertEquals(3.0, PdfDataAggregator.getTrendY(trend, 0), 0.001);
        assertEquals(5.0, PdfDataAggregator.getTrendY(trend, 1), 0.001);
        assertEquals(13.0, PdfDataAggregator.getTrendY(trend, 5), 0.001);
    }

    @Test
    void testGroupSleepEntriesByDate() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 3);
        SleepEntry entry1 = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry1.setDate(LocalDate.of(2024, 1, 1));
        SleepEntry entry2 = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry2.setDate(LocalDate.of(2024, 1, 2));

        Map<LocalDate, List<SleepEntry>> grouped = PdfDataAggregator.groupSleepEntriesByDate(
                Arrays.asList(entry1, entry2), from, to);

        assertEquals(3, grouped.size());
        assertEquals(1, grouped.get(LocalDate.of(2024, 1, 1)).size());
        assertEquals(1, grouped.get(LocalDate.of(2024, 1, 2)).size());
        assertEquals(0, grouped.get(LocalDate.of(2024, 1, 3)).size());
    }

    @Test
    void testGroupSleepEntriesExcludesOutOfRange() {
        LocalDate from = LocalDate.of(2024, 1, 5);
        LocalDate to = LocalDate.of(2024, 1, 5);
        SleepEntry outOfRange = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        outOfRange.setDate(LocalDate.of(2024, 1, 3));

        Map<LocalDate, List<SleepEntry>> grouped = PdfDataAggregator.groupSleepEntriesByDate(
                List.of(outOfRange), from, to);

        assertEquals(1, grouped.size());
        assertTrue(grouped.get(from).isEmpty());
    }

    @Test
    void testAggregateSleepDurationsEmpty() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 3);

        PdfDataAggregator.DailySleepDurations result =
                PdfDataAggregator.aggregateSleepDurations(Collections.emptyList(), from, to);

        assertEquals(3, result.getLabels().size());
        assertEquals(0f, result.getTotalDuration());
        assertEquals(0, result.getDaysWithData());
        assertEquals(0f, result.getAvgDuration());
        assertEquals(0f, result.getMinDuration());
        assertEquals(0f, result.getMaxDuration());
    }

    @Test
    void testAggregateSleepDurationsWithData() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 2);
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setDate(LocalDate.of(2024, 1, 1));

        PdfDataAggregator.DailySleepDurations result =
                PdfDataAggregator.aggregateSleepDurations(List.of(entry), from, to);

        assertEquals(2, result.getLabels().size());
        assertEquals(1, result.getDaysWithData());
        assertTrue(result.getTotalDuration() > 0);
    }

    @Test
    void testAggregateDiaryPointsEmpty() {
        DiaryService diaryService = new DiaryService();
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 3);

        PdfDataAggregator.DailyDiaryPoints result =
                PdfDataAggregator.aggregateDiaryPoints(Collections.emptyList(), diaryService, from, to);

        assertEquals(3, result.getLabels().size());
        assertEquals(3, result.getPoints().size());
        assertEquals(0f, result.getTotalPoints());
        assertEquals(0, result.getDaysWithData());
        assertEquals(0f, result.getAvgPoints());
    }

    @Test
    void testAggregateDiaryPointsWithEntries() {
        DiaryService diaryService = new DiaryService();
        LocalDate date = LocalDate.of(2024, 1, 2);
        DiaryEntry entry = new DiaryEntry("Test", EventSignificance.MAJOR, date);

        PdfDataAggregator.DailyDiaryPoints result =
                PdfDataAggregator.aggregateDiaryPoints(List.of(entry), diaryService,
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3));

        assertEquals(3, result.getLabels().size());
        assertTrue(result.getDaysWithData() > 0);
        assertTrue(result.getTotalPoints() > 0);
    }

    @Test
    void testFilterEmotionEntriesByDateRange() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        EmotionEntry inRange = new EmotionEntry();
        inRange.setEmotionPair(pair);
        inRange.setStrength(5);
        inRange.setRecordedAt(LocalDateTime.of(2024, 1, 5, 10, 0));
        EmotionEntry outOfRange = new EmotionEntry();
        outOfRange.setEmotionPair(pair);
        outOfRange.setStrength(-3);
        outOfRange.setRecordedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

        List<EmotionEntry> filtered = PdfDataAggregator.filterEmotionEntriesByDateRange(
                Arrays.asList(inRange, outOfRange),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));

        assertEquals(1, filtered.size());
        assertEquals(5, filtered.get(0).getStrength());
    }

    @Test
    void testComputeEmotionDailyAveragesEmpty() {
        PdfDataAggregator.EmotionDailyAverages result =
                PdfDataAggregator.computeEmotionDailyAverages(Collections.emptyList(),
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3));

        assertEquals(3, result.getLabels().size());
        assertEquals(3, result.getDates().size());
        assertTrue(result.getPairIds().isEmpty());
        assertTrue(result.getPairDailyValues().isEmpty());
    }

    @Test
    void testComputeEmotionDailyAveragesWithData() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        pair.setId("pair-1");
        EmotionEntry e1 = new EmotionEntry();
        e1.setEmotionPair(pair);
        e1.setStrength(4);
        e1.setRecordedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        EmotionEntry e2 = new EmotionEntry();
        e2.setEmotionPair(pair);
        e2.setStrength(6);
        e2.setRecordedAt(LocalDateTime.of(2024, 1, 1, 14, 0));

        PdfDataAggregator.EmotionDailyAverages result =
                PdfDataAggregator.computeEmotionDailyAverages(Arrays.asList(e1, e2),
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2));

        assertEquals(1, result.getPairIds().size());
        assertEquals("pair-1", result.getPairIds().get(0));
        List<Float> dailyVals = result.getPairDailyValues().get(0);
        assertEquals(5.0f, dailyVals.get(0), 0.001);
        assertNull(dailyVals.get(1));
    }

    @Test
    void testDailyHabitPointsGetters() {
        PdfDataAggregator.DailyHabitPoints points = new PdfDataAggregator.DailyHabitPoints(
                List.of("2024-01-01"), List.of(5.0f), 5.0f, 1, 5.0f);
        assertEquals(List.of("2024-01-01"), points.getLabels());
        assertEquals(List.of(5.0f), points.getValues());
        assertEquals(5.0f, points.getTotalPoints());
        assertEquals(1, points.getDaysWithData());
        assertEquals(5.0f, points.getAvgPoints());
    }

    @Test
    void testHabitBreakdownGetters() {
        PdfDataAggregator.HabitBreakdown breakdown =
                new PdfDataAggregator.HabitBreakdown("Running", 42, 7);
        assertEquals("Running", breakdown.getHabitName());
        assertEquals(42, breakdown.getTotalPoints());
        assertEquals(7, breakdown.getActiveDays());
    }

    @Test
    void testDailySleepDurationsGetters() {
        PdfDataAggregator.DailySleepDurations durations = new PdfDataAggregator.DailySleepDurations(
                List.of("2024-01-01"), List.of(8.0f), 8.0f, 1, 8.0f, 8.0f, 8.0f);
        assertEquals(List.of("2024-01-01"), durations.getLabels());
        assertEquals(List.of(8.0f), durations.getDurations());
        assertEquals(8.0f, durations.getTotalDuration());
        assertEquals(1, durations.getDaysWithData());
        assertEquals(8.0f, durations.getAvgDuration());
        assertEquals(8.0f, durations.getMinDuration());
        assertEquals(8.0f, durations.getMaxDuration());
    }
}
