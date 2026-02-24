package de.idrinth.habitevaluator.shared.persistence;

import static de.idrinth.habitevaluator.shared.gson.GsonSerializers.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.idrinth.habitevaluator.shared.gson.GsonSerializers;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileSystemSlotConfirmationRepository implements SlotConfirmationRepository {

    private final Map<String, SlotConfirmation> store = new ConcurrentHashMap<>();
    private final File storageFile;
    private final Gson gson;
    private WeekPlannerSlotRepository slotRepository;
    private PlannerActivityRepository activityRepository;
    private PlannerGroupRepository groupRepository;

    public FileSystemSlotConfirmationRepository(File storageDir) {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.storageFile = new File(storageDir, "slot-confirmations.json");
        this.gson = GsonSerializers.createGson();
        load();
    }

    /**
     * Sets the related repositories used to resolve references during deserialization.
     * Must be called after construction and before accessing confirmations.
     */
    public void setRelatedRepositories(WeekPlannerSlotRepository slotRepository,
                                       PlannerActivityRepository activityRepository,
                                       PlannerGroupRepository groupRepository) {
        this.slotRepository = slotRepository;
        this.activityRepository = activityRepository;
        this.groupRepository = groupRepository;
        // Reload to resolve references now that the repositories are available
        store.clear();
        load();
    }

    @Override
    public synchronized SlotConfirmation save(SlotConfirmation confirmation) {
        store.put(confirmation.getId(), confirmation);
        persist();
        return confirmation;
    }

    @Override
    public Optional<SlotConfirmation> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<SlotConfirmation> findAll() {
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
    public List<SlotConfirmation> findByUserId(String userId) {
        return store.values().stream()
                .filter(c -> c.getUser() != null && userId.equals(c.getUser().getId()))
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        try {
            JsonArray array = new JsonArray();
            for (SlotConfirmation confirmation : store.values()) {
                array.add(serializeConfirmation(confirmation));
            }
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist slot confirmations to filesystem", e);
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
                SlotConfirmation confirmation = deserializeConfirmation(element.getAsJsonObject());
                store.put(confirmation.getId(), confirmation);
            }
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException("Failed to load slot confirmations from filesystem", e);
        }
    }

    private JsonObject serializeConfirmation(SlotConfirmation confirmation) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", confirmation.getId());
        obj.addProperty("confirmed", confirmation.isConfirmed());
        obj.add("date", gson.toJsonTree(confirmation.getDate()));
        obj.add("createdAt", gson.toJsonTree(confirmation.getCreatedAt()));

        if (confirmation.getSlot() != null) {
            obj.addProperty("slotId", confirmation.getSlot().getId());
        }
        if (confirmation.getActivity() != null) {
            obj.addProperty("activityId", confirmation.getActivity().getId());
        }
        if (confirmation.getGroup() != null) {
            obj.addProperty("groupId", confirmation.getGroup().getId());
        }

        if (confirmation.getUser() != null) {
            obj.add("user", serializeUser(confirmation.getUser()));
        }

        return obj;
    }

    private SlotConfirmation deserializeConfirmation(JsonObject obj) {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setId(obj.get("id").getAsString());
        confirmation.setConfirmed(getBooleanOrDefault(obj, "confirmed", false));

        if (hasNonNull(obj, "date")) {
            confirmation.setDate(gson.fromJson(obj.get("date"), LocalDate.class));
        }
        if (hasNonNull(obj, "createdAt")) {
            confirmation.setCreatedAt(gson.fromJson(obj.get("createdAt"), LocalDateTime.class));
        }

        String slotId = getStringOrNull(obj, "slotId");
        if (slotId != null && slotRepository != null) {
            slotRepository.findById(slotId).ifPresent(confirmation::setSlot);
        }

        String activityId = getStringOrNull(obj, "activityId");
        if (activityId != null && activityRepository != null) {
            activityRepository.findById(activityId).ifPresent(confirmation::setActivity);
        }

        String groupId = getStringOrNull(obj, "groupId");
        if (groupId != null && groupRepository != null) {
            groupRepository.findById(groupId).ifPresent(confirmation::setGroup);
        }

        if (hasNonNull(obj, "user")) {
            confirmation.setUser(deserializeUser(obj.getAsJsonObject("user")));
        }

        return confirmation;
    }
}
