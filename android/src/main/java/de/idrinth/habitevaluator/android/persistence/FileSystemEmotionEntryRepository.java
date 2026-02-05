package de.idrinth.habitevaluator.android.persistence;

import static de.idrinth.habitevaluator.android.persistence.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemEmotionEntryRepository implements EmotionEntryRepository {

    private final Map<String, EmotionEntry> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;
    private final EmotionPairRepository emotionPairRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FileSystemEmotionEntryRepository(File storageDir, EmotionPairRepository emotionPairRepository) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "emotion-entries.json");
        this.gson = GsonSerializers.createGson();
        this.emotionPairRepository = emotionPairRepository;
        load();
    }

    @Override
    public synchronized EmotionEntry save(EmotionEntry entry) {
        store.put(entry.getId(), entry);
        persist();
        return entry;
    }

    @Override
    public Optional<EmotionEntry> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<EmotionEntry> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public List<EmotionEntry> findByUserId(String userId) {
        List<EmotionEntry> result = new ArrayList<>();
        for (EmotionEntry entry : store.values()) {
            if (entry.getUser() != null && userId.equals(entry.getUser().getId())) {
                result.add(entry);
            }
        }
        return result;
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (EmotionEntry entry : store.values()) {
                array.add(serializeEntry(entry));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist emotion entries to filesystem", e);
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
                EmotionEntry entry = deserializeEntry(element.getAsJsonObject());
                if (entry != null) {
                    store.put(entry.getId(), entry);
                }
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load emotion entries from filesystem", e);
        }
    }

    private JsonObject serializeEntry(EmotionEntry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", entry.getId());
        obj.addProperty("strength", entry.getStrength());
        obj.addProperty("recordedAt", entry.getRecordedAt().format(FORMATTER));
        obj.addProperty("notes", entry.getNotes());

        if (entry.getEmotionPair() != null) {
            obj.addProperty("emotionPairId", entry.getEmotionPair().getId());
        }

        if (entry.getUser() != null) {
            obj.add("user", serializeUser(entry.getUser()));
        }

        return obj;
    }

    private EmotionEntry deserializeEntry(JsonObject obj) {
        EmotionEntry entry = new EmotionEntry();
        entry.setId(obj.get("id").getAsString());
        entry.setStrength(obj.get("strength").getAsInt());

        if (hasNonNull(obj, "recordedAt")) {
            entry.setRecordedAt(LocalDateTime.parse(obj.get("recordedAt").getAsString(), FORMATTER));
        }

        entry.setNotes(getStringOrNull(obj, "notes"));

        if (hasNonNull(obj, "emotionPairId")) {
            String pairId = obj.get("emotionPairId").getAsString();
            Optional<EmotionPair> pair = emotionPairRepository.findById(pairId);
            if (pair.isPresent()) {
                entry.setEmotionPair(pair.get());
            } else {
                return null;
            }
        }

        if (hasNonNull(obj, "user")) {
            entry.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return entry;
    }
}
