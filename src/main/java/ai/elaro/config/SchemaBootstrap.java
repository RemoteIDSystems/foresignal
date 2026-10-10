package ai.elaro.config;

import javax.sql.DataSource;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the parts of the schema Hibernate cannot (Architecture §7.3).
 *
 * <p>Two phases, because they have different ordering needs:
 *
 * <ol>
 *   <li>{@link PostgisExtensionInitializer} runs <em>before</em> the {@code EntityManagerFactory}
 *       is built, since Hibernate's DDL references the {@code geometry} type.
 *   <li>{@link SpatialIndexInitializer} runs <em>after</em> Hibernate's DDL, since the GiST indexes
 *       need their tables to exist.
 * </ol>
 *
 * <p>When Flyway is introduced for production (prompt 042), both phases move into the baseline
 * migration and this class and its two initializers are deleted.
 */
@Configuration
public class SchemaBootstrap {

    static final String POSTGIS_INITIALIZER_BEAN = "postgisExtensionInitializer";

    @Bean(POSTGIS_INITIALIZER_BEAN)
    public PostgisExtensionInitializer postgisExtensionInitializer(DataSource dataSource) {
        return new PostgisExtensionInitializer(dataSource);
    }

    @Bean
    public SpatialIndexInitializer spatialIndexInitializer(DataSource dataSource) {
        return new SpatialIndexInitializer(dataSource);
    }

    /**
     * Forces the {@code EntityManagerFactory} to wait for the PostGIS extension.
     *
     * <p>Without this, bean creation order is undefined and Hibernate can try to create
     * {@code geometry} columns before the extension exists.
     */
    @Bean
    public static EntityManagerFactoryDependsOnPostProcessor postgisDependsOnPostProcessor() {
        return new EntityManagerFactoryDependsOnPostProcessor(POSTGIS_INITIALIZER_BEAN);
    }
}
