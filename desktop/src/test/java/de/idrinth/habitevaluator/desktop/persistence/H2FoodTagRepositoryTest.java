package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2FoodTagRepositoryTest extends H2RepositoryTestBase {

    private H2FoodTagRepository foodTagRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        foodTagRepository = new H2FoodTagRepository();
        userRepository = new H2UserRepository();

        for (User user : userRepository.findAll()) {
            for (FoodTag tag : foodTagRepository.findByUserId(user.getId())) {
                foodTagRepository.deleteById(tag.getId());
            }
            userRepository.deleteById(user.getId());
        }

        testUser = new User("foodtaguser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        FoodTag tag = new FoodTag("Apple");
        tag.setUser(testUser);
        FoodTag saved = foodTagRepository.save(tag);

        assertNotNull(saved);
        Optional<FoodTag> found = foodTagRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Apple", found.get().getName());
        assertEquals("apple", found.get().getNameLower());
    }

    @Test
    void testSaveUpdatesExisting() {
        FoodTag tag = new FoodTag("Apple");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        tag.setName("Green Apple");
        foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findById(tag.getId());
        assertTrue(found.isPresent());
        assertEquals("Green Apple", found.get().getName());
        assertEquals("green apple", found.get().getNameLower());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<FoodTag> found = foodTagRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        FoodTag tag = new FoodTag("Banana");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        assertTrue(foodTagRepository.findById(tag.getId()).isPresent());
        foodTagRepository.deleteById(tag.getId());
        assertFalse(foodTagRepository.findById(tag.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> foodTagRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        FoodTag tag1 = new FoodTag("Apple");
        tag1.setUser(testUser);
        foodTagRepository.save(tag1);

        User otherUser = new User("othertaguser", "password123");
        userRepository.save(otherUser);
        FoodTag tag2 = new FoodTag("Banana");
        tag2.setUser(otherUser);
        foodTagRepository.save(tag2);

        List<FoodTag> userTags = foodTagRepository.findByUserId(testUser.getId());
        assertEquals(1, userTags.size());
        assertEquals("Apple", userTags.get(0).getName());
    }

    @Test
    void testFindByNameLowerAndUserId() {
        FoodTag tag = new FoodTag("Apple Juice");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findByNameLowerAndUserId("apple juice", testUser.getId());
        assertTrue(found.isPresent());
        assertEquals("Apple Juice", found.get().getName());
    }

    @Test
    void testFindByNameLowerAndUserIdNotFound() {
        Optional<FoodTag> found = foodTagRepository.findByNameLowerAndUserId("nonexistent", testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByNameLowerAndUserIdWrongUser() {
        FoodTag tag = new FoodTag("Apple");
        tag.setUser(testUser);
        foodTagRepository.save(tag);

        Optional<FoodTag> found = foodTagRepository.findByNameLowerAndUserId("apple", "wrong-user-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteEmptyTagsRemovesEmptyNames() {
        FoodTag emptyTag = new FoodTag();
        emptyTag.setName("");
        emptyTag.setUser(testUser);
        foodTagRepository.save(emptyTag);

        FoodTag validTag = new FoodTag("Apple");
        validTag.setUser(testUser);
        foodTagRepository.save(validTag);

        foodTagRepository.deleteEmptyTags(testUser.getId());

        assertFalse(foodTagRepository.findById(emptyTag.getId()).isPresent());
        assertTrue(foodTagRepository.findById(validTag.getId()).isPresent());
    }

    @Test
    void testDeleteEmptyTagsDoesNotAffectOtherUsers() {
        FoodTag emptyTag = new FoodTag();
        emptyTag.setName("");
        emptyTag.setUser(testUser);
        foodTagRepository.save(emptyTag);

        User otherUser = new User("otheremptyuser", "password123");
        userRepository.save(otherUser);
        FoodTag otherEmptyTag = new FoodTag();
        otherEmptyTag.setName("");
        otherEmptyTag.setUser(otherUser);
        foodTagRepository.save(otherEmptyTag);

        foodTagRepository.deleteEmptyTags(testUser.getId());

        assertFalse(foodTagRepository.findById(emptyTag.getId()).isPresent());
        assertTrue(foodTagRepository.findById(otherEmptyTag.getId()).isPresent());
    }
}
