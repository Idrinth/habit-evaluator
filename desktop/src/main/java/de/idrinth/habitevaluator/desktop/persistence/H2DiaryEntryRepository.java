package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;

import java.util.List;
import java.util.Optional;

public class H2DiaryEntryRepository implements DiaryEntryRepository {

    @Override
    public DiaryEntry save(DiaryEntry entry) {
        return JpaTransactionHelper.saveOrUpdate(DiaryEntry.class, entry, DiaryEntry::getId);
    }

    @Override
    public Optional<DiaryEntry> findById(String id) {
        return JpaTransactionHelper.findById(DiaryEntry.class, id);
    }

    @Override
    public List<DiaryEntry> findAll() {
        return JpaTransactionHelper.findAll(DiaryEntry.class, "DiaryEntry");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(DiaryEntry.class, id);
    }

    @Override
    public List<DiaryEntry> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                DiaryEntry.class,
                "SELECT d FROM DiaryEntry d WHERE d.user.id = :userId",
                "userId",
                userId);
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                String.class,
                "SELECT DISTINCT d.description FROM DiaryEntry d WHERE d.user.id = :userId ORDER BY d.description",
                "userId",
                userId);
    }
}
