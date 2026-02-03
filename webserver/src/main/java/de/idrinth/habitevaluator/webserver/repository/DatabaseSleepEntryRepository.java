package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseSleepEntryRepository implements SleepEntryRepository {

    private final JpaSleepEntryRepository jpaRepository;

    public DatabaseSleepEntryRepository(JpaSleepEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SleepEntry save(SleepEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<SleepEntry> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SleepEntry> findAll() {
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

    @Override
    public List<SleepEntry> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
