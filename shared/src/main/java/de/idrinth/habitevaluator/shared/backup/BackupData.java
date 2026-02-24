package de.idrinth.habitevaluator.shared.backup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data transfer object that holds all user data for backup/restore operations.
 * Uses simple types to avoid JPA/framework-specific serialization issues.
 */
public class BackupData {

    private String backupDate;
    private int version = 1;
    private UserData user;
    private List<HabitData> habits = new ArrayList<>();
    private List<CategoryData> categories = new ArrayList<>();
    private List<DiaryEntryData> diaryEntries = new ArrayList<>();
    private List<SleepEntryData> sleepEntries = new ArrayList<>();
    private List<SportLogData> sportLogs = new ArrayList<>();
    private List<FoodLogData> foodLogs = new ArrayList<>();
    private List<FoodTagData> foodTags = new ArrayList<>();
    private List<EmotionPairData> emotionPairs = new ArrayList<>();
    private List<EmotionEntryData> emotionEntries = new ArrayList<>();
    private List<MeetingEntryData> meetingEntries = new ArrayList<>();
    private List<ActivityLogData> activityLogs = new ArrayList<>();
    private List<MedicationData> medications = new ArrayList<>();
    private List<MedicationLogData> medicationLogs = new ArrayList<>();
    private ReminderSettingsData reminderSettings;
    private ModuleVisibilityData moduleVisibility;
    private List<EmergencyPlanStepData> emergencyPlanSteps = new ArrayList<>();
    private List<PlannerGroupData> plannerGroups = new ArrayList<>();
    private List<PlannerActivityData> plannerActivities = new ArrayList<>();
    private List<WeekPlannerSlotData> weekPlannerSlots = new ArrayList<>();
    private List<SlotConfirmationData> slotConfirmations = new ArrayList<>();

    public String getBackupDate() {
        return backupDate;
    }

    public void setBackupDate(String backupDate) {
        this.backupDate = backupDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public List<HabitData> getHabits() {
        return habits;
    }

    public void setHabits(List<HabitData> habits) {
        this.habits = habits;
    }

    public List<CategoryData> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryData> categories) {
        this.categories = categories;
    }

    public List<DiaryEntryData> getDiaryEntries() {
        return diaryEntries;
    }

    public void setDiaryEntries(List<DiaryEntryData> diaryEntries) {
        this.diaryEntries = diaryEntries;
    }

    public List<SleepEntryData> getSleepEntries() {
        return sleepEntries;
    }

    public void setSleepEntries(List<SleepEntryData> sleepEntries) {
        this.sleepEntries = sleepEntries;
    }

    public List<SportLogData> getSportLogs() {
        return sportLogs;
    }

    public void setSportLogs(List<SportLogData> sportLogs) {
        this.sportLogs = sportLogs;
    }

    public List<FoodLogData> getFoodLogs() {
        return foodLogs;
    }

    public void setFoodLogs(List<FoodLogData> foodLogs) {
        this.foodLogs = foodLogs;
    }

    public List<FoodTagData> getFoodTags() {
        return foodTags;
    }

    public void setFoodTags(List<FoodTagData> foodTags) {
        this.foodTags = foodTags;
    }

    public List<EmotionPairData> getEmotionPairs() {
        return emotionPairs;
    }

    public void setEmotionPairs(List<EmotionPairData> emotionPairs) {
        this.emotionPairs = emotionPairs;
    }

    public List<EmotionEntryData> getEmotionEntries() {
        return emotionEntries;
    }

    public void setEmotionEntries(List<EmotionEntryData> emotionEntries) {
        this.emotionEntries = emotionEntries;
    }

    public List<MeetingEntryData> getMeetingEntries() {
        return meetingEntries;
    }

    public void setMeetingEntries(List<MeetingEntryData> meetingEntries) {
        this.meetingEntries = meetingEntries;
    }

    public List<ActivityLogData> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(List<ActivityLogData> activityLogs) {
        this.activityLogs = activityLogs;
    }

