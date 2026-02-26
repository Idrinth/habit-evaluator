package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemGratitudeEntryRepository implements GratitudeEntryRepository {

    private final Map<String, GratitudeEntry> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemGratitudeEntryRepository(File storageDir) {
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created && !storageDir.exists()) {
                throw new IllegalStateException(
                        "Failed to create storage directory: " + storageDir.getAbsolutePath());
            }
        }
        this.storageFile = new File(storageDir, "gratitude_entries.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    @Override
    public synchronized GratitudeEntry save(GratitudeEntry entry) {
        store.put(entry.getId(), entry);
        persist();
        return entry;
    }

    @Override
    public Optional<GratitudeEntry> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<GratitudeEntry> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public List<GratitudeEntry> findByUserId(String userId) {
        return store.values().stream()
                .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId()))
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (GratitudeEntry entry : store.values()) {
                array.add(serializeEntry(entry));
            }
            Path storagePath = storageFile.toPath();
            Path tempPath = storagePath.resolveSibling(storageFile.getName() + ".tmp");
            try (FileWriter writer = new FileWriter(tempPath.toFile())) {
                gson.toJson(array, writer);
                writer.flush();
            }
            try {
                Files.move(tempPath, storagePath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist gratitude entries to filesystem", e);
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
                GratitudeEntry entry = deserializeEntry(element.getAsJsonObject());
                store.put(entry.getId(), entry);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load gratitude entries from filesystem", e);
        }
    }

    private JsonObject serializeEntry(GratitudeEntry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", entry.getId());
        obj.addProperty("description", entry.getDescription());
        obj.addProperty("reason", entry.getReason());
        obj.add("eventDate", gson.toJsonTree(entry.getEventDate()));
        obj.add("createdAt", gson.toJsonTree(entry.getCreatedAt()));

        if (entry.getUser() != null) {
            obj.add("user", serializeUser(entry.getUser()));
        }

        return obj;
    }

    private GratitudeEntry deserializeEntry(JsonObject obj) {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setId(obj.get("id").getAsString());
        entry.setDescription(getStringOrNull(obj, "description"));
        entry.setReason(getStringOrNull(obj, "reason"));

        if (hasNonNull(obj, "eventDate")) {
            entry.setEventDate(gson.fromJson(obj.get("eventDate"), LocalDate.class));
        }
        if (hasNonNull(obj, "createdAt")) {
            entry.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (hasNonNull(obj, "user")) {
            entry.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return entry;
    }
}
