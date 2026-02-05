package de.idrinth.habitevaluator.android.persistence;

import static de.idrinth.habitevaluator.android.persistence.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemHabitCategoryRepository implements HabitCategoryRepository {

    private final Map<String, HabitCategory> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemHabitCategoryRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "categories.json");
        this.gson = GsonSerializers.createGsonWithDateTime();
        load();
    }

    @Override
    public synchronized HabitCategory save(HabitCategory category) {
        store.put(category.getId(), category);
        persist();
        return category;
    }

    @Override
    public Optional<HabitCategory> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<HabitCategory> findAll() {
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
    public List<HabitCategory> findByUserId(String userId) {
        List<HabitCategory> result = new ArrayList<>();
        for (HabitCategory category : store.values()) {
            if (category.getUser() != null && userId.equals(category.getUser().getId())) {
                result.add(category);
            }
        }
        return result;
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (HabitCategory category : store.values()) {
                array.add(serializeCategory(category));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist categories to filesystem", e);
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
                HabitCategory category = deserializeCategory(element.getAsJsonObject());
                store.put(category.getId(), category);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load categories from filesystem", e);
        }
    }

    private JsonObject serializeCategory(HabitCategory category) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", category.getId());
        obj.addProperty("name", category.getName());
        obj.addProperty("description", category.getDescription());
        obj.addProperty("color", category.getColor());

        if (category.getUser() != null) {
            obj.add("user", serializeUser(category.getUser()));
        }

        if (category.getNameTranslations() != null && !category.getNameTranslations().isEmpty()) {
            JsonObject nameTransObj = new JsonObject();
            for (Map.Entry<String, String> e : category.getNameTranslations().entrySet()) {
                nameTransObj.addProperty(e.getKey(), e.getValue());
            }
            obj.add("nameTranslations", nameTransObj);
        }
        if (category.getDescriptionTranslations() != null && !category.getDescriptionTranslations().isEmpty()) {
            JsonObject descTransObj = new JsonObject();
            for (Map.Entry<String, String> e : category.getDescriptionTranslations().entrySet()) {
                descTransObj.addProperty(e.getKey(), e.getValue());
            }
            obj.add("descriptionTranslations", descTransObj);
        }

        return obj;
    }

    private HabitCategory deserializeCategory(JsonObject obj) {
        HabitCategory category = new HabitCategory();
        category.setId(obj.get("id").getAsString());
        category.setName(getStringOrNull(obj, "name"));
        category.setDescription(getStringOrNull(obj, "description"));
        category.setColor(getStringOrNull(obj, "color"));

        if (hasNonNull(obj, "user")) {
            category.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        if (hasNonNull(obj, "nameTranslations")) {
            Map<String, String> nameTrans = new HashMap<>();
            JsonObject ntObj = obj.getAsJsonObject("nameTranslations");
            for (String key : ntObj.keySet()) {
                if (!ntObj.get(key).isJsonNull()) {
                    nameTrans.put(key, ntObj.get(key).getAsString());
                }
            }
            category.setNameTranslations(nameTrans);
        }
        if (hasNonNull(obj, "descriptionTranslations")) {
            Map<String, String> descTrans = new HashMap<>();
            JsonObject dtObj = obj.getAsJsonObject("descriptionTranslations");
            for (String key : dtObj.keySet()) {
                if (!dtObj.get(key).isJsonNull()) {
                    descTrans.put(key, dtObj.get(key).getAsString());
                }
            }
            category.setDescriptionTranslations(descTrans);
        }

        return category;
    }
}
