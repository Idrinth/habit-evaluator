package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderSettingsControllerTest {

    private ReminderSettingsRepository reminderSettingsRepository;
    private UserRepository userRepository;
    private ReminderSettingsController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        reminderSettingsRepository = mock(ReminderSettingsRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new ReminderSettingsController(reminderSettingsRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetSettingsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<ReminderSettings> response = controller.getSettings(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSettingsReturnsExisting() {
        ReminderSettings existing = new ReminderSettings();
        existing.setSleepReminderEnabled(true);
        when(reminderSettingsRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<ReminderSettings> response = controller.getSettings(session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSleepReminderEnabled());
    }

    @Test
    void testGetSettingsReturnsDefaultWhenNoneExist() {
        when(reminderSettingsRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        ResponseEntity<ReminderSettings> response = controller.getSettings(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSleepReminderEnabled());
    }

    @Test
    void testUpdateSettingsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<ReminderSettings> response =
                controller.updateSettings(new ReminderSettings(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateSettingsUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<ReminderSettings> response =
                controller.updateSettings(new ReminderSettings(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateSettingsCreatesNewWhenNoneExist() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(reminderSettingsRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(reminderSettingsRepository.save(any(ReminderSettings.class))).thenAnswer(i -> i.getArgument(0));

        ReminderSettings incoming = new ReminderSettings();
        incoming.setSleepReminderEnabled(true);
        incoming.setSleepReminderTime(LocalTime.of(9, 0));
        incoming.setEmotionReminderCount(5);

        ResponseEntity<ReminderSettings> response = controller.updateSettings(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSleepReminderEnabled());
        assertEquals(LocalTime.of(9, 0), response.getBody().getSleepReminderTime());
        assertEquals(5, response.getBody().getEmotionReminderCount());
        assertEquals(testUser, response.getBody().getUser());
    }

    @Test
    void testUpdateSettingsUpdatesExisting() {
        ReminderSettings existing = new ReminderSettings();
        existing.setUser(testUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(reminderSettingsRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existing));
        when(reminderSettingsRepository.save(any(ReminderSettings.class))).thenAnswer(i -> i.getArgument(0));

        ReminderSettings incoming = new ReminderSettings();
        incoming.setDiaryReminderEnabled(true);
        incoming.setDiaryReminderTime(LocalTime.of(21, 30));
        incoming.setWakingHoursStart(LocalTime.of(6, 0));
        incoming.setWakingHoursEnd(LocalTime.of(23, 0));

        ResponseEntity<ReminderSettings> response = controller.updateSettings(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isDiaryReminderEnabled());
        assertEquals(LocalTime.of(21, 30), response.getBody().getDiaryReminderTime());
        assertEquals(LocalTime.of(6, 0), response.getBody().getWakingHoursStart());
        assertEquals(LocalTime.of(23, 0), response.getBody().getWakingHoursEnd());
        verify(reminderSettingsRepository).save(existing);
    }
}
