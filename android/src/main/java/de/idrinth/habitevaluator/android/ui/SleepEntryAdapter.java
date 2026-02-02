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
import de.idrinth.habitevaluator.shared.model.SleepEntry;

public class SleepEntryAdapter extends RecyclerView.Adapter<SleepEntryAdapter.ViewHolder> {

    private final List<SleepEntry> entries;
    private final OnSleepEntryDeleteListener deleteListener;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public interface OnSleepEntryDeleteListener {
        void onDelete(SleepEntry entry);
    }

    public SleepEntryAdapter(List<SleepEntry> entries, OnSleepEntryDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SleepEntry entry = entries.get(position);
        holder.dateText.setText(entry.getDate().format(DATE_FORMAT));
        holder.hoursText.setText(String.format("%.1f h", entry.getHours()));

        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(entry.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
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
        final TextView dateText;
        final TextView hoursText;
        final TextView notesText;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.sleepEntryDate);
            hoursText = itemView.findViewById(R.id.sleepEntryHours);
            notesText = itemView.findViewById(R.id.sleepEntryNotes);
            deleteButton = itemView.findViewById(R.id.sleepEntryDeleteButton);
        }
    }
}
