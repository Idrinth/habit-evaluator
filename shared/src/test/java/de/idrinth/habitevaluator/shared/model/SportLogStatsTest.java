package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SportLogStatsTest {

    @Test
    void testDefaultConstructor() {
        SportLogStats stats = new SportLogStats();
        assertNull(stats.getPeriodStart());
        assertNull(stats.getPeriodEnd());
        assertEquals(0, stats.getAverageDurationHours());
        assertEquals(0, stats.getTotalEntries());
    }

    @Test
    void testFullConstructor() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);
        SportLogStats stats = new SportLogStats(start, end, 1.5, 0.5, 3.0, 10.0, 5.0, 20.0, 5);
        assertEquals(start, stats.getPeriodStart());
        assertEquals(end, stats.getPeriodEnd());
        assertEquals(1.5, stats.getAverageDurationHours(), 0.01);
        assertEquals(0.5, stats.getMinDurationHours(), 0.01);
        assertEquals(3.0, stats.getMaxDurationHours(), 0.01);
        assertEquals(10.0, stats.getAverageMeasurement(), 0.01);
        assertEquals(5.0, stats.getMinMeasurement(), 0.01);
        assertEquals(20.0, stats.getMaxMeasurement(), 0.01);
        assertEquals(5, stats.getTotalEntries());
    }

    @Test
    void testSetters() {
        SportLogStats stats = new SportLogStats();
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);
        stats.setPeriodStart(start);
        stats.setPeriodEnd(end);
        stats.setAverageDurationHours(2.0);
        stats.setMinDurationHours(1.0);
        stats.setMaxDurationHours(4.0);
        stats.setAverageMeasurement(15.0);
        stats.setMinMeasurement(8.0);
        stats.setMaxMeasurement(25.0);
        stats.setTotalEntries(10);

        assertEquals(start, stats.getPeriodStart());
        assertEquals(end, stats.getPeriodEnd());
        assertEquals(2.0, stats.getAverageDurationHours(), 0.01);
        assertEquals(1.0, stats.getMinDurationHours(), 0.01);
        assertEquals(4.0, stats.getMaxDurationHours(), 0.01);
        assertEquals(15.0, stats.getAverageMeasurement(), 0.01);
        assertEquals(8.0, stats.getMinMeasurement(), 0.01);
        assertEquals(25.0, stats.getMaxMeasurement(), 0.01);
        assertEquals(10, stats.getTotalEntries());
    }
}
