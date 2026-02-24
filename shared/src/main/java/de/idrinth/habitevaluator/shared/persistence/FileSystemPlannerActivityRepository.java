package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemPlannerActivityRepository implements PlannerActivityRepository {

    private final Map<String, PlannerActivity> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;
    private PlannerGroupRepository plannerGroupRepository;

    public FileSystemPlannerActivityRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "planner-activities.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    /**
     * Sets the planner group repository used to resolve group references during deserialization.
     * Must be called after construction and before accessing activities with group references.
     */
    public void setPlannerGroupRepository(PlannerGroupRepository plannerGroupRepository) {
        this.plannerGroupRepository = plannerGroupRepository;
        // Reload to resolve group references now that the repository is available
        store.clear();
        load();
    }

    @Override
    public synchronized PlannerActivity save(PlannerActivity activity) {
        store.put(activity.getId(), activity);
        persist();
        return activity;
    }

    @Override
    public Optional<PlannerActivity> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<PlannerActivity> findAll() {
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
    public List<PlannerActivity> findByUserId(String userId) {
        return store.values().stream()
                .filter(a -> a.getUser() != null && userId.equals(a.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlannerActivity> findByGroupId(String groupId) {
        return store.values().stream()
                .filter(a -> a.getGroups().stream().anyMatch(g -> groupId.equals(g.getId())))
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (PlannerActivity activity : store.values()) {
                array.add(serializeActivity(activity));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist planner activities to filesystem", e);
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
                PlannerActivity activity = deserializeActivity(element.getAsJsonObject());
                store.put(activity.getId(), activity);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load planner activities from filesystem", e);
        }
    }

    private JsonObject serializeActivity(PlannerActivity activity) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", activity.getId());
        obj.addProperty("name", activity.getName());
        obj.addProperty("description", activity.getDescription());
        obj.add("createdAt", gson.toJsonTree(activity.getCreatedAt()));

        JsonArray groupIds = new JsonArray();
        for (PlannerGroup group : activity.getGroups()) {
            groupIds.add(group.getId());
        }
        obj.add("groupIds", groupIds);

        if (activity.getUser() != null) {
            obj.add("user", serializeUser(activity.getUser()));
        }

        return obj;
    }

    private PlannerActivity deserializeActivity(JsonObject obj) {
        PlannerActivity activity = new PlannerActivity();
        activity.setId(obj.get("id").getAsString());
        activity.setName(getStringOrNull(obj, "name"));
        activity.setDescription(getStringOrNull(obj, "description"));

        if (hasNonNull(obj, "createdAt")) {
            activity.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        if (hasNonNull(obj, "groupIds") && plannerGroupRepository != null) {
            Set<PlannerGroup> groups = new HashSet<>();
            for (JsonElement groupIdElement : obj.getAsJsonArray("groupIds")) {
                String groupId = groupIdElement.getAsString();
                plannerGroupRepository.findById(groupId).ifPresent(groups::add);
            }
            activity.setGroups(groups);
        }

        if (hasNonNull(obj, "user")) {
            activity.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return activity;
    }
}
