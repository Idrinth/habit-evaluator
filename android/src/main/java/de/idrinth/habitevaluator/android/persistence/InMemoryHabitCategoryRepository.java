package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryHabitCategoryRepository implements HabitCategoryRepository {

    private final Map<String, HabitCategory> store = new ConcurrentHashMap<>();

    @Override
    public HabitCategory save(HabitCategory category) {
        store.put(category.getId(), category);
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
    public void deleteById(String id) {
        store.remove(id);
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
}
