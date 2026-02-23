package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseMeetingEntryRepository implements MeetingEntryRepository {

    private final JpaMeetingEntryRepository jpaRepository;

    public DatabaseMeetingEntryRepository(JpaMeetingEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MeetingEntry save(MeetingEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<MeetingEntry> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MeetingEntry> findAll() {
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
    public List<MeetingEntry> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<String> findDistinctPlacesByUserId(String userId) {
        return jpaRepository.findDistinctPlacesByUserId(userId);
    }
}
