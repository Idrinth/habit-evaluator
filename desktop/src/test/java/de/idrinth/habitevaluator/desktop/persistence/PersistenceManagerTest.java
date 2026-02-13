package de.idrinth.habitevaluator.desktop.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceManagerTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset static state before each test
        resetFactory();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up after each test
        try {
            EntityManagerFactory emf = getFactory();
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        } catch (Exception ignored) {
        }
        resetFactory();
    }

    @Test
    void testGetEntityManagerFactoryReturnsNonNull() throws Exception {
        injectTestFactory();
        EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();
        assertNotNull(emf);
    }

    @Test
    void testGetEntityManagerFactoryReturnsSameInstance() throws Exception {
        injectTestFactory();
        EntityManagerFactory first = PersistenceManager.getEntityManagerFactory();
        EntityManagerFactory second = PersistenceManager.getEntityManagerFactory();
        assertSame(first, second);
    }

    @Test
    void testCreateEntityManagerReturnsOpenEntityManager() throws Exception {
        injectTestFactory();
        EntityManager em = PersistenceManager.createEntityManager();
        assertNotNull(em);
        assertTrue(em.isOpen());
        em.close();
    }

    @Test
    void testCloseClosesFactory() throws Exception {
        injectTestFactory();
        EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();
        assertTrue(emf.isOpen());

        PersistenceManager.close();

        assertFalse(emf.isOpen());
    }

    @Test
    void testCloseNullsFactory() throws Exception {
        injectTestFactory();
        PersistenceManager.getEntityManagerFactory();

        PersistenceManager.close();

        assertNull(getFactory());
    }

    @Test
    void testCloseWhenAlreadyNullDoesNotThrow() {
        assertDoesNotThrow(() -> PersistenceManager.close());
    }

    @Test
    void testCloseWhenAlreadyClosedDoesNotThrow() throws Exception {
        injectTestFactory();
        EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();
        emf.close();

        assertDoesNotThrow(() -> PersistenceManager.close());
    }

    @Test
    void testGetEntityManagerFactoryRecreatesAfterClose() throws Exception {
        injectTestFactory();
        EntityManagerFactory first = PersistenceManager.getEntityManagerFactory();
        PersistenceManager.close();

        injectTestFactory();
        EntityManagerFactory second = PersistenceManager.getEntityManagerFactory();

        assertNotNull(second);
        assertTrue(second.isOpen());
        assertNotSame(first, second);
    }

    @Test
    void testMultipleEntityManagersCanBeCreated() throws Exception {
        injectTestFactory();
        EntityManager em1 = PersistenceManager.createEntityManager();
        EntityManager em2 = PersistenceManager.createEntityManager();

        assertNotSame(em1, em2);
        assertTrue(em1.isOpen());
        assertTrue(em2.isOpen());

        em1.close();
        em2.close();
    }

    @Test
    void testClosingEntityManagerDoesNotCloseFactory() throws Exception {
        injectTestFactory();
        EntityManager em = PersistenceManager.createEntityManager();
        em.close();

        EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();
        assertTrue(emf.isOpen());
    }

    private void injectTestFactory() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:pm_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE");
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");

        EntityManagerFactory testEmf = Persistence.createEntityManagerFactory("habit-evaluator-desktop-test", properties);
        Field emfField = PersistenceManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        emfField.set(null, testEmf);
    }

    private EntityManagerFactory getFactory() throws Exception {
        Field emfField = PersistenceManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        return (EntityManagerFactory) emfField.get(null);
    }

    private void resetFactory() throws Exception {
        Field emfField = PersistenceManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        EntityManagerFactory existing = (EntityManagerFactory) emfField.get(null);
        if (existing != null && existing.isOpen()) {
            existing.close();
        }
        emfField.set(null, null);
    }
}
