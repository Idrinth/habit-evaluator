package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaMedicationRepositoryTest {

    @Autowired
    private JpaMedicationRepository medicationRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("meduser", "password123", "meduser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        Medication med = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        med.setUser(testUser);
        Medication saved = medicationRepository.save(med);

        Optional<Medication> found = medicationRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Ibuprofen", found.get().getName());
        assertEquals(MedicationProvisionType.PILL, found.get().getProvisionType());
    }

    @Test
    void testFindByUserId() {
        Medication med1 = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        med1.setUser(testUser);
        medicationRepository.save(med1);

        Medication med2 = new Medication("Vitamin D", MedicationProvisionType.LIQUID_DROPS);
        med2.setUser(testUser);
        medicationRepository.save(med2);

        List<Medication> meds = medicationRepository.findByUserId(testUser.getId());

        assertEquals(2, meds.size());
    }

    @Test
    void testFindByUserIdSortedByName() {
        Medication medZ = new Medication("Zinc", MedicationProvisionType.PILL);
        medZ.setUser(testUser);
        medicationRepository.save(medZ);

        Medication medA = new Medication("Aspirin", MedicationProvisionType.PILL);
        medA.setUser(testUser);
        medicationRepository.save(medA);

        List<Medication> meds = medicationRepository.findByUserId(testUser.getId());

        assertEquals(2, meds.size());
        assertEquals("Aspirin", meds.get(0).getName());
        assertEquals("Zinc", meds.get(1).getName());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        Medication med = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        med.setUser(testUser);
        medicationRepository.save(med);

        User otherUser = new User("othermeduser", "password", "othermed@example.com");
        otherUser = userRepository.save(otherUser);

        List<Medication> meds = medicationRepository.findByUserId(otherUser.getId());
        assertTrue(meds.isEmpty());
    }

    @Test
    void testDeleteById() {
        Medication med = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        med.setUser(testUser);
        Medication saved = medicationRepository.save(med);

        medicationRepository.deleteById(saved.getId());

        assertFalse(medicationRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testAllProvisionTypes() {
        Medication pill = new Medication("Aspirin", MedicationProvisionType.PILL);
        pill.setUser(testUser);
        medicationRepository.save(pill);

        Medication drops = new Medication("Eye Drops", MedicationProvisionType.LIQUID_DROPS);
        drops.setUser(testUser);
        medicationRepository.save(drops);

        Medication liquid = new Medication("Cough Syrup", MedicationProvisionType.LIQUID_ML);
        liquid.setUser(testUser);
        medicationRepository.save(liquid);

        List<Medication> meds = medicationRepository.findByUserId(testUser.getId());
        assertEquals(3, meds.size());
    }
}
