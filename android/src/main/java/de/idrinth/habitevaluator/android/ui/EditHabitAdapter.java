package de.idrinth.habitevaluator.android.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class EditHabitAdapter extends RecyclerView.Adapter<EditHabitAdapter.EditHabitViewHolder> {

    private final List<Habit> habits;
    private final Map<String, EditedHabitValues> editedValues = new HashMap<>();

    public EditHabitAdapter(List<Habit> habits) {
        this.habits = habits;
        for (Habit habit : habits) {
            ScoringRule rule = habit.getScoringRule();
            editedValues.put(habit.getId(), new EditedHabitValues(
                    habit.getTargetFrequency(),
                    habit.getMaxEntriesPerDay(),
                    habit.isPositiveScoring(),
                    rule != null ? rule.getThresholdFor1Point() : 1,
                    rule != null ? rule.getThresholdFor2Points() : 2,
                    rule != null ? rule.getThresholdFor4Points() : 4,
                    rule != null ? rule.getThresholdFor8Points() : 7
            ));
        }
    }

    public Map<String, EditedHabitValues> getEditedValues() {
        return editedValues;
    }

    @NonNull
    @Override
    public EditHabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_habit, parent, false);
        return new EditHabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditHabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        EditedHabitValues values = editedValues.get(habit.getId());

        holder.nameText.setText(habit.getName());
        holder.descriptionText.setText(habit.getDescription());

        // Remove previous watchers
        removeWatchers(holder);

        holder.targetFrequency.setText(String.valueOf(values.targetFrequency));
        holder.maxEntriesPerDay.setText(String.valueOf(values.maxEntriesPerDay));
        holder.positiveScoring.setChecked(values.positiveScoring);
        holder.threshold1.setText(String.valueOf(values.threshold1));
        holder.threshold2.setText(String.valueOf(values.threshold2));
        holder.threshold4.setText(String.valueOf(values.threshold4));
        holder.threshold8.setText(String.valueOf(values.threshold8));

        // Set up watchers
        holder.targetWatcher = createIntWatcher(val -> values.targetFrequency = val);
        holder.targetFrequency.addTextChangedListener(holder.targetWatcher);

        holder.maxEntriesWatcher = createIntWatcher(val -> values.maxEntriesPerDay = val);
        holder.maxEntriesPerDay.addTextChangedListener(holder.maxEntriesWatcher);

        holder.positiveScoring.setOnCheckedChangeListener((buttonView, isChecked) ->
                values.positiveScoring = isChecked);

        holder.threshold1Watcher = createIntWatcher(val -> values.threshold1 = val);
        holder.threshold1.addTextChangedListener(holder.threshold1Watcher);

        holder.threshold2Watcher = createIntWatcher(val -> values.threshold2 = val);
        holder.threshold2.addTextChangedListener(holder.threshold2Watcher);

        holder.threshold4Watcher = createIntWatcher(val -> values.threshold4 = val);
        holder.threshold4.addTextChangedListener(holder.threshold4Watcher);

        holder.threshold8Watcher = createIntWatcher(val -> values.threshold8 = val);
        holder.threshold8.addTextChangedListener(holder.threshold8Watcher);
    }

    private void removeWatchers(EditHabitViewHolder holder) {
        if (holder.targetWatcher != null) {
            holder.targetFrequency.removeTextChangedListener(holder.targetWatcher);
        }
        if (holder.maxEntriesWatcher != null) {
            holder.maxEntriesPerDay.removeTextChangedListener(holder.maxEntriesWatcher);
        }
        if (holder.threshold1Watcher != null) {
            holder.threshold1.removeTextChangedListener(holder.threshold1Watcher);
        }
        if (holder.threshold2Watcher != null) {
            holder.threshold2.removeTextChangedListener(holder.threshold2Watcher);
        }
        if (holder.threshold4Watcher != null) {
            holder.threshold4.removeTextChangedListener(holder.threshold4Watcher);
        }
        if (holder.threshold8Watcher != null) {
            holder.threshold8.removeTextChangedListener(holder.threshold8Watcher);
        }
    }

    private TextWatcher createIntWatcher(IntConsumer consumer) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val >= 0) {
                        consumer.accept(val);
                    }
                } catch (NumberFormatException e) {
                    // ignore invalid input
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public static class EditedHabitValues {
        public int targetFrequency;
        public int maxEntriesPerDay;
        public boolean positiveScoring;
        public int threshold1;
        public int threshold2;
        public int threshold4;
        public int threshold8;

        public EditedHabitValues(int targetFrequency, int maxEntriesPerDay, boolean positiveScoring,
                                 int threshold1, int threshold2, int threshold4, int threshold8) {
            this.targetFrequency = targetFrequency;
            this.maxEntriesPerDay = maxEntriesPerDay;
            this.positiveScoring = positiveScoring;
            this.threshold1 = threshold1;
            this.threshold2 = threshold2;
            this.threshold4 = threshold4;
            this.threshold8 = threshold8;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    static class EditHabitViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView descriptionText;
        private final EditText targetFrequency;
        private final EditText maxEntriesPerDay;
        private final SwitchMaterial positiveScoring;
        private final EditText threshold1;
        private final EditText threshold2;
        private final EditText threshold4;
        private final EditText threshold8;
        TextWatcher targetWatcher;
        TextWatcher maxEntriesWatcher;
        TextWatcher threshold1Watcher;
        TextWatcher threshold2Watcher;
        TextWatcher threshold4Watcher;
        TextWatcher threshold8Watcher;

        EditHabitViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.editHabitName);
            descriptionText = itemView.findViewById(R.id.editHabitDescription);
            targetFrequency = itemView.findViewById(R.id.editTargetFrequency);
            maxEntriesPerDay = itemView.findViewById(R.id.editMaxEntriesPerDay);
            positiveScoring = itemView.findViewById(R.id.editPositiveScoring);
            threshold1 = itemView.findViewById(R.id.editThreshold1);
            threshold2 = itemView.findViewById(R.id.editThreshold2);
            threshold4 = itemView.findViewById(R.id.editThreshold4);
            threshold8 = itemView.findViewById(R.id.editThreshold8);
        }
    }
}
