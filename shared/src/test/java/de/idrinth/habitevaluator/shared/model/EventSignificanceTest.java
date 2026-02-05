package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventSignificanceTest {

    @Test
    void testMinorPoints() {
        assertEquals(1, EventSignificance.MINOR.getPoints());
    }

    @Test
    void testNormalPoints() {
        assertEquals(2, EventSignificance.NORMAL.getPoints());
    }

    @Test
    void testMajorPoints() {
        assertEquals(4, EventSignificance.MAJOR.getPoints());
    }

    @Test
    void testValueOf() {
        assertEquals(EventSignificance.MINOR, EventSignificance.valueOf("MINOR"));
        assertEquals(EventSignificance.NORMAL, EventSignificance.valueOf("NORMAL"));
        assertEquals(EventSignificance.MAJOR, EventSignificance.valueOf("MAJOR"));
    }

    @Test
    void testValues() {
        EventSignificance[] values = EventSignificance.values();
        assertEquals(3, values.length);
    }
}
