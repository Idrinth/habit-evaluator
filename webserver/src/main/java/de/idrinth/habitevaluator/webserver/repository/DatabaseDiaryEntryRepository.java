package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseDiaryEntryRepository implements DiaryEntryRepository {

    private final JpaDiaryEntryRepository jpaRepository;

    public DatabaseDiaryEntryRepository(JpaDiaryEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DiaryEntry save(DiaryEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<DiaryEntry> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<DiaryEntry> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<DiaryEntry> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return jpaRepository.findDistinctDescriptionsByUserId(userId);
    }
}
