package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter;

public class EmotionDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ENTRY = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<EmotionPair> pairs;
    private final Map<String, List<EmotionEntry>> entriesByPair;
    private final List<Object> displayItems;
    private final Set<String> expandedPairs;
    private final OnPairClickListener pairClickListener;
    private final OnPairDeleteListener pairDeleteListener;
    private final OnEntryDeleteListener entryDeleteListener;

    public interface OnPairClickListener {
        void onClick(EmotionPair pair);
    }

    public interface OnPairDeleteListener {
        void onDelete(EmotionPair pair);
    }

    public interface OnEntryDeleteListener {
        void onDelete(EmotionEntry entry);
    }

    public EmotionDataAdapter(OnPairClickListener pairClickListener,
                              OnPairDeleteListener pairDeleteListener,
                              OnEntryDeleteListener entryDeleteListener) {
        this.pairs = new ArrayList<>();
        this.entriesByPair = new HashMap<>();
        this.displayItems = new ArrayList<>();
        this.expandedPairs = new HashSet<>();
        this.pairClickListener = pairClickListener;
        this.pairDeleteListener = pairDeleteListener;
        this.entryDeleteListener = entryDeleteListener;
    }

    public void setData(List<EmotionPair> pairs, Map<String, List<EmotionEntry>> entriesByPair) {
        this.pairs.clear();
        this.pairs.addAll(pairs);
        this.entriesByPair.clear();
        this.entriesByPair.putAll(entriesByPair);
        rebuildDisplayItems();
    }

    private void rebuildDisplayItems() {
        displayItems.clear();
        for (EmotionPair pair : pairs) {
            displayItems.add(pair);
            if (expandedPairs.contains(pair.getId())) {
                List<EmotionEntry> entries = entriesByPair.get(pair.getId());
                if (entries != null) {
                    displayItems.addAll(entries);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void toggleExpanded(String pairId) {
        if (expandedPairs.contains(pairId)) {
            expandedPairs.remove(pairId);
        } else {
            expandedPairs.add(pairId);
        }
        rebuildDisplayItems();
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position) instanceof EmotionPair ? TYPE_HEADER : TYPE_ENTRY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_emotion_pair, parent, false);
            return new PairViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_emotion_entry, parent, false);
            return new EntryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayItems.get(position);
        if (holder instanceof PairViewHolder && item instanceof EmotionPair) {
            PairViewHolder pairHolder = (PairViewHolder) holder;
            EmotionPair pair = (EmotionPair) item;
            pairHolder.emotionPairText.setText(pair.toString());

            List<EmotionEntry> entries = entriesByPair.get(pair.getId());
            boolean isExpanded = expandedPairs.contains(pair.getId());

            if (entries != null && !entries.isEmpty()) {
                EmotionEntry latest = entries.get(0);
                pairHolder.currentStatusText.setText(
                        EmotionStrengthFormatter.format(latest.getStrength(), pair));
                pairHolder.currentStatusText.setVisibility(View.VISIBLE);
            } else {
                pairHolder.currentStatusText.setText(R.string.no_entries_yet);
                pairHolder.currentStatusText.setVisibility(View.VISIBLE);
            }

            pairHolder.expandCollapseButton.setRotation(isExpanded ? 0f : -90f);
            pairHolder.expandCollapseButton.setContentDescription(
                    pairHolder.itemView.getContext().getString(
                            isExpanded ? R.string.collapse_emotion_entries : R.string.expand_emotion_entries));
            pairHolder.expandCollapseButton.setOnClickListener(v -> toggleExpanded(pair.getId()));

            pairHolder.addButton.setOnClickListener(v -> {
                if (pairClickListener != null) {
                    pairClickListener.onClick(pair);
                }
            });
            pairHolder.deleteButton.setOnClickListener(v -> {
                if (pairDeleteListener != null) {
                    pairDeleteListener.onDelete(pair);
                }
            });
        } else if (holder instanceof EntryViewHolder && item instanceof EmotionEntry) {
            EntryViewHolder entryHolder = (EntryViewHolder) holder;
            EmotionEntry entry = (EmotionEntry) item;
            entryHolder.dateText.setText(entry.getRecordedAt().format(DATE_FORMAT));
            entryHolder.strengthText.setText(EmotionStrengthFormatter.format(entry.getStrength(), entry.getEmotionPair()));
            String notes = entry.getNotes();
            entryHolder.notesText.setText(notes != null ? notes : "");
            entryHolder.deleteButton.setOnClickListener(v -> {
                if (entryDeleteListener != null) {
                    entryDeleteListener.onDelete(entry);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    static class PairViewHolder extends RecyclerView.ViewHolder {
        final ImageButton expandCollapseButton;
        final TextView emotionPairText;
        final TextView currentStatusText;
        final ImageButton addButton;
        final ImageButton deleteButton;

        PairViewHolder(@NonNull View itemView) {
            super(itemView);
            expandCollapseButton = itemView.findViewById(R.id.expandCollapseButton);
            emotionPairText = itemView.findViewById(R.id.emotionPairText);
            currentStatusText = itemView.findViewById(R.id.currentStatusText);
            addButton = itemView.findViewById(R.id.addEmotionEntryButton);
            deleteButton = itemView.findViewById(R.id.deleteEmotionPairButton);
        }
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        final TextView dateText;
        final TextView strengthText;
        final TextView notesText;
        final ImageButton deleteButton;

        EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.entryDateText);
            strengthText = itemView.findViewById(R.id.entryStrengthText);
            notesText = itemView.findViewById(R.id.entryNotesText);
            deleteButton = itemView.findViewById(R.id.deleteEmotionEntryButton);
        }
    }
}
