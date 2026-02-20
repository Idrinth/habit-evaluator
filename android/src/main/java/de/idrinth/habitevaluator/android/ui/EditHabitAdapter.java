package de.idrinth.habitevaluator.android.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.android.SettingsActivity;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class EditHabitAdapter extends RecyclerView.Adapter<EditHabitAdapter.EditHabitViewHolder> {

    private static final String[] LANGUAGES = {
            SettingsActivity.LANGUAGE_EN,
            SettingsActivity.LANGUAGE_DE,
            SettingsActivity.LANGUAGE_ES,
            SettingsActivity.LANGUAGE_FR
    };
    private static final String[] LANGUAGE_LABELS = {"English", "Deutsch", "Español", "Français"};

    private final List<Habit> habits;
    private final Map<String, EditedHabitValues> editedValues = new HashMap<>();
    private final boolean translationsEnabled;
    private final List<HabitCategory> categories;
    private final List<String> categoryNames = new ArrayList<>();
    private final Map<String, String> categoryDisplayNameToId = new LinkedHashMap<>();
    private final List<String> frequencyTypeLabels = new ArrayList<>();
    private final String displayLanguage;

    public EditHabitAdapter(List<Habit> habits, boolean translationsEnabled,
                            List<HabitCategory> categories, String displayLanguage) {
        this.habits = habits;
        this.translationsEnabled = translationsEnabled;
        this.categories = categories;
        this.displayLanguage = displayLanguage;
        for (HabitCategory cat : categories) {
            String displayName = displayLanguage != null ? cat.getDisplayName(displayLanguage) : cat.getName();
            categoryNames.add(displayName);
            categoryDisplayNameToId.put(displayName, cat.getId());
        }
        for (Habit habit : habits) {
            ScoringRule rule = habit.getScoringRule();
            EditedHabitValues values = new EditedHabitValues(
                    habit.getName(),
                    habit.getDescription(),
                    habit.getTargetFrequency(),
                    habit.getMaxEntriesPerDay(),
                    habit.isPositiveScoring(),
                    rule != null ? rule.getThresholdFor1Point() : 1,
                    rule != null ? rule.getThresholdFor2Points() : 2,
                    rule != null ? rule.getThresholdFor4Points() : 4,
                    rule != null ? rule.getThresholdFor8Points() : 7,
                    habit.getCategoryId(),
                    habit.getFrequencyType()
            );
            if (translationsEnabled) {
                for (String lang : LANGUAGES) {
                    String nameVal = habit.getNameTranslations().get(lang);
                    if (nameVal != null) {
                        values.nameTranslations.put(lang, nameVal);
                    }
                    String descVal = habit.getDescriptionTranslations().get(lang);
                    if (descVal != null) {
                        values.descriptionTranslations.put(lang, descVal);
                    }
                }
            }
            editedValues.put(habit.getId(), values);
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

    private String getFrequencyTypeLabel(Context context, FrequencyType ft) {
        switch (ft) {
            case DAILY:
                return context.getString(R.string.frequency_daily);
            case WEEKLY:
                return context.getString(R.string.frequency_weekly);
            case MONTHLY:
                return context.getString(R.string.frequency_monthly);
            default:
                return ft.name().substring(0, 1) + ft.name().substring(1).toLowerCase();
        }
    }

    private int findCategoryPosition(String categoryId) {
        if (categoryId == null) {
            return 0;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(categoryId)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull EditHabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        EditedHabitValues values = editedValues.get(habit.getId());
        Context context = holder.itemView.getContext();

        // Remove previous watchers
        removeWatchers(holder);

        holder.nameText.setText(values.name);
        holder.descriptionText.setText(values.description != null ? values.description : "");

        // Category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.categorySpinner.setAdapter(categoryAdapter);
        holder.categorySpinner.setSelection(findCategoryPosition(values.categoryId));
        holder.categorySpinner.setOnItemSelectedListener(
                new CategorySelectionListener(categoryNames, categoryDisplayNameToId, values));

        // Frequency type spinner
        List<String> freqLabels = new ArrayList<>();
        for (FrequencyType ft : FrequencyType.values()) {
            freqLabels.add(getFrequencyTypeLabel(context, ft));
        }
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, freqLabels);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.frequencyTypeSpinner.setAdapter(freqAdapter);
        holder.frequencyTypeSpinner.setSelection(values.frequencyType.ordinal());
        holder.frequencyTypeSpinner.setOnItemSelectedListener(
                new FrequencyTypeSelectionListener(values));

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

        holder.nameWatcher = createStringWatcher(val -> values.name = val);
        holder.nameText.addTextChangedListener(holder.nameWatcher);

        holder.descriptionWatcher = createStringWatcher(val -> values.description = val.isEmpty() ? null : val);
        holder.descriptionText.addTextChangedListener(holder.descriptionWatcher);

        // Translation fields
        setupTranslationFields(holder, values);
    }

    private void setupTranslationFields(EditHabitViewHolder holder, EditedHabitValues values) {
        holder.translationsContainer.removeAllViews();
        if (!translationsEnabled) {
            holder.translationsContainer.setVisibility(View.GONE);
            return;
        }
        holder.translationsContainer.setVisibility(View.VISIBLE);
        Context context = holder.itemView.getContext();

        TextView label = new TextView(context);
        label.setText(R.string.translations_section);
        label.setTextSize(12);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        holder.translationsContainer.addView(label);

        for (int i = 0; i < LANGUAGES.length; i++) {
            String lang = LANGUAGES[i];
            String langLabel = LANGUAGE_LABELS[i];

            TextInputLayout nameLayout = new TextInputLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 4;
            nameLayout.setLayoutParams(params);
            nameLayout.setHint(context.getString(R.string.translation_name_hint, langLabel));

            TextInputEditText nameInput = new TextInputEditText(context);
            nameInput.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String currentName = values.nameTranslations.get(lang);
            if (currentName != null) {
                nameInput.setText(currentName);
            }
            nameInput.addTextChangedListener(createStringWatcher(val -> {
                if (val.isEmpty()) {
                    values.nameTranslations.remove(lang);
                } else {
                    values.nameTranslations.put(lang, val);
                }
            }));
            nameLayout.addView(nameInput);
            holder.translationsContainer.addView(nameLayout);

            TextInputLayout descLayout = new TextInputLayout(context);
            descLayout.setLayoutParams(params);
            descLayout.setHint(context.getString(R.string.translation_description_hint, langLabel));

            TextInputEditText descInput = new TextInputEditText(context);
            descInput.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String currentDesc = values.descriptionTranslations.get(lang);
            if (currentDesc != null) {
                descInput.setText(currentDesc);
            }
            descInput.addTextChangedListener(createStringWatcher(val -> {
                if (val.isEmpty()) {
                    values.descriptionTranslations.remove(lang);
                } else {
                    values.descriptionTranslations.put(lang, val);
                }
            }));
            descLayout.addView(descInput);
            holder.translationsContainer.addView(descLayout);
        }
    }

    private void removeWatchers(EditHabitViewHolder holder) {
        if (holder.nameWatcher != null) {
            holder.nameText.removeTextChangedListener(holder.nameWatcher);
        }
        if (holder.descriptionWatcher != null) {
            holder.descriptionText.removeTextChangedListener(holder.descriptionWatcher);
        }
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
        return new IntTextWatcher(consumer);
    }

    private TextWatcher createStringWatcher(StringConsumer consumer) {
        return new StringTextWatcher(consumer);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    private static class CategorySelectionListener implements AdapterView.OnItemSelectedListener {
        private final List<String> categoryNames;
        private final Map<String, String> categoryDisplayNameToId;
        private final EditedHabitValues values;

        CategorySelectionListener(List<String> categoryNames,
                                  Map<String, String> categoryDisplayNameToId,
                                  EditedHabitValues values) {
            this.categoryNames = categoryNames;
            this.categoryDisplayNameToId = categoryDisplayNameToId;
            this.values = values;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selectedName = categoryNames.get(pos);
            String catId = categoryDisplayNameToId.get(selectedName);
            if (catId != null) {
                values.categoryId = catId;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static class FrequencyTypeSelectionListener implements AdapterView.OnItemSelectedListener {
        private final EditedHabitValues values;

        FrequencyTypeSelectionListener(EditedHabitValues values) {
            this.values = values;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos >= 0 && pos < FrequencyType.values().length) {
                values.frequencyType = FrequencyType.values()[pos];
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static class IntTextWatcher implements TextWatcher {
        private final IntConsumer consumer;

        IntTextWatcher(IntConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

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
    }

    private static class StringTextWatcher implements TextWatcher {
        private final StringConsumer consumer;

        StringTextWatcher(StringConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            consumer.accept(s.toString());
        }
    }

    public static class EditedHabitValues {
        public String name;
        public String description;
        public int targetFrequency;
        public int maxEntriesPerDay;
        public boolean positiveScoring;
        public int threshold1;
        public int threshold2;
        public int threshold4;
        public int threshold8;
        public String categoryId;
        public FrequencyType frequencyType;
        public Map<String, String> nameTranslations = new HashMap<>();
        public Map<String, String> descriptionTranslations = new HashMap<>();

        public EditedHabitValues(String name, String description,
                                 int targetFrequency, int maxEntriesPerDay, boolean positiveScoring,
                                 int threshold1, int threshold2, int threshold4, int threshold8,
                                 String categoryId, FrequencyType frequencyType) {
            this.name = name;
            this.description = description;
            this.targetFrequency = targetFrequency;
            this.maxEntriesPerDay = maxEntriesPerDay;
            this.positiveScoring = positiveScoring;
            this.threshold1 = threshold1;
            this.threshold2 = threshold2;
            this.threshold4 = threshold4;
            this.threshold8 = threshold8;
            this.categoryId = categoryId;
            this.frequencyType = frequencyType;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private interface StringConsumer {
        void accept(String value);
    }

    static class EditHabitViewHolder extends RecyclerView.ViewHolder {
        private final EditText nameText;
        private final EditText descriptionText;
        private final Spinner categorySpinner;
        private final Spinner frequencyTypeSpinner;
        private final EditText targetFrequency;
        private final EditText maxEntriesPerDay;
        private final SwitchMaterial positiveScoring;
        private final EditText threshold1;
        private final EditText threshold2;
        private final EditText threshold4;
        private final EditText threshold8;
        private final LinearLayout translationsContainer;
        TextWatcher nameWatcher;
        TextWatcher descriptionWatcher;
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
            categorySpinner = itemView.findViewById(R.id.editCategorySpinner);
            frequencyTypeSpinner = itemView.findViewById(R.id.editFrequencyTypeSpinner);
            targetFrequency = itemView.findViewById(R.id.editTargetFrequency);
            maxEntriesPerDay = itemView.findViewById(R.id.editMaxEntriesPerDay);
            positiveScoring = itemView.findViewById(R.id.editPositiveScoring);
            threshold1 = itemView.findViewById(R.id.editThreshold1);
            threshold2 = itemView.findViewById(R.id.editThreshold2);
            threshold4 = itemView.findViewById(R.id.editThreshold4);
            threshold8 = itemView.findViewById(R.id.editThreshold8);
            translationsContainer = itemView.findViewById(R.id.translationsContainer);
        }
    }
}
