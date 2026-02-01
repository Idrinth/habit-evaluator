package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryHabitRepository implements HabitRepository {

    private final Map<String, Habit> store = new ConcurrentHashMap<>();

    @Override
    public Habit save(Habit habit) {
        store.put(habit.getId(), habit);
        return habit;
    }

    @Override
    public Optional<Habit> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Habit> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        return store.values().stream()
                .filter(h -> h.getUser() != null && userId.equals(h.getUser().getId()))
                .collect(Collectors.toList());
    }
}
