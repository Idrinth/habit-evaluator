package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.SleepEntry;

import static org.junit.jupiter.api.Assertions.*;

class SleepEntryAdapterTest {

    private List<SleepEntry> entries;
    private TestDeleteListener deleteListener;
    private SleepEntryAdapter adapter;

    @BeforeEach
    void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new SleepEntryAdapter(entries, deleteListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithEntries() {
        entries.add(createEntry(LocalTime.of(22, 0), LocalTime.of(6, 0)));
        entries.add(createEntry(LocalTime.of(23, 0), LocalTime.of(7, 0)));
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry(LocalTime.of(22, 0), LocalTime.of(6, 0)));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        SleepEntry entry = createEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        entries.add(createEntry(LocalTime.of(22, 0), LocalTime.of(6, 0)));
        entries.add(createEntry(LocalTime.of(23, 30), LocalTime.of(7, 30)));
        entries.add(createEntry(LocalTime.of(21, 0), LocalTime.of(5, 0)));
        assertEquals(3, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullDeleteListener() {
        SleepEntryAdapter nullListenerAdapter = new SleepEntryAdapter(entries, null);
        entries.add(createEntry(LocalTime.of(22, 0), LocalTime.of(6, 0)));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    private SleepEntry createEntry(LocalTime from, LocalTime until) {
        SleepEntry entry = new SleepEntry();
        entry.setFromTime(from);
        entry.setUntilTime(until);
        entry.setDate(LocalDate.now());
        return entry;
    }

    private static class TestDeleteListener implements SleepEntryAdapter.OnSleepEntryDeleteListener {
        SleepEntry lastDeleted;

        @Override
        public void onDelete(SleepEntry entry) {
            lastDeleted = entry;
        }
    }
}
