package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleVisibilityControllerTest {

    private ModuleVisibilityRepository moduleVisibilityRepository;
    private UserRepository userRepository;
    private ModuleVisibilityController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        moduleVisibilityRepository = mock(ModuleVisibilityRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new ModuleVisibilityController(moduleVisibilityRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetSettingsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<ModuleVisibility> response = controller.getSettings(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSettingsReturnsExisting() {
        ModuleVisibility existing = new ModuleVisibility();
        existing.setDiaryVisible(false);
        existing.setSleepVisible(false);
        when(moduleVisibilityRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<ModuleVisibility> response = controller.getSettings(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isDiaryVisible());
        assertFalse(response.getBody().isSleepVisible());
    }

    @Test
    void testGetSettingsReturnsDefaultWhenNoneExist() {
        when(moduleVisibilityRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        ResponseEntity<ModuleVisibility> response = controller.getSettings(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isDiaryVisible());
        assertTrue(response.getBody().isSleepVisible());
        assertTrue(response.getBody().isEmotionsVisible());
        assertTrue(response.getBody().isPointsVisible());
        assertTrue(response.getBody().isStatisticsVisible());
        assertTrue(response.getBody().isFoodLogVisible());
        assertTrue(response.getBody().isSportLogVisible());
        assertTrue(response.getBody().isMedicationVisible());
        assertTrue(response.getBody().isBackupVisible());
        assertTrue(response.getBody().isPdfExportVisible());
    }

    @Test
    void testUpdateSettingsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<ModuleVisibility> response =
                controller.updateSettings(new ModuleVisibility(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateSettingsUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<ModuleVisibility> response =
                controller.updateSettings(new ModuleVisibility(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateSettingsCreatesNewWhenNoneExist() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(moduleVisibilityRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(moduleVisibilityRepository.save(any(ModuleVisibility.class))).thenAnswer(i -> i.getArgument(0));

        ModuleVisibility incoming = new ModuleVisibility();
        incoming.setDiaryVisible(false);
        incoming.setSleepVisible(false);
        incoming.setEmotionsVisible(true);
        incoming.setPointsVisible(true);
        incoming.setStatisticsVisible(false);
        incoming.setFoodLogVisible(true);
        incoming.setSportLogVisible(false);
        incoming.setMedicationVisible(true);
        incoming.setBackupVisible(false);
        incoming.setPdfExportVisible(true);

        ResponseEntity<ModuleVisibility> response = controller.updateSettings(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        ModuleVisibility body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isDiaryVisible());
        assertFalse(body.isSleepVisible());
        assertTrue(body.isEmotionsVisible());
        assertTrue(body.isPointsVisible());
        assertFalse(body.isStatisticsVisible());
        assertTrue(body.isFoodLogVisible());
        assertFalse(body.isSportLogVisible());
        assertTrue(body.isMedicationVisible());
        assertFalse(body.isBackupVisible());
        assertTrue(body.isPdfExportVisible());
        assertEquals(testUser, body.getUser());
    }

    @Test
    void testUpdateSettingsUpdatesExisting() {
        ModuleVisibility existing = new ModuleVisibility();
        existing.setUser(testUser);
        existing.setDiaryVisible(true);
        existing.setSleepVisible(true);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(moduleVisibilityRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existing));
        when(moduleVisibilityRepository.save(any(ModuleVisibility.class))).thenAnswer(i -> i.getArgument(0));

        ModuleVisibility incoming = new ModuleVisibility();
        incoming.setDiaryVisible(false);
        incoming.setSleepVisible(false);
        incoming.setEmotionsVisible(false);
        incoming.setPointsVisible(false);
        incoming.setStatisticsVisible(false);
        incoming.setFoodLogVisible(false);
        incoming.setSportLogVisible(false);
        incoming.setMedicationVisible(false);
        incoming.setBackupVisible(false);
        incoming.setPdfExportVisible(false);

        ResponseEntity<ModuleVisibility> response = controller.updateSettings(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        ModuleVisibility body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isDiaryVisible());
        assertFalse(body.isSleepVisible());
        assertFalse(body.isEmotionsVisible());
        assertFalse(body.isPointsVisible());
        assertFalse(body.isStatisticsVisible());
        assertFalse(body.isFoodLogVisible());
        assertFalse(body.isSportLogVisible());
        assertFalse(body.isMedicationVisible());
        assertFalse(body.isBackupVisible());
        assertFalse(body.isPdfExportVisible());
        verify(moduleVisibilityRepository).save(existing);
    }
}
