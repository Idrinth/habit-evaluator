package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.api.SyncData;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SyncControllerTest {

    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private StatsCacheService statsCacheService;
    private SyncController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = mock(HabitRepository.class);
        userRepository = mock(UserRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new SyncController(habitRepository, userRepository, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testSyncUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<SyncData> response = controller.sync(new SyncData(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testSyncUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<SyncData> response = controller.sync(new SyncData(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testSyncWithEmptyData() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        SyncData incoming = new SyncData(new ArrayList<>());
        ResponseEntity<SyncData> response = controller.sync(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testSyncClientOnlyHabitCreatedOnServer() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(habitRepository.findByUserId(testUser.getId()))
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(new Habit("ClientHabit", "desc")));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        Habit clientHabit = new Habit("ClientHabit", "desc");
        SyncData incoming = new SyncData(List.of(clientHabit));
        ResponseEntity<SyncData> response = controller.sync(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void testSyncMergesEntriesForExistingHabit() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        Habit serverHabit = new Habit("SharedHabit", "desc");
        serverHabit.setUser(testUser);
        HabitEntry serverEntry = new HabitEntry();
        serverHabit.addEntry(serverEntry);

        when(habitRepository.findByUserId(testUser.getId()))
                .thenReturn(List.of(serverHabit))
                .thenReturn(List.of(serverHabit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        Habit clientHabit = new Habit("SharedHabit", "desc");
        HabitEntry clientEntry = new HabitEntry();
        clientHabit.addEntry(clientEntry);

        SyncData incoming = new SyncData(List.of(clientHabit));
        ResponseEntity<SyncData> response = controller.sync(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        verify(habitRepository).save(serverHabit);
        assertEquals(2, serverHabit.getEntries().size());
    }

    @Test
    void testSyncDoesNotDuplicateExistingEntries() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        Habit serverHabit = new Habit("SharedHabit", "desc");
        serverHabit.setUser(testUser);
        HabitEntry sharedEntry = new HabitEntry();
        serverHabit.addEntry(sharedEntry);

        when(habitRepository.findByUserId(testUser.getId()))
                .thenReturn(List.of(serverHabit))
                .thenReturn(List.of(serverHabit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        Habit clientHabit = new Habit("SharedHabit", "desc");
        HabitEntry clientEntry = new HabitEntry();
        // Use the same ID to simulate already-existing entry
        clientEntry = sharedEntry;
        clientHabit.addEntry(clientEntry);

        SyncData incoming = new SyncData(List.of(clientHabit));
        ResponseEntity<SyncData> response = controller.sync(incoming, session);

        assertEquals(200, response.getStatusCode().value());
        // Entry should not be duplicated
        assertEquals(1, serverHabit.getEntries().size());
    }
}
