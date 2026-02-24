package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2SlotConfirmationRepositoryTest extends H2RepositoryTestBase {

    private H2SlotConfirmationRepository confirmationRepository;
    private H2WeekPlannerSlotRepository slotRepository;
    private H2PlannerActivityRepository activityRepository;
    private H2PlannerGroupRepository groupRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        confirmationRepository = new H2SlotConfirmationRepository();
        slotRepository = new H2WeekPlannerSlotRepository();
        activityRepository = new H2PlannerActivityRepository();
        groupRepository = new H2PlannerGroupRepository();
        userRepository = new H2UserRepository();

        for (SlotConfirmation c : confirmationRepository.findAll()) {
            confirmationRepository.deleteById(c.getId());
        }
        for (WeekPlannerSlot slot : slotRepository.findAll()) {
            slotRepository.deleteById(slot.getId());
        }
        for (PlannerActivity activity : activityRepository.findAll()) {
            activityRepository.deleteById(activity.getId());
        }
        for (PlannerGroup group : groupRepository.findAll()) {
            groupRepository.deleteById(group.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("confirmationuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setDate(LocalDate.now());
        confirmation.setUser(testUser);
        SlotConfirmation saved = confirmationRepository.save(confirmation);

        assertNotNull(saved);
        Optional<SlotConfirmation> found = confirmationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isConfirmed());
    }

    @Test
    void testSaveUpdatesExisting() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(false);
        confirmation.setUser(testUser);
        confirmationRepository.save(confirmation);

        confirmation.setConfirmed(true);
        confirmationRepository.save(confirmation);

        Optional<SlotConfirmation> found = confirmationRepository.findById(confirmation.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isConfirmed());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<SlotConfirmation> found = confirmationRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        SlotConfirmation c1 = new SlotConfirmation();
        c1.setConfirmed(true);
        c1.setUser(testUser);
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setConfirmed(false);
        c2.setUser(testUser);
        confirmationRepository.save(c1);
        confirmationRepository.save(c2);

        List<SlotConfirmation> all = confirmationRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setUser(testUser);
        confirmationRepository.save(confirmation);

        assertTrue(confirmationRepository.findById(confirmation.getId()).isPresent());
        confirmationRepository.deleteById(confirmation.getId());
        assertFalse(confirmationRepository.findById(confirmation.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> confirmationRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setUser(testUser);
        confirmationRepository.save(confirmation);

        assertTrue(confirmationRepository.existsById(confirmation.getId()));
        assertFalse(confirmationRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        SlotConfirmation c1 = new SlotConfirmation();
        c1.setConfirmed(true);
        c1.setUser(testUser);
        confirmationRepository.save(c1);

        User otherUser = new User("otherconfirmationuser", "password123");
        userRepository.save(otherUser);
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setConfirmed(false);
        c2.setUser(otherUser);
        confirmationRepository.save(c2);

        List<SlotConfirmation> userConfirmations = confirmationRepository.findByUserId(testUser.getId());
        assertEquals(1, userConfirmations.size());
        assertTrue(userConfirmations.get(0).isConfirmed());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<SlotConfirmation> confirmations = confirmationRepository.findByUserId("nonexistent-user-id");
        assertTrue(confirmations.isEmpty());
    }
}
