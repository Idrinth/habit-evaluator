package de.idrinth.habitevaluator.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentMedicationLogBinding;
import de.idrinth.habitevaluator.android.ui.MedicationAdapter;
import de.idrinth.habitevaluator.android.ui.MedicationLogAdapter;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;

public class MedicationLogFragment extends Fragment implements MedicationLogAdapter.OnMedicationLogDeleteListener, MedicationAdapter.OnDeleteListener {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FragmentMedicationLogBinding binding;
    private MedicationLogAdapter adapter;
    private MedicationAdapter medicationAdapter;
    private List<MedicationLog> displayedEntries;
    private List<Medication> medications;
    private LocalDate selectedDate;
    private LocalTime selectedTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicationLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        medications = new ArrayList<>();
        selectedDate = LocalDate.now();
        selectedTime = LocalTime.now().withSecond(0).withNano(0);

        setupRecyclerView();
        setupMedicationListRecyclerView();
        setupDateTimePicker();
        setupMedicationSpinner();
        setupProvisionTypeSpinner();
        binding.addMedicationLogButton.setOnClickListener(v -> addMedicationLog());
        binding.addMedicationButton.setOnClickListener(v -> addMedication());
        loadMedications();
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMedications();
        loadEntries();
    }

    private void setupRecyclerView() {
        adapter = new MedicationLogAdapter(displayedEntries, this);
        binding.medicationLogRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicationLogRecyclerView.setAdapter(adapter);
    }

    private void setupMedicationListRecyclerView() {
        medicationAdapter = new MedicationAdapter(medications, this);
        binding.medicationListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicationListRecyclerView.setAdapter(medicationAdapter);
    }

    private void setupDateTimePicker() {
        updateDateTimeDisplay();
        binding.medicationDateTimeInput.setOnClickListener(v -> showDatePicker());
    }

    private void updateDateTimeDisplay() {
        LocalDateTime dt = LocalDateTime.of(selectedDate, selectedTime);
        binding.medicationDateTimeInput.setText(dt.format(DT_DISPLAY_FORMAT));
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    showTimePicker();
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        int hour = selectedTime != null ? selectedTime.getHour() : LocalTime.now().getHour();
        int minute = selectedTime != null ? selectedTime.getMinute() : 0;

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = LocalTime.of(selectedHour, selectedMinute);
                    updateDateTimeDisplay();
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void setupMedicationSpinner() {
        updateMedicationSpinner();
    }

    private void updateMedicationSpinner() {
        List<Medication> meds = MainActivity.getSharedMedications();
        medications.clear();
        if (meds != null) {
            medications.addAll(meds);
        }
        ArrayAdapter<Medication> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, medications);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.medicationSpinner.setAdapter(spinnerAdapter);
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

    private MedicationProvisionType getSelectedProvisionType() {
        int position = binding.provisionTypeSpinner.getSelectedItemPosition();
        MedicationProvisionType[] types = MedicationProvisionType.values();
        if (position >= 0 && position < types.length) {
            return types[position];
        }
        return null;
    }

    private void addMedicationLog() {
        if (medications.isEmpty()) {
            Toast.makeText(requireContext(), R.string.medication_log_no_medications, Toast.LENGTH_SHORT).show();
            return;
        }

        Medication selectedMedication = (Medication) binding.medicationSpinner.getSelectedItem();
        if (selectedMedication == null) {
            Toast.makeText(requireContext(), R.string.medication_log_select_medication, Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = binding.medicationAmountInput.getText() != null
                ? binding.medicationAmountInput.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.medication_log_amount_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.medication_log_amount_required, Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDateTime takenAt = LocalDateTime.of(selectedDate, selectedTime);
        MedicationLog entry = new MedicationLog(selectedMedication, amount, takenAt);
        User user = MainActivity.getSharedLocalUser();
        entry.setUser(user);

        String notes = binding.medicationNotesInput.getText() != null
                ? binding.medicationNotesInput.getText().toString().trim() : "";
        if (!notes.isEmpty()) {
            entry.setNotes(notes);
        }

        MedicationLogRepository repository = MainActivity.getSharedMedicationLogRepository();
        if (repository != null) {
            repository.save(entry);
        }

        binding.medicationAmountInput.setText("");
        binding.medicationNotesInput.setText("");
        selectedDate = LocalDate.now();
        selectedTime = LocalTime.now().withSecond(0).withNano(0);
        updateDateTimeDisplay();

        Toast.makeText(requireContext(), R.string.medication_log_entry_added, Toast.LENGTH_SHORT).show();
        loadEntries();
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

        Toast.makeText(requireContext(), R.string.medication_added, Toast.LENGTH_SHORT).show();
        loadMedications();
    }

    @Override
    public void onDelete(MedicationLog entry) {
        MedicationLogRepository repository = MainActivity.getSharedMedicationLogRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        Toast.makeText(requireContext(), R.string.medication_log_entry_removed, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    @Override
    public void onDeleteMedication(Medication medication) {
        MedicationRepository repository = MainActivity.getSharedMedicationRepository();
        if (repository != null) {
            repository.deleteById(medication.getId());
        }
        Toast.makeText(requireContext(), R.string.medication_deleted, Toast.LENGTH_SHORT).show();
        loadMedications();
        loadEntries();
    }

    private void loadMedications() {
        medications.clear();
        List<Medication> meds = MainActivity.getSharedMedications();
        if (meds != null) {
            medications.addAll(meds);
        } else {
            MedicationRepository repository = MainActivity.getSharedMedicationRepository();
            if (repository != null && MainActivity.getSharedLocalUser() != null) {
                medications.addAll(repository.findByUserId(MainActivity.getSharedLocalUser().getId()));
            }
        }
        updateMedicationSpinner();
        if (medicationAdapter != null) {
            medicationAdapter.notifyDataSetChanged();
        }
    }

    private void loadEntries() {
        displayedEntries.clear();

        MedicationLogRepository repository = MainActivity.getSharedMedicationLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<MedicationLog> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            allEntries.sort(Comparator.comparing(MedicationLog::getTakenAt).reversed()
                    .thenComparing(Comparator.comparing(MedicationLog::getCreatedAt).reversed()));
            displayedEntries.addAll(allEntries);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
