package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class InMemoryHabitRepositoryTest {

    private InMemoryHabitRepository repository;

    @Before
    public void setUp() {
        repository = new InMemoryHabitRepository();
    }

    @Test
    public void testSaveAndFindById() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        repository.save(habit);

        Optional<Habit> found = repository.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
    }

    @Test
    public void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(new Habit("Exercise", "Daily exercise"));
        repository.save(new Habit("Reading", "Read a book"));

        assertEquals(2, repository.findAll().size());
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

        repository.save(h1);
        repository.save(h2);

        List<Habit> userHabits = repository.findByUserId(user.getId());
        assertEquals(1, userHabits.size());
        assertEquals("Exercise", userHabits.get(0).getName());
    }

    @Test
    public void testUpdateExisting() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        repository.save(habit);

        habit.setName("Updated");
        repository.save(habit);

        assertEquals(1, repository.findAll().size());
        assertEquals("Updated", repository.findById(habit.getId()).get().getName());
    }

    @Test
    public void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
