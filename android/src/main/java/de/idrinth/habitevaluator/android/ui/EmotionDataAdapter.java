package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter;

public class EmotionDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ENTRY = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Object> items;
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

    public EmotionDataAdapter(List<Object> items,
                              OnPairClickListener pairClickListener,
                              OnPairDeleteListener pairDeleteListener,
                              OnEntryDeleteListener entryDeleteListener) {
        this.items = items;
        this.pairClickListener = pairClickListener;
        this.pairDeleteListener = pairDeleteListener;
        this.entryDeleteListener = entryDeleteListener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof EmotionPair ? TYPE_HEADER : TYPE_ENTRY;
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
        Object item = items.get(position);
        if (holder instanceof PairViewHolder && item instanceof EmotionPair) {
            PairViewHolder pairHolder = (PairViewHolder) holder;
            EmotionPair pair = (EmotionPair) item;
            pairHolder.emotionPairText.setText(pair.toString());
            pairHolder.itemView.setOnClickListener(v -> {
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
        return items.size();
    }

    static class PairViewHolder extends RecyclerView.ViewHolder {
        final TextView emotionPairText;
        final ImageButton deleteButton;

        PairViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionPairText = itemView.findViewById(R.id.emotionPairText);
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
