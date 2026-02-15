package de.idrinth.habitevaluator.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;

public class EmergencyDialogueActivity extends AppCompatActivity {

    static final String PHONE_URI_SCHEME = "tel:";

    private List<EmergencyPlanStep> steps;
    private int currentStepIndex = 0;

    private TextView questionText;
    private TextView progressText;
    private LinearLayout actionsContainer;
    private LinearLayout questionButtonsContainer;
    private Button yesButton;
    private Button noButton;
    private Button nextButton;
    private Button finishButton;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(FontSizeHelper.applyFontScale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_dialogue);

        questionText = findViewById(R.id.dialogueQuestionText);
        progressText = findViewById(R.id.dialogueProgressText);
        actionsContainer = findViewById(R.id.dialogueActionsContainer);
        questionButtonsContainer = findViewById(R.id.questionButtonsContainer);
        yesButton = findViewById(R.id.dialogueYesButton);
        noButton = findViewById(R.id.dialogueNoButton);
        nextButton = findViewById(R.id.dialogueNextButton);
        finishButton = findViewById(R.id.dialogueFinishButton);

        loadSteps();

        yesButton.setOnClickListener(v -> onYes());
        noButton.setOnClickListener(v -> onNo());
        nextButton.setOnClickListener(v -> moveToNextStep());
        finishButton.setOnClickListener(v -> finish());

        if (!steps.isEmpty()) {
            showCurrentStep();
        } else {
            finish();
        }
    }

    private void loadSteps() {
        EmergencyPlanStepRepository repository = MainActivity.getSharedEmergencyPlanStepRepository();
        User user = MainActivity.getSharedLocalUser();
        if (repository != null && user != null) {
            steps = repository.findByUserId(user.getId());
        } else {
            steps = java.util.Collections.emptyList();
        }
    }

    private void showCurrentStep() {
        if (currentStepIndex >= steps.size()) {
            showCompletion();
            return;
        }

        EmergencyPlanStep step = steps.get(currentStepIndex);
        questionText.setText(step.getQuestion());
        progressText.setText(getString(R.string.emergency_dialogue_progress,
                currentStepIndex + 1, steps.size()));

        // Show question buttons, hide actions and next/finish
        questionButtonsContainer.setVisibility(View.VISIBLE);
        actionsContainer.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
    }

    private void onYes() {
        // Show actions for the current step
        EmergencyPlanStep step = steps.get(currentStepIndex);
        questionButtonsContainer.setVisibility(View.GONE);
        actionsContainer.setVisibility(View.VISIBLE);
        actionsContainer.removeAllViews();

        List<EmergencyPlanAction> actions = step.getActions();
        if (actions != null) {
            for (EmergencyPlanAction action : actions) {
                View actionView = LayoutInflater.from(this)
                        .inflate(R.layout.item_dialogue_action, actionsContainer, false);

                TextView actionText = actionView.findViewById(R.id.dialogueActionText);
                actionText.setText(action.getActionText());

                LinearLayout phoneContainer = actionView.findViewById(R.id.dialogueActionPhoneContainer);
                if (action.getPhoneNumber() != null && !action.getPhoneNumber().isEmpty()) {
                    phoneContainer.setVisibility(View.VISIBLE);
                    TextView phoneNumber = actionView.findViewById(R.id.dialogueActionPhoneNumber);
                    phoneNumber.setText(action.getPhoneNumber());

                    Button callButton = actionView.findViewById(R.id.dialogueActionCallButton);
                    callButton.setOnClickListener(v -> callPhone(action.getPhoneNumber()));

                    Button copyButton = actionView.findViewById(R.id.dialogueActionCopyButton);
                    copyButton.setOnClickListener(v -> copyPhone(action.getPhoneNumber()));
                } else {
                    phoneContainer.setVisibility(View.GONE);
                }

                actionsContainer.addView(actionView);
            }
        }

        showNextOrFinish();
    }

    private void onNo() {
        moveToNextStep();
    }

    private void moveToNextStep() {
        currentStepIndex++;
        if (currentStepIndex < steps.size()) {
            showCurrentStep();
        } else {
            showCompletion();
        }
    }

    private void showNextOrFinish() {
        if (currentStepIndex < steps.size() - 1) {
            nextButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.VISIBLE);
        }
    }

    private void showCompletion() {
        questionText.setText(R.string.emergency_dialogue_complete);
        progressText.setVisibility(View.GONE);
        questionButtonsContainer.setVisibility(View.GONE);
        actionsContainer.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.VISIBLE);
    }

    private void callPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(PHONE_URI_SCHEME + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.emergency_plan_no_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyPhone(String phoneNumber) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("phone", phoneNumber));
            Toast.makeText(this, R.string.emergency_plan_number_copied, Toast.LENGTH_SHORT).show();
        }
    }
}
