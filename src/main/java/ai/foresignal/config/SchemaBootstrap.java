package ai.foresignal.config;

import ai.foresignal.store.history.HistoryProperties;
import ai.foresignal.store.history.HistorySchemaInitializer;
import ai.foresignal.store.history.PartitionManager;
import io.micrometer.core.instrument.MeterRegistry;
import javax.sql.DataSource;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Creates the parts of the schema Hibernate cannot (Core-Data-Model §9).
 *
 * <p>Three steps, because they have different ordering needs:
 *
 * <ol>
 *   <li>{@link PostgisExtensionInitializer} runs <em>before</em> the {@code EntityManagerFactory}
 *       is built, since Hibernate's DDL references the {@code geometry} type.
 *   <li>{@link HistorySchemaInitializer} runs after it and also before the
 *       {@code EntityManagerFactory}: it creates the partitioned {@code record_version}, which
 *       Hibernate then maps as an existing table, and attaches its first partitions through the
 *       {@link PartitionManager}.
 *   <li>{@link SpatialIndexInitializer} runs <em>after</em> Hibernate's DDL, since its GiST indexes
 *       and storage parameters need their tables to exist.
 * </ol>
 *
 * <p>When Flyway is introduced for production (prompt 144), this DDL moves into the baseline
 * migration and the initializers are deleted; the {@link PartitionManager} stays.
 */
@Configuration
public class SchemaBootstrap {

    static final String POSTGIS_INITIALIZER_BEAN = "postgisExtensionInitializer";
    static final String HISTORY_INITIALIZER_BEAN = "historySchemaInitializer";

    @Bean(POSTGIS_INITIALIZER_BEAN)
    public PostgisExtensionInitializer postgisExtensionInitializer(DataSource dataSource) {
        return new PostgisExtensionInitializer(dataSource);
    }

    /** Keeps {@code record_version}'s partitions ahead of allocation; no JPA dependency. */
    @Bean
    @DependsOn(POSTGIS_INITIALIZER_BEAN)
    public PartitionManager partitionManager(
            DataSource dataSource, MeterRegistry meterRegistry, HistoryProperties historyProperties) {
        return new PartitionManager(dataSource, meterRegistry, historyProperties);
    }

    /** Creates the partitioned history table before the {@code EntityManagerFactory}. */
    @Bean(HISTORY_INITIALIZER_BEAN)
    @DependsOn(POSTGIS_INITIALIZER_BEAN)
    public HistorySchemaInitializer historySchemaInitializer(
            DataSource dataSource, PartitionManager partitionManager) {
        return new HistorySchemaInitializer(dataSource, partitionManager);
    }

    @Bean
    public SpatialIndexInitializer spatialIndexInitializer(DataSource dataSource) {
        return new SpatialIndexInitializer(dataSource);
    }

    /**
     * Forces the {@code EntityManagerFactory} to wait for the PostGIS extension and the partitioned
     * history table.
     *
     * <p>Without this, bean creation order is undefined: Hibernate could try to create
     * {@code geometry} columns before the extension exists, or create {@code record_version} as a
     * plain table before the initializer creates it partitioned.
     */
    @Bean
    public static EntityManagerFactoryDependsOnPostProcessor postgisDependsOnPostProcessor() {
        return new EntityManagerFactoryDependsOnPostProcessor(
                POSTGIS_INITIALIZER_BEAN, HISTORY_INITIALIZER_BEAN);
    }
}
