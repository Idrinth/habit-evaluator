package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaModuleVisibilityRepository extends JpaRepository<ModuleVisibility, String> {

    @Query("SELECT mv FROM ModuleVisibility mv WHERE mv.user.id = :userId")
    Optional<ModuleVisibility> findByUserId(@Param("userId") String userId);

    void deleteByUserId(String userId);
}
