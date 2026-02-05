package de.idrinth.habitevaluator.desktop.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper class to reduce boilerplate in JPA repository implementations.
 * Provides common patterns for transaction management and CRUD operations.
 */
public final class JpaTransactionHelper {

    private JpaTransactionHelper() {
        // Utility class
    }

    /**
     * Executes an action within a transaction, handling commit/rollback and resource cleanup.
     *
     * @param action the action to execute, receives the EntityManager
     * @param <R>    the return type
     * @return the result of the action
     */
    public static <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Executes a void action within a transaction.
     *
     * @param action the action to execute
     */
    public static void executeInTransactionVoid(Consumer<EntityManager> action) {
        executeInTransaction(em -> {
            action.accept(em);
            return null;
        });
    }

    /**
     * Executes a read-only action (no transaction needed).
     *
     * @param action the action to execute
     * @param <R>    the return type
     * @return the result of the action
     */
    public static <R> R executeReadOnly(Function<EntityManager, R> action) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return action.apply(em);
        } finally {
            em.close();
        }
    }

    /**
     * Saves or updates an entity using merge/persist pattern.
     *
     * @param entityClass the entity class
     * @param entity      the entity to save
     * @param getId       function to get the entity's ID
     * @param <T>         the entity type
     * @return the saved entity
     */
    public static <T> T saveOrUpdate(Class<T> entityClass, T entity, Function<T, String> getId) {
        return executeInTransaction(em -> {
            T existing = em.find(entityClass, getId.apply(entity));
            if (existing != null) {
                return em.merge(entity);
            } else {
                em.persist(entity);
                return entity;
            }
        });
    }

    /**
     * Finds an entity by ID.
     *
     * @param entityClass the entity class
     * @param id          the entity ID
     * @param <T>         the entity type
     * @return Optional containing the entity if found
     */
    public static <T> Optional<T> findById(Class<T> entityClass, String id) {
        return executeReadOnly(em -> Optional.ofNullable(em.find(entityClass, id)));
    }

    /**
     * Finds all entities of a given type.
     *
     * @param entityClass the entity class
     * @param entityName  the JPQL entity name
     * @param <T>         the entity type
     * @return list of all entities
     */
    public static <T> List<T> findAll(Class<T> entityClass, String entityName) {
        return executeReadOnly(em ->
                em.createQuery("SELECT e FROM " + entityName + " e", entityClass)
                        .getResultList());
    }

    /**
     * Finds entities by a single parameter.
     *
     * @param entityClass the entity class
     * @param jpql        the JPQL query
     * @param paramName   the parameter name
     * @param paramValue  the parameter value
     * @param <T>         the entity type
     * @return list of matching entities
     */
    public static <T> List<T> findByParameter(Class<T> entityClass, String jpql,
                                               String paramName, Object paramValue) {
        return executeReadOnly(em ->
                em.createQuery(jpql, entityClass)
                        .setParameter(paramName, paramValue)
                        .getResultList());
    }

    /**
     * Deletes an entity by ID.
     *
     * @param entityClass the entity class
     * @param id          the entity ID
     * @param <T>         the entity type
     */
    public static <T> void deleteById(Class<T> entityClass, String id) {
        executeInTransactionVoid(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
        });
    }

    /**
     * Checks if an entity exists by ID.
     *
     * @param entityClass the entity class
     * @param id          the entity ID
     * @param <T>         the entity type
     * @return true if the entity exists
     */
    public static <T> boolean existsById(Class<T> entityClass, String id) {
        return executeReadOnly(em -> em.find(entityClass, id) != null);
    }

    /**
     * Finds a single entity by a parameter, returning Optional.empty() if not found.
     *
     * @param entityClass the entity class
     * @param jpql        the JPQL query
     * @param paramName   the parameter name
     * @param paramValue  the parameter value
     * @param <T>         the entity type
     * @return Optional containing the entity if found
     */
    public static <T> Optional<T> findSingleByParameter(Class<T> entityClass, String jpql,
                                                         String paramName, Object paramValue) {
        return executeReadOnly(em -> {
            try {
                T result = em.createQuery(jpql, entityClass)
                        .setParameter(paramName, paramValue)
                        .getSingleResult();
                return Optional.of(result);
            } catch (jakarta.persistence.NoResultException e) {
                return Optional.empty();
            }
        });
    }

    /**
     * Counts entities matching a query parameter.
     *
     * @param jpql       the JPQL count query
     * @param paramName  the parameter name
     * @param paramValue the parameter value
     * @return the count
     */
    public static long countByParameter(String jpql, String paramName, Object paramValue) {
        return executeReadOnly(em ->
                em.createQuery(jpql, Long.class)
                        .setParameter(paramName, paramValue)
                        .getSingleResult());
    }
}
