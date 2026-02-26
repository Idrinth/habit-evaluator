package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GratitudeServiceTest {

    private GratitudeService service;

    @BeforeEach
    void setUp() {
        service = new GratitudeService();
    }

    @Test
    void testGetDayCountEmptyList() {
        assertEquals(0, service.getDayCount(new ArrayList<>(), LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetDayCountSingleEntry() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("Grateful for sun", LocalDate.of(2026, 2, 1)));
        assertEquals(1, service.getDayCount(entries, LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetDayCountMultipleEntries() {
        List<GratitudeEntry> entries = new ArrayList<>();
        LocalDate date = LocalDate.of(2026, 2, 1);
        entries.add(new GratitudeEntry("Family", date));
        entries.add(new GratitudeEntry("Health", date));
        entries.add(new GratitudeEntry("Friends", date));
        assertEquals(3, service.getDayCount(entries, date));
    }

    @Test
    void testGetDayCountFiltersOtherDates() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.of(2026, 2, 1)));
        entries.add(new GratitudeEntry("B", LocalDate.of(2026, 2, 2)));
        assertEquals(1, service.getDayCount(entries, LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetCountInRangeInclusive() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.of(2026, 2, 1)));
        entries.add(new GratitudeEntry("B", LocalDate.of(2026, 2, 3)));
        entries.add(new GratitudeEntry("C", LocalDate.of(2026, 2, 5)));

        assertEquals(2, service.getCountInRange(entries,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3)));
    }

    @Test
    void testGetCountInRangeExcludesOutside() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.of(2026, 1, 31)));
        entries.add(new GratitudeEntry("B", LocalDate.of(2026, 2, 1)));
        entries.add(new GratitudeEntry("C", LocalDate.of(2026, 2, 6)));

        assertEquals(1, service.getCountInRange(entries,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5)));
    }

    @Test
    void testGetEntriesInRange() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.of(2026, 2, 1)));
        entries.add(new GratitudeEntry("B", LocalDate.of(2026, 2, 3)));
        entries.add(new GratitudeEntry("C", LocalDate.of(2026, 2, 5)));

        List<GratitudeEntry> result = service.getEntriesInRange(entries,
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4));
        assertEquals(1, result.size());
        assertEquals("B", result.get(0).getDescription());
    }

    @Test
    void testGetEntriesInRangeEmpty() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.of(2026, 2, 1)));

        List<GratitudeEntry> result = service.getEntriesInRange(entries,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCountInRangeEmptyList() {
        assertEquals(0, service.getCountInRange(new ArrayList<>(),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 7)));
    }

    @Test
    void testGetDailyAverageForMonthEmptyList() {
        assertEquals(0.0, service.getDailyAverageForMonth(new ArrayList<>()));
    }

    @Test
    void testGetDailyAverageForMonthWithEntries() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(monthStart, today) + 1;

        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", monthStart));
        entries.add(new GratitudeEntry("B", monthStart));
        entries.add(new GratitudeEntry("C", monthStart));
        // 3 entries over daysElapsed days
        double expected = 3.0 / daysElapsed;
        assertEquals(expected, service.getDailyAverageForMonth(entries), 0.001);
    }

    @Test
    void testGetCurrentStreakEmptyList() {
        assertEquals(0, service.getCurrentStreak(new ArrayList<>()));
    }

    @Test
    void testGetCurrentStreakToday() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("Today's gratitude", LocalDate.now()));
        assertEquals(1, service.getCurrentStreak(entries));
    }

    @Test
    void testGetCurrentStreakConsecutiveDays() {
        List<GratitudeEntry> entries = new ArrayList<>();
        LocalDate today = LocalDate.now();
        entries.add(new GratitudeEntry("A", today));
        entries.add(new GratitudeEntry("B", today.minusDays(1)));
        entries.add(new GratitudeEntry("C", today.minusDays(2)));
        assertEquals(3, service.getCurrentStreak(entries));
    }

    @Test
    void testGetCurrentStreakBrokenByGap() {
        List<GratitudeEntry> entries = new ArrayList<>();
        LocalDate today = LocalDate.now();
        entries.add(new GratitudeEntry("A", today));
        entries.add(new GratitudeEntry("B", today.minusDays(1)));
        // Gap on today.minusDays(2)
        entries.add(new GratitudeEntry("C", today.minusDays(3)));
        assertEquals(2, service.getCurrentStreak(entries));
    }

    @Test
    void testGetCurrentStreakNoEntryToday() {
        List<GratitudeEntry> entries = new ArrayList<>();
        entries.add(new GratitudeEntry("A", LocalDate.now().minusDays(1)));
        assertEquals(0, service.getCurrentStreak(entries));
    }
}
