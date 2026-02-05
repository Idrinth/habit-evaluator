package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2HabitRepositoryTest extends H2RepositoryTestBase {

    private H2HabitRepository habitRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = new H2HabitRepository();
        userRepository = new H2UserRepository();

        // Clean up
        for (Habit habit : habitRepository.findAll()) {
            habitRepository.deleteById(habit.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("habituser", "password123");
        userRepository.save(testUser);
    }

    private Habit createHabit(String name, String description) {
        Habit habit = new Habit(name, description);
        habit.setScoringRule(null);
        return habit;
    }

    @Test
    void testSaveAndFindById() {
        Habit habit = createHabit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        Habit saved = habitRepository.save(habit);

        assertNotNull(saved);
        Optional<Habit> found = habitRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
    }

    @Test
    void testSaveUpdatesExistingHabit() {
        Habit habit = createHabit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        habit.setDescription("Updated description");
        habitRepository.save(habit);

        Optional<Habit> found = habitRepository.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated description", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<Habit> found = habitRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        Habit habit1 = createHabit("Exercise", "Daily exercise");
        habit1.setUser(testUser);
        Habit habit2 = createHabit("Reading", "Read books");
        habit2.setUser(testUser);
        habit2.setCategoryId("different-category");
        habitRepository.save(habit1);
        habitRepository.save(habit2);

        List<Habit> all = habitRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        Habit habit = createHabit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        assertTrue(habitRepository.existsById(habit.getId()));
        habitRepository.deleteById(habit.getId());
        assertFalse(habitRepository.existsById(habit.getId()));
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> habitRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        Habit habit = createHabit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        assertTrue(habitRepository.existsById(habit.getId()));
        assertFalse(habitRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        Habit habit1 = createHabit("Exercise", "Daily exercise");
        habit1.setUser(testUser);
        Habit habit2 = createHabit("Reading", "Read books");
        habit2.setUser(testUser);
        habit2.setCategoryId("different-category");
        habitRepository.save(habit1);
        habitRepository.save(habit2);

        User otherUser = new User("otheruser", "password123");
        userRepository.save(otherUser);
        Habit habit3 = createHabit("Cooking", "Cook meals");
        habit3.setUser(otherUser);
        habitRepository.save(habit3);

        List<Habit> userHabits = habitRepository.findByUserId(testUser.getId());
        assertEquals(2, userHabits.size());

        List<Habit> otherUserHabits = habitRepository.findByUserId(otherUser.getId());
        assertEquals(1, otherUserHabits.size());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<Habit> habits = habitRepository.findByUserId("nonexistent-user-id");
        assertTrue(habits.isEmpty());
    }

    @Test
    void testHabitFrequencyTypePersisted() {
        Habit habit = createHabit("Weekly Review", "Review weekly goals");
        habit.setUser(testUser);
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habit.setTargetFrequency(1);
        habitRepository.save(habit);

        Optional<Habit> found = habitRepository.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals(FrequencyType.WEEKLY, found.get().getFrequencyType());
        assertEquals(1, found.get().getTargetFrequency());
    }
}
