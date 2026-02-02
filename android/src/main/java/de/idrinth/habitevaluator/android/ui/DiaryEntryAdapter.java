package de.idrinth.habitevaluator.android.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

import de.idrinth.habitevaluator.android.R;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.ViewHolder> {

    private final List<DiaryEntry> entries;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DiaryEntryAdapter(List<DiaryEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = entries.get(position);
        holder.description.setText(entry.getDescription());
        holder.date.setText(entry.getEventDate().format(DATE_FORMAT));

        String significanceLabel;
        EventSignificance sig = entry.getSignificance();
        if (sig == EventSignificance.MINOR) {
            significanceLabel = holder.itemView.getContext().getString(R.string.diary_minor);
        } else if (sig == EventSignificance.MAJOR) {
            significanceLabel = holder.itemView.getContext().getString(R.string.diary_major);
        } else {
            significanceLabel = holder.itemView.getContext().getString(R.string.diary_normal);
        }
        holder.significance.setText(significanceLabel);

        holder.points.setText(holder.itemView.getContext().getString(R.string.diary_points_value, entry.getPoints()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView description;
        final TextView date;
        final TextView significance;
        final TextView points;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.entryDescription);
            date = itemView.findViewById(R.id.entryDate);
            significance = itemView.findViewById(R.id.entrySignificance);
            points = itemView.findViewById(R.id.entryPoints);
        }
    }
}
