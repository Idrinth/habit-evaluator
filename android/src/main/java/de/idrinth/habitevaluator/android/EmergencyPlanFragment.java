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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;

public class EmergencyPlanFragment extends Fragment implements EmergencyPlanStepAdapter.OnStepActionListener {

    static final String PHONE_URI_SCHEME = "tel:";

    private FragmentEmergencyPlanBinding binding;
    private EmergencyPlanStepAdapter stepAdapter;
    private List<EmergencyPlanStep> steps;
    private boolean formVisible = false;
    private final List<ActionInputRow> actionInputRows = new ArrayList<>();
    private EmergencyPlanStep editingStep = null;

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
        binding.addStepButton.setOnClickListener(v -> saveStep());
        binding.addActionRowButton.setOnClickListener(v -> addActionInputRow());
        binding.startDialogueButton.setOnClickListener(v -> startDialogue());
        binding.cancelEditButton.setOnClickListener(v -> cancelEdit());
        addActionInputRow();
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

    private void addActionInputRow() {
        LinearLayout row = (LinearLayout) LayoutInflater.from(requireContext())
                .inflate(R.layout.item_action_input_row, binding.actionInputsContainer, false);

        EditText actionInput = row.findViewById(R.id.rowActionInput);
        EditText phoneInput = row.findViewById(R.id.rowPhoneInput);
        ImageButton removeButton = row.findViewById(R.id.removeActionRowButton);

        ActionInputRow inputRow = new ActionInputRow(row, actionInput, phoneInput);
        actionInputRows.add(inputRow);

        removeButton.setOnClickListener(v -> {
            if (actionInputRows.size() > 1) {
                actionInputRows.remove(inputRow);
                binding.actionInputsContainer.removeView(row);
            }
        });

        // Hide remove button if this is the only row
        removeButton.setVisibility(actionInputRows.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        // Update visibility of all remove buttons
        for (ActionInputRow r : actionInputRows) {
            ImageButton btn = r.row.findViewById(R.id.removeActionRowButton);
            btn.setVisibility(actionInputRows.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        }

        binding.actionInputsContainer.addView(row);
    }

    private void saveStep() {
        String question = binding.questionInput.getText() != null
                ? binding.questionInput.getText().toString().trim() : "";
        if (question.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emergency_plan_question_required, Toast.LENGTH_SHORT).show();
            return;
        }

        List<EmergencyPlanAction> actions = new ArrayList<>();
        int actionOrder = 0;
        for (ActionInputRow inputRow : actionInputRows) {
            String actionText = inputRow.actionInput.getText() != null
                    ? inputRow.actionInput.getText().toString().trim() : "";
            if (!actionText.isEmpty()) {
                String phone = inputRow.phoneInput.getText() != null
                        ? inputRow.phoneInput.getText().toString().trim() : "";
                EmergencyPlanAction action = new EmergencyPlanAction(actionText, actionOrder);
                if (!phone.isEmpty()) {
                    action.setPhoneNumber(phone);
                }
                actions.add(action);
                actionOrder++;
            }
        }

        if (actions.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emergency_plan_action_required, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = MainActivity.getSharedLocalUser();
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        if (user == null || repository == null) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingStep != null) {
            editingStep.setQuestion(question);
            editingStep.setActions(new ArrayList<>());
            for (EmergencyPlanAction action : actions) {
                editingStep.addAction(action);
            }
            repository.save(editingStep);
            editingStep = null;
            setEditMode(false);
            Toast.makeText(requireContext(), R.string.emergency_plan_step_updated, Toast.LENGTH_SHORT).show();
        } else {
            int nextOrder = steps.isEmpty() ? 0 : steps.get(steps.size() - 1).getStepOrder() + 1;
            EmergencyPlanStep step = new EmergencyPlanStep(question, nextOrder);
            for (EmergencyPlanAction action : actions) {
                step.addAction(action);
            }
            step.setUser(user);
            repository.save(step);
            Toast.makeText(requireContext(), R.string.emergency_plan_step_added, Toast.LENGTH_SHORT).show();
        }

        // Clear form
        binding.questionInput.setText("");
        clearActionInputRows();
        loadSteps();
    }

    private void clearActionInputRows() {
        actionInputRows.clear();
        binding.actionInputsContainer.removeAllViews();
        addActionInputRow();
    }

    @Override
    public void onEditStep(EmergencyPlanStep step) {
        editingStep = step;
        setEditMode(true);

        // Open form if closed
        if (!formVisible) {
            toggleForm();
        }

        // Populate question
        binding.questionInput.setText(step.getQuestion());

        // Populate actions
        clearActionInputRows();
        List<EmergencyPlanAction> actions = step.getActions();
        if (actions != null && !actions.isEmpty()) {
            // clearActionInputRows already added one empty row, remove it
            actionInputRows.clear();
            binding.actionInputsContainer.removeAllViews();
            for (EmergencyPlanAction action : actions) {
                addActionInputRow();
                ActionInputRow lastRow = actionInputRows.get(actionInputRows.size() - 1);
                lastRow.actionInput.setText(action.getActionText());
                if (action.getPhoneNumber() != null) {
                    lastRow.phoneInput.setText(action.getPhoneNumber());
                }
            }
        }

        // Scroll to top so user can see the form
        if (binding.getRoot() instanceof androidx.core.widget.NestedScrollView) {
            ((androidx.core.widget.NestedScrollView) binding.getRoot()).smoothScrollTo(0, 0);
        }
    }

    private void cancelEdit() {
        editingStep = null;
        setEditMode(false);
        binding.questionInput.setText("");
        clearActionInputRows();
    }

    private void setEditMode(boolean editing) {
        if (editing) {
            binding.addStepButton.setText(R.string.emergency_plan_update_step);
            binding.cancelEditButton.setVisibility(View.VISIBLE);
        } else {
            binding.addStepButton.setText(R.string.emergency_plan_add_step);
            binding.cancelEditButton.setVisibility(View.GONE);
        }
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

    private void startDialogue() {
        if (steps.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emergency_plan_no_steps, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), EmergencyDialogueActivity.class);
        startActivity(intent);
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
        updateDialogueButtonVisibility();
    }

    private void updateDialogueButtonVisibility() {
        if (binding != null) {
            binding.startDialogueButton.setVisibility(steps.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ActionInputRow {
        final LinearLayout row;
        final EditText actionInput;
        final EditText phoneInput;

        ActionInputRow(LinearLayout row, EditText actionInput, EditText phoneInput) {
            this.row = row;
            this.actionInput = actionInput;
            this.phoneInput = phoneInput;
        }
    }
}
