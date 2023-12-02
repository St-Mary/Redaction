package com.stmarygate.redaction.database;

import com.stmarygate.redaction.Constants;
import jakarta.persistence.Entity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    /**
     * Retrieve or build the Hibernate session.
     *
     * @return The Hibernate session.
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

                Map<String, Object> settings = getStringObjectMap();

                registryBuilder.applySettings(settings);
                registry = registryBuilder.build();

                MetadataSources sources = new MetadataSources(registry);
                Reflections reflections = new Reflections("com.stmarygate.redaction.entities");
                for (Class<?> cls : reflections.getTypesAnnotatedWith(Entity.class)) {
                    sources.addAnnotatedClass(cls);
                }

                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }

                LOGGER.error("An error occurred while building the Hibernate session factory: ", e);
            }
        }
        return sessionFactory;
    }

    /**
     * Get the settings for the Hibernate session.
     *
     * @return The settings for the Hibernate session.
     */
    @NotNull
    private static Map<String, Object> getStringObjectMap() {
        Map<String, Object> settings = new HashMap<>();
        settings.put(Environment.DRIVER, "org.postgresql.Driver");
        settings.put(
                Environment.URL,
                "jdbc:postgresql"
                        + "://"
                        + Constants.getDatabaseHost()
                        + Constants.getDatabasePort()
                        + "/"
                        + Constants.getDatabaseName());
        settings.put(Environment.USER, Constants.getDatabaseUsername());
        settings.put(Environment.PASS, Constants.getDatabasePassword());
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.SHOW_SQL, false);
        settings.put(Environment.FORMAT_SQL, true);
        settings.put(
                Environment.CONNECTION_PROVIDER,
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        // HikariCP settings

        settings.put("hibernate.hikari.connectionTimeout", "20000");
        settings.put("hibernate.hikari.minimumIdle", "10");
        settings.put("hibernate.hikari.maximumPoolSize", "20");
        settings.put("hibernate.hikari.idleTimeout", "300000");
        return settings;
    }

    /** Close the Hibernate session. */
    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    /**
     * Find an entity by its ID.
     *
     * @param id The ID of the entity.
     * @param cls The class of the entity.
     * @param <T> The type of the entity.
     * @return The entity corresponding to the ID, or null if not found.
     */
    public static <T> T findById(Long id, Class<T> cls) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        T obj = session.find(cls, id);
        session.getTransaction().commit();
        session.close();
        return obj;
    }

    public static <T> T findByName(String name, Class<T> cls) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        T obj = session.createQuery("from " + cls.getSimpleName() + " WHERE name = :name", cls)
                .setParameter("name", name)
                .uniqueResult();
        session.getTransaction().commit();
        session.close();
        return obj;
    }

    /**
     * Save or update an object in the database.
     *
     * @param obj The object to save or update.
     */
    public static void save(Object obj) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(obj);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Delete an object from the database.
     *
     * @param obj The object to delete.
     */
    public static void delete(Object obj) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(obj);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Update an object in the database.
     *
     * @param obj The object to update.
     */
    public static void update(Object obj) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(obj);
        session.getTransaction().commit();
        session.close();
    }
}
