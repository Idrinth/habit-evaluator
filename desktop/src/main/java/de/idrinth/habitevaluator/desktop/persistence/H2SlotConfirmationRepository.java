package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;

import java.util.List;
import java.util.Optional;

public class H2SlotConfirmationRepository implements SlotConfirmationRepository {

    @Override
    public SlotConfirmation save(SlotConfirmation confirmation) {
        return JpaTransactionHelper.saveOrUpdate(SlotConfirmation.class, confirmation, SlotConfirmation::getId);
    }

    @Override
    public Optional<SlotConfirmation> findById(String id) {
        return JpaTransactionHelper.findById(SlotConfirmation.class, id);
    }

    @Override
    public List<SlotConfirmation> findAll() {
        return JpaTransactionHelper.findAll(SlotConfirmation.class, "SlotConfirmation");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(SlotConfirmation.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(SlotConfirmation.class, id);
    }

    @Override
    public List<SlotConfirmation> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                SlotConfirmation.class,
                "SELECT c FROM SlotConfirmation c WHERE c.user.id = :userId ORDER BY c.date DESC, c.createdAt DESC",
                "userId",
                userId);
    }
}
