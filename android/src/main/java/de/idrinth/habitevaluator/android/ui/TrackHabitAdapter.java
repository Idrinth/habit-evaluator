package de.idrinth.habitevaluator.android.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class TrackHabitAdapter extends RecyclerView.Adapter<TrackHabitAdapter.TrackHabitViewHolder> {

    private final List<Habit> habits;
    private final Set<String> checkedHabitIds = new HashSet<>();
    private final Map<String, Integer> habitValues = new HashMap<>();

    public TrackHabitAdapter(List<Habit> habits) {
        this.habits = habits;
    }

    public Set<String> getCheckedHabitIds() {
        return checkedHabitIds;
    }

    public int getHabitValue(String habitId) {
        Integer value = habitValues.get(habitId);
        return value != null ? value : 1;
    }

    @NonNull
    @Override
    public TrackHabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track_habit, parent, false);
        return new TrackHabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackHabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        boolean atLimit = habit.hasReachedDailyLimit(LocalDate.now());
        holder.nameText.setText(habit.getName());
        holder.descriptionText.setText(atLimit ? "Daily limit reached" : habit.getDescription());
        holder.checkBox.setEnabled(!atLimit);
        holder.checkBox.setChecked(checkedHabitIds.contains(habit.getId()));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkedHabitIds.add(habit.getId());
            } else {
                checkedHabitIds.remove(habit.getId());
            }
        });
        holder.itemView.setAlpha(atLimit ? 0.5f : 1.0f);
        holder.itemView.setOnClickListener(v -> {
            if (!atLimit) {
                holder.checkBox.toggle();
            }
        });

        // Show habit parameters info
        StringBuilder info = new StringBuilder();
        info.append(habit.getFrequencyType().name().toLowerCase());
        info.append(", target: ").append(habit.getTargetFrequency());
        if (habit.getMaxEntriesPerDay() > 0) {
            info.append(", max/day: ").append(habit.getMaxEntriesPerDay());
        }
        info.append(", scoring: ").append(habit.isPositiveScoring() ? "+" : "-");
        ScoringRule rule = habit.getScoringRule();
        if (rule != null) {
            info.append(" (").append(rule.getThresholdFor1Point())
                .append("/").append(rule.getThresholdFor2Points())
                .append("/").append(rule.getThresholdFor4Points())
                .append("/").append(rule.getThresholdFor8Points())
                .append(")");
        }
        holder.infoText.setText(info.toString());

        // Value input
        holder.valueInput.setEnabled(!atLimit);
        Integer savedValue = habitValues.get(habit.getId());
        holder.valueInput.setText(String.valueOf(savedValue != null ? savedValue : 1));

        // Remove previous watcher to avoid duplicate callbacks
        if (holder.valueWatcher != null) {
            holder.valueInput.removeTextChangedListener(holder.valueWatcher);
        }
        holder.valueWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val > 0) {
                        habitValues.put(habit.getId(), val);
                    }
                } catch (NumberFormatException e) {
                    // ignore invalid input
                }
            }
        };
        holder.valueInput.addTextChangedListener(holder.valueWatcher);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class TrackHabitViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView nameText;
        private final TextView descriptionText;
        private final TextView infoText;
        private final EditText valueInput;
        TextWatcher valueWatcher;

        TrackHabitViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.habitCheckBox);
            nameText = itemView.findViewById(R.id.trackHabitName);
            descriptionText = itemView.findViewById(R.id.trackHabitDescription);
            infoText = itemView.findViewById(R.id.trackHabitInfo);
            valueInput = itemView.findViewById(R.id.habitValueInput);
        }
    }
}
