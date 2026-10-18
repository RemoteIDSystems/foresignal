package ai.foresignal.config;

import ai.foresignal.search.SemanticCapability;
import ai.foresignal.search.VectorExtensionProbe;
import ai.foresignal.store.history.HistoryPartitionBootstrap;
import ai.foresignal.store.history.HistoryProperties;
import ai.foresignal.store.history.PartitionManager;
import io.micrometer.core.instrument.MeterRegistry;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Schema ownership since prompt 144 (Architecture §25): Flyway builds and migrates the schema from
 * {@code src/main/resources/db/migration}, and Hibernate only validates it ({@code ddl-auto=validate}).
 *
 * <p>Startup order:
 *
 * <ol>
 *   <li>Spring Boot's {@code flywayInitializer} runs {@code migrate} through {@link BaselineAwareMigrationStrategy}.
 *       Boot already orders the {@code EntityManagerFactory} after it.
 *   <li>{@link HistoryPartitionBootstrap} attaches {@code record_version}'s leaves through the {@link PartitionManager}
 *       (the one part of the history schema that stays dynamic), and {@link VectorExtensionProbe} records whether
 *       pgvector exists. Both force the Flyway initializer first and run before the {@code EntityManagerFactory}.
 * </ol>
 *
 * <p>What still runs DDL at runtime, by design: the {@link PartitionManager} (leaf create, attach, detach, drop), the
 * history downsampler's leaf rewrite, chronicle leaves, the embedding reindex's vector slots, the reference loader's
 * staging tables, and the SQLite export writers. {@code NoBootstrapDdlTest} keeps it that way.
 */
@Configuration
public class SchemaConfiguration {

    static final String PARTITION_BOOTSTRAP_BEAN = "historyPartitionBootstrap";
    static final String VECTOR_PROBE_BEAN = "vectorExtensionProbe";

    /** Runs {@code migrate}, warning before an existing database is adopted at version 1. */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new BaselineAwareMigrationStrategy();
    }

    /** Whether pgvector is available; filled in by {@link #vectorExtensionProbe}. */
    @Bean
    public SemanticCapability semanticCapability() {
        return new SemanticCapability();
    }

    /** Records whether pgvector exists, after Flyway; never fails the start. */
    @Bean(VECTOR_PROBE_BEAN)
    public VectorExtensionProbe vectorExtensionProbe(
            DataSource dataSource,
            SemanticCapability semanticCapability,
            ObjectProvider<FlywayMigrationInitializer> flywayInitializer) {
        flywayInitializer.getIfAvailable();
        return new VectorExtensionProbe(dataSource, semanticCapability);
    }

    /** Keeps {@code record_version}'s partitions ahead of allocation; no JPA dependency. */
    @Bean
    public PartitionManager partitionManager(
            DataSource dataSource, MeterRegistry meterRegistry, HistoryProperties historyProperties) {
        return new PartitionManager(dataSource, meterRegistry, historyProperties);
    }

    /** Attaches the first leaves once Flyway has created the partitioned parent. */
    @Bean(PARTITION_BOOTSTRAP_BEAN)
    public HistoryPartitionBootstrap historyPartitionBootstrap(
            PartitionManager partitionManager, ObjectProvider<FlywayMigrationInitializer> flywayInitializer) {
        flywayInitializer.getIfAvailable();
        return new HistoryPartitionBootstrap(partitionManager);
    }

    /**
     * Makes the {@code EntityManagerFactory} wait for the leaves and the pgvector check, so neither Hibernate's
     * validation nor anything that writes history runs before them.
     */
    @Bean
    public static EntityManagerFactoryDependsOnPostProcessor schemaDependsOnPostProcessor() {
        return new EntityManagerFactoryDependsOnPostProcessor(PARTITION_BOOTSTRAP_BEAN, VECTOR_PROBE_BEAN);
    }
}
