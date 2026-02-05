package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import java.util.List;
import java.util.Optional;

public class H2EmotionPairRepository implements EmotionPairRepository {

    @Override
    public EmotionPair save(EmotionPair pair) {
        return JpaTransactionHelper.saveOrUpdate(EmotionPair.class, pair, EmotionPair::getId);
    }

    @Override
    public Optional<EmotionPair> findById(String id) {
        return JpaTransactionHelper.findById(EmotionPair.class, id);
    }

    @Override
    public List<EmotionPair> findAll() {
        return JpaTransactionHelper.findAll(EmotionPair.class, "EmotionPair");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(EmotionPair.class, id);
    }

    @Override
    public List<EmotionPair> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                EmotionPair.class,
                "SELECT e FROM EmotionPair e WHERE e.user.id = :userId",
                "userId",
                userId);
    }
}
