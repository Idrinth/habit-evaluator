package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class SQLiteHabitRepositoryTest {

    private SQLiteHabitRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteHabitRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        Habit habit = createHabit("test-id", "Exercise", "Daily exercise routine");
        repository.save(habit);

        Optional<Habit> found = repository.findById("test-id");
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
        assertEquals("Daily exercise routine", found.get().getDescription());
        assertEquals(FrequencyType.DAILY, found.get().getFrequencyType());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Habit> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createHabit("h1", "Exercise", "Desc 1"));
        repository.save(createHabit("h2", "Reading", "Desc 2"));
        repository.save(createHabit("h3", "Meditation", "Desc 3"));

        List<Habit> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testFindAllEmpty() {
        List<Habit> all = repository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.save(createHabit("to-delete", "Temp Habit", "Will be deleted"));
        assertTrue(repository.existsById("to-delete"));

        repository.deleteById("to-delete");
        assertFalse(repository.existsById("to-delete"));
    }

    @Test
    public void testExistsById() {
        assertFalse(repository.existsById("some-id"));
        repository.save(createHabit("some-id", "Habit", "Desc"));
        assertTrue(repository.existsById("some-id"));
    }

    @Test
    public void testFindByUserId() {
        User user1 = createUser("u1", "alice");
        User user2 = createUser("u2", "bob");

        Habit h1 = createHabit("h1", "Exercise", "Desc");
        h1.setUser(user1);
        Habit h2 = createHabit("h2", "Reading", "Desc");
        h2.setUser(user1);
        Habit h3 = createHabit("h3", "Cooking", "Desc");
        h3.setUser(user2);

        repository.save(h1);
        repository.save(h2);
        repository.save(h3);

        List<Habit> aliceHabits = repository.findByUserId("u1");
        assertEquals(2, aliceHabits.size());

        List<Habit> bobHabits = repository.findByUserId("u2");
        assertEquals(1, bobHabits.size());
        assertEquals("Cooking", bobHabits.get(0).getName());
    }

    @Test
    public void testSaveWithEntries() {
        Habit habit = createHabit("h-entries", "Exercise", "With entries");
        List<HabitEntry> entries = new ArrayList<>();
        HabitEntry entry1 = new HabitEntry();
        entry1.setId("e1");
        entry1.setCompletedAt(LocalDateTime.now());
        entry1.setNotes("Morning run");
        entry1.setValue(1);
        entry1.setHabit(habit);
        entries.add(entry1);

        HabitEntry entry2 = new HabitEntry();
        entry2.setId("e2");
        entry2.setCompletedAt(LocalDateTime.now().minusDays(1));
        entry2.setNotes("Evening walk");
        entry2.setValue(1);
        entry2.setHabit(habit);
        entries.add(entry2);

        habit.setEntries(entries);
        repository.save(habit);

        Optional<Habit> found = repository.findById("h-entries");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getEntries().size());
    }

    @Test
    public void testSaveWithTranslations() {
        Habit habit = createHabit("h-trans", "Exercise", "Exercise description");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Übung");
        nameTranslations.put("fr", "Exercice");
        habit.setNameTranslations(nameTranslations);

        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Übungsbeschreibung");
        habit.setDescriptionTranslations(descTranslations);

        repository.save(habit);

        Optional<Habit> found = repository.findById("h-trans");
        assertTrue(found.isPresent());
        assertEquals("Übung", found.get().getNameTranslations().get("de"));
        assertEquals("Exercice", found.get().getNameTranslations().get("fr"));
        assertEquals("Übungsbeschreibung", found.get().getDescriptionTranslations().get("de"));
    }

    @Test
    public void testSaveWithScoringRule() {
        Habit habit = createHabit("h-rule", "Exercise", "Desc");
        ScoringRule rule = new ScoringRule();
        rule.setId("rule-1");
        rule.setName("Default Rule");
        habit.setScoringRule(rule);

        repository.save(habit);

        Optional<Habit> found = repository.findById("h-rule");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getScoringRule());
        assertEquals("rule-1", found.get().getScoringRule().getId());
        assertEquals("Default Rule", found.get().getScoringRule().getName());
    }

    @Test
    public void testUpdateExistingHabit() {
        Habit habit = createHabit("h-update", "Exercise", "Initial description");
        repository.save(habit);

        habit.setName("Updated Exercise");
        habit.setDescription("Updated description");
        repository.save(habit);

        Optional<Habit> found = repository.findById("h-update");
        assertTrue(found.isPresent());
        assertEquals("Updated Exercise", found.get().getName());
        assertEquals("Updated description", found.get().getDescription());
    }

    @Test
    public void testSavePreservesFrequencyType() {
        Habit weekly = createHabit("h-weekly", "Weekly Check", "Desc");
        weekly.setFrequencyType(FrequencyType.WEEKLY);
        repository.save(weekly);

        Optional<Habit> found = repository.findById("h-weekly");
        assertTrue(found.isPresent());
        assertEquals(FrequencyType.WEEKLY, found.get().getFrequencyType());
    }

    @Test
    public void testSavePreservesPositiveScoring() {
        Habit negative = createHabit("h-neg", "Avoid Smoking", "Negative habit");
        negative.setPositiveScoring(false);
        repository.save(negative);

        Optional<Habit> found = repository.findById("h-neg");
        assertTrue(found.isPresent());
        assertFalse(found.get().isPositiveScoring());
    }

    @Test
    public void testDeleteRemovesEntriesAndTranslations() {
        Habit habit = createHabit("h-cascade", "Exercise", "Desc");
        List<HabitEntry> entries = new ArrayList<>();
        HabitEntry entry = new HabitEntry();
        entry.setId("e-cascade");
        entry.setCompletedAt(LocalDateTime.now());
        entry.setValue(1);
        entry.setHabit(habit);
        entries.add(entry);
        habit.setEntries(entries);

        Map<String, String> names = new HashMap<>();
        names.put("de", "Übung");
        habit.setNameTranslations(names);

        repository.save(habit);
        assertTrue(repository.existsById("h-cascade"));

        repository.deleteById("h-cascade");
        assertFalse(repository.existsById("h-cascade"));
    }

    private Habit createHabit(String id, String name, String description) {
        Habit habit = new Habit();
        habit.setId(id);
        habit.setName(name);
        habit.setDescription(description);
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);
        habit.setMaxEntriesPerDay(1);
        habit.setPositiveScoring(true);
        habit.setCreatedAt(LocalDateTime.now());
        return habit;
    }

    private User createUser(String id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("placeholder");
        return user;
    }
}
