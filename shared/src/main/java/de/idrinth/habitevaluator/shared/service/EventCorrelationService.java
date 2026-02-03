package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes time-weighted correlations between different events over a yearly timeframe.
 * Newer entries receive higher weight than older ones using linear decay.
 */
public class EventCorrelationService {

    private static final int YEAR_DAYS = 365;
    private static final int TOP_CORRELATIONS = 10;
    private static final int MIN_SHARED_DAYS = 7;

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, and sleep hours.
     *
     * @param habits       all habits with their entries
     * @param diaryEntries all diary entries
     * @param sleepEntries all sleep entries
     * @return the top 10 strongest correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries) {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(YEAR_DAYS - 1);
        int totalDays = YEAR_DAYS;

        Map<String, double[]> eventSignals = new LinkedHashMap<>();

        for (Habit habit : habits) {
            double[] signal = new double[totalDays];
            for (HabitEntry entry : habit.getEntries()) {
                LocalDate entryDate = entry.getCompletedAt().toLocalDate();
                if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                    int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                    signal[dayIndex] += entry.getValue();
                }
            }
            eventSignals.put("Habit: " + habit.getName(), signal);
        }

        double[] diarySignal = new double[totalDays];
        for (DiaryEntry entry : diaryEntries) {
            LocalDate entryDate = entry.getEventDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                diarySignal[dayIndex] += entry.getPoints();
            }
        }
        eventSignals.put("Diary Points", diarySignal);

        double[] sleepSignal = new double[totalDays];
        for (SleepEntry entry : sleepEntries) {
            LocalDate entryDate = entry.getDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                sleepSignal[dayIndex] += entry.getHours();
            }
        }
        eventSignals.put("Sleep Hours", sleepSignal);

        double[] weights = new double[totalDays];
        for (int i = 0; i < totalDays; i++) {
            weights[i] = (double) (i + 1) / totalDays;
        }

        List<String> eventNames = new ArrayList<>(eventSignals.keySet());
        List<EventCorrelation> correlations = new ArrayList<>();

        for (int a = 0; a < eventNames.size(); a++) {
            for (int b = a + 1; b < eventNames.size(); b++) {
                String nameA = eventNames.get(a);
                String nameB = eventNames.get(b);
                double[] signalA = eventSignals.get(nameA);
                double[] signalB = eventSignals.get(nameB);

                int sharedDays = 0;
                for (int i = 0; i < totalDays; i++) {
                    if (signalA[i] != 0 && signalB[i] != 0) {
                        sharedDays++;
                    }
                }

                if (sharedDays < MIN_SHARED_DAYS) {
                    continue;
                }

                double corr = weightedPearson(signalA, signalB, weights);
                if (!Double.isNaN(corr)) {
                    correlations.add(new EventCorrelation(nameA, nameB, corr, sharedDays));
                }
            }
        }

        correlations.sort(Comparator.comparingDouble(
                (EventCorrelation c) -> Math.abs(c.getCorrelation())).reversed());

        if (correlations.size() > TOP_CORRELATIONS) {
            return correlations.subList(0, TOP_CORRELATIONS);
        }
        return correlations;
    }

    private double weightedPearson(double[] x, double[] y, double[] w) {
        double sumW = 0;
        double sumWX = 0;
        double sumWY = 0;

        for (int i = 0; i < x.length; i++) {
            sumW += w[i];
            sumWX += w[i] * x[i];
            sumWY += w[i] * y[i];
        }

        double meanX = sumWX / sumW;
        double meanY = sumWY / sumW;

        double sumWXY = 0;
        double sumWXX = 0;
        double sumWYY = 0;

        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sumWXY += w[i] * dx * dy;
            sumWXX += w[i] * dx * dx;
            sumWYY += w[i] * dy * dy;
        }

        double denom = Math.sqrt(sumWXX * sumWYY);
        if (denom == 0) {
            return Double.NaN;
        }
        return sumWXY / denom;
    }
}
