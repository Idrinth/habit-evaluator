package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseFoodLogRepository implements FoodLogRepository {

    private final JpaFoodLogRepository jpaRepository;

    public DatabaseFoodLogRepository(JpaFoodLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FoodLog save(FoodLog entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<FoodLog> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FoodLog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<FoodLog> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
