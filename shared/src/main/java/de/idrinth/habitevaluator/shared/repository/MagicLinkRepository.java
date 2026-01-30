package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.MagicLink;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for magic link persistence.
 * Platform-specific implementations will handle the actual storage.
 */
public interface MagicLinkRepository {

    /**
     * Saves a magic link.
     *
     * @param magicLink the magic link to save
     * @return the saved magic link
     */
    MagicLink save(MagicLink magicLink);

    /**
     * Finds a magic link by its ID.
     *
     * @param id the magic link ID
     * @return the magic link if found
     */
    Optional<MagicLink> findById(String id);

    /**
     * Finds a magic link by its token.
     *
     * @param token the unique token
     * @return the magic link if found
     */
    Optional<MagicLink> findByToken(String token);

    /**
     * Finds all magic links for a specific user.
     *
     * @param userId the user ID
     * @return list of magic links for the user
     */
    List<MagicLink> findByUserId(String userId);

    /**
     * Deletes a magic link by its ID.
     *
     * @param id the magic link ID to delete
     */
    void deleteById(String id);
}
