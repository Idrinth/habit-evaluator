package de.idrinth.habitevaluator.android.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemSleepEntryRepository implements SleepEntryRepository {

    private final Map<String, SleepEntry> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public FileSystemSleepEntryRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "sleep_entries.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> {
                    if (src == null) return JsonNull.INSTANCE;
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                })
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    if (json.isJsonNull()) return null;
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                })
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> {
                    if (src == null) return JsonNull.INSTANCE;
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
                })
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
                    if (json.isJsonNull()) return null;
                    return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                })
                .create();
        load();
    }

    @Override
    public synchronized SleepEntry save(SleepEntry entry) {
        store.put(entry.getId(), entry);
        persist();
        return entry;
    }

    @Override
    public Optional<SleepEntry> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<SleepEntry> findAll() {
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
    public List<SleepEntry> findByUserId(String userId) {
        List<SleepEntry> result = new ArrayList<>();
        for (SleepEntry entry : store.values()) {
            if (entry.getUser() != null && userId.equals(entry.getUser().getId())) {
                result.add(entry);
            }
        }
        return result;
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (SleepEntry entry : store.values()) {
                array.add(serializeEntry(entry));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist sleep entries to filesystem", e);
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
                SleepEntry entry = deserializeEntry(element.getAsJsonObject());
                store.put(entry.getId(), entry);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load sleep entries from filesystem", e);
        }
    }

    private JsonObject serializeEntry(SleepEntry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", entry.getId());
        if (entry.getFromTime() != null) {
            obj.addProperty("fromTime", entry.getFromTime().format(TIME_FORMAT));
        }
        if (entry.getUntilTime() != null) {
            obj.addProperty("untilTime", entry.getUntilTime().format(TIME_FORMAT));
        }
        if (entry.getDate() != null) {
            obj.addProperty("date", entry.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (entry.getCreatedAt() != null) {
            obj.addProperty("createdAt", entry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        obj.addProperty("notes", entry.getNotes());

        if (entry.getUser() != null) {
            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", entry.getUser().getId());
            userObj.addProperty("username", entry.getUser().getUsername());
            obj.add("user", userObj);
        }

        return obj;
    }

    private SleepEntry deserializeEntry(JsonObject obj) {
        SleepEntry entry = new SleepEntry();
        entry.setId(obj.get("id").getAsString());

        if (obj.has("fromTime") && !obj.get("fromTime").isJsonNull()) {
            entry.setFromTime(LocalTime.parse(obj.get("fromTime").getAsString(), TIME_FORMAT));
        }
        if (obj.has("untilTime") && !obj.get("untilTime").isJsonNull()) {
            entry.setUntilTime(LocalTime.parse(obj.get("untilTime").getAsString(), TIME_FORMAT));
        }
        if (obj.has("date") && !obj.get("date").isJsonNull()) {
            entry.setDate(LocalDate.parse(obj.get("date").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (obj.has("createdAt") && !obj.get("createdAt").isJsonNull()) {
            entry.setCreatedAt(LocalDateTime.parse(obj.get("createdAt").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (obj.has("notes") && !obj.get("notes").isJsonNull()) {
            entry.setNotes(obj.get("notes").getAsString());
        }

        if (obj.has("user") && !obj.get("user").isJsonNull()) {
            JsonObject userObj = obj.getAsJsonObject("user");
            User user = new User();
            user.setId(userObj.get("id").getAsString());
            if (userObj.has("username") && !userObj.get("username").isJsonNull()) {
                user.setUsername(userObj.get("username").getAsString());
            }
            user.setPassword("placeholder");
            entry.setUser(user);
        }

        return entry;
    }
}
