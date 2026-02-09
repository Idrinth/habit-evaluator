package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2DiaryReferenceRepositoryTest extends H2RepositoryTestBase {

    private H2DiaryReferenceRepository referenceRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        referenceRepository = new H2DiaryReferenceRepository();
        userRepository = new H2UserRepository();

        for (DiaryReference ref : referenceRepository.findAll()) {
            referenceRepository.deleteById(ref.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("refuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        DiaryReference ref = new DiaryReference("Morning jog");
        ref.setUser(testUser);
        DiaryReference saved = referenceRepository.save(ref);

        assertNotNull(saved);
        Optional<DiaryReference> found = referenceRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Morning jog", found.get().getDescription());
        assertEquals("morning jog", found.get().getDescriptionLower());
    }

    @Test
    void testSaveUpdatesExisting() {
        DiaryReference ref = new DiaryReference("Original");
        ref.setUser(testUser);
        referenceRepository.save(ref);

        ref.setDescription("Updated");
        referenceRepository.save(ref);

        Optional<DiaryReference> found = referenceRepository.findById(ref.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<DiaryReference> found = referenceRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        DiaryReference ref1 = new DiaryReference("Ref 1");
        ref1.setUser(testUser);
        DiaryReference ref2 = new DiaryReference("Ref 2");
        ref2.setUser(testUser);
        referenceRepository.save(ref1);
        referenceRepository.save(ref2);

        List<DiaryReference> all = referenceRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        DiaryReference ref = new DiaryReference("To delete");
        ref.setUser(testUser);
        referenceRepository.save(ref);

        assertTrue(referenceRepository.findById(ref.getId()).isPresent());
        referenceRepository.deleteById(ref.getId());
        assertFalse(referenceRepository.findById(ref.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> referenceRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        DiaryReference ref1 = new DiaryReference("Ref 1");
        ref1.setUser(testUser);
        referenceRepository.save(ref1);

        User otherUser = new User("otherrefuser", "password123");
        userRepository.save(otherUser);
        DiaryReference ref2 = new DiaryReference("Ref 2");
        ref2.setUser(otherUser);
        referenceRepository.save(ref2);

        List<DiaryReference> userRefs = referenceRepository.findByUserId(testUser.getId());
        assertEquals(1, userRefs.size());
        assertEquals("Ref 1", userRefs.get(0).getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCase() {
        DiaryReference ref = new DiaryReference("Morning Jog");
        ref.setUser(testUser);
        referenceRepository.save(ref);

        Optional<DiaryReference> found = referenceRepository.findByUserIdAndDescriptionIgnoreCase(
                testUser.getId(), "morning jog");
        assertTrue(found.isPresent());
        assertEquals("Morning Jog", found.get().getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        Optional<DiaryReference> found = referenceRepository.findByUserIdAndDescriptionIgnoreCase(
                testUser.getId(), "nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseWithNull() {
        Optional<DiaryReference> found = referenceRepository.findByUserIdAndDescriptionIgnoreCase(
                testUser.getId(), null);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindDistinctDescriptionsByUserId() {
        DiaryReference ref1 = new DiaryReference("Alpha");
        ref1.setUser(testUser);
        DiaryReference ref2 = new DiaryReference("Beta");
        ref2.setUser(testUser);
        referenceRepository.save(ref1);
        referenceRepository.save(ref2);

        List<String> descriptions = referenceRepository.findDistinctDescriptionsByUserId(testUser.getId());
        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Alpha"));
        assertTrue(descriptions.contains("Beta"));
    }

    @Test
    void testFindDistinctDescriptionsByUserIdReturnsEmptyForNonExistentUser() {
        List<String> descriptions = referenceRepository.findDistinctDescriptionsByUserId("nonexistent-user-id");
        assertTrue(descriptions.isEmpty());
    }
}
