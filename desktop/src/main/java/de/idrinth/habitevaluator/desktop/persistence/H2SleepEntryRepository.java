package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;

import java.util.List;
import java.util.Optional;

public class H2SleepEntryRepository implements SleepEntryRepository {

    @Override
    public SleepEntry save(SleepEntry entry) {
        return JpaTransactionHelper.saveOrUpdate(SleepEntry.class, entry, SleepEntry::getId);
    }

    @Override
    public Optional<SleepEntry> findById(String id) {
        return JpaTransactionHelper.findById(SleepEntry.class, id);
    }

    @Override
    public List<SleepEntry> findAll() {
        return JpaTransactionHelper.findAll(SleepEntry.class, "SleepEntry");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(SleepEntry.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(SleepEntry.class, id);
    }

    @Override
    public List<SleepEntry> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                SleepEntry.class,
                "SELECT s FROM SleepEntry s WHERE s.user.id = :userId ORDER BY s.date DESC, s.fromTime DESC",
                "userId",
                userId);
    }
}
