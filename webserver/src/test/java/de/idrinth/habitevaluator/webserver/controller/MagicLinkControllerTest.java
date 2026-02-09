package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.MagicLink;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.MagicLinkRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MagicLinkControllerTest {

    private MagicLinkRepository magicLinkRepository;
    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private MagicLinkController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        magicLinkRepository = mock(MagicLinkRepository.class);
        habitRepository = mock(HabitRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new MagicLinkController(magicLinkRepository, habitRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testCreateMagicLinkSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(magicLinkRepository.save(any(MagicLink.class))).thenAnswer(i -> i.getArgument(0));

        MagicLinkController.MagicLinkRequest request = new MagicLinkController.MagicLinkRequest();
        request.categoryId = "cat-1";
        request.filterStart = LocalDate.of(2025, 1, 1);
        request.filterEnd = LocalDate.of(2025, 12, 31);

        ResponseEntity<MagicLink> response = controller.createMagicLink(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testCreateMagicLinkUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<MagicLink> response = controller.createMagicLink(new MagicLinkController.MagicLinkRequest(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testListMagicLinks() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        when(magicLinkRepository.findByUserId(testUser.getId())).thenReturn(List.of(link));

        ResponseEntity<List<MagicLink>> response = controller.listMagicLinks(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testListMagicLinksUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<MagicLink>> response = controller.listMagicLinks(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteMagicLinkSuccess() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        when(magicLinkRepository.findById(link.getId())).thenReturn(Optional.of(link));

        ResponseEntity<Void> response = controller.deleteMagicLink(link.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(magicLinkRepository).deleteById(link.getId());
    }

    @Test
    void testDeleteMagicLinkNotFound() {
        when(magicLinkRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteMagicLink("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteMagicLinkNotOwned() {
        User otherUser = new User("other", "pass");
        MagicLink link = new MagicLink();
        link.setUser(otherUser);
        when(magicLinkRepository.findById(link.getId())).thenReturn(Optional.of(link));

        ResponseEntity<Void> response = controller.deleteMagicLink(link.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetSharedDataSuccess() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        when(magicLinkRepository.findByToken(link.getToken())).thenReturn(Optional.of(link));

        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(habit));

        ResponseEntity<Map<String, Object>> response = controller.getSharedData(link.getToken());

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("habits"));
    }

    @Test
    void testGetSharedDataTokenNotFound() {
        when(magicLinkRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.getSharedData("invalid-token");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetSharedDataExpiredLink() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        link.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(magicLinkRepository.findByToken(link.getToken())).thenReturn(Optional.of(link));

        ResponseEntity<Map<String, Object>> response = controller.getSharedData(link.getToken());
        assertEquals(410, response.getStatusCode().value());
    }

    @Test
    void testGetSharedDataNullUser() {
        MagicLink link = new MagicLink();
        // Deliberately do not set a user on the link
        when(magicLinkRepository.findByToken(link.getToken())).thenReturn(Optional.of(link));

        ResponseEntity<Map<String, Object>> response = controller.getSharedData(link.getToken());
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetSharedDataFiltersByCategory() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        link.setCategoryId("cat-1");
        when(magicLinkRepository.findByToken(link.getToken())).thenReturn(Optional.of(link));

        Habit matchingHabit = new Habit("Exercise", "desc");
        matchingHabit.setCategoryId("cat-1");
        Habit nonMatchingHabit = new Habit("Reading", "desc");
        nonMatchingHabit.setCategoryId("cat-2");
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(matchingHabit, nonMatchingHabit));

        ResponseEntity<Map<String, Object>> response = controller.getSharedData(link.getToken());

        assertEquals(200, response.getStatusCode().value());
        List<?> habits = (List<?>) response.getBody().get("habits");
        assertEquals(1, habits.size());
    }

    @Test
    void testGetSharedDataFiltersByDateRange() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        link.setFilterStart(LocalDate.of(2025, 6, 1));
        link.setFilterEnd(LocalDate.of(2025, 6, 30));
        when(magicLinkRepository.findByToken(link.getToken())).thenReturn(Optional.of(link));

        Habit habit = new Habit("Exercise", "desc");
        HabitEntry inRangeEntry = new HabitEntry();
        inRangeEntry.setCompletedAt(LocalDateTime.of(2025, 6, 15, 10, 0));
        habit.addEntry(inRangeEntry);
        HabitEntry outOfRangeEntry = new HabitEntry();
        outOfRangeEntry.setCompletedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        habit.addEntry(outOfRangeEntry);
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(habit));

        ResponseEntity<Map<String, Object>> response = controller.getSharedData(link.getToken());

        assertEquals(200, response.getStatusCode().value());
        List<?> habits = (List<?>) response.getBody().get("habits");
        Map<?, ?> habitMap = (Map<?, ?>) habits.get(0);
        List<?> entries = (List<?>) habitMap.get("entries");
        assertEquals(1, entries.size());
    }
}
