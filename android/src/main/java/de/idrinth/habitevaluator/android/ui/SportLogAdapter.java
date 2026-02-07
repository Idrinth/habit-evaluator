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
import de.idrinth.habitevaluator.shared.model.SportLog;

public class SportLogAdapter extends RecyclerView.Adapter<SportLogAdapter.ViewHolder> {

    private final List<SportLog> entries;
    private final OnSportLogDeleteListener deleteListener;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public interface OnSportLogDeleteListener {
        void onDelete(SportLog entry);
    }

    public SportLogAdapter(List<SportLog> entries, OnSportLogDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sport_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SportLog entry = entries.get(position);
        holder.name.setText(entry.getName());
        holder.date.setText(entry.getDate().format(DATE_FORMAT));

        String fromStr = entry.getStartTime() != null ? entry.getStartTime().format(TIME_FORMAT) : "?";
        String untilStr = entry.getEndTime() != null ? entry.getEndTime().format(TIME_FORMAT) : "?";
        double hours = entry.getDurationHours();
        holder.time.setText(String.format(Locale.getDefault(), "%s - %s (%.1fh)", fromStr, untilStr, hours));

        holder.measurement.setText(String.format(Locale.getDefault(), "%.1f %s",
                entry.getMeasurement(), entry.getMeasurementUnit()));

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
        final TextView name;
        final TextView date;
        final TextView time;
        final TextView notes;
        final TextView measurement;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.sportLogName);
            date = itemView.findViewById(R.id.sportLogDate);
            time = itemView.findViewById(R.id.sportLogTime);
            notes = itemView.findViewById(R.id.sportLogNotes);
            measurement = itemView.findViewById(R.id.sportLogMeasurement);
            deleteButton = itemView.findViewById(R.id.sportLogDeleteButton);
        }
    }
}
