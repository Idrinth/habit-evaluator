package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SleepStatsTest {

    @Test
    void testDefaultConstructor() {
        SleepStats stats = new SleepStats();
        assertNull(stats.getPeriodStart());
        assertNull(stats.getPeriodEnd());
        assertEquals(0.0, stats.getAverageHours());
        assertEquals(0.0, stats.getMinHours());
        assertEquals(0.0, stats.getMaxHours());
        assertEquals(0, stats.getTotalEntries());
    }

    @Test
    void testFullConstructor() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 7);
        SleepStats stats = new SleepStats(start, end, 7.5, 6.0, 9.0, 7);

        assertEquals(start, stats.getPeriodStart());
        assertEquals(end, stats.getPeriodEnd());
        assertEquals(7.5, stats.getAverageHours(), 0.01);
        assertEquals(6.0, stats.getMinHours(), 0.01);
        assertEquals(9.0, stats.getMaxHours(), 0.01);
        assertEquals(7, stats.getTotalEntries());
    }

    @Test
    void testSetPeriodStart() {
        SleepStats stats = new SleepStats();
        LocalDate date = LocalDate.of(2026, 3, 1);
        stats.setPeriodStart(date);
        assertEquals(date, stats.getPeriodStart());
    }

    @Test
    void testSetPeriodEnd() {
        SleepStats stats = new SleepStats();
        LocalDate date = LocalDate.of(2026, 3, 7);
        stats.setPeriodEnd(date);
        assertEquals(date, stats.getPeriodEnd());
    }

    @Test
    void testSetAverageHours() {
        SleepStats stats = new SleepStats();
        stats.setAverageHours(8.25);
        assertEquals(8.25, stats.getAverageHours(), 0.01);
    }

    @Test
    void testSetMinHours() {
        SleepStats stats = new SleepStats();
        stats.setMinHours(4.5);
        assertEquals(4.5, stats.getMinHours(), 0.01);
    }

    @Test
    void testSetMaxHours() {
        SleepStats stats = new SleepStats();
        stats.setMaxHours(10.0);
        assertEquals(10.0, stats.getMaxHours(), 0.01);
    }

    @Test
    void testSetTotalEntries() {
        SleepStats stats = new SleepStats();
        stats.setTotalEntries(14);
        assertEquals(14, stats.getTotalEntries());
    }
}
