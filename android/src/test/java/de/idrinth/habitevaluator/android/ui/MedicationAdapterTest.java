package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import static org.junit.jupiter.api.Assertions.*;

class MedicationAdapterTest {

    private List<Medication> medications;
    private TestDeleteListener deleteListener;
    private TestEditLinkListener editLinkListener;
    private MedicationAdapter adapter;

    @BeforeEach
    void setUp() {
        medications = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        editLinkListener = new TestEditLinkListener();
        adapter = new MedicationAdapter(medications, deleteListener, editLinkListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithMedications() {
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        medications.add(new Medication("Cough Syrup", MedicationProvisionType.LIQUID_ML));
        medications.add(new Medication("Eye Drops", MedicationProvisionType.LIQUID_DROPS));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        medications.add(med);
        assertEquals(1, adapter.getItemCount());
        medications.remove(med);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        medications.add(new Medication("Ibuprofen", MedicationProvisionType.PILL));
        assertEquals(2, adapter.getItemCount());
        medications.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullListeners() {
        MedicationAdapter nullAdapter = new MedicationAdapter(medications, null, null);
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        assertEquals(1, nullAdapter.getItemCount());
    }

    @Test
    void testAdapterWithNullDeleteListenerOnly() {
        MedicationAdapter partialAdapter = new MedicationAdapter(medications, null, editLinkListener);
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        assertEquals(1, partialAdapter.getItemCount());
    }

    @Test
    void testAdapterWithNullEditLinkListenerOnly() {
        MedicationAdapter partialAdapter = new MedicationAdapter(medications, deleteListener, null);
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        assertEquals(1, partialAdapter.getItemCount());
    }

    @Test
    void testAdapterBackedByOriginalList() {
        medications.add(new Medication("Aspirin", MedicationProvisionType.PILL));
        assertEquals(1, adapter.getItemCount());
        medications.add(new Medication("Ibuprofen", MedicationProvisionType.PILL));
        assertEquals(2, adapter.getItemCount());
        medications.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testMedicationWithAllProvisionTypes() {
        medications.add(new Medication("Pills", MedicationProvisionType.PILL));
        medications.add(new Medication("Drops", MedicationProvisionType.LIQUID_DROPS));
        medications.add(new Medication("Liquid", MedicationProvisionType.LIQUID_ML));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testMedicationWithNullProvisionType() {
        Medication med = new Medication("Unknown", null);
        medications.add(med);
        assertEquals(1, adapter.getItemCount());
    }

    private static class TestDeleteListener implements MedicationAdapter.OnDeleteListener {
        Medication lastDeleted;

        @Override
        public void onDeleteMedication(Medication medication) {
            lastDeleted = medication;
        }
    }

    private static class TestEditLinkListener implements MedicationAdapter.OnEditLinkListener {
        Medication lastEdited;

        @Override
        public void onEditMedicationLink(Medication medication) {
            lastEdited = medication;
        }
    }
}