    public List<MedicationData> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationData> medications) {
        this.medications = medications;
    }

    public List<MedicationLogData> getMedicationLogs() {
        return medicationLogs;
    }

    public void setMedicationLogs(List<MedicationLogData> medicationLogs) {
        this.medicationLogs = medicationLogs;
    }

    public ReminderSettingsData getReminderSettings() {
        return reminderSettings;
    }

    public void setReminderSettings(ReminderSettingsData reminderSettings) {
        this.reminderSettings = reminderSettings;
    }

    public ModuleVisibilityData getModuleVisibility() {
        return moduleVisibility;
    }

    public void setModuleVisibility(ModuleVisibilityData moduleVisibility) {
        this.moduleVisibility = moduleVisibility;
    }

    public List<EmergencyPlanStepData> getEmergencyPlanSteps() {
        return emergencyPlanSteps;
    }

    public void setEmergencyPlanSteps(List<EmergencyPlanStepData> emergencyPlanSteps) {
        this.emergencyPlanSteps = emergencyPlanSteps;
    }

    public List<PlannerGroupData> getPlannerGroups() {
        return plannerGroups;
    }

    public void setPlannerGroups(List<PlannerGroupData> plannerGroups) {
        this.plannerGroups = plannerGroups;
    }

    public List<PlannerActivityData> getPlannerActivities() {
        return plannerActivities;
    }

    public void setPlannerActivities(List<PlannerActivityData> plannerActivities) {
        this.plannerActivities = plannerActivities;
    }

    public List<WeekPlannerSlotData> getWeekPlannerSlots() {
        return weekPlannerSlots;
    }

    public void setWeekPlannerSlots(List<WeekPlannerSlotData> weekPlannerSlots) {
        this.weekPlannerSlots = weekPlannerSlots;
    }

    public List<SlotConfirmationData> getSlotConfirmations() {
        return slotConfirmations;
    }

    public void setSlotConfirmations(List<SlotConfirmationData> slotConfirmations) {
        this.slotConfirmations = slotConfirmations;
    }

    public static class UserData {
        private String id;
        private String username;
        private String email;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class HabitData {
        private String id;
        private String name;
        private String description;
        private String categoryId;
        private String frequencyType;
        private int targetFrequency;
        private int maxEntriesPerDay;
        private boolean positiveScoring;
        private String createdAt;
        private ScoringRuleData scoringRule;
        private List<HabitEntryData> entries = new ArrayList<>();
        private Map<String, String> nameTranslations = new HashMap<>();
        private Map<String, String> descriptionTranslations = new HashMap<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getFrequencyType() {
            return frequencyType;
        }

        public void setFrequencyType(String frequencyType) {
            this.frequencyType = frequencyType;
        }

        public int getTargetFrequency() {
            return targetFrequency;
        }

        public void setTargetFrequency(int targetFrequency) {
            this.targetFrequency = targetFrequency;
        }

        public int getMaxEntriesPerDay() {
            return maxEntriesPerDay;
        }

        public void setMaxEntriesPerDay(int maxEntriesPerDay) {
            this.maxEntriesPerDay = maxEntriesPerDay;
        }

        public boolean isPositiveScoring() {
            return positiveScoring;
        }

        public void setPositiveScoring(boolean positiveScoring) {
            this.positiveScoring = positiveScoring;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public ScoringRuleData getScoringRule() {
            return scoringRule;
        }

        public void setScoringRule(ScoringRuleData scoringRule) {
            this.scoringRule = scoringRule;
        }

        public List<HabitEntryData> getEntries() {
            return entries;
        }

        public void setEntries(List<HabitEntryData> entries) {
            this.entries = entries;
        }

        public Map<String, String> getNameTranslations() {
            return nameTranslations;
        }

        public void setNameTranslations(Map<String, String> nameTranslations) {
            this.nameTranslations = nameTranslations;
        }

        public Map<String, String> getDescriptionTranslations() {
            return descriptionTranslations;
        }

        public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
            this.descriptionTranslations = descriptionTranslations;
        }
    }

    public static class HabitEntryData {
        private String id;
        private String completedAt;
        private String notes;
        private int value;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class ScoringRuleData {
        private String id;
        private String name;
        private int thresholdFor1Point;
        private int thresholdFor2Points;
        private int thresholdFor4Points;
        private int thresholdFor8Points;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getThresholdFor1Point() {
            return thresholdFor1Point;
        }

        public void setThresholdFor1Point(int thresholdFor1Point) {
            this.thresholdFor1Point = thresholdFor1Point;
        }

        public int getThresholdFor2Points() {
            return thresholdFor2Points;
        }

        public void setThresholdFor2Points(int thresholdFor2Points) {
            this.thresholdFor2Points = thresholdFor2Points;
        }

        public int getThresholdFor4Points() {
            return thresholdFor4Points;
        }

        public void setThresholdFor4Points(int thresholdFor4Points) {
            this.thresholdFor4Points = thresholdFor4Points;
        }

        public int getThresholdFor8Points() {
            return thresholdFor8Points;
        }

        public void setThresholdFor8Points(int thresholdFor8Points) {
            this.thresholdFor8Points = thresholdFor8Points;
        }
    }

    public static class CategoryData {
        private String id;
        private String name;
        private String description;
        private String color;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public static class DiaryEntryData {
        private String id;
        private String description;
        private String significance;
        private String eventDate;
        private String createdAt;
        private String startTime;
        private String endTime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSignificance() {
            return significance;
        }

        public void setSignificance(String significance) {
            this.significance = significance;
        }

        public String getEventDate() {
            return eventDate;
        }

        public void setEventDate(String eventDate) {
            this.eventDate = eventDate;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

    public static class SleepEntryData {
        private String id;
        private String fromTime;
        private String untilTime;
        private String date;
        private String notes;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFromTime() {
            return fromTime;
        }

        public void setFromTime(String fromTime) {
            this.fromTime = fromTime;
        }

        public String getUntilTime() {
            return untilTime;
        }

        public void setUntilTime(String untilTime) {
            this.untilTime = untilTime;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class SportLogData {
        private String id;
        private String name;
        private double measurement;
        private String measurementUnit;
        private String startTime;
        private String endTime;
        private String date;
        private String notes;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getMeasurement() {
            return measurement;
        }

        public void setMeasurement(double measurement) {
            this.measurement = measurement;
        }

        public String getMeasurementUnit() {
            return measurementUnit;
        }

        public void setMeasurementUnit(String measurementUnit) {
            this.measurementUnit = measurementUnit;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class FoodLogData {
        private String id;
        private Double carbohydrates;
        private Integer kcal;
        private String dateTime;
        private String foodItems;
        private String notes;
        private String createdAt;
        private List<String> tagNames = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getCarbohydrates() {
            return carbohydrates;
        }

        public void setCarbohydrates(Double carbohydrates) {
            this.carbohydrates = carbohydrates;
        }

        public Integer getKcal() {
            return kcal;
        }

        public void setKcal(Integer kcal) {
            this.kcal = kcal;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getFoodItems() {
            return foodItems;
        }

        public void setFoodItems(String foodItems) {
            this.foodItems = foodItems;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public List<String> getTagNames() {
            return tagNames;
        }

        public void setTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
        }
    }

    public static class FoodTagData {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class EmotionPairData {
        private String id;
        private String negativeLabel;
        private String positiveLabel;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNegativeLabel() {
            return negativeLabel;
        }

        public void setNegativeLabel(String negativeLabel) {
            this.negativeLabel = negativeLabel;
        }

        public String getPositiveLabel() {
            return positiveLabel;
        }

        public void setPositiveLabel(String positiveLabel) {
            this.positiveLabel = positiveLabel;
        }
    }

    public static class EmotionEntryData {
        private String id;
        private String emotionPairId;
        private int strength;
        private String recordedAt;
        private String notes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmotionPairId() {
            return emotionPairId;
        }

        public void setEmotionPairId(String emotionPairId) {
            this.emotionPairId = emotionPairId;
        }

        public int getStrength() {
            return strength;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }

        public String getRecordedAt() {
            return recordedAt;
        }

        public void setRecordedAt(String recordedAt) {
            this.recordedAt = recordedAt;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class MeetingEntryData {
        private String id;
        private String place;
        private String attendants;
        private String startTime;
        private String endTime;
        private String date;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public String getAttendants() {
            return attendants;
        }

        public void setAttendants(String attendants) {
            this.attendants = attendants;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ActivityLogData {
        private String id;
        private String persons;
        private String location;
        private String startTime;
        private String endTime;
        private String date;
        private String activity;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPersons() {
            return persons;
        }

        public void setPersons(String persons) {
            this.persons = persons;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ReminderSettingsData {
        private String id;
        private boolean sleepReminderEnabled;
        private String sleepReminderTime;
        private boolean diaryReminderEnabled;
        private String diaryReminderTime;
        private boolean emotionReminderEnabled;
        private int emotionReminderCount;
        private String wakingHoursStart;
        private String wakingHoursEnd;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isSleepReminderEnabled() {
            return sleepReminderEnabled;
        }

        public void setSleepReminderEnabled(boolean sleepReminderEnabled) {
            this.sleepReminderEnabled = sleepReminderEnabled;
        }

        public String getSleepReminderTime() {
            return sleepReminderTime;
        }

        public void setSleepReminderTime(String sleepReminderTime) {
            this.sleepReminderTime = sleepReminderTime;
        }

        public boolean isDiaryReminderEnabled() {
            return diaryReminderEnabled;
        }

        public void setDiaryReminderEnabled(boolean diaryReminderEnabled) {
            this.diaryReminderEnabled = diaryReminderEnabled;
        }

        public String getDiaryReminderTime() {
            return diaryReminderTime;
        }

        public void setDiaryReminderTime(String diaryReminderTime) {
            this.diaryReminderTime = diaryReminderTime;
        }

        public boolean isEmotionReminderEnabled() {
            return emotionReminderEnabled;
        }

        public void setEmotionReminderEnabled(boolean emotionReminderEnabled) {
            this.emotionReminderEnabled = emotionReminderEnabled;
        }

        public int getEmotionReminderCount() {
            return emotionReminderCount;
        }

        public void setEmotionReminderCount(int emotionReminderCount) {
            this.emotionReminderCount = emotionReminderCount;
        }

        public String getWakingHoursStart() {
            return wakingHoursStart;
        }

        public void setWakingHoursStart(String wakingHoursStart) {
            this.wakingHoursStart = wakingHoursStart;
        }

        public String getWakingHoursEnd() {
            return wakingHoursEnd;
        }

        public void setWakingHoursEnd(String wakingHoursEnd) {
            this.wakingHoursEnd = wakingHoursEnd;
        }
    }

    public static class MedicationData {
        private String id;
        private String name;
        private String wikipediaLink;
        private String provisionType;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getWikipediaLink() { return wikipediaLink; }
        public void setWikipediaLink(String wikipediaLink) { this.wikipediaLink = wikipediaLink; }
        public String getProvisionType() { return provisionType; }
        public void setProvisionType(String provisionType) { this.provisionType = provisionType; }
    }

    public static class MedicationLogData {
        private String id;
        private String medicationId;
        private double amount;
        private String takenAt;
        private String notes;
        private String createdAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMedicationId() { return medicationId; }
        public void setMedicationId(String medicationId) { this.medicationId = medicationId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getTakenAt() { return takenAt; }
        public void setTakenAt(String takenAt) { this.takenAt = takenAt; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class ModuleVisibilityData {
        private String id;
        private boolean diaryVisible;
        private boolean sleepVisible;
        private boolean emotionsVisible;
        private boolean pointsVisible;
        private boolean statisticsVisible;
        private boolean foodLogVisible;
        private boolean sportLogVisible;
        private boolean medicationVisible;
        private boolean backupVisible;
        private boolean pdfExportVisible;
        private boolean activityLogVisible;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public boolean isDiaryVisible() { return diaryVisible; }
        public void setDiaryVisible(boolean diaryVisible) { this.diaryVisible = diaryVisible; }
        public boolean isSleepVisible() { return sleepVisible; }
        public void setSleepVisible(boolean sleepVisible) { this.sleepVisible = sleepVisible; }
        public boolean isEmotionsVisible() { return emotionsVisible; }
        public void setEmotionsVisible(boolean emotionsVisible) { this.emotionsVisible = emotionsVisible; }
        public boolean isPointsVisible() { return pointsVisible; }
        public void setPointsVisible(boolean pointsVisible) { this.pointsVisible = pointsVisible; }
        public boolean isStatisticsVisible() { return statisticsVisible; }
        public void setStatisticsVisible(boolean statisticsVisible) { this.statisticsVisible = statisticsVisible; }
        public boolean isFoodLogVisible() { return foodLogVisible; }
        public void setFoodLogVisible(boolean foodLogVisible) { this.foodLogVisible = foodLogVisible; }
        public boolean isSportLogVisible() { return sportLogVisible; }
        public void setSportLogVisible(boolean sportLogVisible) { this.sportLogVisible = sportLogVisible; }
        public boolean isMedicationVisible() { return medicationVisible; }
        public void setMedicationVisible(boolean medicationVisible) { this.medicationVisible = medicationVisible; }
        public boolean isBackupVisible() { return backupVisible; }
        public void setBackupVisible(boolean backupVisible) { this.backupVisible = backupVisible; }
        public boolean isPdfExportVisible() { return pdfExportVisible; }
        public void setPdfExportVisible(boolean pdfExportVisible) { this.pdfExportVisible = pdfExportVisible; }
        public boolean isActivityLogVisible() { return activityLogVisible; }
        public void setActivityLogVisible(boolean activityLogVisible) { this.activityLogVisible = activityLogVisible; }
    }

    public static class EmergencyPlanStepData {
        private String id;
        private String question;
        private int stepOrder;
        private List<EmergencyPlanActionData> actions = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public int getStepOrder() { return stepOrder; }
        public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
        public List<EmergencyPlanActionData> getActions() { return actions; }
        public void setActions(List<EmergencyPlanActionData> actions) { this.actions = actions; }
    }

    public static class EmergencyPlanActionData {
        private String id;
        private String actionText;
        private String phoneNumber;
        private int actionOrder;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getActionText() { return actionText; }
        public void setActionText(String actionText) { this.actionText = actionText; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public int getActionOrder() { return actionOrder; }
        public void setActionOrder(int actionOrder) { this.actionOrder = actionOrder; }
    }

    public static class PlannerGroupData {
        private String id;
        private String name;
        private String description;
        private String createdAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class PlannerActivityData {
        private String id;
        private String name;
        private String description;
        private String createdAt;
        private List<String> groupIds = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public List<String> getGroupIds() { return groupIds; }
        public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }
    }

    public static class WeekPlannerSlotData {
        private String id;
        private int dayOfWeek;
        private int hour;
        private List<String> groupIds = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        public List<String> getGroupIds() { return groupIds; }
        public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }
    }

    public static class SlotConfirmationData {
        private String id;
        private String slotId;
        private String activityId;
        private String groupId;
        private boolean confirmed;
        private String date;
        private String createdAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSlotId() { return slotId; }
        public void setSlotId(String slotId) { this.slotId = slotId; }
        public String getActivityId() { return activityId; }
        public void setActivityId(String activityId) { this.activityId = activityId; }
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public boolean isConfirmed() { return confirmed; }
        public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
