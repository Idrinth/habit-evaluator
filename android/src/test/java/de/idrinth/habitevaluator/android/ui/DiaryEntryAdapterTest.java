package de.idrinth.habitevaluator.android.ui;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;

import static org.junit.Assert.*;

public class DiaryEntryAdapterTest {

    private List<DiaryEntry> entries;
    private TestDeleteListener deleteListener;
    private DiaryEntryAdapter adapter;

    @Before
    public void setUp() {
        entries = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        adapter = new DiaryEntryAdapter(entries, deleteListener);
    }

    @Test
    public void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountWithEntries() {
        entries.add(createEntry("Entry 1", EventSignificance.MINOR));
        entries.add(createEntry("Entry 2", EventSignificance.NORMAL));
        entries.add(createEntry("Entry 3", EventSignificance.MAJOR));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        entries.add(createEntry("New entry", EventSignificance.NORMAL));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterRemoving() {
        DiaryEntry entry = createEntry("Entry", EventSignificance.NORMAL);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        entries.remove(entry);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterClearing() {
        entries.add(createEntry("Entry 1", EventSignificance.MINOR));
        entries.add(createEntry("Entry 2", EventSignificance.MAJOR));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapterWithNullDeleteListener() {
        DiaryEntryAdapter nullListenerAdapter = new DiaryEntryAdapter(entries, null);
        entries.add(createEntry("Entry", EventSignificance.NORMAL));
        assertEquals(1, nullListenerAdapter.getItemCount());
    }

    @Test
    public void testAdapterBackedByOriginalList() {
        entries.add(createEntry("Entry 1", EventSignificance.MINOR));
        assertEquals(1, adapter.getItemCount());
        entries.add(createEntry("Entry 2", EventSignificance.MAJOR));
        assertEquals(2, adapter.getItemCount());
        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAllSignificanceLevelsCanBeAdded() {
        entries.add(createEntry("Minor event", EventSignificance.MINOR));
        entries.add(createEntry("Normal event", EventSignificance.NORMAL));
        entries.add(createEntry("Major event", EventSignificance.MAJOR));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testEntryWithStartAndEndTime() {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription("Meeting");
        entry.setSignificance(EventSignificance.NORMAL);
        entry.setEventDate(LocalDate.now());
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(10, 30));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testEntryWithDurationFromTimes() {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription("Meeting");
        entry.setSignificance(EventSignificance.NORMAL);
        entry.setEventDate(LocalDate.now());
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(10, 30));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
        assertEquals(Integer.valueOf(90), entry.getDurationMinutes());
    }

    @Test
    public void testEntryWithNullStartTime() {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription("Quick note");
        entry.setSignificance(EventSignificance.MINOR);
        entry.setEventDate(LocalDate.now());
        entry.setStartTime(null);
        entry.setEndTime(null);
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testEntryWithSpecificDate() {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription("Past event");
        entry.setSignificance(EventSignificance.MAJOR);
        entry.setEventDate(LocalDate.of(2025, 1, 15));
        entries.add(entry);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testManyEntries() {
        for (int i = 0; i < 50; i++) {
            entries.add(createEntry("Entry " + i, EventSignificance.NORMAL));
        }
        assertEquals(50, adapter.getItemCount());
    }

    private DiaryEntry createEntry(String description, EventSignificance significance) {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription(description);
        entry.setSignificance(significance);
        entry.setEventDate(LocalDate.now());
        return entry;
    }

    private static class TestDeleteListener implements DiaryEntryAdapter.OnDiaryEntryDeleteListener {
        DiaryEntry lastDeleted;

        @Override
        public void onDelete(DiaryEntry entry) {
            lastDeleted = entry;
        }
    }
}
