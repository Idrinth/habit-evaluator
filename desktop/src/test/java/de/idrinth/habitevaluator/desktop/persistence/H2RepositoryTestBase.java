package de.idrinth.habitevaluator.desktop.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for H2 repository integration tests.
 * Sets up an in-memory H2 database and injects it into PersistenceManager
 * so the repository classes can be tested without JavaFX runtime.
 */
public abstract class H2RepositoryTestBase {

    private static EntityManagerFactory testEmf;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE");
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");

        testEmf = Persistence.createEntityManagerFactory("habit-evaluator-desktop-test", properties);

        Field emfField = PersistenceManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        emfField.set(null, testEmf);
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        Field emfField = PersistenceManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        emfField.set(null, null);

        if (testEmf != null && testEmf.isOpen()) {
            testEmf.close();
        }
    }
}
