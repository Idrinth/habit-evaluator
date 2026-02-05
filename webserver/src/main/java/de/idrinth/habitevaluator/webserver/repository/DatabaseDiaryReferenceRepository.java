package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseDiaryReferenceRepository implements DiaryReferenceRepository {

    private final JpaDiaryReferenceRepository jpaRepository;

    public DatabaseDiaryReferenceRepository(JpaDiaryReferenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DiaryReference save(DiaryReference reference) {
        return jpaRepository.save(reference);
    }

    @Override
    public Optional<DiaryReference> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<DiaryReference> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<DiaryReference> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description) {
        return jpaRepository.findByUserIdAndDescriptionIgnoreCase(userId, description);
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return jpaRepository.findDistinctDescriptionsByUserId(userId);
    }
}
