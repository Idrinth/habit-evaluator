package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemEmotionPairRepository implements EmotionPairRepository {

    private final Map<String, EmotionPair> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemEmotionPairRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "emotion-pairs.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    @Override
    public synchronized EmotionPair save(EmotionPair pair) {
        store.put(pair.getId(), pair);
        persist();
        return pair;
    }

    @Override
    public Optional<EmotionPair> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<EmotionPair> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public List<EmotionPair> findByUserId(String userId) {
        List<EmotionPair> result = new ArrayList<>();
        for (EmotionPair pair : store.values()) {
            if (pair.getUser() != null && userId.equals(pair.getUser().getId())) {
                result.add(pair);
            }
        }
        return result;
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (EmotionPair pair : store.values()) {
                array.add(serializePair(pair));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist emotion pairs to filesystem", e);
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
                EmotionPair pair = deserializePair(element.getAsJsonObject());
                store.put(pair.getId(), pair);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load emotion pairs from filesystem", e);
        }
    }

    private JsonObject serializePair(EmotionPair pair) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", pair.getId());
        obj.addProperty("negativeLabel", pair.getNegativeLabel());
        obj.addProperty("positiveLabel", pair.getPositiveLabel());

        if (pair.getUser() != null) {
            obj.add("user", serializeUser(pair.getUser()));
        }

        return obj;
    }

    private EmotionPair deserializePair(JsonObject obj) {
        EmotionPair pair = new EmotionPair();
        pair.setId(obj.get("id").getAsString());
        pair.setNegativeLabel(getStringOrNull(obj, "negativeLabel"));
        pair.setPositiveLabel(getStringOrNull(obj, "positiveLabel"));

        if (hasNonNull(obj, "user")) {
            pair.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return pair;
    }
}
