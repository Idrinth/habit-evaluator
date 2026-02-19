package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaReminderSettingsRepositoryTest {

    @Autowired
    private JpaReminderSettingsRepository reminderSettingsRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("reminderuser", "password123", "reminderuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindByUserId() {
        ReminderSettings settings = new ReminderSettings();
        settings.setUser(testUser);
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime(LocalTime.of(7, 30));
        reminderSettingsRepository.save(settings);

        Optional<ReminderSettings> found = reminderSettingsRepository.findByUserId(testUser.getId());

        assertTrue(found.isPresent());
        assertTrue(found.get().isSleepReminderEnabled());
        assertEquals(LocalTime.of(7, 30), found.get().getSleepReminderTime());
    }

    @Test
    void testFindByUserIdNotFound() {
        Optional<ReminderSettings> found = reminderSettingsRepository.findByUserId(testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testSaveWithAllSettings() {
        ReminderSettings settings = new ReminderSettings();
        settings.setUser(testUser);
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime(LocalTime.of(8, 0));
        settings.setDiaryReminderEnabled(true);
        settings.setDiaryReminderTime(LocalTime.of(21, 0));
        settings.setEmotionReminderEnabled(true);
        settings.setEmotionReminderCount(5);
        settings.setWakingHoursStart(LocalTime.of(6, 0));
        settings.setWakingHoursEnd(LocalTime.of(23, 0));
        reminderSettingsRepository.save(settings);

        Optional<ReminderSettings> found = reminderSettingsRepository.findByUserId(testUser.getId());

        assertTrue(found.isPresent());
        ReminderSettings result = found.get();
        assertTrue(result.isSleepReminderEnabled());
        assertTrue(result.isDiaryReminderEnabled());
        assertTrue(result.isEmotionReminderEnabled());
        assertEquals(5, result.getEmotionReminderCount());
        assertEquals(LocalTime.of(6, 0), result.getWakingHoursStart());
        assertEquals(LocalTime.of(23, 0), result.getWakingHoursEnd());
    }

    @Test
    void testFindByUserIdIsolation() {
        ReminderSettings settings = new ReminderSettings();
        settings.setUser(testUser);
        reminderSettingsRepository.save(settings);

        User otherUser = new User("otherreminderuser", "password", "otherreminder@example.com");
        otherUser = userRepository.save(otherUser);

        Optional<ReminderSettings> found = reminderSettingsRepository.findByUserId(otherUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        ReminderSettings settings = new ReminderSettings();
        settings.setUser(testUser);
        ReminderSettings saved = reminderSettingsRepository.save(settings);

        reminderSettingsRepository.deleteById(saved.getId());

        assertFalse(reminderSettingsRepository.findById(saved.getId()).isPresent());
    }
}
