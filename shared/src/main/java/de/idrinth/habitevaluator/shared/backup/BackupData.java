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
    }
}
