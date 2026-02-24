package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemSlotConfirmationRepositoryTest {

    private File tempDir;
    private FileSystemPlannerGroupRepository groupRepository;
    private FileSystemPlannerActivityRepository activityRepository;
    private FileSystemWeekPlannerSlotRepository slotRepository;
    private FileSystemSlotConfirmationRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "slot-confirmation-test-" + System.nanoTime());
        tempDir.mkdirs();
        groupRepository = new FileSystemPlannerGroupRepository(tempDir);
        activityRepository = new FileSystemPlannerActivityRepository(tempDir);
        activityRepository.setPlannerGroupRepository(groupRepository);
        slotRepository = new FileSystemWeekPlannerSlotRepository(tempDir);
        slotRepository.setPlannerGroupRepository(groupRepository);
        repository = new FileSystemSlotConfirmationRepository(tempDir);
        repository.setRelatedRepositories(slotRepository, activityRepository, groupRepository);
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
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setDate(LocalDate.now());
        repository.save(confirmation);

        Optional<SlotConfirmation> found = repository.findById(confirmation.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isConfirmed());
        assertEquals(LocalDate.now(), found.get().getDate());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        SlotConfirmation c1 = new SlotConfirmation();
        c1.setConfirmed(true);
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setConfirmed(false);

        repository.save(c1);
        repository.save(c2);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        repository.save(confirmation);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(confirmation.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testExistsById() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        repository.save(confirmation);

        assertTrue(repository.existsById(confirmation.getId()));
        assertFalse(repository.existsById("nonexistent"));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        SlotConfirmation c1 = new SlotConfirmation();
        c1.setConfirmed(true);
        c1.setUser(user);
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setConfirmed(false);

        repository.save(c1);
        repository.save(c2);

        List<SlotConfirmation> userConfirmations = repository.findByUserId(user.getId());
        assertEquals(1, userConfirmations.size());
        assertTrue(userConfirmations.get(0).isConfirmed());
    }

    @Test
    void testSaveWithRelatedEntities() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        groupRepository.save(group);

        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        activityRepository.save(activity);

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setGroups(java.util.Set.of(group));
        slotRepository.save(slot);

        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setDate(LocalDate.now());
        confirmation.setSlot(slot);
        confirmation.setActivity(activity);
        confirmation.setGroup(group);
        repository.save(confirmation);

        FileSystemSlotConfirmationRepository newRepo = new FileSystemSlotConfirmationRepository(tempDir);
        newRepo.setRelatedRepositories(slotRepository, activityRepository, groupRepository);
        Optional<SlotConfirmation> found = newRepo.findById(confirmation.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getSlot());
        assertNotNull(found.get().getActivity());
        assertNotNull(found.get().getGroup());
        assertEquals(slot.getId(), found.get().getSlot().getId());
        assertEquals(activity.getId(), found.get().getActivity().getId());
        assertEquals(group.getId(), found.get().getGroup().getId());
    }

    @Test
    void testPersistenceAcrossInstances() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        confirmation.setDate(LocalDate.of(2024, 6, 15));
        repository.save(confirmation);

        FileSystemSlotConfirmationRepository newRepo = new FileSystemSlotConfirmationRepository(tempDir);
        newRepo.setRelatedRepositories(slotRepository, activityRepository, groupRepository);
        Optional<SlotConfirmation> found = newRepo.findById(confirmation.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isConfirmed());
        assertEquals(LocalDate.of(2024, 6, 15), found.get().getDate());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
