package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseHabitCategoryRepository implements HabitCategoryRepository {

    private final JpaHabitCategoryRepository jpaRepository;

    public DatabaseHabitCategoryRepository(JpaHabitCategoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HabitCategory save(HabitCategory category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<HabitCategory> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<HabitCategory> findAll() {
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
}
