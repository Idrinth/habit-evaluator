package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.ActivityLog;

import static org.junit.jupiter.api.Assertions.*;

class ActivityLogAdapterTest {

    private List<ActivityLog> entries;
    private TestDeleteListener deleteListener;
    private ActivityLogAdapter adapter;

    @BeforeEach
    void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new ActivityLogAdapter(entries, deleteListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithEntries() {
        entries.add(createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        entries.add(createEntry("Bob, Carol", "Park", LocalTime.of(14, 0), LocalTime.of(15, 30)));
        entries.add(createEntry("Dave", "Office", LocalTime.of(9, 0), LocalTime.of(17, 0)));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        ActivityLog entry = createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        entries.add(createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        entries.add(createEntry("Bob", "Library", LocalTime.of(14, 0), LocalTime.of(15, 0)));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullDeleteListener() {
        ActivityLogAdapter nullListenerAdapter = new ActivityLogAdapter(entries, null);
        entries.add(createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    @Test
    void testAdapterBackedByOriginalList() {
        entries.add(createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertEquals(1, adapter.getItemCount());
        entries.add(createEntry("Bob", "Park", LocalTime.of(14, 0), LocalTime.of(15, 0)));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullStartTime() {
        ActivityLog entry = new ActivityLog("Alice", "Cafe", null, LocalTime.of(11, 0));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithNullEndTime() {
        ActivityLog entry = new ActivityLog("Alice", "Cafe", LocalTime.of(10, 0), null);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithActivity() {
        ActivityLog entry = createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setActivity("Coffee and chat");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithEmptyActivity() {
        ActivityLog entry = createEntry("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setActivity("");
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testEntryWithExplicitDate() {
        ActivityLog entry = new ActivityLog("Alice", "Cafe", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2025, 1, 15));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    private ActivityLog createEntry(String persons, String location,
                                    LocalTime startTime, LocalTime endTime) {
        return new ActivityLog(persons, location, startTime, endTime);
    }

    private static class TestDeleteListener implements ActivityLogAdapter.OnActivityLogDeleteListener {
        ActivityLog lastDeleted;

        @Override
        public void onDelete(ActivityLog entry) {
            lastDeleted = entry;
        }
    }
}
