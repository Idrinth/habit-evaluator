package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;

import java.util.List;
import java.util.Optional;

public class H2EmotionEntryRepository implements EmotionEntryRepository {

    @Override
    public EmotionEntry save(EmotionEntry entry) {
        return JpaTransactionHelper.saveOrUpdate(EmotionEntry.class, entry, EmotionEntry::getId);
    }

    @Override
    public Optional<EmotionEntry> findById(String id) {
        return JpaTransactionHelper.findById(EmotionEntry.class, id);
    }

    @Override
    public List<EmotionEntry> findAll() {
        return JpaTransactionHelper.findAll(EmotionEntry.class, "EmotionEntry");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(EmotionEntry.class, id);
    }

    @Override
    public List<EmotionEntry> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                EmotionEntry.class,
                "SELECT e FROM EmotionEntry e WHERE e.user.id = :userId",
                "userId",
                userId);
    }
}
