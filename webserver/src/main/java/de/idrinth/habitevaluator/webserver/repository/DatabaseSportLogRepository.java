package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseSportLogRepository implements SportLogRepository {

    private final JpaSportLogRepository jpaRepository;

    public DatabaseSportLogRepository(JpaSportLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SportLog save(SportLog entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<SportLog> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SportLog> findAll() {
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
    public List<SportLog> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<String> findDistinctNamesByUserId(String userId) {
        return jpaRepository.findDistinctNamesByUserId(userId);
    }

    @Override
    public List<String> findDistinctMeasurementUnitsByUserId(String userId) {
        return jpaRepository.findDistinctMeasurementUnitsByUserId(userId);
    }
}
