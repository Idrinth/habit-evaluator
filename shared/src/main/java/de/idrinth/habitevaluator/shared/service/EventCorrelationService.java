package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes time-weighted correlations between different events over a yearly timeframe.
 * Newer entries receive higher weight than older ones using linear decay.
 * Events that occur within 2 hours of each other receive additional weight
 * to strengthen temporal proximity correlations.
 */
public class EventCorrelationService {

    private static final int YEAR_DAYS = 365;
    private static final int MIN_SHARED_DAYS = 7;
    private static final int PROXIMITY_HOURS = 2;
    private static final double PROXIMITY_BOOST = 2.0;

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, sleep hours,
     * and emotion pair daily averages. Events occurring within 2 hours of each other
     * receive boosted weight in the correlation calculation.
     *
     * @param habits         all habits with their entries
     * @param diaryEntries   all diary entries
     * @param sleepEntries   all sleep entries
     * @param emotionEntries all emotion entries
     * @return all correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries,
            List<EmotionEntry> emotionEntries) {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(YEAR_DAYS - 1);
        int totalDays = YEAR_DAYS;

        Map<String, double[]> eventSignals = new LinkedHashMap<>();
        Map<String, List<LocalDateTime>[]> eventTimestamps = new LinkedHashMap<>();

        for (Habit habit : habits) {
            double[] signal = new double[totalDays];
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] timestamps = new ArrayList[totalDays];
            for (HabitEntry entry : habit.getEntries()) {
                LocalDate entryDate = entry.getCompletedAt().toLocalDate();
                if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                    int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                    signal[dayIndex] += entry.getValue();
                    if (timestamps[dayIndex] == null) {
                        timestamps[dayIndex] = new ArrayList<>();
                    }
                    timestamps[dayIndex].add(entry.getCompletedAt());
                }
            }
            String name = "Habit: " + habit.getName();
            eventSignals.put(name, signal);
            eventTimestamps.put(name, timestamps);
        }

        double[] diarySignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] diaryTs = new ArrayList[totalDays];
        for (DiaryEntry entry : diaryEntries) {
            LocalDate entryDate = entry.getEventDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                diarySignal[dayIndex] += entry.getPoints();
                if (diaryTs[dayIndex] == null) {
                    diaryTs[dayIndex] = new ArrayList<>();
                }
                // Use startTime/endTime for precise temporal correlation when available
                if (entry.getStartTime() != null) {
                    diaryTs[dayIndex].add(entryDate.atTime(entry.getStartTime()));
                    if (entry.getEndTime() != null) {
                        LocalDateTime endTimestamp = entryDate.atTime(entry.getEndTime());
                        // Handle midnight crossing
                        if (!entry.getEndTime().isAfter(entry.getStartTime())) {
                            endTimestamp = entryDate.plusDays(1).atTime(entry.getEndTime());
                        }
                        diaryTs[dayIndex].add(endTimestamp);
                    }
                } else {
                    // Fall back to createdAt or start of day
                    LocalDateTime timestamp = entry.getCreatedAt() != null
                            ? entry.getCreatedAt()
                            : entryDate.atStartOfDay();
                    diaryTs[dayIndex].add(timestamp);
                }
            }
        }
        eventSignals.put("Diary Points", diarySignal);
        eventTimestamps.put("Diary Points", diaryTs);

        double[] sleepSignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] sleepTs = new ArrayList[totalDays];
        for (SleepEntry entry : sleepEntries) {
            LocalDate entryDate = entry.getDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                sleepSignal[dayIndex] += entry.getHours();
                if (sleepTs[dayIndex] == null) {
                    sleepTs[dayIndex] = new ArrayList<>();
                }
                // Use both fromTime (sleep start) and untilTime (wake-up) so
                // correlations with events near either boundary are captured.
                if (entry.getFromTime() != null) {
                    sleepTs[dayIndex].add(entryDate.atTime(entry.getFromTime()));
                }
                if (entry.getUntilTime() != null) {
                    LocalDateTime wakeUp = entryDate.atTime(entry.getUntilTime());
                    // Handle midnight crossing: if untilTime <= fromTime, wake-up is next day
                    if (entry.getFromTime() != null
                            && !entry.getUntilTime().isAfter(entry.getFromTime())) {
                        wakeUp = entryDate.plusDays(1).atTime(entry.getUntilTime());
                    }
                    sleepTs[dayIndex].add(wakeUp);
                }
                // Fall back to createdAt when neither time is available
                if (entry.getFromTime() == null && entry.getUntilTime() == null) {
                    sleepTs[dayIndex].add(entry.getCreatedAt() != null
                            ? entry.getCreatedAt()
                            : entryDate.atStartOfDay());
                }
            }
        }
        eventSignals.put("Sleep Hours", sleepSignal);
        eventTimestamps.put("Sleep Hours", sleepTs);

        // Group emotion entries by pair and compute daily average strength
        Map<String, Map<Integer, List<Integer>>> emotionByPair = new LinkedHashMap<>();
        Map<String, Map<Integer, List<LocalDateTime>>> emotionTsByPair = new LinkedHashMap<>();
        for (EmotionEntry entry : emotionEntries) {
            if (entry.getEmotionPair() == null) {
                continue;
            }
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                String pairLabel = "Emotion: " + entry.getEmotionPair().toString();
                emotionByPair.computeIfAbsent(pairLabel, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getStrength());
                emotionTsByPair.computeIfAbsent(pairLabel, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getRecordedAt());
            }
        }
        for (Map.Entry<String, Map<Integer, List<Integer>>> pairEntry : emotionByPair.entrySet()) {
            double[] signal = new double[totalDays];
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] timestamps = new ArrayList[totalDays];
            Map<Integer, List<LocalDateTime>> tsMap = emotionTsByPair.get(pairEntry.getKey());
            for (Map.Entry<Integer, List<Integer>> dayEntry : pairEntry.getValue().entrySet()) {
                List<Integer> strengths = dayEntry.getValue();
                double sum = 0;
                for (int s : strengths) {
                    sum += s;
                }
                int dayIdx = dayEntry.getKey();
                signal[dayIdx] = sum / strengths.size();
                if (tsMap != null && tsMap.containsKey(dayIdx)) {
                    timestamps[dayIdx] = new ArrayList<>(tsMap.get(dayIdx));
                }
            }
            eventSignals.put(pairEntry.getKey(), signal);
            eventTimestamps.put(pairEntry.getKey(), timestamps);
        }

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
                List<LocalDateTime>[] tsA = eventTimestamps.get(nameA);
                List<LocalDateTime>[] tsB = eventTimestamps.get(nameB);

                int sharedDays = 0;
                for (int i = 0; i < totalDays; i++) {
                    if (signalA[i] != 0 && signalB[i] != 0) {
                        sharedDays++;
                    }
                }

                if (sharedDays < MIN_SHARED_DAYS) {
                    continue;
                }

                // Create pair-specific weights with proximity boost
                double[] pairWeights = new double[totalDays];
                for (int i = 0; i < totalDays; i++) {
                    pairWeights[i] = weights[i];
                    if (hasTemporalProximity(tsA, tsB, i, totalDays)) {
                        pairWeights[i] *= (1.0 + PROXIMITY_BOOST);
                    }
                }

                double corr = weightedPearson(signalA, signalB, pairWeights);
                if (!Double.isNaN(corr)) {
                    correlations.add(new EventCorrelation(nameA, nameB, corr, sharedDays));
                }
            }
        }

        correlations.sort(Comparator.comparingDouble(
                (EventCorrelation c) -> Math.abs(c.getCorrelation())).reversed());

        return correlations;
    }

    /**
     * Checks whether any events from two signals occurred within PROXIMITY_HOURS
     * of each other on the given day (also checking adjacent days for midnight crossing).
     */
    private boolean hasTemporalProximity(
            List<LocalDateTime>[] tsA,
            List<LocalDateTime>[] tsB,
            int dayIndex,
            int totalDays) {
        long maxMinutes = PROXIMITY_HOURS * 60L;
        // Check A[dayIndex] against B[dayIndex-1, dayIndex, dayIndex+1]
        if (tsA[dayIndex] != null && !tsA[dayIndex].isEmpty()) {
            for (int offset = -1; offset <= 1; offset++) {
                int adjIndex = dayIndex + offset;
                if (adjIndex < 0 || adjIndex >= totalDays) {
                    continue;
                }
                if (tsB[adjIndex] == null || tsB[adjIndex].isEmpty()) {
                    continue;
                }
                for (LocalDateTime a : tsA[dayIndex]) {
                    for (LocalDateTime b : tsB[adjIndex]) {
                        if (Math.abs(ChronoUnit.MINUTES.between(a, b)) <= maxMinutes) {
                            return true;
                        }
                    }
                }
            }
        }
        // Check B[dayIndex] against A[dayIndex-1, dayIndex+1] (dayIndex already covered above)
        if (tsB[dayIndex] != null && !tsB[dayIndex].isEmpty()) {
            for (int offset : new int[]{-1, 1}) {
                int adjIndex = dayIndex + offset;
                if (adjIndex < 0 || adjIndex >= totalDays) {
                    continue;
                }
                if (tsA[adjIndex] == null || tsA[adjIndex].isEmpty()) {
                    continue;
                }
                for (LocalDateTime b : tsB[dayIndex]) {
                    for (LocalDateTime a : tsA[adjIndex]) {
                        if (Math.abs(ChronoUnit.MINUTES.between(a, b)) <= maxMinutes) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
