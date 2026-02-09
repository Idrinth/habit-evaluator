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
import de.idrinth.habitevaluator.shared.model.MedicationLog;

public class MedicationLogAdapter extends RecyclerView.Adapter<MedicationLogAdapter.ViewHolder> {

    private final List<MedicationLog> entries;
    private final OnMedicationLogDeleteListener deleteListener;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public interface OnMedicationLogDeleteListener {
        void onDelete(MedicationLog entry);
    }

    public MedicationLogAdapter(List<MedicationLog> entries, OnMedicationLogDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationLog entry = entries.get(position);

        String name = entry.getMedication() != null ? entry.getMedication().getName() : "?";
        holder.nameText.setText(name);

        String unit = entry.getMedication() != null ? entry.getMedication().getUnit() : "";
        holder.amountText.setText(String.format(Locale.getDefault(), "%.1f %s", entry.getAmount(), unit));

        holder.timeText.setText(entry.getTakenAt() != null ? entry.getTakenAt().format(DT_FORMAT) : "?");

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
        final TextView nameText;
        final TextView amountText;
        final TextView timeText;
        final TextView notesText;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.medicationLogName);
            amountText = itemView.findViewById(R.id.medicationLogAmount);
            timeText = itemView.findViewById(R.id.medicationLogTime);
            notesText = itemView.findViewById(R.id.medicationLogNotes);
            deleteButton = itemView.findViewById(R.id.medicationLogDeleteButton);
        }
    }
}
