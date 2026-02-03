package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseEmotionEntryRepository implements EmotionEntryRepository {

    private final JpaEmotionEntryRepository jpaRepository;

    public DatabaseEmotionEntryRepository(JpaEmotionEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmotionEntry save(EmotionEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<EmotionEntry> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<EmotionEntry> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<EmotionEntry> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
