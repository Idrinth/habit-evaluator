package de.idrinth.habitevaluator.android.ui;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

import static org.junit.Assert.*;

public class EditHabitAdapterTest {

    private List<Habit> habits;
    private List<HabitCategory> categories;

    @Before
    public void setUp() {
        habits = new ArrayList<>();
        categories = new ArrayList<>();
        HabitCategory cat = new HabitCategory();
        cat.setId("cat-1");
        cat.setName("Health");
        categories.add(cat);
    }

    @Test
    public void testEmptyAdapterItemCount() {
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountWithHabits() {
        habits.add(createHabit("Exercise", "Daily exercise", "cat-1"));
        habits.add(createHabit("Reading", "Read a book", "cat-1"));
        habits.add(createHabit("Meditate", "Morning meditation", "cat-1"));
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterAdding() {
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(0, adapter.getItemCount());
        habits.add(createHabit("Exercise", "Daily exercise", "cat-1"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterRemoving() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(1, adapter.getItemCount());
        habits.remove(habit);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterClearing() {
        habits.add(createHabit("Exercise", "Daily exercise", "cat-1"));
        habits.add(createHabit("Reading", "Read books", "cat-1"));
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(2, adapter.getItemCount());
        habits.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testEditedValuesCreatedForEachHabit() {
        habits.add(createHabit("Exercise", "Daily exercise", "cat-1"));
        habits.add(createHabit("Reading", "Read books", "cat-1"));
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        Map<String, EditHabitAdapter.EditedHabitValues> editedValues = adapter.getEditedValues();
        assertEquals(2, editedValues.size());
    }

    @Test
    public void testEditedValuesContainCorrectHabitIds() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        Map<String, EditHabitAdapter.EditedHabitValues> editedValues = adapter.getEditedValues();
        assertTrue(editedValues.containsKey(habit.getId()));
    }

    @Test
    public void testEditedValuesReflectHabitTargetFrequency() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setTargetFrequency(5);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals(5, values.targetFrequency);
    }

    @Test
    public void testEditedValuesReflectHabitMaxEntriesPerDay() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setMaxEntriesPerDay(3);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals(3, values.maxEntriesPerDay);
    }

    @Test
    public void testEditedValuesReflectPositiveScoring() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setPositiveScoring(true);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertTrue(values.positiveScoring);
    }

    @Test
    public void testEditedValuesReflectNegativeScoring() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setPositiveScoring(false);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertFalse(values.positiveScoring);
    }

    @Test
    public void testEditedValuesReflectFrequencyType() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals(FrequencyType.WEEKLY, values.frequencyType);
    }

    @Test
    public void testEditedValuesReflectCategoryId() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals("cat-1", values.categoryId);
    }

    @Test
    public void testEditedValuesWithScoringRule() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        ScoringRule rule = new ScoringRule();
        rule.setThresholdFor1Point(2);
        rule.setThresholdFor2Points(4);
        rule.setThresholdFor4Points(6);
        rule.setThresholdFor8Points(10);
        habit.setScoringRule(rule);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals(2, values.threshold1);
        assertEquals(4, values.threshold2);
        assertEquals(6, values.threshold4);
        assertEquals(10, values.threshold8);
    }

    @Test
    public void testEditedValuesWithNullScoringRuleUsesDefaults() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.setScoringRule(null);
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals(1, values.threshold1);
        assertEquals(2, values.threshold2);
        assertEquals(4, values.threshold4);
        assertEquals(7, values.threshold8);
    }

    @Test
    public void testTranslationsDisabled() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.getNameTranslations().put("de", "Sport");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertTrue(values.nameTranslations.isEmpty());
    }

    @Test
    public void testTranslationsEnabled() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.getNameTranslations().put("de", "Sport");
        habit.getDescriptionTranslations().put("de", "Täglicher Sport");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, true, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals("Sport", values.nameTranslations.get("de"));
        assertEquals("Täglicher Sport", values.descriptionTranslations.get("de"));
    }

    @Test
    public void testTranslationsEnabledMultipleLanguages() {
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-1");
        habit.getNameTranslations().put("de", "Sport");
        habit.getNameTranslations().put("es", "Ejercicio");
        habit.getNameTranslations().put("fr", "Exercice");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, true, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals("Sport", values.nameTranslations.get("de"));
        assertEquals("Ejercicio", values.nameTranslations.get("es"));
        assertEquals("Exercice", values.nameTranslations.get("fr"));
    }

    @Test
    public void testMultipleCategories() {
        HabitCategory cat2 = new HabitCategory();
        cat2.setId("cat-2");
        cat2.setName("Productivity");
        categories.add(cat2);
        Habit habit = createHabit("Exercise", "Daily exercise", "cat-2");
        habits.add(habit);
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        EditHabitAdapter.EditedHabitValues values = adapter.getEditedValues().get(habit.getId());
        assertNotNull(values);
        assertEquals("cat-2", values.categoryId);
    }

    @Test
    public void testNullDisplayLanguage() {
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, null);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testEmptyCategories() {
        categories.clear();
        habits.add(createHabit("Exercise", "Daily exercise", null));
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(1, adapter.getItemCount());
    }

    private Habit createHabit(String name, String description, String categoryId) {
        Habit habit = new Habit(name, description);
        habit.setCategoryId(categoryId);
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);
        habit.setMaxEntriesPerDay(1);
        return habit;
    }
}
