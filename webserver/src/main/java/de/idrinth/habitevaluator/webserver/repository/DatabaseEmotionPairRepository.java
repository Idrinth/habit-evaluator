package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseEmotionPairRepository implements EmotionPairRepository {

    private final JpaEmotionPairRepository jpaRepository;

    public DatabaseEmotionPairRepository(JpaEmotionPairRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmotionPair save(EmotionPair pair) {
        return jpaRepository.save(pair);
    }

    @Override
    public Optional<EmotionPair> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<EmotionPair> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<EmotionPair> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
