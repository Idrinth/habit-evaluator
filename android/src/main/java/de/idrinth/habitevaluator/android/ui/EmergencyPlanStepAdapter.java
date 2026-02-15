package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;

public class EmergencyPlanStepAdapter extends RecyclerView.Adapter<EmergencyPlanStepAdapter.ViewHolder> {

    private final List<EmergencyPlanStep> steps;
    private final OnStepActionListener listener;

    public interface OnStepActionListener {
        void onDeleteStep(EmergencyPlanStep step);
        void onMoveUp(EmergencyPlanStep step);
        void onMoveDown(EmergencyPlanStep step);
        void onCallPhone(String phoneNumber);
        void onCopyPhone(String phoneNumber);
    }

    public EmergencyPlanStepAdapter(List<EmergencyPlanStep> steps, OnStepActionListener listener) {
        this.steps = steps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_plan_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyPlanStep step = steps.get(position);

        holder.stepNumber.setText(
                holder.itemView.getContext().getString(R.string.emergency_plan_step_label, position + 1));
        holder.stepQuestion.setText(step.getQuestion());

        // Clear previous action views and populate with current actions
        holder.actionsContainer.removeAllViews();
        List<EmergencyPlanAction> actions = step.getActions();
        if (actions != null) {
            for (EmergencyPlanAction action : actions) {
                View actionView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_emergency_plan_action, holder.actionsContainer, false);

                TextView actionText = actionView.findViewById(R.id.actionText);
                actionText.setText(holder.itemView.getContext().getString(
                        R.string.emergency_plan_action_label, action.getActionText()));

                LinearLayout phoneContainer = actionView.findViewById(R.id.actionPhoneContainer);
                if (action.getPhoneNumber() != null && !action.getPhoneNumber().isEmpty()) {
                    phoneContainer.setVisibility(View.VISIBLE);
                    TextView phoneNumber = actionView.findViewById(R.id.actionPhoneNumber);
                    phoneNumber.setText(action.getPhoneNumber());

                    Button callButton = actionView.findViewById(R.id.actionCallButton);
                    callButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onCallPhone(action.getPhoneNumber());
                        }
                    });
                    Button copyButton = actionView.findViewById(R.id.actionCopyButton);
                    copyButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onCopyPhone(action.getPhoneNumber());
                        }
                    });
                } else {
                    phoneContainer.setVisibility(View.GONE);
                }

                holder.actionsContainer.addView(actionView);
            }
        }

        holder.moveUpButton.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.moveDownButton.setVisibility(position < steps.size() - 1 ? View.VISIBLE : View.INVISIBLE);

        holder.moveUpButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoveUp(step);
            }
        });
        holder.moveDownButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoveDown(step);
            }
        });
        holder.deleteStepButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteStep(step);
            }
        });
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView stepNumber;
        final TextView stepQuestion;
        final LinearLayout actionsContainer;
        final ImageButton moveUpButton;
        final ImageButton moveDownButton;
        final ImageButton deleteStepButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumber = itemView.findViewById(R.id.stepNumber);
            stepQuestion = itemView.findViewById(R.id.stepQuestion);
            actionsContainer = itemView.findViewById(R.id.actionsContainer);
            moveUpButton = itemView.findViewById(R.id.moveUpButton);
            moveDownButton = itemView.findViewById(R.id.moveDownButton);
            deleteStepButton = itemView.findViewById(R.id.deleteStepButton);
        }
    }
}
