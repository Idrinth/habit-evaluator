package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository;

import java.util.List;
import java.util.Optional;

public class H2GratitudeEntryRepository implements GratitudeEntryRepository {

    @Override
    public GratitudeEntry save(GratitudeEntry entry) {
        return JpaTransactionHelper.saveOrUpdate(GratitudeEntry.class, entry, GratitudeEntry::getId);
    }

    @Override
    public Optional<GratitudeEntry> findById(String id) {
        return JpaTransactionHelper.findById(GratitudeEntry.class, id);
    }

    @Override
    public List<GratitudeEntry> findAll() {
        return JpaTransactionHelper.findAll(GratitudeEntry.class, "GratitudeEntry");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(GratitudeEntry.class, id);
    }

    @Override
    public List<GratitudeEntry> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                GratitudeEntry.class,
                "SELECT g FROM GratitudeEntry g WHERE g.user.id = :userId ORDER BY g.eventDate DESC, g.createdAt DESC",
                "userId",
                userId);
    }
}
