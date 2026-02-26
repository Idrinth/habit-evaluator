package de.idrinth.habitevaluator.shared.backup;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BackupDataTest {

    @Test
    void testDefaultState() {
        BackupData data = new BackupData();
        assertNull(data.getBackupDate());
        assertEquals(2, data.getVersion());
        assertNull(data.getUser());
        assertNotNull(data.getHabits());
        assertTrue(data.getHabits().isEmpty());
        assertNotNull(data.getCategories());
        assertTrue(data.getCategories().isEmpty());
        assertNotNull(data.getDiaryEntries());
        assertTrue(data.getDiaryEntries().isEmpty());
        assertNotNull(data.getSleepEntries());
        assertTrue(data.getSleepEntries().isEmpty());
        assertNotNull(data.getEmotionPairs());
        assertTrue(data.getEmotionPairs().isEmpty());
        assertNotNull(data.getEmotionEntries());
        assertTrue(data.getEmotionEntries().isEmpty());
        assertNull(data.getReminderSettings());
    }

    @Test
    void testSetBackupDate() {
        BackupData data = new BackupData();
        data.setBackupDate("2026-02-01T10:00:00");
        assertEquals("2026-02-01T10:00:00", data.getBackupDate());
    }

    @Test
    void testSetVersion() {
        BackupData data = new BackupData();
        data.setVersion(2);
        assertEquals(2, data.getVersion());
    }

    @Test
    void testSetUser() {
        BackupData data = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId("u1");
        userData.setUsername("john");
        userData.setEmail("john@test.com");
        userData.setCreatedAt("2026-01-01T00:00:00");
        data.setUser(userData);

        assertEquals("u1", data.getUser().getId());
        assertEquals("john", data.getUser().getUsername());
        assertEquals("john@test.com", data.getUser().getEmail());
        assertEquals("2026-01-01T00:00:00", data.getUser().getCreatedAt());
    }

    @Test
    void testSetHabits() {
        BackupData data = new BackupData();
        List<BackupData.HabitData> habits = new ArrayList<>();
        BackupData.HabitData habit = new BackupData.HabitData();
        habit.setId("h1");
        habit.setName("Exercise");
        habit.setDescription("Daily workout");
        habit.setCategoryId("cat-1");
        habit.setFrequencyType("WEEKLY");
        habit.setTargetFrequency(7);
        habit.setMaxEntriesPerDay(1);
        habit.setPositiveScoring(true);
        habit.setCreatedAt("2026-01-01T00:00:00");
        habits.add(habit);
        data.setHabits(habits);

        assertEquals(1, data.getHabits().size());
        BackupData.HabitData h = data.getHabits().get(0);
        assertEquals("h1", h.getId());
        assertEquals("Exercise", h.getName());
        assertEquals("Daily workout", h.getDescription());
        assertEquals("cat-1", h.getCategoryId());
        assertEquals("WEEKLY", h.getFrequencyType());
        assertEquals(7, h.getTargetFrequency());
        assertEquals(1, h.getMaxEntriesPerDay());
        assertTrue(h.isPositiveScoring());
    }

    @Test
    void testHabitDataEntries() {
        BackupData.HabitData habit = new BackupData.HabitData();
        assertNotNull(habit.getEntries());
        assertTrue(habit.getEntries().isEmpty());

        List<BackupData.HabitEntryData> entries = new ArrayList<>();
        BackupData.HabitEntryData entry = new BackupData.HabitEntryData();
        entry.setId("e1");
        entry.setCompletedAt("2026-01-15T10:00:00");
        entry.setNotes("Done");
        entry.setValue(1);
        entries.add(entry);
        habit.setEntries(entries);

        assertEquals(1, habit.getEntries().size());
        assertEquals("e1", habit.getEntries().get(0).getId());
        assertEquals("2026-01-15T10:00:00", habit.getEntries().get(0).getCompletedAt());
        assertEquals("Done", habit.getEntries().get(0).getNotes());
        assertEquals(1, habit.getEntries().get(0).getValue());
    }

    @Test
    void testHabitDataTranslations() {
        BackupData.HabitData habit = new BackupData.HabitData();
        assertNotNull(habit.getNameTranslations());
        assertNotNull(habit.getDescriptionTranslations());

        Map<String, String> nameT = new HashMap<>();
        nameT.put("de", "Übung");
        habit.setNameTranslations(nameT);
        assertEquals("Übung", habit.getNameTranslations().get("de"));

        Map<String, String> descT = new HashMap<>();
        descT.put("de", "Tägliches Training");
        habit.setDescriptionTranslations(descT);
        assertEquals("Tägliches Training", habit.getDescriptionTranslations().get("de"));
    }

    @Test
    void testScoringRuleData() {
        BackupData.ScoringRuleData rule = new BackupData.ScoringRuleData();
        rule.setId("r1");
        rule.setName("custom");
        rule.setThresholdFor1Point(1);
        rule.setThresholdFor2Points(3);
        rule.setThresholdFor4Points(5);
        rule.setThresholdFor8Points(7);

        assertEquals("r1", rule.getId());
        assertEquals("custom", rule.getName());
        assertEquals(1, rule.getThresholdFor1Point());
        assertEquals(3, rule.getThresholdFor2Points());
        assertEquals(5, rule.getThresholdFor4Points());
        assertEquals(7, rule.getThresholdFor8Points());

        BackupData.HabitData habit = new BackupData.HabitData();
        habit.setScoringRule(rule);
        assertSame(rule, habit.getScoringRule());
    }

    @Test
    void testCategoryData() {
        BackupData.CategoryData cat = new BackupData.CategoryData();
        cat.setId("c1");
        cat.setName("Hobbies");
        cat.setDescription("Leisure activities");
        cat.setColor("#4CAF50");

        assertEquals("c1", cat.getId());
        assertEquals("Hobbies", cat.getName());
        assertEquals("Leisure activities", cat.getDescription());
        assertEquals("#4CAF50", cat.getColor());
    }

    @Test
    void testSetCategories() {
        BackupData data = new BackupData();
        List<BackupData.CategoryData> categories = new ArrayList<>();
        BackupData.CategoryData cat = new BackupData.CategoryData();
        cat.setName("Work");
        categories.add(cat);
        data.setCategories(categories);
        assertEquals(1, data.getCategories().size());
    }

    @Test
    void testDiaryEntryData() {
        BackupData.DiaryEntryData entry = new BackupData.DiaryEntryData();
        entry.setId("d1");
        entry.setDescription("Good day");
        entry.setSignificance("MAJOR");
        entry.setEventDate("2026-02-01");
        entry.setCreatedAt("2026-02-01T10:00:00");
        entry.setStartTime("09:00");
        entry.setEndTime("10:30");

        assertEquals("d1", entry.getId());
        assertEquals("Good day", entry.getDescription());
        assertEquals("MAJOR", entry.getSignificance());
        assertEquals("2026-02-01", entry.getEventDate());
        assertEquals("2026-02-01T10:00:00", entry.getCreatedAt());
        assertEquals("09:00", entry.getStartTime());
        assertEquals("10:30", entry.getEndTime());
    }

    @Test
    void testSetDiaryEntries() {
        BackupData data = new BackupData();
        List<BackupData.DiaryEntryData> entries = new ArrayList<>();
        entries.add(new BackupData.DiaryEntryData());
        data.setDiaryEntries(entries);
        assertEquals(1, data.getDiaryEntries().size());
    }

    @Test
    void testSleepEntryData() {
        BackupData.SleepEntryData entry = new BackupData.SleepEntryData();
        entry.setId("s1");
        entry.setFromTime("22:00");
        entry.setUntilTime("06:00");
        entry.setDate("2026-02-01");
        entry.setNotes("Slept well");
        entry.setCreatedAt("2026-02-01T22:00:00");

        assertEquals("s1", entry.getId());
        assertEquals("22:00", entry.getFromTime());
        assertEquals("06:00", entry.getUntilTime());
        assertEquals("2026-02-01", entry.getDate());
        assertEquals("Slept well", entry.getNotes());
        assertEquals("2026-02-01T22:00:00", entry.getCreatedAt());
    }

    @Test
    void testSetSleepEntries() {
        BackupData data = new BackupData();
        List<BackupData.SleepEntryData> entries = new ArrayList<>();
        entries.add(new BackupData.SleepEntryData());
        data.setSleepEntries(entries);
        assertEquals(1, data.getSleepEntries().size());
    }

    @Test
    void testEmotionPairData() {
        BackupData.EmotionPairData pair = new BackupData.EmotionPairData();
        pair.setId("ep1");
        pair.setNegativeLabel("Sad");
        pair.setPositiveLabel("Happy");

        assertEquals("ep1", pair.getId());
        assertEquals("Sad", pair.getNegativeLabel());
        assertEquals("Happy", pair.getPositiveLabel());
    }

    @Test
    void testSetEmotionPairs() {
        BackupData data = new BackupData();
        List<BackupData.EmotionPairData> pairs = new ArrayList<>();
        BackupData.EmotionPairData pair = new BackupData.EmotionPairData();
        pair.setNegativeLabel("Anxious");
        pair.setPositiveLabel("Calm");
        pairs.add(pair);
        data.setEmotionPairs(pairs);
        assertEquals(1, data.getEmotionPairs().size());
        assertEquals("Anxious", data.getEmotionPairs().get(0).getNegativeLabel());
    }

    @Test
    void testEmotionEntryData() {
        BackupData.EmotionEntryData entry = new BackupData.EmotionEntryData();
        entry.setId("ee1");
        entry.setEmotionPairId("ep1");
        entry.setStrength(7);
        entry.setRecordedAt("2026-02-01T14:30:00");
        entry.setNotes("Feeling good");

        assertEquals("ee1", entry.getId());
        assertEquals("ep1", entry.getEmotionPairId());
        assertEquals(7, entry.getStrength());
        assertEquals("2026-02-01T14:30:00", entry.getRecordedAt());
        assertEquals("Feeling good", entry.getNotes());
    }

    @Test
    void testSetEmotionEntries() {
        BackupData data = new BackupData();
        List<BackupData.EmotionEntryData> entries = new ArrayList<>();
        entries.add(new BackupData.EmotionEntryData());
        data.setEmotionEntries(entries);
        assertEquals(1, data.getEmotionEntries().size());
    }

    @Test
    void testReminderSettingsData() {
        BackupData.ReminderSettingsData settings = new BackupData.ReminderSettingsData();
        settings.setId("rs1");
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime("08:00");
        settings.setDiaryReminderEnabled(true);
        settings.setDiaryReminderTime("20:00");
        settings.setEmotionReminderEnabled(true);
        settings.setEmotionReminderCount(5);
        settings.setWakingHoursStart("07:00");
        settings.setWakingHoursEnd("22:00");

        assertEquals("rs1", settings.getId());
        assertTrue(settings.isSleepReminderEnabled());
        assertEquals("08:00", settings.getSleepReminderTime());
        assertTrue(settings.isDiaryReminderEnabled());
        assertEquals("20:00", settings.getDiaryReminderTime());
        assertTrue(settings.isEmotionReminderEnabled());
        assertEquals(5, settings.getEmotionReminderCount());
        assertEquals("07:00", settings.getWakingHoursStart());
        assertEquals("22:00", settings.getWakingHoursEnd());
    }

    @Test
    void testSetReminderSettings() {
        BackupData data = new BackupData();
        assertNull(data.getReminderSettings());

        BackupData.ReminderSettingsData settings = new BackupData.ReminderSettingsData();
        settings.setSleepReminderEnabled(true);
        data.setReminderSettings(settings);

        assertNotNull(data.getReminderSettings());
        assertTrue(data.getReminderSettings().isSleepReminderEnabled());
    }

    @Test
    void testDefaultStateIncludesAllLists() {
        BackupData data = new BackupData();
        assertNotNull(data.getSportLogs());
        assertTrue(data.getSportLogs().isEmpty());
        assertNotNull(data.getFoodLogs());
        assertTrue(data.getFoodLogs().isEmpty());
        assertNotNull(data.getFoodTags());
        assertTrue(data.getFoodTags().isEmpty());
        assertNotNull(data.getMeetingEntries());
        assertTrue(data.getMeetingEntries().isEmpty());
        assertNotNull(data.getMedications());
        assertTrue(data.getMedications().isEmpty());
        assertNotNull(data.getMedicationLogs());
        assertTrue(data.getMedicationLogs().isEmpty());
    }

    @Test
    void testSportLogData() {
        BackupData.SportLogData entry = new BackupData.SportLogData();
        entry.setId("sl1");
        entry.setName("Running");
        entry.setMeasurement(5.2);
        entry.setMeasurementUnit("km");
        entry.setStartTime("07:00");
        entry.setEndTime("07:45");
        entry.setDate("2026-02-01");
        entry.setNotes("Morning run");
        entry.setCreatedAt("2026-02-01T07:00:00");

        assertEquals("sl1", entry.getId());
        assertEquals("Running", entry.getName());
        assertEquals(5.2, entry.getMeasurement());
        assertEquals("km", entry.getMeasurementUnit());
        assertEquals("07:00", entry.getStartTime());
        assertEquals("07:45", entry.getEndTime());
        assertEquals("2026-02-01", entry.getDate());
        assertEquals("Morning run", entry.getNotes());
        assertEquals("2026-02-01T07:00:00", entry.getCreatedAt());
    }

    @Test
    void testSetSportLogs() {
        BackupData data = new BackupData();
        List<BackupData.SportLogData> logs = new ArrayList<>();
        BackupData.SportLogData log = new BackupData.SportLogData();
        log.setName("Swimming");
        logs.add(log);
        data.setSportLogs(logs);
        assertEquals(1, data.getSportLogs().size());
        assertEquals("Swimming", data.getSportLogs().get(0).getName());
    }

    @Test
    void testFoodLogData() {
        BackupData.FoodLogData entry = new BackupData.FoodLogData();
        entry.setId("fl1");
        entry.setCarbohydrates(45.5);
        entry.setKcal(350);
        entry.setDateTime("2026-02-01T12:30:00");
        entry.setFoodItems("Rice, Chicken");
        entry.setNotes("Lunch");
        entry.setCreatedAt("2026-02-01T12:30:00");

        List<String> tagNames = new ArrayList<>();
        tagNames.add("Healthy");
        tagNames.add("HighProtein");
        entry.setTagNames(tagNames);

        assertEquals("fl1", entry.getId());
        assertEquals(45.5, entry.getCarbohydrates());
        assertEquals(350, entry.getKcal());
        assertEquals("2026-02-01T12:30:00", entry.getDateTime());
        assertEquals("Rice, Chicken", entry.getFoodItems());
        assertEquals("Lunch", entry.getNotes());
        assertEquals("2026-02-01T12:30:00", entry.getCreatedAt());
        assertEquals(2, entry.getTagNames().size());
        assertEquals("Healthy", entry.getTagNames().get(0));
    }

    @Test
    void testFoodLogDataDefaultTagNames() {
        BackupData.FoodLogData entry = new BackupData.FoodLogData();
        assertNotNull(entry.getTagNames());
        assertTrue(entry.getTagNames().isEmpty());
    }

    @Test
    void testSetFoodLogs() {
        BackupData data = new BackupData();
        List<BackupData.FoodLogData> logs = new ArrayList<>();
        logs.add(new BackupData.FoodLogData());
        data.setFoodLogs(logs);
        assertEquals(1, data.getFoodLogs().size());
    }

    @Test
    void testFoodTagData() {
        BackupData.FoodTagData tag = new BackupData.FoodTagData();
        tag.setId("ft1");
        tag.setName("Vegan");

        assertEquals("ft1", tag.getId());
        assertEquals("Vegan", tag.getName());
    }

    @Test
    void testSetFoodTags() {
        BackupData data = new BackupData();
        List<BackupData.FoodTagData> tags = new ArrayList<>();
        BackupData.FoodTagData tag = new BackupData.FoodTagData();
        tag.setName("Organic");
        tags.add(tag);
        data.setFoodTags(tags);
        assertEquals(1, data.getFoodTags().size());
        assertEquals("Organic", data.getFoodTags().get(0).getName());
    }

    @Test
    void testMeetingEntryData() {
        BackupData.MeetingEntryData entry = new BackupData.MeetingEntryData();
        entry.setId("me1");
        entry.setPlace("Office Room A");
        entry.setAttendants("Alice, Bob, Charlie");
        entry.setStartTime("14:00");
        entry.setEndTime("15:30");
        entry.setDate("2026-02-01");
        entry.setCreatedAt("2026-02-01T14:00:00");

        assertEquals("me1", entry.getId());
        assertEquals("Office Room A", entry.getPlace());
        assertEquals("Alice, Bob, Charlie", entry.getAttendants());
        assertEquals("14:00", entry.getStartTime());
        assertEquals("15:30", entry.getEndTime());
        assertEquals("2026-02-01", entry.getDate());
        assertEquals("2026-02-01T14:00:00", entry.getCreatedAt());
    }

    @Test
    void testSetMeetingEntries() {
        BackupData data = new BackupData();
        List<BackupData.MeetingEntryData> entries = new ArrayList<>();
        BackupData.MeetingEntryData entry = new BackupData.MeetingEntryData();
        entry.setPlace("Cafe");
        entries.add(entry);
        data.setMeetingEntries(entries);
        assertEquals(1, data.getMeetingEntries().size());
        assertEquals("Cafe", data.getMeetingEntries().get(0).getPlace());
    }

    @Test
    void testMedicationData() {
        BackupData.MedicationData med = new BackupData.MedicationData();
        med.setId("med1");
        med.setName("Ibuprofen");
        med.setWikipediaLink("https://en.wikipedia.org/wiki/Ibuprofen");
        med.setProvisionType("PILL");

        assertEquals("med1", med.getId());
        assertEquals("Ibuprofen", med.getName());
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", med.getWikipediaLink());
        assertEquals("PILL", med.getProvisionType());
    }

    @Test
    void testSetMedications() {
        BackupData data = new BackupData();
        List<BackupData.MedicationData> meds = new ArrayList<>();
        BackupData.MedicationData med = new BackupData.MedicationData();
        med.setName("Aspirin");
        meds.add(med);
        data.setMedications(meds);
        assertEquals(1, data.getMedications().size());
        assertEquals("Aspirin", data.getMedications().get(0).getName());
    }

    @Test
    void testMedicationLogData() {
        BackupData.MedicationLogData log = new BackupData.MedicationLogData();
        log.setId("ml1");
        log.setMedicationId("med1");
        log.setAmount(400.0);
        log.setTakenAt("2026-02-01T08:00:00");
        log.setNotes("After breakfast");
        log.setCreatedAt("2026-02-01T08:00:00");

        assertEquals("ml1", log.getId());
        assertEquals("med1", log.getMedicationId());
        assertEquals(400.0, log.getAmount());
        assertEquals("2026-02-01T08:00:00", log.getTakenAt());
        assertEquals("After breakfast", log.getNotes());
        assertEquals("2026-02-01T08:00:00", log.getCreatedAt());
    }

    @Test
    void testSetMedicationLogs() {
        BackupData data = new BackupData();
        List<BackupData.MedicationLogData> logs = new ArrayList<>();
        logs.add(new BackupData.MedicationLogData());
        data.setMedicationLogs(logs);
        assertEquals(1, data.getMedicationLogs().size());
    }

    @Test
    void testFoodLogDataNullableFields() {
        BackupData.FoodLogData entry = new BackupData.FoodLogData();
        assertNull(entry.getCarbohydrates());
        assertNull(entry.getKcal());
        assertNull(entry.getDateTime());
        assertNull(entry.getFoodItems());
        assertNull(entry.getNotes());
    }

    @Test
    void testUserDataIdField() {
        BackupData.UserData userData = new BackupData.UserData();
        assertNull(userData.getId());
        userData.setId("user-uuid-123");
        assertEquals("user-uuid-123", userData.getId());
    }

    @Test
    void testDefaultStateIncludesModuleVisibilityAndEmergencyPlan() {
        BackupData data = new BackupData();
        assertNull(data.getModuleVisibility());
        assertNotNull(data.getEmergencyPlanSteps());
        assertTrue(data.getEmergencyPlanSteps().isEmpty());
    }

    @Test
    void testModuleVisibilityData() {
        BackupData.ModuleVisibilityData vis = new BackupData.ModuleVisibilityData();
        vis.setId("mv1");
        vis.setDiaryVisible(true);
        vis.setSleepVisible(false);
        vis.setEmotionsVisible(true);
        vis.setPointsVisible(false);
        vis.setStatisticsVisible(true);
        vis.setFoodLogVisible(false);
        vis.setSportLogVisible(true);
        vis.setMedicationVisible(false);
        vis.setBackupVisible(true);
        vis.setPdfExportVisible(false);
        vis.setActivityLogVisible(true);

        assertEquals("mv1", vis.getId());
        assertTrue(vis.isDiaryVisible());
        assertFalse(vis.isSleepVisible());
        assertTrue(vis.isEmotionsVisible());
        assertFalse(vis.isPointsVisible());
        assertTrue(vis.isStatisticsVisible());
        assertFalse(vis.isFoodLogVisible());
        assertTrue(vis.isSportLogVisible());
        assertFalse(vis.isMedicationVisible());
        assertTrue(vis.isBackupVisible());
        assertFalse(vis.isPdfExportVisible());
        assertTrue(vis.isActivityLogVisible());
    }

    @Test
    void testSetModuleVisibility() {
        BackupData data = new BackupData();
        BackupData.ModuleVisibilityData vis = new BackupData.ModuleVisibilityData();
        vis.setDiaryVisible(false);
        data.setModuleVisibility(vis);
        assertNotNull(data.getModuleVisibility());
        assertFalse(data.getModuleVisibility().isDiaryVisible());
    }

    @Test
    void testEmergencyPlanStepData() {
        BackupData.EmergencyPlanStepData step = new BackupData.EmergencyPlanStepData();
        step.setId("eps1");
        step.setQuestion("Are you safe?");
        step.setStepOrder(1);

        assertEquals("eps1", step.getId());
        assertEquals("Are you safe?", step.getQuestion());
        assertEquals(1, step.getStepOrder());
        assertNotNull(step.getActions());
        assertTrue(step.getActions().isEmpty());
    }

    @Test
    void testEmergencyPlanActionData() {
        BackupData.EmergencyPlanActionData action = new BackupData.EmergencyPlanActionData();
        action.setId("epa1");
        action.setActionText("Call emergency services");
        action.setPhoneNumber("112");
        action.setActionOrder(1);

        assertEquals("epa1", action.getId());
        assertEquals("Call emergency services", action.getActionText());
        assertEquals("112", action.getPhoneNumber());
        assertEquals(1, action.getActionOrder());
    }

    @Test
    void testEmergencyPlanStepWithActions() {
        BackupData.EmergencyPlanStepData step = new BackupData.EmergencyPlanStepData();
        step.setQuestion("Do you need help?");

        List<BackupData.EmergencyPlanActionData> actions = new ArrayList<>();
        BackupData.EmergencyPlanActionData action = new BackupData.EmergencyPlanActionData();
        action.setActionText("Breathe deeply");
        actions.add(action);
        step.setActions(actions);

        assertEquals(1, step.getActions().size());
        assertEquals("Breathe deeply", step.getActions().get(0).getActionText());
    }

    @Test
    void testSetEmergencyPlanSteps() {
        BackupData data = new BackupData();
        List<BackupData.EmergencyPlanStepData> steps = new ArrayList<>();
        BackupData.EmergencyPlanStepData step = new BackupData.EmergencyPlanStepData();
        step.setQuestion("Are you okay?");
        steps.add(step);
        data.setEmergencyPlanSteps(steps);
        assertEquals(1, data.getEmergencyPlanSteps().size());
        assertEquals("Are you okay?", data.getEmergencyPlanSteps().get(0).getQuestion());
    }
}
