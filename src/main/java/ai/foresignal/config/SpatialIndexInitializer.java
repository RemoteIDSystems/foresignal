package ai.foresignal.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * The last step of {@link SchemaBootstrap}: creates the indexes and storage parameters JPA cannot
 * express.
 *
 * <p>Runs as the highest-precedence {@link ApplicationRunner}, which puts it after Hibernate's DDL
 * and before the collector starts doing spatial work. Every statement is {@code IF NOT EXISTS}, so
 * a restart is a no-op.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpatialIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SpatialIndexInitializer.class);

    /**
     * The statements Hibernate cannot generate. Prompt 144 moves these into the Flyway baseline and
     * deletes this class, so keep them here as one list rather than scattered.
     *
     * <p>{@code record_version}'s GiST index belongs to {@code HistorySchemaInitializer}, which owns
     * that table's DDL. {@code record_head} has no spatial index any more (its
     * {@code persisted_geom} is never searched), so an index left by an older build is dropped.
     * {@code record_live} gets the only record GiST index plus the storage parameters that keep
     * its touches HOT (Core-Data-Model §8).
     */
    static final List<String> STATEMENTS = List.of(
            "DROP INDEX IF EXISTS record_head_geom_gist",
            "CREATE INDEX IF NOT EXISTS record_live_geom_gist ON record_live USING GIST (geom)",
            "ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02,"
                    + " autovacuum_vacuum_cost_limit = 2000)",
            "CREATE INDEX IF NOT EXISTS trigger_rule_geofence_gist ON trigger_rule USING GIST (geofence)");

    private final DataSource dataSource;

    public SpatialIndexInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        execute();
    }

    /** Applies every statement. Safe to call repeatedly. */
    public void execute() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String sql : STATEMENTS) {
                statement.execute(sql);
            }
            log.info("Spatial indexes and storage parameters are present ({} statements applied)", STATEMENTS.size());
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not create Foresignal's spatial indexes: " + e.getMessage(), e);
        }
    }
}
