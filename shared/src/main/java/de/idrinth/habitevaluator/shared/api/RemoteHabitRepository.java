package de.idrinth.habitevaluator.shared.api;

import com.google.gson.reflect.TypeToken;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Remote API implementation of HabitRepository.
 * Communicates with the webserver REST API for habit persistence.
 */
public class RemoteHabitRepository implements HabitRepository {

    private static final Logger logger = LoggerFactory.getLogger(RemoteHabitRepository.class);
    private static final Type HABIT_LIST_TYPE = new TypeToken<List<Habit>>() {}.getType();
    private static final Type HABIT_TYPE = new TypeToken<Habit>() {}.getType();
    private static final Type ENTRY_TYPE = new TypeToken<HabitEntry>() {}.getType();

    private final ApiClient apiClient;

    public RemoteHabitRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Habit save(Habit habit) {
        try {
            if (existsById(habit.getId())) {
                return apiClient.put("/api/habits/" + habit.getId(), habit, HABIT_TYPE);
            } else {
                return apiClient.post("/api/habits", habit, HABIT_TYPE);
            }
        } catch (IOException e) {
            logger.error("Failed to save habit: {}", habit.getName(), e);
            throw new RuntimeException("Failed to save habit to remote API", e);
        }
    }

    @Override
    public Optional<Habit> findById(String id) {
        try {
            Habit habit = apiClient.get("/api/habits/" + id, HABIT_TYPE);
            return Optional.ofNullable(habit);
        } catch (IOException e) {
            logger.error("Failed to find habit by id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Habit> findAll() {
        try {
            return apiClient.getList("/api/habits", HABIT_LIST_TYPE);
        } catch (IOException e) {
            logger.error("Failed to fetch all habits", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            apiClient.delete("/api/habits/" + id);
        } catch (IOException e) {
            logger.error("Failed to delete habit: {}", id, e);
            throw new RuntimeException("Failed to delete habit from remote API", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        return findAll();
    }

    /**
     * Adds an entry to a habit via the remote API.
     */
    public HabitEntry addEntry(String habitId, HabitEntry entry) {
        try {
            return apiClient.post("/api/habits/" + habitId + "/entries", entry, ENTRY_TYPE);
        } catch (IOException e) {
            logger.error("Failed to add entry to habit: {}", habitId, e);
            throw new RuntimeException("Failed to add habit entry via remote API", e);
        }
    }
}
