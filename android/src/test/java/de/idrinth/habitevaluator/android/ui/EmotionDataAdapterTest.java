package de.idrinth.habitevaluator.android.ui;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;

import static org.junit.Assert.*;

public class EmotionDataAdapterTest {

    private EmotionDataAdapter adapter;

    @Before
    public void setUp() {
        adapter = new EmotionDataAdapter(pair -> {}, pair -> {}, entry -> {});
    }

    @Test
    public void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapterWithNullListeners() {
        EmotionDataAdapter nullAdapter = new EmotionDataAdapter(null, null, null);
        assertEquals(0, nullAdapter.getItemCount());
    }

    @Test
    public void testAdapterInitiallyHasNoItems() {
        EmotionDataAdapter freshAdapter = new EmotionDataAdapter(
                pair -> {}, pair -> {}, entry -> {});
        assertEquals(0, freshAdapter.getItemCount());
    }

    @Test
    public void testCreatePairHasExpectedFields() {
        EmotionPair pair = new EmotionPair();
        pair.setId("test-id");
        pair.setNegativeLabel("sad");
        pair.setPositiveLabel("happy");
        assertEquals("test-id", pair.getId());
        assertEquals("sad", pair.getNegativeLabel());
        assertEquals("happy", pair.getPositiveLabel());
    }

    @Test
    public void testCreateEntryHasExpectedFields() {
        EmotionPair pair = new EmotionPair();
        pair.setId("pair-1");
        pair.setNegativeLabel("sad");
        pair.setPositiveLabel("happy");

        EmotionEntry entry = new EmotionEntry();
        entry.setEmotionPair(pair);
        entry.setStrength(5);
        LocalDateTime now = LocalDateTime.now();
        entry.setRecordedAt(now);

        assertEquals(pair, entry.getEmotionPair());
        assertEquals(5, entry.getStrength());
        assertEquals(now, entry.getRecordedAt());
    }

    @Test
    public void testEmotionEntryStrengthBounds() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(10);
        assertEquals(10, entry.getStrength());

        entry.setStrength(-10);
        assertEquals(-10, entry.getStrength());
    }

    @Test
    public void testEmotionEntryNotes() {
        EmotionEntry entry = new EmotionEntry();
        entry.setNotes("test notes");
        assertEquals("test notes", entry.getNotes());

        entry.setNotes(null);
        assertNull(entry.getNotes());
    }

    @Test
    public void testEmotionPairToString() {
        EmotionPair pair = new EmotionPair();
        pair.setNegativeLabel("sad");
        pair.setPositiveLabel("happy");
        String str = pair.toString();
        assertNotNull(str);
        assertTrue(str.contains("sad") || str.contains("happy"));
    }

    @Test
    public void testEmotionEntryStrengthZero() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(0);
        assertEquals(0, entry.getStrength());
    }

    @Test
    public void testEmotionEntryWithNullPair() {
        EmotionEntry entry = new EmotionEntry();
        entry.setEmotionPair(null);
        assertNull(entry.getEmotionPair());
    }

    @Test
    public void testEmotionEntryWithNullRecordedAt() {
        EmotionEntry entry = new EmotionEntry();
        entry.setRecordedAt(null);
        assertNull(entry.getRecordedAt());
    }

    @Test
    public void testEmotionPairWithNullLabels() {
        EmotionPair pair = new EmotionPair();
        pair.setNegativeLabel(null);
        pair.setPositiveLabel(null);
        assertNull(pair.getNegativeLabel());
        assertNull(pair.getPositiveLabel());
    }

    @Test
    public void testEmotionPairWithEmptyLabels() {
        EmotionPair pair = new EmotionPair();
        pair.setNegativeLabel("");
        pair.setPositiveLabel("");
        assertEquals("", pair.getNegativeLabel());
        assertEquals("", pair.getPositiveLabel());
    }

    @Test
    public void testMultipleAdapterInstancesAreIndependent() {
        EmotionDataAdapter adapter1 = new EmotionDataAdapter(pair -> {}, pair -> {}, entry -> {});
        EmotionDataAdapter adapter2 = new EmotionDataAdapter(pair -> {}, pair -> {}, entry -> {});
        assertEquals(0, adapter1.getItemCount());
        assertEquals(0, adapter2.getItemCount());
    }

    @Test
    public void testEmotionEntryStrengthPositiveBoundary() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(10);
        assertEquals(10, entry.getStrength());
    }

    @Test
    public void testEmotionEntryStrengthNegativeBoundary() {
        EmotionEntry entry = new EmotionEntry();
        entry.setStrength(-10);
        assertEquals(-10, entry.getStrength());
    }

    @Test
    public void testEmotionPairIdCanBeOverridden() {
        EmotionPair pair = new EmotionPair();
        assertNotNull(pair.getId());
        pair.setId("custom-id-123");
        assertEquals("custom-id-123", pair.getId());
    }

    @Test
    public void testEmotionEntryEmptyNotes() {
        EmotionEntry entry = new EmotionEntry();
        entry.setNotes("");
        assertEquals("", entry.getNotes());
    }

    @Test
    public void testEmotionEntryLongNotes() {
        EmotionEntry entry = new EmotionEntry();
        String longNotes = "A".repeat(500);
        entry.setNotes(longNotes);
        assertEquals(longNotes, entry.getNotes());
    }
}
