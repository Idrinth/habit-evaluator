package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;

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

public class FileSystemWeekPlannerSlotRepository implements WeekPlannerSlotRepository {

    private final Map<String, WeekPlannerSlot> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;
    private PlannerGroupRepository plannerGroupRepository;

    public FileSystemWeekPlannerSlotRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "week-planner-slots.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    /**
     * Sets the planner group repository used to resolve group references during deserialization.
     * Must be called after construction and before accessing slots with group references.
     */
    public void setPlannerGroupRepository(PlannerGroupRepository plannerGroupRepository) {
        this.plannerGroupRepository = plannerGroupRepository;
        // Reload to resolve group references now that the repository is available
        store.clear();
        load();
    }

    @Override
    public synchronized WeekPlannerSlot save(WeekPlannerSlot slot) {
        store.put(slot.getId(), slot);
        persist();
        return slot;
    }

    @Override
    public Optional<WeekPlannerSlot> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<WeekPlannerSlot> findAll() {
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
    public List<WeekPlannerSlot> findByUserId(String userId) {
        return store.values().stream()
                .filter(s -> s.getUser() != null && userId.equals(s.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WeekPlannerSlot> findByUserIdAndDayOfWeek(String userId, int dayOfWeek) {
        return store.values().stream()
                .filter(s -> s.getUser() != null && userId.equals(s.getUser().getId()))
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (WeekPlannerSlot slot : store.values()) {
                array.add(serializeSlot(slot));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist week planner slots to filesystem", e);
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
                WeekPlannerSlot slot = deserializeSlot(element.getAsJsonObject());
                store.put(slot.getId(), slot);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load week planner slots from filesystem", e);
        }
    }

    private JsonObject serializeSlot(WeekPlannerSlot slot) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", slot.getId());
        obj.addProperty("dayOfWeek", slot.getDayOfWeek());
        obj.addProperty("hour", slot.getHour());

        if (slot.getGroups() != null && !slot.getGroups().isEmpty()) {
            JsonArray groupIds = new JsonArray();
            for (de.idrinth.habitevaluator.shared.model.PlannerGroup group : slot.getGroups()) {
                groupIds.add(group.getId());
            }
            obj.add("groupIds", groupIds);
        }

        if (slot.getUser() != null) {
            obj.add("user", serializeUser(slot.getUser()));
        }

        return obj;
    }

    private WeekPlannerSlot deserializeSlot(JsonObject obj) {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        slot.setId(obj.get("id").getAsString());
        slot.setDayOfWeek(getIntOrDefault(obj, "dayOfWeek", 1));
        slot.setHour(getIntOrDefault(obj, "hour", 0));

        if (hasNonNull(obj, "groupIds") && plannerGroupRepository != null) {
            java.util.Set<de.idrinth.habitevaluator.shared.model.PlannerGroup> groups = new java.util.HashSet<>();
            JsonArray groupIds = obj.getAsJsonArray("groupIds");
            for (JsonElement gidElement : groupIds) {
                plannerGroupRepository.findById(gidElement.getAsString()).ifPresent(groups::add);
            }
            slot.setGroups(groups);
        } else {
            // Backward compatibility: read old single groupId field
            String groupId = getStringOrNull(obj, "groupId");
            if (groupId != null && plannerGroupRepository != null) {
                java.util.Set<de.idrinth.habitevaluator.shared.model.PlannerGroup> groups = new java.util.HashSet<>();
                plannerGroupRepository.findById(groupId).ifPresent(groups::add);
                slot.setGroups(groups);
            }
        }

        if (hasNonNull(obj, "user")) {
            slot.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return slot;
    }
}
