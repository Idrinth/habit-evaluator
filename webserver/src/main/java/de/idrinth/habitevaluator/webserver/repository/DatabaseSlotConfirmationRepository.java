package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseSlotConfirmationRepository implements SlotConfirmationRepository {

    private final JpaSlotConfirmationRepository jpaRepository;

    public DatabaseSlotConfirmationRepository(JpaSlotConfirmationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SlotConfirmation save(SlotConfirmation confirmation) {
        return jpaRepository.save(confirmation);
    }

    @Override
    public Optional<SlotConfirmation> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SlotConfirmation> findAll() {
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
    public List<SlotConfirmation> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
