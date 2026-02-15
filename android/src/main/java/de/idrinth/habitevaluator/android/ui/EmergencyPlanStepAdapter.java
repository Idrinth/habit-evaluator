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
        holder.stepAction.setText(
                holder.itemView.getContext().getString(R.string.emergency_plan_action_label, step.getAction()));

        if (step.getPhoneNumber() != null && !step.getPhoneNumber().isEmpty()) {
            holder.phoneContainer.setVisibility(View.VISIBLE);
            holder.stepPhoneNumber.setText(step.getPhoneNumber());
            holder.callButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallPhone(step.getPhoneNumber());
                }
            });
            holder.copyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyPhone(step.getPhoneNumber());
                }
            });
        } else {
            holder.phoneContainer.setVisibility(View.GONE);
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
        final TextView stepAction;
        final LinearLayout phoneContainer;
        final TextView stepPhoneNumber;
        final Button callButton;
        final Button copyButton;
        final ImageButton moveUpButton;
        final ImageButton moveDownButton;
        final ImageButton deleteStepButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumber = itemView.findViewById(R.id.stepNumber);
            stepQuestion = itemView.findViewById(R.id.stepQuestion);
            stepAction = itemView.findViewById(R.id.stepAction);
            phoneContainer = itemView.findViewById(R.id.phoneContainer);
            stepPhoneNumber = itemView.findViewById(R.id.stepPhoneNumber);
            callButton = itemView.findViewById(R.id.callButton);
            copyButton = itemView.findViewById(R.id.copyButton);
            moveUpButton = itemView.findViewById(R.id.moveUpButton);
            moveDownButton = itemView.findViewById(R.id.moveDownButton);
            deleteStepButton = itemView.findViewById(R.id.deleteStepButton);
        }
    }
}
