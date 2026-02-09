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
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private final List<Medication> medications;
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDeleteMedication(Medication medication);
    }

    public MedicationAdapter(List<Medication> medications, OnDeleteListener deleteListener) {
        this.medications = medications;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.nameText.setText(medication.getName());
        holder.provisionTypeText.setText(getProvisionTypeLabel(holder.itemView, medication.getProvisionType()));
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteMedication(medication);
            }
        });
    }

    private String getProvisionTypeLabel(View itemView, MedicationProvisionType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case PILL:
                return itemView.getContext().getString(R.string.medication_provision_pill);
            case LIQUID_DROPS:
                return itemView.getContext().getString(R.string.medication_provision_liquid_drops);
            case LIQUID_ML:
                return itemView.getContext().getString(R.string.medication_provision_liquid_ml);
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView provisionTypeText;
        final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.medicationName);
            provisionTypeText = itemView.findViewById(R.id.medicationProvisionType);
            deleteButton = itemView.findViewById(R.id.deleteMedicationButton);
        }
    }
}
