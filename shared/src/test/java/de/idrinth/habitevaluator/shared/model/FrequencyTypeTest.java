package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyTypeTest {

    @Test
    void testValues() {
        FrequencyType[] values = FrequencyType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testDaily() {
        assertEquals(FrequencyType.DAILY, FrequencyType.valueOf("DAILY"));
    }

    @Test
    void testWeekly() {
        assertEquals(FrequencyType.WEEKLY, FrequencyType.valueOf("WEEKLY"));
    }

    @Test
    void testMonthly() {
        assertEquals(FrequencyType.MONTHLY, FrequencyType.valueOf("MONTHLY"));
    }

    @Test
    void testInvalidValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> FrequencyType.valueOf("YEARLY"));
    }
}
