package de.idrinth.habitevaluator.android.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        this.gson = createGson();
        load();
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .create();
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
            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", habit.getUser().getId());
            userObj.addProperty("username", habit.getUser().getUsername());
            obj.add("user", userObj);
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

        if (obj.has("user") && !obj.get("user").isJsonNull()) {
            JsonObject userObj = obj.getAsJsonObject("user");
            User user = new User();
            user.setId(userObj.get("id").getAsString());
            user.setUsername(getStringOrNull(userObj, "username"));
            user.setPassword("placeholder");
            habit.setUser(user);
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

        return habit;
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) {
                return null;
            }
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
