package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * H2 database implementation of UserRepository using JPA.
 * Used by the desktop application for local persistence.
 */
public class H2UserRepository implements UserRepository {

    @Override
    public User save(User user) {
        return JpaTransactionHelper.saveOrUpdate(User.class, user, User::getId);
    }

    @Override
    public Optional<User> findById(String id) {
        return JpaTransactionHelper.findById(User.class, id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return JpaTransactionHelper.findSingleByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.username = :username",
                "username",
                username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return JpaTransactionHelper.findSingleByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.email = :email",
                "email",
                email);
    }

    @Override
    public List<User> findAll() {
        return JpaTransactionHelper.findAll(User.class, "User");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(User.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(User.class, id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return JpaTransactionHelper.countByParameter(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                "username",
                username) > 0;
    }
}
