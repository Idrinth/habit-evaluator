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
        assertEquals(1, data.getVersion());
        assertNull(data.getUser());
        assertNotNull(data.getHabits());
        assertTrue(data.getHabits().isEmpty());
        assertNotNull(data.getCategories());
        assertTrue(data.getCategories().isEmpty());
        assertNotNull(data.getDiaryEntries());
        assertTrue(data.getDiaryEntries().isEmpty());
        assertNotNull(data.getSleepEntries());
        assertTrue(data.getSleepEntries().isEmpty());
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

        assertEquals("d1", entry.getId());
        assertEquals("Good day", entry.getDescription());
        assertEquals("MAJOR", entry.getSignificance());
        assertEquals("2026-02-01", entry.getEventDate());
        assertEquals("2026-02-01T10:00:00", entry.getCreatedAt());
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
}
