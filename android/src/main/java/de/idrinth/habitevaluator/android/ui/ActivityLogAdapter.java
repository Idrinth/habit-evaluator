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
import de.idrinth.habitevaluator.shared.model.ActivityLog;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {

    private final List<ActivityLog> entries;
    private final OnActivityLogActionListener actionListener;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public interface OnActivityLogDeleteListener extends OnActivityLogActionListener {
    }

    public interface OnActivityLogActionListener {
        void onDelete(ActivityLog entry);
        void onEdit(ActivityLog entry);
    }

    public ActivityLogAdapter(List<ActivityLog> entries, OnActivityLogActionListener actionListener) {
        this.entries = entries;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog entry = entries.get(position);
        holder.persons.setText(entry.getPersons());
        holder.location.setText(entry.getLocation());
        holder.date.setText(entry.getDate().format(DATE_FORMAT));

        String fromStr = entry.getStartTime() != null ? entry.getStartTime().format(TIME_FORMAT) : "?";
        String untilStr = entry.getEndTime() != null ? entry.getEndTime().format(TIME_FORMAT) : "?";
        holder.time.setText(String.format(Locale.getDefault(), "%s - %s", fromStr, untilStr));

        Integer duration = entry.getDurationMinutes();
        if (duration != null) {
            holder.duration.setText(String.format(Locale.getDefault(), "%d min", duration));
            holder.duration.setVisibility(View.VISIBLE);
        } else {
            holder.duration.setVisibility(View.GONE);
        }

        if (entry.getActivity() != null && !entry.getActivity().isEmpty()) {
            holder.activity.setVisibility(View.VISIBLE);
            holder.activity.setText(entry.getActivity());
        } else {
            holder.activity.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(entry);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView persons;
        final TextView location;
        final TextView date;
        final TextView time;
        final TextView activity;
        final TextView duration;
        final View editButton;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            persons = itemView.findViewById(R.id.activityLogPersons);
            location = itemView.findViewById(R.id.activityLogLocation);
            date = itemView.findViewById(R.id.activityLogDate);
            time = itemView.findViewById(R.id.activityLogTime);
            activity = itemView.findViewById(R.id.activityLogActivity);
            duration = itemView.findViewById(R.id.activityLogDuration);
            editButton = itemView.findViewById(R.id.activityLogEditButton);
            deleteButton = itemView.findViewById(R.id.activityLogDeleteButton);
        }
    }
}
