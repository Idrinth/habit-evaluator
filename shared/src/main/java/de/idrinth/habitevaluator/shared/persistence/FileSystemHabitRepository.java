package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemHabitRepository implements HabitRepository {

    private final Map<String, Habit> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemHabitRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "habits.json");
        this.gson = GsonSerializers.createGsonWithDateTime();
        load();
    }

    @Override
    public synchronized Habit save(Habit habit) {
        store.put(habit.getId(), habit);
        persist();
        return habit;
    }

    @Override
    public Optional<Habit> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Habit> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        return store.values().stream()
                .filter(h -> h.getUser() != null && userId.equals(h.getUser().getId()))
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (Habit habit : store.values()) {
                array.add(serializeHabit(habit));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist habits to filesystem", e);
        }
    }

    private void load() {
        if (!storageFile.exists() || storageFile.length() == 0) {
            return;
        }
        try (FileReader reader = new FileReader(storageFile)) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            if (array == null) {
                return;
            }
            for (JsonElement element : array) {
                Habit habit = deserializeHabit(element.getAsJsonObject());
                store.put(habit.getId(), habit);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load habits from filesystem", e);
        }
    }

    private JsonObject serializeHabit(Habit habit) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", habit.getId());
        obj.addProperty("name", habit.getName());
        obj.addProperty("description", habit.getDescription());
        obj.addProperty("categoryId", habit.getCategoryId());
        obj.addProperty("frequencyType", habit.getFrequencyType() != null ? habit.getFrequencyType().name() : null);
        obj.addProperty("targetFrequency", habit.getTargetFrequency());
        obj.addProperty("maxEntriesPerDay", habit.getMaxEntriesPerDay());
        obj.addProperty("positiveScoring", habit.isPositiveScoring());
        obj.add("createdAt", gson.toJsonTree(habit.getCreatedAt()));

        if (habit.getUser() != null) {
            obj.add("user", serializeUser(habit.getUser()));
        }

        if (habit.getScoringRule() != null) {
            JsonObject ruleObj = new JsonObject();
            ScoringRule rule = habit.getScoringRule();
            ruleObj.addProperty("id", rule.getId());
            ruleObj.addProperty("name", rule.getName());
            ruleObj.addProperty("thresholdFor1Point", rule.getThresholdFor1Point());
            ruleObj.addProperty("thresholdFor2Points", rule.getThresholdFor2Points());
            ruleObj.addProperty("thresholdFor4Points", rule.getThresholdFor4Points());
            ruleObj.addProperty("thresholdFor8Points", rule.getThresholdFor8Points());
            obj.add("scoringRule", ruleObj);
        }

        JsonArray entriesArray = new JsonArray();
        if (habit.getEntries() != null) {
            for (HabitEntry entry : habit.getEntries()) {
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("id", entry.getId());
                entryObj.add("completedAt", gson.toJsonTree(entry.getCompletedAt()));
                entryObj.addProperty("notes", entry.getNotes());
                entryObj.addProperty("value", entry.getValue());
                entriesArray.add(entryObj);
            }
        }
        obj.add("entries", entriesArray);

        if (habit.getNameTranslations() != null && !habit.getNameTranslations().isEmpty()) {
            JsonObject nameTransObj = new JsonObject();
            for (Map.Entry<String, String> e : habit.getNameTranslations().entrySet()) {
                nameTransObj.addProperty(e.getKey(), e.getValue());
            }
            obj.add("nameTranslations", nameTransObj);
        }
        if (habit.getDescriptionTranslations() != null && !habit.getDescriptionTranslations().isEmpty()) {
            JsonObject descTransObj = new JsonObject();
            for (Map.Entry<String, String> e : habit.getDescriptionTranslations().entrySet()) {
                descTransObj.addProperty(e.getKey(), e.getValue());
            }
            obj.add("descriptionTranslations", descTransObj);
        }

        return obj;
    }

    private Habit deserializeHabit(JsonObject obj) {
        Habit habit = new Habit();
        habit.setId(obj.get("id").getAsString());
        habit.setName(getStringOrNull(obj, "name"));
        habit.setDescription(getStringOrNull(obj, "description"));
        habit.setCategoryId(getStringOrNull(obj, "categoryId"));

        String freqType = getStringOrNull(obj, "frequencyType");
        if (freqType != null) {
            habit.setFrequencyType(FrequencyType.valueOf(freqType));
        }

        if (obj.has("targetFrequency") && !obj.get("targetFrequency").isJsonNull()) {
            habit.setTargetFrequency(obj.get("targetFrequency").getAsInt());
        }
        if (obj.has("maxEntriesPerDay") && !obj.get("maxEntriesPerDay").isJsonNull()) {
            habit.setMaxEntriesPerDay(obj.get("maxEntriesPerDay").getAsInt());
        }
        if (obj.has("positiveScoring") && !obj.get("positiveScoring").isJsonNull()) {
            habit.setPositiveScoring(obj.get("positiveScoring").getAsBoolean());
        }
        if (obj.has("createdAt") && !obj.get("createdAt").isJsonNull()) {
            habit.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (hasNonNull(obj, "user")) {
            habit.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        if (obj.has("scoringRule") && !obj.get("scoringRule").isJsonNull()) {
            JsonObject ruleObj = obj.getAsJsonObject("scoringRule");
            ScoringRule rule = new ScoringRule();
            rule.setId(ruleObj.get("id").getAsString());
            if (ruleObj.has("name") && !ruleObj.get("name").isJsonNull()) {
                rule.setName(ruleObj.get("name").getAsString());
            }
            habit.setScoringRule(rule);
        }

        List<HabitEntry> entries = new ArrayList<>();
        if (obj.has("entries") && !obj.get("entries").isJsonNull()) {
            JsonArray entriesArray = obj.getAsJsonArray("entries");
            for (JsonElement elem : entriesArray) {
                JsonObject entryObj = elem.getAsJsonObject();
                HabitEntry entry = new HabitEntry();
                entry.setId(entryObj.get("id").getAsString());
                if (entryObj.has("completedAt") && !entryObj.get("completedAt").isJsonNull()) {
                    entry.setCompletedAt(gson.fromJson(entryObj.get("completedAt"), LocalDateTime.class));
                }
                entry.setNotes(getStringOrNull(entryObj, "notes"));
                if (entryObj.has("value") && !entryObj.get("value").isJsonNull()) {
                    entry.setValue(entryObj.get("value").getAsInt());
                }
                entry.setHabit(habit);
                entries.add(entry);
            }
        }
        habit.setEntries(entries);

        if (obj.has("nameTranslations") && !obj.get("nameTranslations").isJsonNull()) {
            Map<String, String> nameTrans = new HashMap<>();
            JsonObject ntObj = obj.getAsJsonObject("nameTranslations");
            for (String key : ntObj.keySet()) {
                if (!ntObj.get(key).isJsonNull()) {
                    nameTrans.put(key, ntObj.get(key).getAsString());
                }
            }
            habit.setNameTranslations(nameTrans);
        }
        if (obj.has("descriptionTranslations") && !obj.get("descriptionTranslations").isJsonNull()) {
            Map<String, String> descTrans = new HashMap<>();
            JsonObject dtObj = obj.getAsJsonObject("descriptionTranslations");
            for (String key : dtObj.keySet()) {
                if (!dtObj.get(key).isJsonNull()) {
                    descTrans.put(key, dtObj.get(key).getAsString());
                }
            }
            habit.setDescriptionTranslations(descTrans);
        }

        return habit;
    }
}
