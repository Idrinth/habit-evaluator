package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseMedicationLogRepository implements MedicationLogRepository {

    private final JpaMedicationLogRepository jpaRepository;

    public DatabaseMedicationLogRepository(JpaMedicationLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MedicationLog save(MedicationLog entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<MedicationLog> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MedicationLog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<MedicationLog> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<MedicationLog> findByUserIdPaged(String userId, int limit, int offset) {
        int page = offset / Math.max(limit, 1);
        return jpaRepository.findByUserIdPaged(userId, PageRequest.of(page, limit));
    }

    @Override
    public int countByUserId(String userId) {
        return jpaRepository.countByUserId(userId);
    }
}
