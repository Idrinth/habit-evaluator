package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentPointDevelopmentBinding;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

public class PointDevelopmentFragment extends Fragment {

    private FragmentPointDevelopmentBinding binding;
    private HabitScoringService scoringService;
    private boolean showingWeek = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPointDevelopmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scoringService = new HabitScoringService();

        binding.periodToggle.check(R.id.weekButton);
        binding.periodToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showingWeek = checkedId == R.id.weekButton;
                updateCharts();
            }
        });

        binding.backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHome();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCharts();
    }

    private void updateCharts() {
        List<Habit> habits = MainActivity.getSharedHabits();
        String habitId = MainActivity.getPointDevelopmentHabitId();
        Habit selectedHabit = null;

        if (habits != null && habitId != null) {
            for (Habit h : habits) {
                if (habitId.equals(h.getId())) {
                    selectedHabit = h;
                    break;
                }
            }
        }

        if (selectedHabit == null) {
            binding.dailyChart.setData(new ArrayList<>(), new ArrayList<>(), 0);
            binding.averageChart.setData(new ArrayList<>(), new ArrayList<>(), 0);
            binding.totalPointsText.setText(getString(R.string.total_points, 0));
            binding.averagePointsText.setText(getString(R.string.average_points, 0.0));
            return;
        }

        if (showingWeek) {
            showWeekView(selectedHabit);
        } else {
            showMonthView(selectedHabit);
        }
    }

    private void showWeekView(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Integer> dailyPoints = new ArrayList<>();
        List<String> dayLabels = new ArrayList<>();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE");

        int totalPoints = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            int dayTotal = calculateDayTotal(habit, date);
            dailyPoints.add(dayTotal);
            dayLabels.add(date.format(dayFormat));
            totalPoints += dayTotal;
        }

        double average = totalPoints / 7.0;

        binding.dailyChart.setData(dailyPoints, dayLabels, average);
        binding.totalPointsText.setText(getString(R.string.total_points, totalPoints));
        binding.averagePointsText.setText(getString(R.string.average_points, average));

        // Average chart: show running average per day
        List<Integer> runningAvgs = new ArrayList<>();
        List<String> avgLabels = new ArrayList<>();
        int runningTotal = 0;
        for (int i = 0; i < 7; i++) {
            runningTotal += dailyPoints.get(i);
            runningAvgs.add(Math.round((float) runningTotal / (i + 1)));
            avgLabels.add(dayLabels.get(i));
        }
        binding.averageChart.setData(runningAvgs, avgLabels, average);
    }

    private void showMonthView(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        int daysInMonth = monthEnd.getDayOfMonth();

        List<Integer> dailyPoints = new ArrayList<>();
        List<String> dayLabels = new ArrayList<>();

        int totalPoints = 0;
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = monthStart.plusDays(i);
            int dayTotal = calculateDayTotal(habit, date);
            dailyPoints.add(dayTotal);
            // Show label every few days to avoid crowding
            if (i == 0 || i == daysInMonth - 1 || (i + 1) % 5 == 0) {
                dayLabels.add(String.valueOf(i + 1));
            } else {
                dayLabels.add("");
            }
            totalPoints += dayTotal;
        }

        double average = totalPoints / (double) daysInMonth;

        binding.dailyChart.setData(dailyPoints, dayLabels, average);
        binding.totalPointsText.setText(getString(R.string.total_points, totalPoints));
        binding.averagePointsText.setText(getString(R.string.average_points, average));

        // Average chart: show weekly averages within the month
        List<Integer> weeklyAvgs = new ArrayList<>();
        List<String> weekLabels = new ArrayList<>();
        int weekNum = 1;
        int weekTotal = 0;
        int daysInWeek = 0;

        for (int i = 0; i < daysInMonth; i++) {
            weekTotal += dailyPoints.get(i);
            daysInWeek++;

            LocalDate date = monthStart.plusDays(i);
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY || i == daysInMonth - 1) {
                weeklyAvgs.add(daysInWeek > 0 ? Math.round((float) weekTotal / daysInWeek) : 0);
                weekLabels.add(getString(R.string.week_label, weekNum));
                weekNum++;
                weekTotal = 0;
                daysInWeek = 0;
            }
        }
        binding.averageChart.setData(weeklyAvgs, weekLabels, average);
    }

    private int calculateDayTotal(Habit habit, LocalDate date) {
        return scoringService.calculateHabitScore(habit, date, date).getScore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
