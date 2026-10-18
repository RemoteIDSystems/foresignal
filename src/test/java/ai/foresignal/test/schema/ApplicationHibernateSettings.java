package ai.foresignal.test.schema;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;

/**
 * The Hibernate settings the application's {@code EntityManagerFactory} is built with, minus {@code hbm2ddl.auto},
 * for schema tests that run Hibernate's schema tooling outside the context (prompt 144).
 */
public final class ApplicationHibernateSettings {

    private ApplicationHibernateSettings() {}

    /**
     * Resolves the settings the way Spring Boot does: {@code spring.jpa.properties}, the naming strategies and every
     * {@link HibernatePropertiesCustomizer}.
     *
     * @param jpaProperties the context's JPA properties
     * @param hibernateProperties the context's Hibernate properties
     * @param customizers the context's customizers
     * @return a mutable copy with no {@code hibernate.hbm2ddl.auto}
     */
    public static Map<String, Object> of(JpaProperties jpaProperties, HibernateProperties hibernateProperties,
            ObjectProvider<HibernatePropertiesCustomizer> customizers) {
        Map<String, Object> settings = new HashMap<>(hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(),
                new org.springframework.boot.hibernate.autoconfigure.HibernateSettings()
                        .ddlAuto(() -> "none")
                        .hibernatePropertiesCustomizers(customizers.orderedStream().toList())));
        settings.remove(org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO);
        return settings;
    }
}
