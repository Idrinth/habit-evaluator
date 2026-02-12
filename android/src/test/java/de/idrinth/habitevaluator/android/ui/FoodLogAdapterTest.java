package de.idrinth.habitevaluator.android.ui;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.FoodLog;

import static org.junit.Assert.*;

public class FoodLogAdapterTest {

    private List<FoodLog> entries;
    private TestDeleteListener deleteListener;
    private FoodLogAdapter adapter;

    @Before
    public void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new FoodLogAdapter(entries, deleteListener);
    }

    @Test
    public void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountWithEntries() {
        entries.add(createEntry(200.0, 500, "Rice, Chicken"));
        entries.add(createEntry(50.0, 300, "Salad"));
        entries.add(createEntry(100.0, 800, "Pasta, Bread"));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry(100.0, 400, "Oatmeal"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterRemoving() {
        FoodLog entry = createEntry(100.0, 400, "Oatmeal");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterClearing() {
        entries.add(createEntry(200.0, 500, "Rice"));
        entries.add(createEntry(50.0, 300, "Salad"));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapterWithNullDeleteListener() {
        FoodLogAdapter nullListenerAdapter = new FoodLogAdapter(entries, null);
        entries.add(createEntry(100.0, 400, "Oatmeal"));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    @Test
    public void testAdapterBackedByOriginalList() {
        entries.add(createEntry(100.0, 400, "Oatmeal"));
        assertEquals(1, adapter.getItemCount());
        entries.add(createEntry(50.0, 200, "Apple"));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testEntryWithNullNutritionValues() {
        FoodLog entry = new FoodLog(null, null, LocalDateTime.now(), "Mystery food");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testEntryWithNullDateTime() {
        FoodLog entry = new FoodLog(50.0, 200, null, "Snack");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    private FoodLog createEntry(Double carbs, Integer kcal, String foodItems) {
        return new FoodLog(carbs, kcal, LocalDateTime.now(), foodItems);
    }

    private static class TestDeleteListener implements FoodLogAdapter.OnFoodLogDeleteListener {
        FoodLog lastDeleted;

        @Override
        public void onDelete(FoodLog entry) {
            lastDeleted = entry;
        }
    }
}
