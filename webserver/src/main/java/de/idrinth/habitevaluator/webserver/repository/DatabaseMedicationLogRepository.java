package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
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
}
