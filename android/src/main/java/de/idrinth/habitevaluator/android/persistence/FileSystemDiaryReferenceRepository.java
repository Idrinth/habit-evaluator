package de.idrinth.habitevaluator.android.persistence;

import static de.idrinth.habitevaluator.android.persistence.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemDiaryReferenceRepository implements DiaryReferenceRepository {

    private final Map<String, DiaryReference> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemDiaryReferenceRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "diary_references.json");
        this.gson = GsonSerializers.createGsonWithDateTimeAndDate();
        load();
    }

    @Override
    public synchronized DiaryReference save(DiaryReference reference) {
        store.put(reference.getId(), reference);
        persist();
        return reference;
    }

    @Override
    public Optional<DiaryReference> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DiaryReference> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(String id) {
        store.remove(id);
        persist();
    }

    @Override
    public List<DiaryReference> findByUserId(String userId) {
        return store.values().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description) {
        if (description == null) {
            return Optional.empty();
        }
        String descLower = description.toLowerCase();
        return store.values().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .filter(r -> r.getDescriptionLower() != null && r.getDescriptionLower().equals(descLower))
                .findFirst();
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return store.values().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .map(DiaryReference::getDescription)
                .filter(d -> d != null && !d.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (DiaryReference reference : store.values()) {
                array.add(serializeReference(reference));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist diary references to filesystem", e);
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
                DiaryReference reference = deserializeReference(element.getAsJsonObject());
                store.put(reference.getId(), reference);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load diary references from filesystem", e);
        }
    }

    private JsonObject serializeReference(DiaryReference reference) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", reference.getId());
        obj.addProperty("description", reference.getDescription());
        obj.addProperty("descriptionLower", reference.getDescriptionLower());

        if (reference.getUser() != null) {
            obj.add("user", serializeUser(reference.getUser()));
        }

        return obj;
    }

    private DiaryReference deserializeReference(JsonObject obj) {
        DiaryReference reference = new DiaryReference();
        reference.setId(obj.get("id").getAsString());
        reference.setDescription(getStringOrNull(obj, "description"));
        reference.setDescriptionLower(getStringOrNull(obj, "descriptionLower"));

        if (hasNonNull(obj, "user")) {
            reference.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return reference;
    }
}
