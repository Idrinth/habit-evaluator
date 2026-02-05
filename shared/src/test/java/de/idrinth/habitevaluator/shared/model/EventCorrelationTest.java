package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCorrelationTest {

    @Test
    void testDefaultConstructor() {
        EventCorrelation ec = new EventCorrelation();
        assertNull(ec.getEventA());
        assertNull(ec.getEventB());
        assertEquals(0.0, ec.getCorrelation());
        assertEquals(0, ec.getSharedDays());
    }

    @Test
    void testFullConstructor() {
        EventCorrelation ec = new EventCorrelation("Sleep", "Exercise", 0.85, 30);
        assertEquals("Sleep", ec.getEventA());
        assertEquals("Exercise", ec.getEventB());
        assertEquals(0.85, ec.getCorrelation(), 0.001);
        assertEquals(30, ec.getSharedDays());
    }

    @Test
    void testSetEventA() {
        EventCorrelation ec = new EventCorrelation();
        ec.setEventA("Diet");
        assertEquals("Diet", ec.getEventA());
    }

    @Test
    void testSetEventB() {
        EventCorrelation ec = new EventCorrelation();
        ec.setEventB("Mood");
        assertEquals("Mood", ec.getEventB());
    }

    @Test
    void testSetCorrelation() {
        EventCorrelation ec = new EventCorrelation();
        ec.setCorrelation(-0.5);
        assertEquals(-0.5, ec.getCorrelation(), 0.001);
    }

    @Test
    void testSetSharedDays() {
        EventCorrelation ec = new EventCorrelation();
        ec.setSharedDays(15);
        assertEquals(15, ec.getSharedDays());
    }

    @Test
    void testNegativeCorrelation() {
        EventCorrelation ec = new EventCorrelation("Stress", "Sleep", -0.72, 50);
        assertTrue(ec.getCorrelation() < 0);
    }
}
