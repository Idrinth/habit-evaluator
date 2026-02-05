package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaHabitRepositoryTest {

    @Autowired
    private JpaHabitRepository habitRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("habituser", "password123", "habituser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        Habit saved = habitRepository.save(habit);

        Optional<Habit> found = habitRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
        assertEquals("Daily exercise", found.get().getDescription());
    }

    @Test
    void testFindByUserId() {
        Habit habit1 = new Habit("Exercise", "Daily exercise");
        habit1.setUser(testUser);
        habitRepository.save(habit1);

        Habit habit2 = new Habit("Reading", "Daily reading");
        habit2.setUser(testUser);
        habitRepository.save(habit2);

        List<Habit> habits = habitRepository.findByUserId(testUser.getId());

        assertEquals(2, habits.size());
    }

    @Test
    void testFindByUserIdSortedByName() {
        Habit habitZ = new Habit("Zzz Sleep", "desc");
        habitZ.setUser(testUser);
        habitRepository.save(habitZ);

        Habit habitA = new Habit("Abs Workout", "desc");
        habitA.setUser(testUser);
        habitRepository.save(habitA);

        List<Habit> habits = habitRepository.findByUserId(testUser.getId());

        assertEquals(2, habits.size());
        assertEquals("Abs Workout", habits.get(0).getName());
        assertEquals("Zzz Sleep", habits.get(1).getName());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        habitRepository.save(habit);

        User otherUser = new User("otheruser", "password", "other@example.com");
        otherUser = userRepository.save(otherUser);

        List<Habit> habits = habitRepository.findByUserId(otherUser.getId());
        assertTrue(habits.isEmpty());
    }

    @Test
    void testDeleteHabit() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        Habit saved = habitRepository.save(habit);

        habitRepository.deleteById(saved.getId());

        assertFalse(habitRepository.findById(saved.getId()).isPresent());
    }
}
