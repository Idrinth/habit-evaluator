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
    void testAllCorrelationsReturned() {
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

        // 12 habits + diary + sleep = 14 signals, all pairs with shared days >= 7
        // should return more than 10 correlations (no cap)
        assertTrue(result.size() > 10);
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

    @Test
    void testProximityBoostAffectsCorrelation() {
        LocalDate today = LocalDate.now();

        // Run 1: habit and diary entries close in time on shared days (within 2h)
        Habit habitClose = new Habit("Close", "Test");
        List<DiaryEntry> diaryClose = new ArrayList<>();

        // Run 2: habit and diary entries far apart in time on shared days
        Habit habitFar = new Habit("Far", "Test");
        List<DiaryEntry> diaryFar = new ArrayList<>();

        // Create entries on 2/3 of days for habits, 2/3 of days for diary,
        // resulting in 1/3 of days as shared (where both are non-zero)
        for (int i = 0; i < 90; i++) {
            LocalDate date = today.minusDays(i);
            int value = (i % 4) + 1;
            EventSignificance sig = i % 2 == 0 ? EventSignificance.MAJOR : EventSignificance.MINOR;

            if (i % 3 != 2) { // 2/3 of days have habit entries
                HabitEntry he1 = new HabitEntry();
                he1.setCompletedAt(date.atTime(10, 0));
                he1.setValue(value);
                habitClose.addEntry(he1);

                HabitEntry he2 = new HabitEntry();
                he2.setCompletedAt(date.atTime(10, 0));
                he2.setValue(value);
                habitFar.addEntry(he2);
            }

            if (i % 3 != 1) { // 2/3 of days have diary entries
                DiaryEntry de1 = new DiaryEntry("Event", sig, date);
                de1.setCreatedAt(date.atTime(11, 0)); // 1h after habit -> within 2h
                diaryClose.add(de1);

                DiaryEntry de2 = new DiaryEntry("Event", sig, date);
                de2.setCreatedAt(date.atTime(22, 0)); // 12h after habit -> not within 2h
                diaryFar.add(de2);
            }
        }

        List<EventCorrelation> resultClose = service.calculateCorrelations(
                List.of(habitClose), diaryClose, new ArrayList<>(), new ArrayList<>());
        List<EventCorrelation> resultFar = service.calculateCorrelations(
                List.of(habitFar), diaryFar, new ArrayList<>(), new ArrayList<>());

        assertFalse(resultClose.isEmpty());
        assertFalse(resultFar.isEmpty());

        double corrClose = findCorrelation(resultClose, "Close", "Diary");
        double corrFar = findCorrelation(resultFar, "Far", "Diary");

        // The correlations should differ because proximity boost changes weights
        // selectively on shared days
        assertNotEquals(corrClose, corrFar, 0.001);
    }

    @Test
    void testDiaryUsesCreatedAtForProximity() {
        LocalDate today = LocalDate.now();

        Habit habit = new Habit("Workout", "Test");
        List<DiaryEntry> diaryEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);

            HabitEntry he = new HabitEntry();
            he.setCompletedAt(date.atTime(14, 0));
            habit.addEntry(he);

            // eventDate has no time component, createdAt provides the time
            DiaryEntry diary = new DiaryEntry("Event", EventSignificance.NORMAL, date);
            diary.setCreatedAt(date.atTime(14, 30)); // 30 min after habit -> within 2h
            diaryEntries.add(diary);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habit), diaryEntries, new ArrayList<>(), new ArrayList<>());

        // Should produce results without errors
        assertFalse(result.isEmpty());
    }

    @Test
    void testSleepUsesFromTimeForProximity() {
        LocalDate today = LocalDate.now();

        Habit habit = new Habit("Evening routine", "Test");
        List<SleepEntry> sleepEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);

            HabitEntry he = new HabitEntry();
            he.setCompletedAt(date.atTime(21, 30));
            habit.addEntry(he);

            // Sleep entry with fromTime close to habit completion
            SleepEntry se = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), date);
            sleepEntries.add(se);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habit), new ArrayList<>(), sleepEntries, new ArrayList<>());

        // Should produce results with sleep using fromTime for proximity
        assertFalse(result.isEmpty());
    }

    @Test
    void testSleepUsesUntilTimeForWakeUpProximity() {
        LocalDate today = LocalDate.now();

        // Morning habit at 6:30, sleep until 6:00 (wake-up is next day due to midnight crossing)
        Habit habit = new Habit("Morning jog", "Test");
        List<SleepEntry> sleepEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);

            HabitEntry he = new HabitEntry();
            he.setCompletedAt(date.atTime(6, 30));
            habit.addEntry(he);

            // Sleep 22:00-06:00 recorded on the previous day; wake-up is on 'date'
            // so fromTime=22:00 is far from 6:30, but untilTime crosses midnight to 06:00
            // which is only 30 min from the habit
            LocalDate sleepDate = date.minusDays(1);
            SleepEntry se = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), sleepDate);
            sleepEntries.add(se);
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habit), new ArrayList<>(), sleepEntries, new ArrayList<>());

        // Wake-up time (06:00 next day) should be within 2h of morning habit (06:30)
        assertFalse(result.isEmpty());
    }

    @Test
    void testEmotionProximityWithHabit() {
        LocalDate today = LocalDate.now();

        EmotionPair pair = new EmotionPair("anxious", "calm");
        Habit habit = new Habit("Meditation", "Test");
        List<EmotionEntry> emotionEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);

            HabitEntry he = new HabitEntry();
            he.setCompletedAt(date.atTime(8, 0));
            habit.addEntry(he);

            // Emotion recorded 30 min after meditation -> within 2h
            emotionEntries.add(new EmotionEntry(pair, 7, date.atTime(8, 30), null));
        }

        List<EventCorrelation> result = service.calculateCorrelations(
                List.of(habit), new ArrayList<>(), new ArrayList<>(), emotionEntries);

        assertFalse(result.isEmpty());
        boolean foundHabitEmotion = result.stream()
                .anyMatch(c -> (c.getEventA().contains("Meditation") && c.getEventB().contains("Emotion"))
                        || (c.getEventA().contains("Emotion") && c.getEventB().contains("Meditation")));
        assertTrue(foundHabitEmotion);
    }

    private double findCorrelation(List<EventCorrelation> correlations, String partA, String partB) {
        return correlations.stream()
                .filter(c -> (c.getEventA().contains(partA) && c.getEventB().contains(partB))
                        || (c.getEventA().contains(partB) && c.getEventB().contains(partA)))
                .findFirst()
                .map(EventCorrelation::getCorrelation)
                .orElse(0.0);
    }
}
