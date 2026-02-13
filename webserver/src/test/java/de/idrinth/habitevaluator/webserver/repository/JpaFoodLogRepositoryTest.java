package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaFoodLogRepositoryTest {

    @Autowired
    private JpaFoodLogRepository foodLogRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("foodloguser", "password123", "foodloguser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        FoodLog log = new FoodLog(45.0, 350, LocalDateTime.of(2025, 1, 15, 12, 0), "Rice, Chicken");
        log.setUser(testUser);
        FoodLog saved = foodLogRepository.save(log);

        Optional<FoodLog> found = foodLogRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(45.0, found.get().getCarbohydrates());
        assertEquals(350, found.get().getKcal());
        assertEquals("Rice, Chicken", found.get().getFoodItems());
    }

    @Test
    void testFindByUserId() {
        FoodLog log1 = new FoodLog(30.0, 200, LocalDateTime.of(2025, 1, 15, 8, 0), "Oatmeal");
        log1.setUser(testUser);
        foodLogRepository.save(log1);

        FoodLog log2 = new FoodLog(50.0, 400, LocalDateTime.of(2025, 1, 15, 12, 0), "Pasta");
        log2.setUser(testUser);
        foodLogRepository.save(log2);

        List<FoodLog> logs = foodLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
    }

    @Test
    void testFindByUserIdOrderedByDateTimeDesc() {
        FoodLog older = new FoodLog(30.0, 200, LocalDateTime.of(2025, 1, 10, 8, 0), "Oatmeal");
        older.setUser(testUser);
        foodLogRepository.save(older);

        FoodLog newer = new FoodLog(50.0, 400, LocalDateTime.of(2025, 1, 20, 12, 0), "Pasta");
        newer.setUser(testUser);
        foodLogRepository.save(newer);

        List<FoodLog> logs = foodLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
        assertEquals(LocalDateTime.of(2025, 1, 20, 12, 0), logs.get(0).getDateTime());
        assertEquals(LocalDateTime.of(2025, 1, 10, 8, 0), logs.get(1).getDateTime());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        FoodLog log = new FoodLog(30.0, 200, LocalDateTime.now(), "Oatmeal");
        log.setUser(testUser);
        foodLogRepository.save(log);

        User otherUser = new User("otherfoodloguser", "password", "otherfoodlog@example.com");
        otherUser = userRepository.save(otherUser);

        List<FoodLog> logs = foodLogRepository.findByUserId(otherUser.getId());
        assertTrue(logs.isEmpty());
    }

    @Test
    void testDeleteById() {
        FoodLog log = new FoodLog(30.0, 200, LocalDateTime.now(), "Oatmeal");
        log.setUser(testUser);
        FoodLog saved = foodLogRepository.save(log);

        foodLogRepository.deleteById(saved.getId());

        assertFalse(foodLogRepository.findById(saved.getId()).isPresent());
    }
}
