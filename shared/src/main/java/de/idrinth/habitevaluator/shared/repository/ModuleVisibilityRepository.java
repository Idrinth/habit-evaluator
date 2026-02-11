package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;

import java.util.Optional;

/**
 * Repository interface for user module visibility settings.
 */
public interface ModuleVisibilityRepository {

    ModuleVisibility save(ModuleVisibility settings);

    Optional<ModuleVisibility> findByUserId(String userId);

    void deleteByUserId(String userId);
}
