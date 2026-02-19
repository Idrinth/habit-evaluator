package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseFoodTagRepository implements FoodTagRepository {

    private final JpaFoodTagRepository jpaRepository;

    public DatabaseFoodTagRepository(JpaFoodTagRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FoodTag save(FoodTag tag) {
        return jpaRepository.save(tag);
    }

    @Override
    public Optional<FoodTag> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FoodTag> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<FoodTag> findByNameLowerAndUserId(String nameLower, String userId) {
        return jpaRepository.findByNameLowerAndUserId(nameLower, userId);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteEmptyTags(String userId) {
        jpaRepository.removeEmptyTagLinks(userId);
        jpaRepository.removeEmptyTags(userId);
    }
}
