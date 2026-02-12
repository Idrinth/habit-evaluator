package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteMedicationLogRepositoryTest {

    private SQLiteMedicationLogRepository repository;
    private SQLiteMedicationRepository medicationRepository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        medicationRepository = new SQLiteMedicationRepository(dbHelper);
        repository = new SQLiteMedicationLogRepository(dbHelper, medicationRepository);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");

        MedicationLog log = createLog("ml-1", med, 2.0, "u1");
        repository.save(log);

        Optional<MedicationLog> found = repository.findById("ml-1");
        assertTrue(found.isPresent());
        assertEquals(2.0, found.get().getAmount(), 0.001);
        assertNotNull(found.get().getMedication());
        assertEquals("Ibuprofen", found.get().getMedication().getName());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<MedicationLog> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");

        repository.save(createLog("ml-1", med, 2.0, "u1"));
        repository.save(createLog("ml-2", med, 1.0, "u1"));

        List<MedicationLog> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void testDeleteById() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        repository.save(createLog("to-delete", med, 1.0, "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        Medication med1 = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        Medication med2 = createAndSaveMedication("med-2", "Aspirin", MedicationProvisionType.PILL, "u2");

        repository.save(createLog("ml-1", med1, 2.0, "u1"));
        repository.save(createLog("ml-2", med1, 1.0, "u1"));
        repository.save(createLog("ml-3", med2, 3.0, "u2"));

        List<MedicationLog> user1Logs = repository.findByUserId("u1");
        assertEquals(2, user1Logs.size());

        List<MedicationLog> user2Logs = repository.findByUserId("u2");
        assertEquals(1, user2Logs.size());
    }

    @Test
    public void testFindByUserIdPaged() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");

        for (int i = 0; i < 5; i++) {
            MedicationLog log = createLog("ml-" + i, med, 1.0, "u1");
            log.setTakenAt(LocalDateTime.now().minusHours(i));
            repository.save(log);
        }

        List<MedicationLog> page1 = repository.findByUserIdPaged("u1", 2, 0);
        assertEquals(2, page1.size());

        List<MedicationLog> page2 = repository.findByUserIdPaged("u1", 2, 2);
        assertEquals(2, page2.size());

        List<MedicationLog> page3 = repository.findByUserIdPaged("u1", 2, 4);
        assertEquals(1, page3.size());
    }

    @Test
    public void testCountByUserId() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");

        assertEquals(0, repository.countByUserId("u1"));

        repository.save(createLog("ml-1", med, 2.0, "u1"));
        repository.save(createLog("ml-2", med, 1.0, "u1"));

        assertEquals(2, repository.countByUserId("u1"));
        assertEquals(0, repository.countByUserId("u2"));
    }

    @Test
    public void testSaveWithNotes() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");

        MedicationLog log = createLog("ml-notes", med, 2.0, "u1");
        log.setNotes("Taken after meal");
        repository.save(log);

        Optional<MedicationLog> found = repository.findById("ml-notes");
        assertTrue(found.isPresent());
        assertEquals("Taken after meal", found.get().getNotes());
    }

    @Test
    public void testUpdateExistingLog() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        repository.save(createLog("ml-update", med, 1.0, "u1"));

        MedicationLog updated = createLog("ml-update", med, 2.0, "u1");
        updated.setNotes("Updated dosage");
        repository.save(updated);

        Optional<MedicationLog> found = repository.findById("ml-update");
        assertTrue(found.isPresent());
        assertEquals(2.0, found.get().getAmount(), 0.001);
        assertEquals("Updated dosage", found.get().getNotes());
    }

    @Test
    public void testSavePreservesUser() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        repository.save(createLog("ml-user", med, 1.0, "u1"));

        Optional<MedicationLog> found = repository.findById("ml-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
    }

    @Test
    public void testSavePreservesTakenAt() {
        Medication med = createAndSaveMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        LocalDateTime specificTime = LocalDateTime.of(2025, 3, 15, 8, 30, 0);

        MedicationLog log = createLog("ml-time", med, 1.0, "u1");
        log.setTakenAt(specificTime);
        repository.save(log);

        Optional<MedicationLog> found = repository.findById("ml-time");
        assertTrue(found.isPresent());
        assertEquals(specificTime, found.get().getTakenAt());
    }

    private Medication createAndSaveMedication(String id, String name, MedicationProvisionType type, String userId) {
        Medication med = new Medication();
        med.setId(id);
        med.setName(name);
        med.setProvisionType(type);
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        med.setUser(user);
        medicationRepository.save(med);
        return med;
    }

    private MedicationLog createLog(String id, Medication medication, double amount, String userId) {
        MedicationLog log = new MedicationLog();
        log.setId(id);
        log.setMedication(medication);
        log.setAmount(amount);
        log.setTakenAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        log.setUser(user);
        return log;
    }
}
