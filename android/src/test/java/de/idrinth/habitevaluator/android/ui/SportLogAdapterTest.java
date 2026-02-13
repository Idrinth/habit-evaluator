package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.SportLog;

import static org.junit.jupiter.api.Assertions.*;

class SportLogAdapterTest {

    private List<SportLog> entries;
    private TestDeleteListener deleteListener;
    private SportLogAdapter adapter;

    @BeforeEach
    void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new SportLogAdapter(entries, deleteListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithEntries() {
        entries.add(createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0)));
        entries.add(createEntry("Swimming", 1.5, "km", LocalTime.of(10, 0), LocalTime.of(11, 30)));
        entries.add(createEntry("Cycling", 20.0, "km", LocalTime.of(14, 0), LocalTime.of(15, 0)));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        SportLog entry = createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        entries.add(createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0)));
        entries.add(createEntry("Yoga", 0.0, "min", LocalTime.of(7, 0), LocalTime.of(7, 45)));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullDeleteListener() {
        SportLogAdapter nullListenerAdapter = new SportLogAdapter(entries, null);
        entries.add(createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    @Test
    void testAdapterBackedByOriginalList() {
        entries.add(createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertEquals(1, adapter.getItemCount());
        entries.add(createEntry("Swimming", 1.5, "km", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullStartTime() {
        SportLog entry = new SportLog("Running", 5.0, "km", null, LocalTime.of(9, 0));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullEndTime() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), null);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithNotes() {
        SportLog entry = createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setNotes("Felt great today");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithEmptyNotes() {
        SportLog entry = createEntry("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setNotes("");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithExplicitDate() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0), LocalDate.of(2025, 1, 15));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    private SportLog createEntry(String name, double measurement, String unit,
                                 LocalTime startTime, LocalTime endTime) {
        return new SportLog(name, measurement, unit, startTime, endTime);
    }

    private static class TestDeleteListener implements SportLogAdapter.OnSportLogDeleteListener {
        SportLog lastDeleted;

        @Override
        public void onDelete(SportLog entry) {
            lastDeleted = entry;
        }
    }
}
