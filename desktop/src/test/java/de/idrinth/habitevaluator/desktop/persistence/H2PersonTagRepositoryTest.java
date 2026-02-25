package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PersonTag;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2PersonTagRepositoryTest extends H2RepositoryTestBase {

    private H2PersonTagRepository personTagRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        personTagRepository = new H2PersonTagRepository();
        userRepository = new H2UserRepository();

        for (User user : userRepository.findAll()) {
            for (PersonTag tag : personTagRepository.findByUserId(user.getId())) {
                personTagRepository.deleteById(tag.getId());
            }
            userRepository.deleteById(user.getId());
        }

        testUser = new User("persontaguser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        PersonTag tag = new PersonTag("Alice");
        tag.setUser(testUser);
        PersonTag saved = personTagRepository.save(tag);

        assertNotNull(saved);
        Optional<PersonTag> found = personTagRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
        assertEquals("alice", found.get().getNameLower());
    }

    @Test
    void testSaveUpdatesExisting() {
        PersonTag tag = new PersonTag("Alice");
        tag.setUser(testUser);
        personTagRepository.save(tag);

        tag.setName("Alice Smith");
        personTagRepository.save(tag);

        Optional<PersonTag> found = personTagRepository.findById(tag.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getName());
        assertEquals("alice smith", found.get().getNameLower());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<PersonTag> found = personTagRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        PersonTag tag = new PersonTag("Bob");
        tag.setUser(testUser);
        personTagRepository.save(tag);

        assertTrue(personTagRepository.findById(tag.getId()).isPresent());
        personTagRepository.deleteById(tag.getId());
        assertFalse(personTagRepository.findById(tag.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> personTagRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        PersonTag tag1 = new PersonTag("Alice");
        tag1.setUser(testUser);
        personTagRepository.save(tag1);

        User otherUser = new User("othertaguser", "password123");
        userRepository.save(otherUser);
        PersonTag tag2 = new PersonTag("Bob");
        tag2.setUser(otherUser);
        personTagRepository.save(tag2);

        List<PersonTag> userTags = personTagRepository.findByUserId(testUser.getId());
        assertEquals(1, userTags.size());
        assertEquals("Alice", userTags.get(0).getName());
    }

    @Test
    void testFindByNameLowerAndUserId() {
        PersonTag tag = new PersonTag("Alice Smith");
        tag.setUser(testUser);
        personTagRepository.save(tag);

        Optional<PersonTag> found = personTagRepository.findByNameLowerAndUserId("alice smith", testUser.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getName());
    }

    @Test
    void testFindByNameLowerAndUserIdNotFound() {
        Optional<PersonTag> found = personTagRepository.findByNameLowerAndUserId("nonexistent", testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByNameLowerAndUserIdWrongUser() {
        PersonTag tag = new PersonTag("Alice");
        tag.setUser(testUser);
        personTagRepository.save(tag);

        Optional<PersonTag> found = personTagRepository.findByNameLowerAndUserId("alice", "wrong-user-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteEmptyTagsRemovesEmptyNames() {
        PersonTag emptyTag = new PersonTag();
        emptyTag.setName("");
        emptyTag.setUser(testUser);
        personTagRepository.save(emptyTag);

        PersonTag validTag = new PersonTag("Alice");
        validTag.setUser(testUser);
        personTagRepository.save(validTag);

        personTagRepository.deleteEmptyTags(testUser.getId());

        assertFalse(personTagRepository.findById(emptyTag.getId()).isPresent());
        assertTrue(personTagRepository.findById(validTag.getId()).isPresent());
    }

    @Test
    void testDeleteEmptyTagsDoesNotAffectOtherUsers() {
        PersonTag emptyTag = new PersonTag();
        emptyTag.setName("");
        emptyTag.setUser(testUser);
        personTagRepository.save(emptyTag);

        User otherUser = new User("otheremptyuser", "password123");
        userRepository.save(otherUser);
        PersonTag otherEmptyTag = new PersonTag();
        otherEmptyTag.setName("");
        otherEmptyTag.setUser(otherUser);
        personTagRepository.save(otherEmptyTag);

        personTagRepository.deleteEmptyTags(testUser.getId());

        assertFalse(personTagRepository.findById(emptyTag.getId()).isPresent());
        assertTrue(personTagRepository.findById(otherEmptyTag.getId()).isPresent());
    }
}
