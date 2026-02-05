package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SyncServiceTest {

    @Test
    void testSyncResultConstructorAndGetters() {
        SyncService.SyncResult result = new SyncService.SyncResult(3, 2, 1);
        assertEquals(3, result.getPushed());
        assertEquals(2, result.getPulled());
        assertEquals(1, result.getMerged());
    }

    @Test
    void testSyncResultZeros() {
        SyncService.SyncResult result = new SyncService.SyncResult(0, 0, 0);
        assertEquals(0, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
    }

    // In-memory repository for testing
    private static class InMemoryHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
            habits.removeIf(h -> h.getId().equals(habit.getId()));
            habits.add(habit);
            return habit;
        }

        @Override
        public Optional<Habit> findById(String id) {
            return habits.stream().filter(h -> h.getId().equals(id)).findFirst();
        }

        @Override
        public List<Habit> findAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void deleteById(String id) {
            habits.removeIf(h -> h.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return habits.stream().anyMatch(h -> h.getId().equals(id));
        }

        @Override
        public List<Habit> findByUserId(String userId) {
            List<Habit> result = new ArrayList<>();
            for (Habit h : habits) {
                if (h.getUser() != null && h.getUser().getId().equals(userId)) {
                    result.add(h);
                }
            }
            return result;
        }
    }
}
