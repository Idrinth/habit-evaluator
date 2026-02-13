package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemDiaryReferenceRepositoryTest {

    private File tempDir;
    private FileSystemDiaryReferenceRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "diary-ref-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemDiaryReferenceRepository(tempDir);
    }

    @AfterEach
    void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    void testSaveAndFindById() {
        DiaryReference ref = new DiaryReference("Morning Jog");
        repository.save(ref);

        Optional<DiaryReference> found = repository.findById(ref.getId());
        assertTrue(found.isPresent());
        assertEquals("Morning Jog", found.get().getDescription());
        assertEquals("morning jog", found.get().getDescriptionLower());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new DiaryReference("Ref 1"));
        repository.save(new DiaryReference("Ref 2"));
        repository.save(new DiaryReference("Ref 3"));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        DiaryReference ref = new DiaryReference("To delete");
        repository.save(ref);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(ref.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        DiaryReference ref1 = new DiaryReference("User ref");
        ref1.setUser(user);
        DiaryReference ref2 = new DiaryReference("No user ref");

        repository.save(ref1);
        repository.save(ref2);

        List<DiaryReference> userRefs = repository.findByUserId(user.getId());
        assertEquals(1, userRefs.size());
        assertEquals("User ref", userRefs.get(0).getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCase() {
        User user = new User("testuser", "password");
        DiaryReference ref = new DiaryReference("Morning Jog");
        ref.setUser(user);
        repository.save(ref);

        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase(
                user.getId(), "morning jog");
        assertTrue(found.isPresent());
        assertEquals("Morning Jog", found.get().getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseUpperCase() {
        User user = new User("testuser", "password");
        DiaryReference ref = new DiaryReference("Morning Jog");
        ref.setUser(user);
        repository.save(ref);

        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase(
                user.getId(), "MORNING JOG");
        assertTrue(found.isPresent());
        assertEquals("Morning Jog", found.get().getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        User user = new User("testuser", "password");
        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase(
                user.getId(), "nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseWithNull() {
        User user = new User("testuser", "password");
        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase(
                user.getId(), null);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindDistinctDescriptionsByUserId() {
        User user = new User("testuser", "password");
        DiaryReference ref1 = new DiaryReference("Alpha");
        ref1.setUser(user);
        DiaryReference ref2 = new DiaryReference("Beta");
        ref2.setUser(user);
        DiaryReference ref3 = new DiaryReference("Alpha");
        ref3.setUser(user);

        repository.save(ref1);
        repository.save(ref2);
        // Save ref3 would overwrite ref1 since save uses put by id
        // So we only have 2 unique references, but both have unique descriptions

        List<String> descriptions = repository.findDistinctDescriptionsByUserId(user.getId());
        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Alpha"));
        assertTrue(descriptions.contains("Beta"));
    }

    @Test
    void testPersistenceAcrossInstances() {
        DiaryReference ref = new DiaryReference("Persistent ref");
        User user = new User("testuser", "password");
        ref.setUser(user);
        repository.save(ref);

        FileSystemDiaryReferenceRepository newRepo = new FileSystemDiaryReferenceRepository(tempDir);
        Optional<DiaryReference> found = newRepo.findById(ref.getId());
        assertTrue(found.isPresent());
        assertEquals("Persistent ref", found.get().getDescription());
        assertEquals("persistent ref", found.get().getDescriptionLower());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testUpdateExistingReference() {
        DiaryReference ref = new DiaryReference("Original");
        repository.save(ref);

        ref.setDescription("Updated");
        repository.save(ref);

        List<DiaryReference> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("Updated", all.get(0).getDescription());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseDoesNotMatchDifferentUser() {
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");

        DiaryReference ref = new DiaryReference("Shared");
        ref.setUser(user1);
        repository.save(ref);

        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase(
                user2.getId(), "Shared");
        assertFalse(found.isPresent());
    }
}
