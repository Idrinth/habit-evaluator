package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.EmotionPair;

public class EmotionPairAdapter extends RecyclerView.Adapter<EmotionPairAdapter.ViewHolder> {

    private final List<EmotionPair> emotionPairs;
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(EmotionPair pair);
    }

    public EmotionPairAdapter(List<EmotionPair> emotionPairs, OnDeleteListener deleteListener) {
        this.emotionPairs = emotionPairs;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emotion_pair, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmotionPair pair = emotionPairs.get(position);
        holder.emotionPairText.setText(pair.toString());
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(pair);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emotionPairs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView emotionPairText;
        final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionPairText = itemView.findViewById(R.id.emotionPairText);
            deleteButton = itemView.findViewById(R.id.deleteEmotionPairButton);
        }
    }
}
