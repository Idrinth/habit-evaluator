package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getHabits());
        assertTrue(user.getHabits().isEmpty());
    }

    @Test
    void testUsernamePasswordConstructor() {
        User user = new User("john", "secret");
        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertNotNull(user.getId());
    }

    @Test
    void testFullConstructor() {
        User user = new User("john", "secret", "john@example.com");
        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void testAddHabitSetsUserReference() {
        User user = new User("john", "pass");
        Habit habit = new Habit("Exercise", "Daily");

        user.addHabit(habit);

        assertEquals(1, user.getHabits().size());
        assertSame(user, habit.getUser());
    }

    @Test
    void testRemoveHabitClearsUserReference() {
        User user = new User("john", "pass");
        Habit habit = new Habit("Exercise", "Daily");

        user.addHabit(habit);
        user.removeHabit(habit);

        assertTrue(user.getHabits().isEmpty());
        assertNull(habit.getUser());
    }

    @Test
    void testSetEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        assertEquals("test@test.com", user.getEmail());
    }

    @Test
    void testEqualsSameId() {
        User u1 = new User("a", "a");
        User u2 = new User("b", "b");
        u2.setId(u1.getId());
        assertEquals(u1, u2);
    }

    @Test
    void testEqualsDifferentId() {
        User u1 = new User("a", "a");
        User u2 = new User("a", "a");
        assertNotEquals(u1, u2);
    }

    @Test
    void testEqualsNull() {
        User u = new User();
        assertNotEquals(null, u);
    }

    @Test
    void testEqualsSameObject() {
        User u = new User();
        assertEquals(u, u);
    }

    @Test
    void testHashCodeConsistent() {
        User u1 = new User();
        User u2 = new User();
        u2.setId(u1.getId());
        assertEquals(u1.hashCode(), u2.hashCode());
    }
}
