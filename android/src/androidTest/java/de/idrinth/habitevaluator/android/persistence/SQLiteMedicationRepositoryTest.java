package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteMedicationRepositoryTest {

    private SQLiteMedicationRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteMedicationRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        Medication med = createMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        repository.save(med);

        Optional<Medication> found = repository.findById("med-1");
        assertTrue(found.isPresent());
        assertEquals("Ibuprofen", found.get().getName());
        assertEquals(MedicationProvisionType.PILL, found.get().getProvisionType());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Medication> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1"));
        repository.save(createMedication("med-2", "Eye Drops", MedicationProvisionType.LIQUID_DROPS, "u1"));
        repository.save(createMedication("med-3", "Cough Syrup", MedicationProvisionType.LIQUID_ML, "u1"));

        List<Medication> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createMedication("to-delete", "Temp", MedicationProvisionType.PILL, "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        repository.save(createMedication("med-1", "Ibuprofen", MedicationProvisionType.PILL, "u1"));
        repository.save(createMedication("med-2", "Aspirin", MedicationProvisionType.PILL, "u1"));
        repository.save(createMedication("med-3", "Cough Syrup", MedicationProvisionType.LIQUID_ML, "u2"));

        List<Medication> user1Meds = repository.findByUserId("u1");
        assertEquals(2, user1Meds.size());

        List<Medication> user2Meds = repository.findByUserId("u2");
        assertEquals(1, user2Meds.size());
    }

    @Test
    public void testSaveAllProvisionTypes() {
        repository.save(createMedication("med-pill", "Pill Med", MedicationProvisionType.PILL, "u1"));
        repository.save(createMedication("med-drops", "Drop Med", MedicationProvisionType.LIQUID_DROPS, "u1"));
        repository.save(createMedication("med-ml", "Liquid Med", MedicationProvisionType.LIQUID_ML, "u1"));

        assertEquals(MedicationProvisionType.PILL, repository.findById("med-pill").get().getProvisionType());
        assertEquals(MedicationProvisionType.LIQUID_DROPS, repository.findById("med-drops").get().getProvisionType());
        assertEquals(MedicationProvisionType.LIQUID_ML, repository.findById("med-ml").get().getProvisionType());
    }

    @Test
    public void testSaveWithWikipediaLink() {
        Medication med = createMedication("med-wiki", "Ibuprofen", MedicationProvisionType.PILL, "u1");
        med.setWikipediaLink("https://en.wikipedia.org/wiki/Ibuprofen");
        repository.save(med);

        Optional<Medication> found = repository.findById("med-wiki");
        assertTrue(found.isPresent());
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", found.get().getWikipediaLink());
    }

    @Test
    public void testSaveWithoutWikipediaLink() {
        Medication med = createMedication("med-no-wiki", "Custom Med", MedicationProvisionType.PILL, "u1");
        repository.save(med);

        Optional<Medication> found = repository.findById("med-no-wiki");
        assertTrue(found.isPresent());
        assertNull(found.get().getWikipediaLink());
    }

    @Test
    public void testUpdateExistingMedication() {
        repository.save(createMedication("med-update", "Ibuprofen", MedicationProvisionType.PILL, "u1"));

        Medication updated = createMedication("med-update", "Ibuprofen 400mg", MedicationProvisionType.PILL, "u1");
        updated.setWikipediaLink("https://example.com");
        repository.save(updated);

        Optional<Medication> found = repository.findById("med-update");
        assertTrue(found.isPresent());
        assertEquals("Ibuprofen 400mg", found.get().getName());
        assertEquals("https://example.com", found.get().getWikipediaLink());
    }

    @Test
    public void testSavePreservesUser() {
        Medication med = createMedication("med-user", "Med", MedicationProvisionType.PILL, "u1");
        repository.save(med);

        Optional<Medication> found = repository.findById("med-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
        assertEquals("user_u1", found.get().getUser().getUsername());
    }

    private Medication createMedication(String id, String name, MedicationProvisionType type, String userId) {
        Medication med = new Medication();
        med.setId(id);
        med.setName(name);
        med.setProvisionType(type);
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        med.setUser(user);
        return med;
    }
}
