package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class FileSystemHabitRepositoryTest {

    private File tempDir;
    private FileSystemHabitRepository repository;

    @Before
    public void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "habit-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemHabitRepository(tempDir);
    }

    @After
    public void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    public void testSaveAndFindById() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        repository.save(habit);

        Optional<Habit> found = repository.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
        assertEquals("Daily exercise", found.get().getDescription());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Habit> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        Habit h1 = new Habit("Exercise", "Daily exercise");
        Habit h2 = new Habit("Reading", "Read a book");
        repository.save(h1);
        repository.save(h2);

        List<Habit> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void testDeleteById() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        repository.save(habit);
        assertTrue(repository.existsById(habit.getId()));

        repository.deleteById(habit.getId());
        assertFalse(repository.existsById(habit.getId()));
    }

    @Test
    public void testExistsById() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        assertFalse(repository.existsById(habit.getId()));

        repository.save(habit);
        assertTrue(repository.existsById(habit.getId()));
    }

    @Test
    public void testFindByUserId() {
        User user = new User("testuser", "password");
        Habit h1 = new Habit("Exercise", "Daily exercise");
        h1.setUser(user);
        Habit h2 = new Habit("Reading", "Read a book");
        // h2 has no user

        repository.save(h1);
        repository.save(h2);

        List<Habit> userHabits = repository.findByUserId(user.getId());
        assertEquals(1, userHabits.size());
        assertEquals("Exercise", userHabits.get(0).getName());
    }

    @Test
    public void testPersistenceAcrossInstances() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habit.setTargetFrequency(3);
        repository.save(habit);

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        Optional<Habit> found = newRepo.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
        assertEquals(FrequencyType.WEEKLY, found.get().getFrequencyType());
        assertEquals(3, found.get().getTargetFrequency());
    }

    @Test
    public void testSaveWithEntries() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        entry.setNotes("Morning run");
        entry.setValue(1);
        entry.setHabit(habit);
        habit.setEntries(new ArrayList<>());
        habit.getEntries().add(entry);

        repository.save(habit);

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        Optional<Habit> found = newRepo.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getEntries().size());
        assertEquals("Morning run", found.get().getEntries().get(0).getNotes());
    }

    @Test
    public void testSaveWithScoringRule() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        ScoringRule rule = new ScoringRule();
        rule.setName("Custom Rule");
        habit.setScoringRule(rule);

        repository.save(habit);

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        Optional<Habit> found = newRepo.findById(habit.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getScoringRule());
        assertEquals("Custom Rule", found.get().getScoringRule().getName());
    }

    @Test
    public void testSaveWithTranslations() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Übung");
        nameTranslations.put("es", "Ejercicio");
        habit.setNameTranslations(nameTranslations);

        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Tägliche Übung");
        habit.setDescriptionTranslations(descTranslations);

        repository.save(habit);

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        Optional<Habit> found = newRepo.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Übung", found.get().getNameTranslations().get("de"));
        assertEquals("Ejercicio", found.get().getNameTranslations().get("es"));
        assertEquals("Tägliche Übung", found.get().getDescriptionTranslations().get("de"));
    }

    @Test
    public void testSaveWithUser() {
        User user = new User("testuser", "password");
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(user);

        repository.save(habit);

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        Optional<Habit> found = newRepo.findById(habit.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
        assertEquals("testuser", found.get().getUser().getUsername());
    }

    @Test
    public void testUpdateExistingHabit() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        repository.save(habit);

        habit.setName("Updated Exercise");
        habit.setDescription("Updated description");
        repository.save(habit);

        List<Habit> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("Updated Exercise", all.get(0).getName());
    }

    @Test
    public void testEmptyRepository() {
        List<Habit> all = repository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    public void testSaveWithAllFrequencyTypes() {
        for (FrequencyType type : FrequencyType.values()) {
            Habit habit = new Habit("Habit-" + type.name(), "Description");
            habit.setFrequencyType(type);
            repository.save(habit);
        }

        FileSystemHabitRepository newRepo = new FileSystemHabitRepository(tempDir);
        assertEquals(FrequencyType.values().length, newRepo.findAll().size());
    }
}
