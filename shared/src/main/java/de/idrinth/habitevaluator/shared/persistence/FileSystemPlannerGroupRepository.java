package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemPlannerGroupRepository implements PlannerGroupRepository {

    private final Map<String, PlannerGroup> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;

    public FileSystemPlannerGroupRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "planner-groups.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    @Override
    public synchronized PlannerGroup save(PlannerGroup group) {
        store.put(group.getId(), group);
        persist();
        return group;
    }

    @Override
    public Optional<PlannerGroup> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<PlannerGroup> findAll() {
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
    public List<PlannerGroup> findByUserId(String userId) {
        return store.values().stream()
                .filter(g -> g.getUser() != null && userId.equals(g.getUser().getId()))
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (PlannerGroup group : store.values()) {
                array.add(serializeGroup(group));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist planner groups to filesystem", e);
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
                PlannerGroup group = deserializeGroup(element.getAsJsonObject());
                store.put(group.getId(), group);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load planner groups from filesystem", e);
        }
    }

    private JsonObject serializeGroup(PlannerGroup group) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", group.getId());
        obj.addProperty("name", group.getName());
        obj.addProperty("description", group.getDescription());
        obj.add("createdAt", gson.toJsonTree(group.getCreatedAt()));

        if (group.getUser() != null) {
            obj.add("user", serializeUser(group.getUser()));
        }

        return obj;
    }

    private PlannerGroup deserializeGroup(JsonObject obj) {
        PlannerGroup group = new PlannerGroup();
        group.setId(obj.get("id").getAsString());
        group.setName(getStringOrNull(obj, "name"));
        group.setDescription(getStringOrNull(obj, "description"));

        if (hasNonNull(obj, "createdAt")) {
            group.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (hasNonNull(obj, "user")) {
            group.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return group;
    }
}
