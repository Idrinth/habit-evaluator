package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import android.widget.ImageButton;

import java.time.LocalDate;
import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.Habit;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habits;
    private final OnHabitClickListener listener;
    private OnHabitEditListener editListener;
    private OnHabitDeleteListener deleteListener;
    private String displayLanguage;
    private int selectedPosition = -1;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onHabitDeselect();
    }

    public interface OnHabitEditListener {
        void onHabitEdit(Habit habit);
    }

    public interface OnHabitDeleteListener {
        void onHabitDelete(Habit habit);
    }

    public HabitAdapter(List<Habit> habits, OnHabitClickListener listener) {
        this.habits = habits;
        this.listener = listener;
    }

    public void setOnHabitEditListener(OnHabitEditListener editListener) {
        this.editListener = editListener;
    }

    public void setOnHabitDeleteListener(OnHabitDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
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
            int clickedPosition = holder.getBindingAdapterPosition();

            if (previousSelected == clickedPosition) {
                selectedPosition = -1;
                notifyItemChanged(previousSelected);
                listener.onHabitDeselect();
            } else {
                selectedPosition = clickedPosition;
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected);
                }
                notifyItemChanged(selectedPosition);
                listener.onHabitClick(habit);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onHabitEdit(habit);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onHabitDelete(habit);
            }
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
        private final MaterialCardView cardView;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            nameText = itemView.findViewById(R.id.habitName);
            descriptionText = itemView.findViewById(R.id.habitDescription);
            entriesCountText = itemView.findViewById(R.id.entriesCount);
            editButton = itemView.findViewById(R.id.editHabitButton);
            deleteButton = itemView.findViewById(R.id.deleteHabitButton);
        }

        void bind(Habit habit, boolean isSelected, String language) {
            if (language != null) {
                nameText.setText(habit.getDisplayName(language));
                descriptionText.setText(habit.getDisplayDescription(language));
            } else {
                nameText.setText(habit.getName());
                descriptionText.setText(habit.getDescription());
            }
            LocalDate today = LocalDate.now();
            long todayEntries = habit.getEntries().stream()
                    .filter(entry -> entry.getCompletedAt().toLocalDate().equals(today))
                    .count();
            entriesCountText.setText(itemView.getContext().getString(
                    R.string.daily_entries_count, todayEntries, habit.getMaxEntriesPerDay()));
            itemView.setSelected(isSelected);

            if (isSelected) {
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                cardView.setStrokeWidth((int) (2 * itemView.getContext().getResources().getDisplayMetrics().density));
                cardView.setCardElevation(6 * itemView.getContext().getResources().getDisplayMetrics().density);
            } else {
                cardView.setStrokeWidth(0);
                cardView.setCardElevation(2 * itemView.getContext().getResources().getDisplayMetrics().density);
            }
        }
    }
}
