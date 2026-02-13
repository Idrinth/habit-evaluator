package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private DiaryReferenceRepository diaryReferenceRepository;

    public FileSystemDiaryEntryRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "diary_entries.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    /**
     * Sets the diary reference repository. This is used to look up references during deserialization.
     * Must be called after construction and before accessing entries.
     */
    public void setDiaryReferenceRepository(DiaryReferenceRepository diaryReferenceRepository) {
        this.diaryReferenceRepository = diaryReferenceRepository;
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

    @Override
    public List<DiaryEntry> findEntriesNeedingMigration(String userId) {
        return store.values().stream()
                .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId()))
                .filter(DiaryEntry::needsMigration)
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
        obj.addProperty("significance", entry.getSignificance() != null ? entry.getSignificance().name() : null);
        obj.add("eventDate", gson.toJsonTree(entry.getEventDate()));
        obj.add("createdAt", gson.toJsonTree(entry.getCreatedAt()));

        // Store reference ID if available, otherwise store legacy description
        if (entry.getDiaryReference() != null) {
            obj.addProperty("diaryReferenceId", entry.getDiaryReference().getId());
        } else {
            obj.addProperty("legacyDescription", entry.getLegacyDescription());
        }

        // Keep description for backward compatibility with old format readers
        obj.addProperty("description", entry.getDescription());

        if (entry.getStartTime() != null) {
            obj.add("startTime", gson.toJsonTree(entry.getStartTime()));
        }
        if (entry.getEndTime() != null) {
            obj.add("endTime", gson.toJsonTree(entry.getEndTime()));
        }

        if (entry.getUser() != null) {
            obj.add("user", serializeUser(entry.getUser()));
        }

        return obj;
    }

    private DiaryEntry deserializeEntry(JsonObject obj) {
        DiaryEntry entry = new DiaryEntry();
        entry.setId(obj.get("id").getAsString());

        // Check for reference ID first (new format)
        String refId = getStringOrNull(obj, "diaryReferenceId");
        if (refId != null && diaryReferenceRepository != null) {
            diaryReferenceRepository.findById(refId).ifPresent(entry::setDiaryReference);
        }

        // Fall back to legacy description or old description field
        if (entry.getDiaryReference() == null) {
            String legacyDesc = getStringOrNull(obj, "legacyDescription");
            if (legacyDesc != null) {
                entry.setLegacyDescription(legacyDesc);
            } else {
                // Old format: description was stored directly
                entry.setLegacyDescription(getStringOrNull(obj, "description"));
            }
        }

        String significance = getStringOrNull(obj, "significance");
        if (significance != null) {
            entry.setSignificance(EventSignificance.valueOf(significance));
        }

        if (hasNonNull(obj, "eventDate")) {
            entry.setEventDate(gson.fromJson(obj.get("eventDate"), LocalDate.class));
        }
        if (hasNonNull(obj, "createdAt")) {
            entry.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (hasNonNull(obj, "startTime")) {
            entry.setStartTime(gson.fromJson(obj.get("startTime"), LocalTime.class));
        }
        if (hasNonNull(obj, "endTime")) {
            entry.setEndTime(gson.fromJson(obj.get("endTime"), LocalTime.class));
        }

        if (hasNonNull(obj, "user")) {
            entry.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return entry;
    }
}
