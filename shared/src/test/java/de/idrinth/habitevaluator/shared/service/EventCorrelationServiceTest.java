package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventCorrelationServiceTest {

    private EventCorrelationService service;

    @BeforeEach
    void setUp() {
        service = new EventCorrelationService();
    }

    @Test
    void testEmptyInputsReturnEmptyList() {
        List<EventCorrelation> result = service.calculateCorrelations(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testInsufficientSharedDaysReturnsEmpty() {
        // Create a habit with only 3 days of entries (below MIN_SHARED_DAYS=7)
        Habit habit = new Habit("Exercise", "Test");
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry();
            entry.setCompletedAt(today.minusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        // Create diary entries on same 3 days
        List<DiaryEntry> diaryEntries = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            diaryEntries.add(new DiaryEntry("Event", EventSignificance.NORMAL, today.minusDays(i)));
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habit), diaryEntries, new ArrayList<>(), new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testCorrelatedHabitsReturnResults() {
        LocalDate today = LocalDate.now();
        Habit habitA = new Habit("Exercise", "Test");
        Habit habitB = new Habit("Sleep well", "Test");

        // Create entries on the same 30 days for both habits -> should correlate
        for (int i = 0; i < 30; i++) {
            HabitEntry entryA = new HabitEntry();
            entryA.setCompletedAt(today.minusDays(i).atTime(10, 0));
            habitA.addEntry(entryA);

            HabitEntry entryB = new HabitEntry();
            entryB.setCompletedAt(today.minusDays(i).atTime(22, 0));
            habitB.addEntry(entryB);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habitA, habitB), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertFalse(result.isEmpty());
        EventCorrelation first = result.get(0);
        assertTrue(first.getSharedDays() >= 7);
    }

    @Test
    void testResultsLimitedToTop10() {
        LocalDate today = LocalDate.now();
        // Create 12 habits all correlated with each other
        List<Habit> habits = new ArrayList<>();
        for (int h = 0; h < 12; h++) {
            Habit habit = new Habit("Habit" + h, "Test");
            for (int i = 0; i < 30; i++) {
                HabitEntry entry = new HabitEntry();
                entry.setCompletedAt(today.minusDays(i).atTime(10, 0));
                habit.addEntry(entry);
            }
            habits.add(habit);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                habits, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertTrue(result.size() <= 10);
    }

    @Test
    void testResultsSortedByAbsoluteCorrelation() {
        LocalDate today = LocalDate.now();
        Habit habitA = new Habit("A", "Test");
        Habit habitB = new Habit("B", "Test");

        for (int i = 0; i < 30; i++) {
            HabitEntry entryA = new HabitEntry();
            entryA.setCompletedAt(today.minusDays(i).atTime(10, 0));
            habitA.addEntry(entryA);

            HabitEntry entryB = new HabitEntry();
            entryB.setCompletedAt(today.minusDays(i).atTime(22, 0));
            habitB.addEntry(entryB);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habitA, habitB), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        for (int i = 1; i < result.size(); i++) {
            assertTrue(Math.abs(result.get(i - 1).getCorrelation())
                    >= Math.abs(result.get(i).getCorrelation()));
        }
    }

    @Test
    void testDiaryAndSleepSignalsIncluded() {
        LocalDate today = LocalDate.now();

        List<DiaryEntry> diaryEntries = new ArrayList<>();
        List<SleepEntry> sleepEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            diaryEntries.add(new DiaryEntry("Event", EventSignificance.NORMAL, today.minusDays(i)));
            sleepEntries.add(new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), today.minusDays(i)));
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                new ArrayList<>(), diaryEntries, sleepEntries, new ArrayList<>());

        assertFalse(result.isEmpty());
        // Should find correlation between Diary Points and Sleep Hours
        boolean foundDiarySleep = result.stream()
                .anyMatch(c -> (c.getEventA().contains("Diary") && c.getEventB().contains("Sleep"))
                        || (c.getEventA().contains("Sleep") && c.getEventB().contains("Diary")));
        assertTrue(foundDiarySleep);
    }

    @Test
    void testEmotionEntriesWithNullPairSkipped() {
        LocalDate today = LocalDate.now();

        List<EmotionEntry> emotionEntries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // Entry with null pair should be skipped
            EmotionEntry entry = new EmotionEntry(null, 5, today.minusDays(i).atTime(10, 0), null);
            emotionEntries.add(entry);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), emotionEntries);

        // No crash, and no emotion-based correlations (since pair is null, they're skipped)
        assertTrue(result.isEmpty());
    }

    @Test
    void testEmotionEntriesIncluded() {
        LocalDate today = LocalDate.now();

        EmotionPair pair = new EmotionPair("sad", "happy");

        List<EmotionEntry> emotionEntries = new ArrayList<>();
        List<DiaryEntry> diaryEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            emotionEntries.add(new EmotionEntry(pair, 5, today.minusDays(i).atTime(10, 0), null));
            diaryEntries.add(new DiaryEntry("Event", EventSignificance.NORMAL, today.minusDays(i)));
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                new ArrayList<>(), diaryEntries, new ArrayList<>(), emotionEntries);

        assertFalse(result.isEmpty());
    }
}
