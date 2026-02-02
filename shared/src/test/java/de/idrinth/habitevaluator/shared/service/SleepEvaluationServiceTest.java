package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SleepEvaluationServiceTest {

    private SleepEvaluationService service;
    private static final LocalDate DATE = LocalDate.of(2026, 2, 1);

    @BeforeEach
    void setUp() {
        service = new SleepEvaluationService();
    }

    @Test
    void noOverlapWithEmptyList() {
        assertFalse(service.hasOverlap(
                new ArrayList<>(), DATE,
                LocalTime.of(22, 0), LocalTime.of(23, 0)
        ));
    }

    @Test
    void noOverlapWithNullList() {
        assertFalse(service.hasOverlap(
                null, DATE,
                LocalTime.of(22, 0), LocalTime.of(23, 0)
        ));
    }

    @Test
    void detectsOverlapSameDay() {
        List<SleepEntry> entries = new ArrayList<>();
        entries.add(new SleepEntry(LocalTime.of(10, 0), LocalTime.of(12, 0), DATE));

        assertTrue(service.hasOverlap(entries, DATE,
                LocalTime.of(11, 0), LocalTime.of(13, 0)));
    }

    @Test
    void noOverlapAdjacentSameDay() {
        List<SleepEntry> entries = new ArrayList<>();
        entries.add(new SleepEntry(LocalTime.of(10, 0), LocalTime.of(12, 0), DATE));

        assertFalse(service.hasOverlap(entries, DATE,
                LocalTime.of(12, 0), LocalTime.of(14, 0)));
    }

    @Test
    void detectsOverlapWhenExistingCrossesMidnight() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 1: 22:30 - 02:00 (crosses midnight into Feb 2)
        entries.add(new SleepEntry(LocalTime.of(22, 30), LocalTime.of(2, 0), DATE));

        // New entry on Feb 2: 01:00 - 05:00 overlaps with the midnight-crossing entry
        assertTrue(service.hasOverlap(entries, DATE.plusDays(1),
                LocalTime.of(1, 0), LocalTime.of(5, 0)));
    }

    @Test
    void noOverlapAfterMidnightCrossingEntry() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 1: 22:30 - 02:00 (crosses midnight into Feb 2)
        entries.add(new SleepEntry(LocalTime.of(22, 30), LocalTime.of(2, 0), DATE));

        // New entry on Feb 2: 02:00 - 05:00 starts exactly when crossing entry ends
        assertFalse(service.hasOverlap(entries, DATE.plusDays(1),
                LocalTime.of(2, 0), LocalTime.of(5, 0)));
    }

    @Test
    void detectsOverlapWhenNewEntryCrossesMidnight() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 2: 01:00 - 05:00
        entries.add(new SleepEntry(LocalTime.of(1, 0), LocalTime.of(5, 0), DATE.plusDays(1)));

        // New entry on Feb 1: 23:00 - 02:00 crosses midnight and overlaps
        assertTrue(service.hasOverlap(entries, DATE,
                LocalTime.of(23, 0), LocalTime.of(2, 0)));
    }

    @Test
    void noOverlapWhenNewMidnightCrossingEndsBeforeNextDay() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 2: 03:00 - 05:00
        entries.add(new SleepEntry(LocalTime.of(3, 0), LocalTime.of(5, 0), DATE.plusDays(1)));

        // New entry on Feb 1: 23:00 - 02:00 crosses midnight but ends before 03:00
        assertFalse(service.hasOverlap(entries, DATE,
                LocalTime.of(23, 0), LocalTime.of(2, 0)));
    }

    @Test
    void adjacentEntriesAtMidnightBoundaryDoNotOverlap() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 1: 22:30 - 00:00 (crosses midnight)
        entries.add(new SleepEntry(LocalTime.of(22, 30), LocalTime.of(0, 0), DATE));

        // New entry on Feb 2: 00:00 - 01:30 starts exactly at midnight
        assertFalse(service.hasOverlap(entries, DATE.plusDays(1),
                LocalTime.of(0, 0), LocalTime.of(1, 30)));
    }

    @Test
    void detectsOverlapBothCrossMidnight() {
        List<SleepEntry> entries = new ArrayList<>();
        // Entry on Feb 1: 22:00 - 02:00 (crosses midnight)
        entries.add(new SleepEntry(LocalTime.of(22, 0), LocalTime.of(2, 0), DATE));

        // New entry on Feb 1: 23:00 - 03:00 also crosses midnight and overlaps
        assertTrue(service.hasOverlap(entries, DATE,
                LocalTime.of(23, 0), LocalTime.of(3, 0)));
    }

    @Test
    void noOverlapDifferentDaysNoMidnightCrossing() {
        List<SleepEntry> entries = new ArrayList<>();
        entries.add(new SleepEntry(LocalTime.of(10, 0), LocalTime.of(12, 0), DATE));

        assertFalse(service.hasOverlap(entries, DATE.plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }
}
