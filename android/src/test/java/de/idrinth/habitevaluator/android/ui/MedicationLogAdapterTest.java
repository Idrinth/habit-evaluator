package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import static org.junit.jupiter.api.Assertions.*;

class MedicationLogAdapterTest {

    private List<MedicationLog> entries;
    private TestDeleteListener deleteListener;
    private MedicationLogAdapter adapter;

    @BeforeEach
    void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new MedicationLogAdapter(entries, deleteListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithEntries() {
        entries.add(createEntry("Aspirin", MedicationProvisionType.PILL, 2.0));
        entries.add(createEntry("Cough Syrup", MedicationProvisionType.LIQUID_ML, 10.0));
        entries.add(createEntry("Eye Drops", MedicationProvisionType.LIQUID_DROPS, 3.0));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry("Aspirin", MedicationProvisionType.PILL, 1.0));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        MedicationLog entry = createEntry("Aspirin", MedicationProvisionType.PILL, 1.0);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        entries.add(createEntry("Aspirin", MedicationProvisionType.PILL, 2.0));
        entries.add(createEntry("Vitamins", MedicationProvisionType.LIQUID_DROPS, 5.0));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullDeleteListener() {
        MedicationLogAdapter nullListenerAdapter = new MedicationLogAdapter(entries, null);
        entries.add(createEntry("Aspirin", MedicationProvisionType.PILL, 1.0));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    @Test
    void testAdapterBackedByOriginalList() {
        entries.add(createEntry("Aspirin", MedicationProvisionType.PILL, 1.0));
        assertEquals(1, adapter.getItemCount());
        entries.add(createEntry("Ibuprofen", MedicationProvisionType.PILL, 2.0));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullMedication() {
        MedicationLog entry = new MedicationLog(null, 1.0, LocalDateTime.now());
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullTakenAt() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog entry = new MedicationLog(med, 1.0, null);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithNotes() {
        MedicationLog entry = createEntry("Aspirin", MedicationProvisionType.PILL, 1.0);
        entry.setNotes("Taken with food");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithEmptyNotes() {
        MedicationLog entry = createEntry("Aspirin", MedicationProvisionType.PILL, 1.0);
        entry.setNotes("");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    private MedicationLog createEntry(String medName, MedicationProvisionType type, double amount) {
        Medication medication = new Medication(medName, type);
        return new MedicationLog(medication, amount, LocalDateTime.now());
    }

    private static class TestDeleteListener implements MedicationLogAdapter.OnMedicationLogDeleteListener {
        MedicationLog lastDeleted;

        @Override
        public void onDelete(MedicationLog entry) {
            lastDeleted = entry;
        }
    }
}
