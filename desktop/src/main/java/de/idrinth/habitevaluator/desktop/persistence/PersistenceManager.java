package de.idrinth.habitevaluator.desktop.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages JPA EntityManagerFactory lifecycle for desktop application.
 * Uses H2 database for local persistence.
 */
public class PersistenceManager {

    private static final String PERSISTENCE_UNIT_NAME = "habit-evaluator-desktop";
    private static EntityManagerFactory entityManagerFactory;

    private PersistenceManager() {
    }

    /**
     * Gets or creates the EntityManagerFactory with H2 configuration.
     */
    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            Map<String, String> properties = new HashMap<>();

            // H2 database configuration - file-based for persistence
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + "/.habit-evaluator/data";
            properties.put("jakarta.persistence.jdbc.url",
                    "jdbc:h2:file:" + dbPath + ";DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE");
            properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            properties.put("jakarta.persistence.jdbc.user", "sa");
            properties.put("jakarta.persistence.jdbc.password", "");

            // Hibernate settings
            properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.show_sql", "false");
            properties.put("hibernate.format_sql", "false");

            entityManagerFactory = Persistence.createEntityManagerFactory(
                    PERSISTENCE_UNIT_NAME, properties);
        }
        return entityManagerFactory;
    }

    /**
     * Creates a new EntityManager from the factory.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Closes the EntityManagerFactory. Should be called on application shutdown.
     */
    public static synchronized void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
