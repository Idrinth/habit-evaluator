package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MagicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for MagicLink entities.
 */
@Repository
public interface JpaMagicLinkRepository extends JpaRepository<MagicLink, String> {

    Optional<MagicLink> findByToken(String token);

    List<MagicLink> findByUserId(String userId);
}
