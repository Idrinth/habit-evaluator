package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DatabaseModuleVisibilityRepository implements ModuleVisibilityRepository {

    private final JpaModuleVisibilityRepository jpaRepository;

    public DatabaseModuleVisibilityRepository(JpaModuleVisibilityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ModuleVisibility save(ModuleVisibility settings) {
        return jpaRepository.save(settings);
    }

    @Override
    public Optional<ModuleVisibility> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
