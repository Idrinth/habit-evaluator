package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PersonTag;
import de.idrinth.habitevaluator.shared.repository.PersonTagRepository;

import java.util.List;
import java.util.Optional;

public class H2PersonTagRepository implements PersonTagRepository {

    @Override
    public PersonTag save(PersonTag tag) {
        return JpaTransactionHelper.saveOrUpdate(PersonTag.class, tag, PersonTag::getId);
    }

    @Override
    public Optional<PersonTag> findById(String id) {
        return JpaTransactionHelper.findById(PersonTag.class, id);
    }

    @Override
    public List<PersonTag> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                PersonTag.class,
                "SELECT t FROM PersonTag t WHERE t.user.id = :userId ORDER BY LOWER(t.name)",
                "userId",
                userId);
    }

    @Override
    public Optional<PersonTag> findByNameLowerAndUserId(String nameLower, String userId) {
        List<PersonTag> results = JpaTransactionHelper.executeReadOnly(em ->
                em.createQuery(
                                "SELECT t FROM PersonTag t WHERE t.nameLower = :nameLower AND t.user.id = :userId",
                                PersonTag.class)
                        .setParameter("nameLower", nameLower)
                        .setParameter("userId", userId)
                        .getResultList());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(PersonTag.class, id);
    }

    @Override
    public void deleteEmptyTags(String userId) {
        JpaTransactionHelper.executeInTransactionVoid(em -> {
            em.createNativeQuery(
                    "DELETE FROM activity_log_person_tags WHERE person_tag_id IN "
                            + "(SELECT id FROM person_tags WHERE (name IS NULL OR TRIM(name) = '') AND user_id = :userId)")
                    .setParameter("userId", userId)
                    .executeUpdate();
            em.createNativeQuery(
                    "DELETE FROM person_tags WHERE (name IS NULL OR TRIM(name) = '') AND user_id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }
}
