package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseMedicationRepository implements MedicationRepository {

    private final JpaMedicationRepository jpaRepository;

    public DatabaseMedicationRepository(JpaMedicationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Medication save(Medication medication) {
        return jpaRepository.save(medication);
    }

    @Override
    public Optional<Medication> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Medication> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Medication> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
