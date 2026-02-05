package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmotionEntryTest {

    @Test
    void testDefaultConstructor() {
        EmotionEntry entry = new EmotionEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getRecordedAt());
        assertEquals(0, entry.getStrength());
    }

    @Test
    void testFullConstructor() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        LocalDateTime time = LocalDateTime.of(2026, 1, 15, 10, 30);
        EmotionEntry entry = new EmotionEntry(pair, 5, time, "Feeling good");

        assertNotNull(entry.getId());
        assertSame(pair, entry.getEmotionPair());
        assertEquals(5, entry.getStrength());
        assertEquals(time, entry.getRecordedAt());
        assertEquals("Feeling good", entry.getNotes());
    }

    @Test
    void testConstructorClampsStrengthAbove10() {
        EmotionPair pair = new EmotionPair("low", "high");
        EmotionEntry entry = new EmotionEntry(pair, 15, LocalDateTime.now(), null);
        assertEquals(10, entry.getStrength());
    }

    @Test
    void testConstructorClampsStrengthBelowMinus10() {
        EmotionPair pair = new EmotionPair("low", "high");
        EmotionEntry entry = new EmotionEntry(pair, -20, LocalDateTime.now(), null);
        assertEquals(-10, entry.getStrength());
    }

    @Test
    void testSetStrengthClampsAbove10() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(15);
        assertEquals(10, entry.getStrength());
    }

    @Test
    void testSetStrengthClampsBelowMinus10() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(-15);
        assertEquals(-10, entry.getStrength());
    }

    @Test
    void testSetStrengthWithinRange() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(7);
        assertEquals(7, entry.getStrength());
        entry.setStrength(-3);
        assertEquals(-3, entry.getStrength());
    }

    @Test
    void testSetEmotionPair() {
        EmotionEntry entry = new EmotionEntry();
        EmotionPair pair = new EmotionPair("angry", "calm");
        entry.setEmotionPair(pair);
        assertSame(pair, entry.getEmotionPair());
    }

    @Test
    void testSetRecordedAt() {
        EmotionEntry entry = new EmotionEntry();
        LocalDateTime time = LocalDateTime.of(2026, 3, 1, 8, 0);
        entry.setRecordedAt(time);
        assertEquals(time, entry.getRecordedAt());
    }

    @Test
    void testSetNotes() {
        EmotionEntry entry = new EmotionEntry();
        entry.setNotes("test note");
        assertEquals("test note", entry.getNotes());
    }

    @Test
    void testSetUser() {
        EmotionEntry entry = new EmotionEntry();
        User user = new User("test", "pass");
        entry.setUser(user);
        assertSame(user, entry.getUser());
    }

    @Test
    void testEqualsSameId() {
        EmotionEntry e1 = new EmotionEntry();
        EmotionEntry e2 = new EmotionEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        EmotionEntry e1 = new EmotionEntry();
        EmotionEntry e2 = new EmotionEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        EmotionEntry e = new EmotionEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        EmotionEntry e = new EmotionEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        EmotionEntry e1 = new EmotionEntry();
        EmotionEntry e2 = new EmotionEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
