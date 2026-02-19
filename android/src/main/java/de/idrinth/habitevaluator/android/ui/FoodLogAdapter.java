package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

        holder.foodItems.removeAllViews();
        for (String item : entry.getFoodItemList()) {
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText(item);
            chip.setClickable(false);
            chip.setCheckable(false);
            holder.foodItems.addView(chip);
        }

        StringBuilder nutritionText = new StringBuilder();
        if (entry.getKcal() != null) {
            nutritionText.append(String.format(Locale.getDefault(), "%d kcal", entry.getKcal()));
        }
        if (entry.getCarbohydrates() != null) {
            if (nutritionText.length() > 0) {
                nutritionText.append(", ");
            }
            nutritionText.append(String.format(Locale.getDefault(), "%.1fg carbs", entry.getCarbohydrates()));
        }
        if (nutritionText.length() > 0) {
            holder.nutrition.setVisibility(View.VISIBLE);
            holder.nutrition.setText(nutritionText.toString());
        } else {
            holder.nutrition.setVisibility(View.GONE);
        }

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
        final ChipGroup foodItems;
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
