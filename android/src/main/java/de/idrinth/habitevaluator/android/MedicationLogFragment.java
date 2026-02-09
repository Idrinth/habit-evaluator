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
import de.idrinth.habitevaluator.android.ui.MedicationLogAdapter;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;

public class MedicationLogFragment extends Fragment implements MedicationLogAdapter.OnMedicationLogDeleteListener {

    private static final DateTimeFormatter DT_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FragmentMedicationLogBinding binding;
    private MedicationLogAdapter adapter;
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
        setupDateTimePicker();
        setupMedicationSpinner();
        setupFormToggle();
        binding.addMedicationLogButton.setOnClickListener(v -> addMedicationLog());
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

        binding.addEntryFormContainer.setVisibility(View.GONE);
        binding.toggleFormButton.setImageResource(android.R.drawable.arrow_down_float);

        Toast.makeText(requireContext(), R.string.medication_log_entry_added, Toast.LENGTH_SHORT).show();
        loadEntries();
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

    private void loadMedications() {
        medications.clear();
        List<Medication> meds = MainActivity.getSharedMedications();
        if (meds != null) {
            medications.addAll(meds);
        }
        updateMedicationSpinner();
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
