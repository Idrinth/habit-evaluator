package de.idrinth.habitevaluator.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentEmergencyPlanBinding;
import de.idrinth.habitevaluator.android.ui.EmergencyPlanStepAdapter;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;

public class EmergencyPlanFragment extends Fragment implements EmergencyPlanStepAdapter.OnStepActionListener {

    static final String PHONE_URI_SCHEME = "tel:";

    private FragmentEmergencyPlanBinding binding;
    private EmergencyPlanStepAdapter stepAdapter;
    private List<EmergencyPlanStep> steps;
    private boolean formVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEmergencyPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        steps = new ArrayList<>();

        setupRecyclerView();
        setupToggleForm();
        binding.addStepButton.setOnClickListener(v -> addStep());
        loadSteps();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSteps();
    }

    private void setupRecyclerView() {
        stepAdapter = new EmergencyPlanStepAdapter(steps, this);
        binding.stepsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void setupToggleForm() {
        binding.addStepHeader.setOnClickListener(v -> toggleForm());
        binding.toggleFormButton.setOnClickListener(v -> toggleForm());
    }

    private void toggleForm() {
        formVisible = !formVisible;
        binding.addStepFormContainer.setVisibility(formVisible ? View.VISIBLE : View.GONE);
        binding.toggleFormButton.setImageResource(
                formVisible ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
    }

    private void addStep() {
        String question = binding.questionInput.getText() != null
                ? binding.questionInput.getText().toString().trim() : "";
        if (question.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emergency_plan_question_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String action = binding.actionInput.getText() != null
                ? binding.actionInput.getText().toString().trim() : "";
        if (action.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emergency_plan_action_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = binding.phoneInput.getText() != null
                ? binding.phoneInput.getText().toString().trim() : "";

        int nextOrder = steps.isEmpty() ? 0 : steps.get(steps.size() - 1).getStepOrder() + 1;
        EmergencyPlanStep step = new EmergencyPlanStep(question, action, nextOrder);
        if (!phone.isEmpty()) {
            step.setPhoneNumber(phone);
        }

        User user = MainActivity.getSharedLocalUser();
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        if (user == null || repository == null) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        step.setUser(user);
        repository.save(step);

        binding.questionInput.setText("");
        binding.actionInput.setText("");
        binding.phoneInput.setText("");

        Toast.makeText(requireContext(), R.string.emergency_plan_step_added, Toast.LENGTH_SHORT).show();
        loadSteps();
    }

    @Override
    public void onDeleteStep(EmergencyPlanStep step) {
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        if (repository != null) {
            repository.deleteById(step.getId());
        }
        Toast.makeText(requireContext(), R.string.emergency_plan_step_deleted, Toast.LENGTH_SHORT).show();
        loadSteps();
    }

    @Override
    public void onMoveUp(EmergencyPlanStep step) {
        int index = steps.indexOf(step);
        if (index <= 0) {
            return;
        }
        swapStepOrders(steps.get(index - 1), step);
        loadSteps();
    }

    @Override
    public void onMoveDown(EmergencyPlanStep step) {
        int index = steps.indexOf(step);
        if (index < 0 || index >= steps.size() - 1) {
            return;
        }
        swapStepOrders(step, steps.get(index + 1));
        loadSteps();
    }

    private void swapStepOrders(EmergencyPlanStep a, EmergencyPlanStep b) {
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        if (repository == null) {
            return;
        }
        int orderA = a.getStepOrder();
        int orderB = b.getStepOrder();
        a.setStepOrder(orderB);
        b.setStepOrder(orderA);
        repository.saveAll(Arrays.asList(a, b));
    }

    @Override
    public void onCallPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(PHONE_URI_SCHEME + phoneNumber));
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), R.string.emergency_plan_no_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopyPhone(String phoneNumber) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("phone", phoneNumber));
            Toast.makeText(requireContext(), R.string.emergency_plan_number_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSteps() {
        steps.clear();
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        User user = MainActivity.getSharedLocalUser();
        if (repository != null && user != null) {
            steps.addAll(repository.findByUserId(user.getId()));
        }
        if (stepAdapter != null) {
            stepAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
