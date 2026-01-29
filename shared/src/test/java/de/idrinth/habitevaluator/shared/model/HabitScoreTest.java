package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HabitScoreTest {

    @Test
    void testDefaultConstructor() {
        HabitScore score = new HabitScore();
        assertNull(score.getHabitId());
        assertNull(score.getHabitName());
        assertEquals(0, score.getCompletionCount());
        assertEquals(0, score.getScore());
    }

    @Test
    void testParameterizedConstructor() {
        HabitScore score = new HabitScore("h1", "Exercise", 5, 4);
        assertEquals("h1", score.getHabitId());
        assertEquals("Exercise", score.getHabitName());
        assertEquals(5, score.getCompletionCount());
        assertEquals(4, score.getScore());
    }

    @Test
    void testSetCategoryId() {
        HabitScore score = new HabitScore();
        score.setCategoryId("cat-1");
        assertEquals("cat-1", score.getCategoryId());
    }

    @Test
    void testSetters() {
        HabitScore score = new HabitScore();
        score.setHabitId("id-1");
        score.setHabitName("Reading");
        score.setCompletionCount(3);
        score.setScore(2);

        assertEquals("id-1", score.getHabitId());
        assertEquals("Reading", score.getHabitName());
        assertEquals(3, score.getCompletionCount());
        assertEquals(2, score.getScore());
    }

    @Test
    void testEqualsSameHabitId() {
        HabitScore s1 = new HabitScore("h1", "A", 1, 1);
        HabitScore s2 = new HabitScore("h1", "B", 2, 2);
        assertEquals(s1, s2);
    }

    @Test
    void testEqualsDifferentHabitId() {
        HabitScore s1 = new HabitScore("h1", "A", 1, 1);
        HabitScore s2 = new HabitScore("h2", "A", 1, 1);
        assertNotEquals(s1, s2);
    }

    @Test
    void testEqualsNull() {
        HabitScore s = new HabitScore("h1", "A", 1, 1);
        assertNotEquals(null, s);
    }

    @Test
    void testEqualsSameObject() {
        HabitScore s = new HabitScore("h1", "A", 1, 1);
        assertEquals(s, s);
    }

    @Test
    void testHashCodeConsistent() {
        HabitScore s1 = new HabitScore("h1", "A", 1, 1);
        HabitScore s2 = new HabitScore("h1", "B", 5, 4);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
