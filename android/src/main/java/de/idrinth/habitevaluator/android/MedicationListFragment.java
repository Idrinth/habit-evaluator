package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentMedicationListBinding;
import de.idrinth.habitevaluator.android.ui.MedicationAdapter;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;

public class MedicationListFragment extends Fragment implements MedicationAdapter.OnDeleteListener, MedicationAdapter.OnEditLinkListener {

    private FragmentMedicationListBinding binding;
    private MedicationAdapter medicationAdapter;
    private List<Medication> medications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        medications = new ArrayList<>();

        setupMedicationListRecyclerView();
        setupProvisionTypeSpinner();
        setupFormToggle();
        binding.addMedicationButton.setOnClickListener(v -> addMedication());
        loadMedications();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMedications();
    }

    private void setupMedicationListRecyclerView() {
        medicationAdapter = new MedicationAdapter(medications, this, this);
        binding.medicationListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicationListRecyclerView.setAdapter(medicationAdapter);
    }

    private void setupFormToggle() {
        binding.addEntryHeader.setOnClickListener(v -> toggleForm());
        binding.toggleFormButton.setOnClickListener(v -> toggleForm());
    }

    private void toggleForm() {
        boolean isVisible = binding.addEntryFormContainer.getVisibility() == View.VISIBLE;
        binding.addEntryFormContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.toggleFormButton.setImageResource(isVisible
                ? android.R.drawable.arrow_down_float
                : android.R.drawable.arrow_up_float);
    }

    private void setupProvisionTypeSpinner() {
        String[] provisionTypeLabels = new String[]{
                getString(R.string.medication_provision_pill),
                getString(R.string.medication_provision_liquid_drops),
                getString(R.string.medication_provision_liquid_ml)
        };
        ArrayAdapter<String> provisionAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, provisionTypeLabels);
        provisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.provisionTypeSpinner.setAdapter(provisionAdapter);
    }

    static MedicationProvisionType mapPositionToProvisionType(int position) {
        MedicationProvisionType[] types = MedicationProvisionType.values();
        if (position >= 0 && position < types.length) {
            return types[position];
        }
        return null;
    }

    private MedicationProvisionType getSelectedProvisionType() {
        return mapPositionToProvisionType(binding.provisionTypeSpinner.getSelectedItemPosition());
    }

    private void addMedication() {
        String name = binding.newMedicationNameInput.getText() != null
                ? binding.newMedicationNameInput.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.medication_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        MedicationProvisionType provisionType = getSelectedProvisionType();
        if (provisionType == null) {
            Toast.makeText(requireContext(), R.string.medication_provision_type_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Medication medication = new Medication(name, provisionType);
        User user = MainActivity.getSharedLocalUser();
        medication.setUser(user);

        String wikipediaLink = binding.newMedicationWikipediaInput.getText() != null
                ? binding.newMedicationWikipediaInput.getText().toString().trim() : "";
        if (!wikipediaLink.isEmpty()) {
            medication.setWikipediaLink(wikipediaLink);
        }

        MedicationRepository repository = MainActivity.getSharedMedicationRepository();
        if (repository != null) {
            repository.save(medication);
        }

        binding.newMedicationNameInput.setText("");
        binding.newMedicationWikipediaInput.setText("");

        binding.addEntryFormContainer.setVisibility(View.GONE);
        binding.toggleFormButton.setImageResource(android.R.drawable.arrow_down_float);

        Toast.makeText(requireContext(), R.string.medication_added, Toast.LENGTH_SHORT).show();
        loadMedications();
    }

    @Override
    public void onDeleteMedication(Medication medication) {
        MedicationRepository repository = MainActivity.getSharedMedicationRepository();
        if (repository != null) {
            repository.deleteById(medication.getId());
        }
        Toast.makeText(requireContext(), R.string.medication_deleted, Toast.LENGTH_SHORT).show();
        loadMedications();
    }

    @Override
    public void onEditMedicationLink(Medication medication) {
        EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint(R.string.medication_wikipedia_hint);
        if (medication.getWikipediaLink() != null) {
            input.setText(medication.getWikipediaLink());
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.medication_edit_link)
                .setView(input)
                .setPositiveButton(R.string.medication_edit_link_save, (dialog, which) -> {
                    String link = input.getText().toString().trim();
                    medication.setWikipediaLink(link.isEmpty() ? null : link);
                    MedicationRepository repository = MainActivity.getSharedMedicationRepository();
                    if (repository != null) {
                        repository.save(medication);
                    }
                    Toast.makeText(requireContext(), R.string.medication_link_updated, Toast.LENGTH_SHORT).show();
                    loadMedications();
                })
                .setNegativeButton(R.string.medication_edit_link_cancel, null)
                .show();
    }

    private void loadMedications() {
        medications.clear();
        MedicationRepository repository = MainActivity.getSharedMedicationRepository();
        User user = MainActivity.getSharedLocalUser();
        if (repository != null && user != null) {
            medications.addAll(repository.findByUserId(user.getId()));
        }
        MainActivity.refreshSharedMedications();
        if (medicationAdapter != null) {
            medicationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
