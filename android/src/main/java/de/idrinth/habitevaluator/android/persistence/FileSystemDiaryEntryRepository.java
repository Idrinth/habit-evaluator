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

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemDiaryEntryRepository implements DiaryEntryRepository {

    private final Map<String, DiaryEntry> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemDiaryEntryRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "diary_entries.json");
        this.gson = createGson();
        load();
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .create();
    }

    @Override
    public synchronized DiaryEntry save(DiaryEntry entry) {
        store.put(entry.getId(), entry);
        persist();
        return entry;
    }

    @Override
    public Optional<DiaryEntry> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DiaryEntry> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public List<DiaryEntry> findByUserId(String userId) {
        return store.values().stream()
                .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return store.values().stream()
                .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId()))
                .map(DiaryEntry::getDescription)
                .filter(d -> d != null && !d.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (DiaryEntry entry : store.values()) {
                array.add(serializeEntry(entry));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist diary entries to filesystem", e);
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
                DiaryEntry entry = deserializeEntry(element.getAsJsonObject());
                store.put(entry.getId(), entry);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load diary entries from filesystem", e);
        }
    }

    private JsonObject serializeEntry(DiaryEntry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", entry.getId());
        obj.addProperty("description", entry.getDescription());
        obj.addProperty("significance", entry.getSignificance() != null ? entry.getSignificance().name() : null);
        obj.add("eventDate", gson.toJsonTree(entry.getEventDate()));
        obj.add("createdAt", gson.toJsonTree(entry.getCreatedAt()));

        if (entry.getUser() != null) {
            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", entry.getUser().getId());
            userObj.addProperty("username", entry.getUser().getUsername());
            obj.add("user", userObj);
        }

        return obj;
    }

    private DiaryEntry deserializeEntry(JsonObject obj) {
        DiaryEntry entry = new DiaryEntry();
        entry.setId(obj.get("id").getAsString());
        entry.setDescription(getStringOrNull(obj, "description"));

        String significance = getStringOrNull(obj, "significance");
        if (significance != null) {
            entry.setSignificance(EventSignificance.valueOf(significance));
        }

        if (obj.has("eventDate") && !obj.get("eventDate").isJsonNull()) {
            entry.setEventDate(gson.fromJson(obj.get("eventDate"), LocalDate.class));
        }
        if (obj.has("createdAt") && !obj.get("createdAt").isJsonNull()) {
            entry.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (obj.has("user") && !obj.get("user").isJsonNull()) {
            JsonObject userObj = obj.getAsJsonObject("user");
            User user = new User();
            user.setId(userObj.get("id").getAsString());
            user.setUsername(getStringOrNull(userObj, "username"));
            user.setPassword("placeholder");
            entry.setUser(user);
        }

        return entry;
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

    private static class LocalDateSerializer implements JsonSerializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    private static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) {
                return null;
            }
            return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}
