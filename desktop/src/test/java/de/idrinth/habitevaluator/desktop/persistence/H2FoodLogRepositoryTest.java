package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2FoodLogRepositoryTest extends H2RepositoryTestBase {

    private H2FoodLogRepository foodLogRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        foodLogRepository = new H2FoodLogRepository();
        userRepository = new H2UserRepository();

        for (FoodLog entry : foodLogRepository.findAll()) {
            foodLogRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("fooduser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice, Chicken");
        entry.setUser(testUser);
        FoodLog saved = foodLogRepository.save(entry);

        assertNotNull(saved);
        Optional<FoodLog> found = foodLogRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Rice, Chicken", found.get().getFoodItems());
        assertEquals(450, found.get().getKcal());
    }

    @Test
    void testSaveUpdatesExisting() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry.setUser(testUser);
        foodLogRepository.save(entry);

        entry.setFoodItems("Rice, Beans");
        entry.setKcal(600);
        foodLogRepository.save(entry);

        Optional<FoodLog> found = foodLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Rice, Beans", found.get().getFoodItems());
        assertEquals(600, found.get().getKcal());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<FoodLog> found = foodLogRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        FoodLog entry1 = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry1.setUser(testUser);
        FoodLog entry2 = new FoodLog(50.0, 700, LocalDateTime.now(), "Pasta");
        entry2.setUser(testUser);
        foodLogRepository.save(entry1);
        foodLogRepository.save(entry2);

        List<FoodLog> all = foodLogRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry.setUser(testUser);
        foodLogRepository.save(entry);

        assertTrue(foodLogRepository.findById(entry.getId()).isPresent());
        foodLogRepository.deleteById(entry.getId());
        assertFalse(foodLogRepository.findById(entry.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> foodLogRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry.setUser(testUser);
        foodLogRepository.save(entry);

        assertTrue(foodLogRepository.existsById(entry.getId()));
        assertFalse(foodLogRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        FoodLog entry1 = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry1.setUser(testUser);
        foodLogRepository.save(entry1);

        User otherUser = new User("otherfooduser", "password123");
        userRepository.save(otherUser);
        FoodLog entry2 = new FoodLog(50.0, 700, LocalDateTime.now(), "Pasta");
        entry2.setUser(otherUser);
        foodLogRepository.save(entry2);

        List<FoodLog> userEntries = foodLogRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
        assertEquals("Rice", userEntries.get(0).getFoodItems());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<FoodLog> entries = foodLogRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }
}
