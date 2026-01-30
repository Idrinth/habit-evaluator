package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MagicLink;
import de.idrinth.habitevaluator.shared.repository.MagicLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of MagicLinkRepository.
 * Uses Spring Data JPA for persistence operations.
 */
@Repository
public class DatabaseMagicLinkRepository implements MagicLinkRepository {

    private final JpaMagicLinkRepository jpaRepository;

    public DatabaseMagicLinkRepository(JpaMagicLinkRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MagicLink save(MagicLink magicLink) {
        return jpaRepository.save(magicLink);
    }

    @Override
    public Optional<MagicLink> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<MagicLink> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public List<MagicLink> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
