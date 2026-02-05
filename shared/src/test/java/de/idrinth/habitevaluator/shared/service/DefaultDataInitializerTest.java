package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDataInitializerTest {

    private InMemoryHabitCategoryRepository categoryRepository;
    private InMemoryHabitRepository habitRepository;
    private DefaultDataInitializer initializer;
    private User user;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryHabitCategoryRepository();
        habitRepository = new InMemoryHabitRepository();
        initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
        user = new User("testuser", "password");
    }

    @Test
    void testInitializeDefaultsCreatesCategories() {
        initializer.initializeDefaults(user);
        assertFalse(categoryRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testInitializeDefaultsCreatesHabits() {
        initializer.initializeDefaults(user);
        assertFalse(habitRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testInitializeDefaultsCreates10Categories() {
        initializer.initializeDefaults(user);
        assertEquals(10, categoryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testInitializeDefaultsIsIdempotent() {
        initializer.initializeDefaults(user);
        int categoryCount = categoryRepository.findByUserId(user.getId()).size();
        int habitCount = habitRepository.findByUserId(user.getId()).size();

        // Run again
        initializer.initializeDefaults(user);
        assertEquals(categoryCount, categoryRepository.findByUserId(user.getId()).size());
        assertEquals(habitCount, habitRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testInitializeDefaultsSetsUserOnHabits() {
        initializer.initializeDefaults(user);
        for (Habit habit : habitRepository.findByUserId(user.getId())) {
            assertSame(user, habit.getUser());
        }
    }

    @Test
    void testInitializeDefaultsSetsUserOnCategories() {
        initializer.initializeDefaults(user);
        for (HabitCategory cat : categoryRepository.findByUserId(user.getId())) {
            assertSame(user, cat.getUser());
        }
    }

    @Test
    void testInitializeDefaultsCategoriesHaveNames() {
        initializer.initializeDefaults(user);
        for (HabitCategory cat : categoryRepository.findByUserId(user.getId())) {
            assertNotNull(cat.getName());
            assertFalse(cat.getName().isEmpty());
        }
    }

    @Test
    void testInitializeDefaultsHabitsHaveCategories() {
        initializer.initializeDefaults(user);
        List<String> categoryIds = new ArrayList<>();
        for (HabitCategory cat : categoryRepository.findByUserId(user.getId())) {
            categoryIds.add(cat.getId());
        }
        for (Habit habit : habitRepository.findByUserId(user.getId())) {
            assertNotNull(habit.getCategoryId());
            assertTrue(categoryIds.contains(habit.getCategoryId()));
        }
    }

    @Test
    void testInitializeDefaultsWithGermanLanguage() {
        initializer.initializeDefaults(user, "de");
        List<HabitCategory> categories = categoryRepository.findByUserId(user.getId());
        assertFalse(categories.isEmpty());
        // Verify that at least one category has a German name
        boolean hasGermanCategory = categories.stream()
                .anyMatch(c -> "Hobbys".equals(c.getName()) || "Arbeit".equals(c.getName())
                        || "Emotionen".equals(c.getName()) || "Schlaf".equals(c.getName()));
        assertTrue(hasGermanCategory, "Should have German category names");
    }

    @Test
    void testInitializeDefaultsWithEnglishLanguage() {
        initializer.initializeDefaults(user, "en");
        List<HabitCategory> categories = categoryRepository.findByUserId(user.getId());
        assertFalse(categories.isEmpty());
        // Verify that at least one category has an English name
        boolean hasEnglishCategory = categories.stream()
                .anyMatch(c -> "Hobbies".equals(c.getName()) || "Work".equals(c.getName())
                        || "Emotions".equals(c.getName()) || "Sleep".equals(c.getName()));
        assertTrue(hasEnglishCategory, "Should have English category names");
    }

    @Test
    void testInitializeDefaultsWithDifferentLanguagesCreatesDifferentNames() {
        // Create two users with different languages
        User germanUser = new User("germanuser", "password");
        User englishUser = new User("englishuser", "password");

        initializer.initializeDefaults(germanUser, "de");
        initializer.initializeDefaults(englishUser, "en");

        List<HabitCategory> germanCategories = categoryRepository.findByUserId(germanUser.getId());
        List<HabitCategory> englishCategories = categoryRepository.findByUserId(englishUser.getId());

        // Both should have 10 categories
        assertEquals(10, germanCategories.size());
        assertEquals(10, englishCategories.size());

        // Get first category names and verify they are different
        String germanFirstCategoryName = germanCategories.get(0).getName();
        String englishFirstCategoryName = englishCategories.get(0).getName();
        assertNotEquals(germanFirstCategoryName, englishFirstCategoryName,
                "German and English category names should be different");
    }

    // Simple in-memory repository implementations for testing

    private static class InMemoryHabitCategoryRepository implements HabitCategoryRepository {
        private final List<HabitCategory> categories = new ArrayList<>();

        @Override
        public HabitCategory save(HabitCategory category) {
            categories.removeIf(c -> c.getId().equals(category.getId()));
            categories.add(category);
            return category;
        }

        @Override
        public Optional<HabitCategory> findById(String id) {
            return categories.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<HabitCategory> findAll() {
            return new ArrayList<>(categories);
        }

        @Override
        public void deleteById(String id) {
            categories.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return categories.stream().anyMatch(c -> c.getId().equals(id));
        }

        @Override
        public List<HabitCategory> findByUserId(String userId) {
            List<HabitCategory> result = new ArrayList<>();
            for (HabitCategory cat : categories) {
                if (cat.getUser() != null && cat.getUser().getId().equals(userId)) {
                    result.add(cat);
                }
            }
            return result;
        }
    }

    private static class InMemoryHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
            habits.removeIf(h -> h.getId().equals(habit.getId()));
            habits.add(habit);
            return habit;
        }

        @Override
        public Optional<Habit> findById(String id) {
            return habits.stream().filter(h -> h.getId().equals(id)).findFirst();
        }

        @Override
        public List<Habit> findAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void deleteById(String id) {
            habits.removeIf(h -> h.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return habits.stream().anyMatch(h -> h.getId().equals(id));
        }

        @Override
        public List<Habit> findByUserId(String userId) {
            List<Habit> result = new ArrayList<>();
            for (Habit habit : habits) {
                if (habit.getUser() != null && habit.getUser().getId().equals(userId)) {
                    result.add(habit);
                }
            }
            return result;
        }
    }
}
