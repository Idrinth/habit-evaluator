package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyScoreTest {

    @Test
    void testDefaultConstructor() {
        WeeklyScore score = new WeeklyScore();
        assertNotNull(score.getId());
        assertNotNull(score.getHabitScores());
        assertTrue(score.getHabitScores().isEmpty());
        assertEquals(0, score.getTotalScore());
    }

    @Test
    void testDateConstructorSetsWeekNumber() {
        // 2024-01-01 is a Monday, ISO week 1
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        WeeklyScore score = new WeeklyScore(weekStart, weekEnd);

        assertEquals(weekStart, score.getWeekStart());
        assertEquals(weekEnd, score.getWeekEnd());
        assertEquals(1, score.getWeekNumber());
        assertEquals(2024, score.getYear());
    }

    @Test
    void testAddHabitScoreUpdatesTotalScore() {
        WeeklyScore weeklyScore = new WeeklyScore();

        HabitScore hs1 = new HabitScore("h1", "Exercise", 5, 4);
        HabitScore hs2 = new HabitScore("h2", "Reading", 7, 8);

        weeklyScore.addHabitScore(hs1);
        assertEquals(4, weeklyScore.getTotalScore());

        weeklyScore.addHabitScore(hs2);
        assertEquals(12, weeklyScore.getTotalScore());
        assertEquals(2, weeklyScore.getHabitScores().size());
    }

    @Test
    void testSetHabitScoresRecalculatesTotal() {
        WeeklyScore weeklyScore = new WeeklyScore();
        HabitScore hs1 = new HabitScore("h1", "A", 3, 2);
        HabitScore hs2 = new HabitScore("h2", "B", 5, 4);

        weeklyScore.setHabitScores(List.of(hs1, hs2));
        assertEquals(6, weeklyScore.getTotalScore());
    }

    @Test
    void testGetScoreByCategory() {
        WeeklyScore weeklyScore = new WeeklyScore();

        HabitScore hs1 = new HabitScore("h1", "Exercise", 5, 4);
        hs1.setCategoryId("health");
        HabitScore hs2 = new HabitScore("h2", "Reading", 7, 8);
        hs2.setCategoryId("learning");
        HabitScore hs3 = new HabitScore("h3", "Yoga", 3, 2);
        hs3.setCategoryId("health");

        weeklyScore.addHabitScore(hs1);
        weeklyScore.addHabitScore(hs2);
        weeklyScore.addHabitScore(hs3);

        assertEquals(6, weeklyScore.getScoreByCategory("health"));
        assertEquals(8, weeklyScore.getScoreByCategory("learning"));
        assertEquals(0, weeklyScore.getScoreByCategory("nonexistent"));
    }

    @Test
    void testGetScoreByCategoryWithNullCategoryId() {
        WeeklyScore weeklyScore = new WeeklyScore();

        HabitScore hs1 = new HabitScore("h1", "Exercise", 5, 4);
        // categoryId is null by default
        weeklyScore.addHabitScore(hs1);

        assertEquals(4, weeklyScore.getScoreByCategory(null));
    }

    @Test
    void testRecalculateTotalScore() {
        WeeklyScore weeklyScore = new WeeklyScore();
        weeklyScore.addHabitScore(new HabitScore("h1", "A", 1, 1));
        weeklyScore.addHabitScore(new HabitScore("h2", "B", 2, 2));

        // Manually set total to wrong value
        weeklyScore.setTotalScore(999);
        assertEquals(999, weeklyScore.getTotalScore());

        weeklyScore.recalculateTotalScore();
        assertEquals(3, weeklyScore.getTotalScore());
    }

    @Test
    void testEqualsSameId() {
        WeeklyScore s1 = new WeeklyScore();
        WeeklyScore s2 = new WeeklyScore();
        s2.setId(s1.getId());
        assertEquals(s1, s2);
    }

    @Test
    void testEqualsDifferentId() {
        WeeklyScore s1 = new WeeklyScore();
        WeeklyScore s2 = new WeeklyScore();
        assertNotEquals(s1, s2);
    }

    @Test
    void testHashCodeConsistentWithEquals() {
        WeeklyScore s1 = new WeeklyScore();
        WeeklyScore s2 = new WeeklyScore();
        s2.setId(s1.getId());
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
