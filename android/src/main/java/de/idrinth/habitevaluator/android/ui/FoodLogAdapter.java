package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.FoodLog;

public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.ViewHolder> {

    private final List<FoodLog> entries;
    private final OnFoodLogDeleteListener deleteListener;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public interface OnFoodLogDeleteListener {
        void onDelete(FoodLog entry);
    }

    public FoodLogAdapter(List<FoodLog> entries, OnFoodLogDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodLog entry = entries.get(position);

        holder.dateTime.setText(entry.getDateTime() != null ? entry.getDateTime().format(DT_FORMAT) : "");
        holder.foodItems.setText(entry.getFoodItems());
        holder.nutrition.setText(String.format(Locale.getDefault(), "%d kcal, %.1fg carbs",
                entry.getKcal(), entry.getCarbohydrates()));

        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(entry.getNotes());
        } else {
            holder.notes.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dateTime;
        final TextView foodItems;
        final TextView nutrition;
        final TextView notes;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.foodLogDateTime);
            foodItems = itemView.findViewById(R.id.foodLogFoodItems);
            nutrition = itemView.findViewById(R.id.foodLogNutrition);
            notes = itemView.findViewById(R.id.foodLogNotes);
            deleteButton = itemView.findViewById(R.id.foodLogDeleteButton);
        }
    }
}
