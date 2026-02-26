package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseGratitudeEntryRepository implements GratitudeEntryRepository {

    private final JpaGratitudeEntryRepository jpaRepository;

    public DatabaseGratitudeEntryRepository(JpaGratitudeEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GratitudeEntry save(GratitudeEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<GratitudeEntry> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<GratitudeEntry> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<GratitudeEntry> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
