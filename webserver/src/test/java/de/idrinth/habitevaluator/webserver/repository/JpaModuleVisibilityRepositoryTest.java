package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaModuleVisibilityRepositoryTest {

    @Autowired
    private JpaModuleVisibilityRepository moduleVisibilityRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("visuser", "password123", "visuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindByUserId() {
        ModuleVisibility visibility = new ModuleVisibility();
        visibility.setUser(testUser);
        visibility.setDiaryVisible(false);
        visibility.setSleepVisible(false);
        moduleVisibilityRepository.save(visibility);

        Optional<ModuleVisibility> found = moduleVisibilityRepository.findByUserId(testUser.getId());

        assertTrue(found.isPresent());
        assertFalse(found.get().isDiaryVisible());
        assertFalse(found.get().isSleepVisible());
        assertTrue(found.get().isEmotionsVisible());
        assertTrue(found.get().isPointsVisible());
    }

    @Test
    void testDefaultValuesAllVisible() {
        ModuleVisibility visibility = new ModuleVisibility();
        visibility.setUser(testUser);
        moduleVisibilityRepository.save(visibility);

        Optional<ModuleVisibility> found = moduleVisibilityRepository.findByUserId(testUser.getId());

        assertTrue(found.isPresent());
        ModuleVisibility result = found.get();
        assertTrue(result.isDiaryVisible());
        assertTrue(result.isSleepVisible());
        assertTrue(result.isEmotionsVisible());
        assertTrue(result.isPointsVisible());
        assertTrue(result.isStatisticsVisible());
        assertTrue(result.isFoodLogVisible());
        assertTrue(result.isSportLogVisible());
        assertTrue(result.isMedicationVisible());
        assertTrue(result.isBackupVisible());
        assertTrue(result.isPdfExportVisible());
    }

    @Test
    void testFindByUserIdNotFound() {
        Optional<ModuleVisibility> found = moduleVisibilityRepository.findByUserId(testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserIdIsolation() {
        ModuleVisibility visibility = new ModuleVisibility();
        visibility.setUser(testUser);
        moduleVisibilityRepository.save(visibility);

        User otherUser = new User("othervisuser", "password", "othervis@example.com");
        otherUser = userRepository.save(otherUser);

        Optional<ModuleVisibility> found = moduleVisibilityRepository.findByUserId(otherUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        ModuleVisibility visibility = new ModuleVisibility();
        visibility.setUser(testUser);
        ModuleVisibility saved = moduleVisibilityRepository.save(visibility);

        moduleVisibilityRepository.deleteById(saved.getId());

        assertFalse(moduleVisibilityRepository.findById(saved.getId()).isPresent());
    }
}
