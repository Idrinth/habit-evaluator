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

import static java.time.format.DateTimeFormatter.ofPattern;

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.ViewHolder> {

    private final List<DiaryEntry> entries;
    private final OnDiaryEntryDeleteListener deleteListener;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = ofPattern("HH:mm");

    public interface OnDiaryEntryDeleteListener {
        void onDelete(DiaryEntry entry);
    }

    public DiaryEntryAdapter(List<DiaryEntry> entries, OnDiaryEntryDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
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

        if (entry.getStartTime() != null && entry.getEndTime() != null) {
            String timeRange = entry.getStartTime().format(TIME_FORMAT) + " - " + entry.getEndTime().format(TIME_FORMAT);
            Integer durationMinutes = entry.getDurationMinutes();
            if (durationMinutes != null) {
                int hours = durationMinutes / 60;
                int mins = durationMinutes % 60;
                String durationStr = hours > 0
                        ? String.format("%dh %02dmin", hours, mins)
                        : String.format("%dmin", mins);
                holder.duration.setText(timeRange + " (" + durationStr + ")");
            } else {
                holder.duration.setText(timeRange);
            }
            holder.duration.setVisibility(View.VISIBLE);
        } else {
            holder.duration.setVisibility(View.GONE);
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
        final TextView description;
        final TextView date;
        final TextView duration;
        final TextView significance;
        final TextView points;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.entryDescription);
            date = itemView.findViewById(R.id.entryDate);
            duration = itemView.findViewById(R.id.entryDuration);
            significance = itemView.findViewById(R.id.entrySignificance);
            points = itemView.findViewById(R.id.entryPoints);
            deleteButton = itemView.findViewById(R.id.diaryEntryDeleteButton);
        }
    }
}
