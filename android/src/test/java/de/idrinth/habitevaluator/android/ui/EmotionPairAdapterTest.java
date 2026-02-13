package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.EmotionPair;

import static org.junit.jupiter.api.Assertions.*;

class EmotionPairAdapterTest {

    private List<EmotionPair> emotionPairs;
    private TestDeleteListener deleteListener;
    private TestClickListener clickListener;
    private EmotionPairAdapter adapter;

    @BeforeEach
    void setUp() {
        emotionPairs = new ArrayList<>();
        deleteListener = new TestDeleteListener();
        clickListener = new TestClickListener();
        adapter = new EmotionPairAdapter(emotionPairs, deleteListener, clickListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountWithPairs() {
        emotionPairs.add(createPair("sad", "happy"));
        emotionPairs.add(createPair("anxious", "calm"));
        emotionPairs.add(createPair("tired", "energetic"));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAdding() {
        assertEquals(0, adapter.getItemCount());
        emotionPairs.add(createPair("sad", "happy"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemoving() {
        EmotionPair pair = createPair("sad", "happy");
        emotionPairs.add(pair);
        assertEquals(1, adapter.getItemCount());
        emotionPairs.remove(pair);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterClearing() {
        emotionPairs.add(createPair("sad", "happy"));
        emotionPairs.add(createPair("anxious", "calm"));
        assertEquals(2, adapter.getItemCount());
        emotionPairs.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testAdapterWithNullListeners() {
        EmotionPairAdapter nullListenersAdapter = new EmotionPairAdapter(emotionPairs, null, null);
        emotionPairs.add(createPair("sad", "happy"));
        assertEquals(1, nullListenersAdapter.getItemCount());
    }

    private EmotionPair createPair(String negative, String positive) {
        EmotionPair pair = new EmotionPair();
        pair.setNegativeLabel(negative);
        pair.setPositiveLabel(positive);
        return pair;
    }

    private static class TestDeleteListener implements EmotionPairAdapter.OnDeleteListener {
        EmotionPair lastDeleted;

        @Override
        public void onDelete(EmotionPair pair) {
            lastDeleted = pair;
        }
    }

    private static class TestClickListener implements EmotionPairAdapter.OnClickListener {
        EmotionPair lastClicked;

        @Override
        public void onClick(EmotionPair pair) {
            lastClicked = pair;
        }
    }
}
