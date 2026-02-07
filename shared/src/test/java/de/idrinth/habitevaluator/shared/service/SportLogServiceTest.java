package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SportLogServiceTest {

    private SportLogService service;
    private static final LocalDate DATE = LocalDate.of(2026, 2, 1);

    @BeforeEach
    void setUp() {
        service = new SportLogService();
    }

    @Test
    void calculateStatsEmptyList() {
        SportLogStats stats = service.calculateStats(new ArrayList<>(), DATE, DATE.plusDays(6));
        assertEquals(0, stats.getAverageDurationHours());
        assertEquals(0, stats.getAverageMeasurement());
        assertEquals(0, stats.getTotalEntries());
    }

    @Test
    void calculateStatsSingleEntry() {
        List<SportLog> entries = new ArrayList<>();
        entries.add(new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0), DATE));

        SportLogStats stats = service.calculateStats(entries, DATE, DATE);
        assertEquals(1.0, stats.getAverageDurationHours(), 0.01);
        assertEquals(1.0, stats.getMinDurationHours(), 0.01);
        assertEquals(1.0, stats.getMaxDurationHours(), 0.01);
        assertEquals(5.0, stats.getAverageMeasurement(), 0.01);
        assertEquals(5.0, stats.getMinMeasurement(), 0.01);
        assertEquals(5.0, stats.getMaxMeasurement(), 0.01);
        assertEquals(1, stats.getTotalEntries());
    }

    @Test
    void calculateStatsMultipleEntries() {
        List<SportLog> entries = new ArrayList<>();
        // 1 hour duration, 5 km
        entries.add(new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0), DATE));
        // 2 hours duration, 15 km
        entries.add(new SportLog("Cycling", 15.0, "km",
                LocalTime.of(14, 0), LocalTime.of(16, 0), DATE.plusDays(1)));

        SportLogStats stats = service.calculateStats(entries, DATE, DATE.plusDays(1));
        assertEquals(1.5, stats.getAverageDurationHours(), 0.01);
        assertEquals(1.0, stats.getMinDurationHours(), 0.01);
        assertEquals(2.0, stats.getMaxDurationHours(), 0.01);
        assertEquals(10.0, stats.getAverageMeasurement(), 0.01);
        assertEquals(5.0, stats.getMinMeasurement(), 0.01);
        assertEquals(15.0, stats.getMaxMeasurement(), 0.01);
        assertEquals(2, stats.getTotalEntries());
    }

    @Test
    void calculateStatsFiltersOutOfPeriodEntries() {
        List<SportLog> entries = new ArrayList<>();
        entries.add(new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0), DATE));
        entries.add(new SportLog("Swimming", 30.0, "laps",
                LocalTime.of(10, 0), LocalTime.of(11, 0), DATE.plusDays(10)));

        SportLogStats stats = service.calculateStats(entries, DATE, DATE);
        assertEquals(1.0, stats.getAverageDurationHours(), 0.01);
        assertEquals(5.0, stats.getAverageMeasurement(), 0.01);
        assertEquals(1, stats.getTotalEntries());
    }

    @Test
    void calculateStatsDurationCrossingMidnight() {
        List<SportLog> entries = new ArrayList<>();
        // 23:00 to 01:00 = 2 hours
        entries.add(new SportLog("Late run", 10.0, "km",
                LocalTime.of(23, 0), LocalTime.of(1, 0), DATE));

        SportLogStats stats = service.calculateStats(entries, DATE, DATE);
        assertEquals(2.0, stats.getAverageDurationHours(), 0.01);
        assertEquals(1, stats.getTotalEntries());
    }
}
