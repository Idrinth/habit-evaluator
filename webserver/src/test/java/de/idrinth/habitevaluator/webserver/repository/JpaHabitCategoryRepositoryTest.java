package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
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
class JpaHabitCategoryRepositoryTest {

    @Autowired
    private JpaHabitCategoryRepository categoryRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("catuser", "password123", "catuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        HabitCategory category = new HabitCategory("Health", "Health related habits", "#00FF00");
        category.setUser(testUser);
        HabitCategory saved = categoryRepository.save(category);

        Optional<HabitCategory> found = categoryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
        assertEquals("Health related habits", found.get().getDescription());
        assertEquals("#00FF00", found.get().getColor());
    }

    @Test
    void testFindByUserId() {
        HabitCategory cat1 = new HabitCategory("Health");
        cat1.setUser(testUser);
        categoryRepository.save(cat1);

        HabitCategory cat2 = new HabitCategory("Fitness");
        cat2.setUser(testUser);
        categoryRepository.save(cat2);

        List<HabitCategory> categories = categoryRepository.findByUserId(testUser.getId());

        assertEquals(2, categories.size());
    }

    @Test
    void testFindByUserIdSortedByName() {
        HabitCategory catZ = new HabitCategory("Zzz Sleep");
        catZ.setUser(testUser);
        categoryRepository.save(catZ);

        HabitCategory catA = new HabitCategory("Abs Training");
        catA.setUser(testUser);
        categoryRepository.save(catA);

        HabitCategory catM = new HabitCategory("Meditation");
        catM.setUser(testUser);
        categoryRepository.save(catM);

        List<HabitCategory> categories = categoryRepository.findByUserId(testUser.getId());

        assertEquals(3, categories.size());
        assertEquals("Abs Training", categories.get(0).getName());
        assertEquals("Meditation", categories.get(1).getName());
        assertEquals("Zzz Sleep", categories.get(2).getName());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        HabitCategory category = new HabitCategory("Health");
        category.setUser(testUser);
        categoryRepository.save(category);

        User otherUser = new User("othercatuser", "password", "othercat@example.com");
        otherUser = userRepository.save(otherUser);

        List<HabitCategory> categories = categoryRepository.findByUserId(otherUser.getId());
        assertTrue(categories.isEmpty());
    }

    @Test
    void testDeleteById() {
        HabitCategory category = new HabitCategory("Health");
        category.setUser(testUser);
        HabitCategory saved = categoryRepository.save(category);

        categoryRepository.deleteById(saved.getId());

        assertFalse(categoryRepository.findById(saved.getId()).isPresent());
    }
}
