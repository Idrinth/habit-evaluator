package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SportLog;

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
 * <p>
 * Correlations are scaled by a confidence factor based on shared days
 * (Bayesian shrinkage: {@code correlation * sharedDays / (sharedDays + CONFIDENCE_SCALE)}).
 * This prevents a single co-occurrence from producing a perfect ±1 correlation.
 */
public class EventCorrelationService {

    private static final int YEAR_DAYS = 365;
    private static final int PROXIMITY_HOURS = 2;
    private static final double PROXIMITY_BOOST = 2.0;
    private static final int CONFIDENCE_SCALE = 7;

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
        return calculateCorrelations(habits, diaryEntries, sleepEntries, emotionEntries, new ArrayList<>());
    }

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, sleep hours,
     * emotion pair daily averages, and sport log data (duration and measurement per activity).
     *
     * @param habits         all habits with their entries
     * @param diaryEntries   all diary entries
     * @param sleepEntries   all sleep entries
     * @param emotionEntries all emotion entries
     * @param sportLogs      all sport log entries
     * @return all correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries,
            List<EmotionEntry> emotionEntries,
            List<SportLog> sportLogs) {
        return calculateCorrelations(habits, diaryEntries, sleepEntries, emotionEntries,
                sportLogs, new ArrayList<>());
    }

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, sleep hours,
     * emotion pair daily averages, sport log data, and meeting duration.
     *
     * @param habits          all habits with their entries
     * @param diaryEntries    all diary entries
     * @param sleepEntries    all sleep entries
     * @param emotionEntries  all emotion entries
     * @param sportLogs       all sport log entries
     * @param meetingEntries  all meeting entries
     * @return all correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries,
            List<EmotionEntry> emotionEntries,
            List<SportLog> sportLogs,
            List<MeetingEntry> meetingEntries) {
        return calculateCorrelations(habits, diaryEntries, sleepEntries, emotionEntries,
                sportLogs, meetingEntries, new ArrayList<>());
    }

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, sleep hours,
     * emotion pair daily averages, sport log data, meeting duration, and activity log data.
     *
     * @param habits          all habits with their entries
     * @param diaryEntries    all diary entries
     * @param sleepEntries    all sleep entries
     * @param emotionEntries  all emotion entries
     * @param sportLogs       all sport log entries
     * @param meetingEntries  all meeting entries
     * @param activityLogs    all activity log entries
     * @return all correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries,
            List<EmotionEntry> emotionEntries,
            List<SportLog> sportLogs,
            List<MeetingEntry> meetingEntries,
            List<ActivityLog> activityLogs) {
        return calculateCorrelations(habits, diaryEntries, sleepEntries, emotionEntries,
                sportLogs, meetingEntries, activityLogs, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Calculates the top event correlations over the past year using time-weighted
     * Pearson correlation. Events include individual habits, diary points, sleep hours,
     * emotion pair daily averages, sport log data, meeting duration, activity log data,
     * food log data (daily calories and carbohydrates), and medication log data
     * (daily dose per medication).
     *
     * @param habits          all habits with their entries
     * @param diaryEntries    all diary entries
     * @param sleepEntries    all sleep entries
     * @param emotionEntries  all emotion entries
     * @param sportLogs       all sport log entries
     * @param meetingEntries  all meeting entries
     * @param activityLogs    all activity log entries
     * @param foodLogs        all food log entries
     * @param medicationLogs  all medication log entries
     * @return all correlations sorted by absolute correlation strength
     */
    public List<EventCorrelation> calculateCorrelations(
            List<Habit> habits,
            List<DiaryEntry> diaryEntries,
            List<SleepEntry> sleepEntries,
            List<EmotionEntry> emotionEntries,
            List<SportLog> sportLogs,
            List<MeetingEntry> meetingEntries,
            List<ActivityLog> activityLogs,
            List<FoodLog> foodLogs,
            List<MedicationLog> medicationLogs) {

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
        double[] diaryDurationSignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] diaryTs = new ArrayList[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] diaryDurationTs = new ArrayList[totalDays];
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
                // Track duration signal for entries that have start and end times
                Integer durationMinutes = entry.getDurationMinutes();
                if (durationMinutes != null) {
                    diaryDurationSignal[dayIndex] += durationMinutes / 60.0;
                    if (diaryDurationTs[dayIndex] == null) {
                        diaryDurationTs[dayIndex] = new ArrayList<>();
                    }
                    diaryDurationTs[dayIndex].add(entryDate.atTime(entry.getStartTime()));
                    LocalDateTime endTimestamp = entryDate.atTime(entry.getEndTime());
                    if (!entry.getEndTime().isAfter(entry.getStartTime())) {
                        endTimestamp = entryDate.plusDays(1).atTime(entry.getEndTime());
                    }
                    diaryDurationTs[dayIndex].add(endTimestamp);
                }
            }
        }
        eventSignals.put("Diary Points", diarySignal);
        eventTimestamps.put("Diary Points", diaryTs);
        // Only add diary duration signal if any entries have duration data
        boolean hasDurationData = false;
        for (double v : diaryDurationSignal) {
            if (v != 0) {
                hasDurationData = true;
                break;
            }
        }
        if (hasDurationData) {
            eventSignals.put("Diary Duration", diaryDurationSignal);
            eventTimestamps.put("Diary Duration", diaryDurationTs);
        }

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

        // Group sport logs by activity name and create duration and measurement signals
        Map<String, Map<Integer, List<Double>>> sportDurationByName = new LinkedHashMap<>();
        Map<String, Map<Integer, List<Double>>> sportMeasurementByName = new LinkedHashMap<>();
        Map<String, Map<Integer, List<LocalDateTime>>> sportTsByName = new LinkedHashMap<>();
        for (SportLog entry : sportLogs) {
            LocalDate entryDate = entry.getDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                String sportName = entry.getName();
                sportDurationByName.computeIfAbsent(sportName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getDurationHours());
                sportMeasurementByName.computeIfAbsent(sportName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getMeasurement());
                sportTsByName.computeIfAbsent(sportName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>());
                if (entry.getStartTime() != null) {
                    sportTsByName.get(sportName).get(dayIndex)
                            .add(entryDate.atTime(entry.getStartTime()));
                }
                if (entry.getEndTime() != null) {
                    LocalDateTime endTimestamp = entryDate.atTime(entry.getEndTime());
                    if (entry.getStartTime() != null
                            && !entry.getEndTime().isAfter(entry.getStartTime())) {
                        endTimestamp = entryDate.plusDays(1).atTime(entry.getEndTime());
                    }
                    sportTsByName.get(sportName).get(dayIndex).add(endTimestamp);
                }
            }
        }
        for (String sportName : sportDurationByName.keySet()) {
            double[] durationSignal = new double[totalDays];
            double[] measurementSignal = new double[totalDays];
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] sportTimestamps = new ArrayList[totalDays];
            Map<Integer, List<Double>> durationMap = sportDurationByName.get(sportName);
            Map<Integer, List<Double>> measurementMap = sportMeasurementByName.get(sportName);
            Map<Integer, List<LocalDateTime>> tsMap = sportTsByName.get(sportName);
            for (Map.Entry<Integer, List<Double>> dayEntry : durationMap.entrySet()) {
                int dayIdx = dayEntry.getKey();
                double totalDur = 0;
                for (double d : dayEntry.getValue()) {
                    totalDur += d;
                }
                durationSignal[dayIdx] = totalDur;
                if (tsMap != null && tsMap.containsKey(dayIdx)) {
                    sportTimestamps[dayIdx] = new ArrayList<>(tsMap.get(dayIdx));
                }
            }
            for (Map.Entry<Integer, List<Double>> dayEntry : measurementMap.entrySet()) {
                int dayIdx = dayEntry.getKey();
                double totalMeas = 0;
                for (double m : dayEntry.getValue()) {
                    totalMeas += m;
                }
                measurementSignal[dayIdx] = totalMeas;
            }
            String durationLabel = "Sport Duration: " + sportName;
            String measurementLabel = "Sport Measurement: " + sportName;
            eventSignals.put(durationLabel, durationSignal);
            eventTimestamps.put(durationLabel, sportTimestamps);
            // Reuse same timestamps for measurement signal
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] measurementTs = new ArrayList[totalDays];
            for (int i = 0; i < totalDays; i++) {
                measurementTs[i] = sportTimestamps[i];
            }
            eventSignals.put(measurementLabel, measurementSignal);
            eventTimestamps.put(measurementLabel, measurementTs);
        }

        // Process meeting entries: track meeting count and duration per day
        double[] meetingCountSignal = new double[totalDays];
        double[] meetingDurationSignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] meetingTs = new ArrayList[totalDays];
        for (MeetingEntry entry : meetingEntries) {
            LocalDate entryDate = entry.getDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                meetingCountSignal[dayIndex] += 1;
                Integer durationMinutes = entry.getDurationMinutes();
                if (durationMinutes != null) {
                    meetingDurationSignal[dayIndex] += durationMinutes / 60.0;
                }
                if (meetingTs[dayIndex] == null) {
                    meetingTs[dayIndex] = new ArrayList<>();
                }
                if (entry.getStartTime() != null) {
                    meetingTs[dayIndex].add(entryDate.atTime(entry.getStartTime()));
                }
                if (entry.getEndTime() != null) {
                    LocalDateTime endTimestamp = entryDate.atTime(entry.getEndTime());
                    if (entry.getStartTime() != null
                            && !entry.getEndTime().isAfter(entry.getStartTime())) {
                        endTimestamp = entryDate.plusDays(1).atTime(entry.getEndTime());
                    }
                    meetingTs[dayIndex].add(endTimestamp);
                }
            }
        }
        boolean hasMeetingData = false;
        for (double v : meetingCountSignal) {
            if (v != 0) {
                hasMeetingData = true;
                break;
            }
        }
        if (hasMeetingData) {
            eventSignals.put("Meeting Count", meetingCountSignal);
            eventTimestamps.put("Meeting Count", meetingTs);
            eventSignals.put("Meeting Duration", meetingDurationSignal);
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] meetingDurationTs = new ArrayList[totalDays];
            for (int i = 0; i < totalDays; i++) {
                meetingDurationTs[i] = meetingTs[i];
            }
            eventTimestamps.put("Meeting Duration", meetingDurationTs);
        }

        // Process activity log entries: track count and duration per day
        double[] activityCountSignal = new double[totalDays];
        double[] activityDurationSignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] activityTs = new ArrayList[totalDays];
        for (ActivityLog entry : activityLogs) {
            LocalDate entryDate = entry.getDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                activityCountSignal[dayIndex] += 1;
                Integer durationMinutes = entry.getDurationMinutes();
                if (durationMinutes != null) {
                    activityDurationSignal[dayIndex] += durationMinutes / 60.0;
                }
                if (activityTs[dayIndex] == null) {
                    activityTs[dayIndex] = new ArrayList<>();
                }
                if (entry.getStartTime() != null) {
                    activityTs[dayIndex].add(entryDate.atTime(entry.getStartTime()));
                }
                if (entry.getEndTime() != null) {
                    LocalDateTime endTimestamp = entryDate.atTime(entry.getEndTime());
                    if (entry.getStartTime() != null
                            && !entry.getEndTime().isAfter(entry.getStartTime())) {
                        endTimestamp = entryDate.plusDays(1).atTime(entry.getEndTime());
                    }
                    activityTs[dayIndex].add(endTimestamp);
                }
            }
        }
        boolean hasActivityData = false;
        for (double v : activityCountSignal) {
            if (v != 0) {
                hasActivityData = true;
                break;
            }
        }
        if (hasActivityData) {
            eventSignals.put("Activity Count", activityCountSignal);
            eventTimestamps.put("Activity Count", activityTs);
            eventSignals.put("Activity Duration", activityDurationSignal);
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] activityDurationTs = new ArrayList[totalDays];
            for (int i = 0; i < totalDays; i++) {
                activityDurationTs[i] = activityTs[i];
            }
            eventTimestamps.put("Activity Duration", activityDurationTs);
        }

        // Process food log entries: track daily calories and carbohydrates
        double[] foodKcalSignal = new double[totalDays];
        double[] foodCarbsSignal = new double[totalDays];
        @SuppressWarnings("unchecked")
        List<LocalDateTime>[] foodTs = new ArrayList[totalDays];
        for (FoodLog entry : foodLogs) {
            LocalDate entryDate = entry.getDateTime().toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                if (entry.getKcal() != null) {
                    foodKcalSignal[dayIndex] += entry.getKcal();
                }
                if (entry.getCarbohydrates() != null) {
                    foodCarbsSignal[dayIndex] += entry.getCarbohydrates();
                }
                if (foodTs[dayIndex] == null) {
                    foodTs[dayIndex] = new ArrayList<>();
                }
                foodTs[dayIndex].add(entry.getDateTime());
            }
        }
        boolean hasFoodKcalData = false;
        for (double v : foodKcalSignal) {
            if (v != 0) {
                hasFoodKcalData = true;
                break;
            }
        }
        boolean hasFoodCarbsData = false;
        for (double v : foodCarbsSignal) {
            if (v != 0) {
                hasFoodCarbsData = true;
                break;
            }
        }
        if (hasFoodKcalData) {
            eventSignals.put("Food Calories", foodKcalSignal);
            eventTimestamps.put("Food Calories", foodTs);
        }
        if (hasFoodCarbsData) {
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] foodCarbsTs = new ArrayList[totalDays];
            for (int i = 0; i < totalDays; i++) {
                foodCarbsTs[i] = foodTs[i];
            }
            eventSignals.put("Food Carbohydrates", foodCarbsSignal);
            eventTimestamps.put("Food Carbohydrates", foodCarbsTs);
        }

        // Process medication log entries: track daily dose per medication
        Map<String, Map<Integer, List<Double>>> medDoseByName = new LinkedHashMap<>();
        Map<String, Map<Integer, List<LocalDateTime>>> medTsByName = new LinkedHashMap<>();
        for (MedicationLog entry : medicationLogs) {
            LocalDate entryDate = entry.getTakenAt().toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                String medName = entry.getMedication() != null ? entry.getMedication().getName() : null;
                if (medName == null) {
                    continue;
                }
                medDoseByName.computeIfAbsent(medName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getAmount());
                medTsByName.computeIfAbsent(medName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(dayIndex, k -> new ArrayList<>())
                        .add(entry.getTakenAt());
            }
        }
        for (String medName : medDoseByName.keySet()) {
            double[] doseSignal = new double[totalDays];
            @SuppressWarnings("unchecked")
            List<LocalDateTime>[] medTimestamps = new ArrayList[totalDays];
            Map<Integer, List<Double>> doseMap = medDoseByName.get(medName);
            Map<Integer, List<LocalDateTime>> tsMap = medTsByName.get(medName);
            for (Map.Entry<Integer, List<Double>> dayEntry : doseMap.entrySet()) {
                int dayIdx = dayEntry.getKey();
                double totalDose = 0;
                for (double d : dayEntry.getValue()) {
                    totalDose += d;
                }
                doseSignal[dayIdx] = totalDose;
                if (tsMap != null && tsMap.containsKey(dayIdx)) {
                    medTimestamps[dayIdx] = new ArrayList<>(tsMap.get(dayIdx));
                }
            }
            String label = "Medication: " + medName;
            eventSignals.put(label, doseSignal);
            eventTimestamps.put(label, medTimestamps);
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
                    // Apply Bayesian shrinkage: scale correlation toward zero when
                    // shared days are few, so a single co-occurrence cannot produce ±1.
                    double adjusted = corr * sharedDays / (sharedDays + CONFIDENCE_SCALE);
                    correlations.add(new EventCorrelation(nameA, nameB, adjusted, sharedDays));
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
