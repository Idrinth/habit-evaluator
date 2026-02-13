package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodTag;
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
class JpaFoodTagRepositoryTest {

    @Autowired
    private JpaFoodTagRepository foodTagRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("foodtaguser", "password123", "foodtaguser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        FoodTag saved = foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Banana", found.get().getName());
        assertEquals("banana", found.get().getNameLower());
    }

    @Test
    void testFindByUserId() {
        FoodTag tag1 = new FoodTag("Banana");
        tag1.setUser(testUser);
        foodTagRepository.save(tag1);

        FoodTag tag2 = new FoodTag("Apple");
        tag2.setUser(testUser);
        foodTagRepository.save(tag2);

        List<FoodTag> tags = foodTagRepository.findByUserId(testUser.getId());

        assertEquals(2, tags.size());
    }

    @Test
    void testFindByUserIdSortedByName() {
        FoodTag tagZ = new FoodTag("Zucchini");
        tagZ.setUser(testUser);
        foodTagRepository.save(tagZ);

        FoodTag tagA = new FoodTag("Apple");
        tagA.setUser(testUser);
        foodTagRepository.save(tagA);

        List<FoodTag> tags = foodTagRepository.findByUserId(testUser.getId());

        assertEquals(2, tags.size());
        assertEquals("Apple", tags.get(0).getName());
        assertEquals("Zucchini", tags.get(1).getName());
    }

    @Test
    void testFindByNameLowerAndUserId() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findByNameLowerAndUserId("banana", testUser.getId());

        assertTrue(found.isPresent());
        assertEquals("Banana", found.get().getName());
    }

    @Test
    void testFindByNameLowerAndUserIdNotFound() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findByNameLowerAndUserId("apple", testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        User otherUser = new User("otherfoodtaguser", "password", "otherfoodtag@example.com");
        otherUser = userRepository.save(otherUser);

        List<FoodTag> tags = foodTagRepository.findByUserId(otherUser.getId());
        assertTrue(tags.isEmpty());
    }

    @Test
    void testDeleteById() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        FoodTag saved = foodTagRepository.save(tag);

        foodTagRepository.deleteById(saved.getId());

        assertFalse(foodTagRepository.findById(saved.getId()).isPresent());
    }
}
