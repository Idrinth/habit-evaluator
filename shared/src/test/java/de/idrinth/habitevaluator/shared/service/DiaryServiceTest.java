package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiaryServiceTest {

    private DiaryService service;

    @BeforeEach
    void setUp() {
        service = new DiaryService();
    }

    @Test
    void testGetDayPointsEmptyList() {
        assertEquals(0, service.getDayPoints(new ArrayList<>(), LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetDayPointsSingleEntry() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("Test", EventSignificance.NORMAL, LocalDate.of(2026, 2, 1)));
        assertEquals(2, service.getDayPoints(entries, LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetDayPointsMultipleEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        LocalDate date = LocalDate.of(2026, 2, 1);
        entries.add(new DiaryEntry("A", EventSignificance.MINOR, date));
        entries.add(new DiaryEntry("B", EventSignificance.MAJOR, date));
        entries.add(new DiaryEntry("C", EventSignificance.NORMAL, date));
        assertEquals(7, service.getDayPoints(entries, date)); // 1 + 4 + 2
    }

    @Test
    void testGetDayPointsFiltersOtherDates() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("A", EventSignificance.MAJOR, LocalDate.of(2026, 2, 1)));
        entries.add(new DiaryEntry("B", EventSignificance.MAJOR, LocalDate.of(2026, 2, 2)));
        assertEquals(4, service.getDayPoints(entries, LocalDate.of(2026, 2, 1)));
    }

    @Test
    void testGetPointsInRangeInclusive() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("A", EventSignificance.NORMAL, LocalDate.of(2026, 2, 1)));
        entries.add(new DiaryEntry("B", EventSignificance.NORMAL, LocalDate.of(2026, 2, 3)));
        entries.add(new DiaryEntry("C", EventSignificance.NORMAL, LocalDate.of(2026, 2, 5)));

        // Range Feb 1-3 should include first two
        assertEquals(4, service.getPointsInRange(entries,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3)));
    }

    @Test
    void testGetPointsInRangeExcludesOutside() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("A", EventSignificance.MAJOR, LocalDate.of(2026, 1, 31)));
        entries.add(new DiaryEntry("B", EventSignificance.NORMAL, LocalDate.of(2026, 2, 1)));
        entries.add(new DiaryEntry("C", EventSignificance.MINOR, LocalDate.of(2026, 2, 6)));

        assertEquals(2, service.getPointsInRange(entries,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5)));
    }

    @Test
    void testGetEntriesInRange() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("A", EventSignificance.NORMAL, LocalDate.of(2026, 2, 1)));
        entries.add(new DiaryEntry("B", EventSignificance.NORMAL, LocalDate.of(2026, 2, 3)));
        entries.add(new DiaryEntry("C", EventSignificance.NORMAL, LocalDate.of(2026, 2, 5)));

        List<DiaryEntry> result = service.getEntriesInRange(entries,
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4));
        assertEquals(1, result.size());
        assertEquals("B", result.get(0).getDescription());
    }

    @Test
    void testGetEntriesInRangeEmpty() {
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(new DiaryEntry("A", EventSignificance.NORMAL, LocalDate.of(2026, 2, 1)));

        List<DiaryEntry> result = service.getEntriesInRange(entries,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPointsInRangeEmptyList() {
        assertEquals(0, service.getPointsInRange(new ArrayList<>(),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 7)));
    }
}
