package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PersonTag;
import de.idrinth.habitevaluator.shared.repository.PersonTagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabasePersonTagRepository implements PersonTagRepository {

    private final JpaPersonTagRepository jpaRepository;

    public DatabasePersonTagRepository(JpaPersonTagRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PersonTag save(PersonTag tag) {
        return jpaRepository.save(tag);
    }

    @Override
    public Optional<PersonTag> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PersonTag> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<PersonTag> findByNameLowerAndUserId(String nameLower, String userId) {
        return jpaRepository.findByNameLowerAndUserId(nameLower, userId);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteEmptyTags(String userId) {
        jpaRepository.removeEmptyTagLinks(userId);
        jpaRepository.removeEmptyTags(userId);
    }
}
