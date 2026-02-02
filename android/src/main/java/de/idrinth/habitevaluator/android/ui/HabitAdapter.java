package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.Habit;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habits;
    private final OnHabitClickListener listener;
    private String displayLanguage;
    private int selectedPosition = -1;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
    }

    public HabitAdapter(List<Habit> habits, OnHabitClickListener listener) {
        this.habits = habits;
        this.listener = listener;
    }

    public void setDisplayLanguage(String displayLanguage) {
        this.displayLanguage = displayLanguage;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.bind(habit, position == selectedPosition, displayLanguage);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            listener.onHabitClick(habit);
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView descriptionText;
        private final TextView entriesCountText;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.habitName);
            descriptionText = itemView.findViewById(R.id.habitDescription);
            entriesCountText = itemView.findViewById(R.id.entriesCount);
        }

        void bind(Habit habit, boolean isSelected, String language) {
            if (language != null) {
                nameText.setText(habit.getDisplayName(language));
                descriptionText.setText(habit.getDisplayDescription(language));
            } else {
                nameText.setText(habit.getName());
                descriptionText.setText(habit.getDescription());
            }
            entriesCountText.setText(String.format("%d entries", habit.getEntries().size()));
            itemView.setSelected(isSelected);
        }
    }
}
