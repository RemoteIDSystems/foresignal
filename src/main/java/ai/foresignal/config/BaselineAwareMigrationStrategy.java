package ai.foresignal.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;

/**
 * Runs Flyway's {@code migrate}, first warning when an existing development database is about to be adopted rather
 * than built (Architecture §25, prompt 144).
 *
 * <p>With {@code spring.flyway.baseline-on-migrate=true} and {@code baseline-version=1}, a non-empty schema that has
 * no history table is marked as already at version 1 and {@code V1__baseline.sql} is <em>not</em> run. That schema
 * was created by the pre-1.0 Hibernate {@code ddl-auto=update} plus bootstrap code, so it can differ from the
 * baseline in ways {@code ddl-auto=validate} does not catch (indexes, defaults, nullability). The warning tells the
 * operator to compare the two with {@code scripts/db/dump-schema.sh}, or to recreate the volume. A database that
 * already has the history table, or an empty one, migrates without a warning.
 */
public class BaselineAwareMigrationStrategy implements FlywayMigrationStrategy {

    private static final Logger log = LoggerFactory.getLogger(BaselineAwareMigrationStrategy.class);

    /**
     * Whether the current schema holds a table, partitioned table, view or sequence that no extension owns: the same
     * notion of "not empty" Flyway's PostgreSQL support uses (extension members such as PostGIS's
     * {@code spatial_ref_sys} do not count).
     */
    static final String SELECT_NON_EMPTY = """
            SELECT EXISTS (
              SELECT 1 FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN pg_catalog.pg_depend d ON d.objid = c.oid AND d.deptype = 'e'
               WHERE n.nspname = current_schema() AND d.objid IS NULL
                 AND c.relkind IN ('r', 'p', 'v', 'S') AND c.relname <> ?)
            """;

    /** Whether the history table exists in the current schema. */
    static final String SELECT_HISTORY = "SELECT to_regclass(quote_ident(current_schema()) || '.' || quote_ident(?)) IS NOT NULL";

    /** The operator-facing warning, logged once before an adoption. */
    static final String ADOPTION_WARNING = """
            Adopting an existing database at schema version 1 without running V1__baseline.sql \
            (spring.flyway.baseline-on-migrate). This schema was created before Flyway owned it, by Hibernate \
            ddl-auto=update and the old bootstrap code, and may differ from the baseline. Compare the two with \
            scripts/db/dump-schema.sh (see Architecture §25), or recreate the database volume \
            (docker compose down -v) so Flyway builds it from the baseline.""";

    @Override
    public void migrate(Flyway flyway) {
        if (willAdopt(flyway.getConfiguration())) {
            log.warn(ADOPTION_WARNING);
        }
        flyway.migrate();
    }

    /** True when baseline-on-migrate is on, the schema has no history table and the schema is not empty. */
    boolean willAdopt(Configuration configuration) {
        if (!configuration.isBaselineOnMigrate()) {
            return false;
        }
        DataSource dataSource = configuration.getDataSource();
        if (dataSource == null) {
            return false;
        }
        String table = configuration.getTable();
        try (Connection connection = dataSource.getConnection()) {
            return !queryBoolean(connection, SELECT_HISTORY, table) && queryBoolean(connection, SELECT_NON_EMPTY, table);
        } catch (SQLException e) {
            log.debug("Could not check whether Flyway will adopt the existing schema: {}", e.getMessage());
            return false;
        }
    }

    private static boolean queryBoolean(Connection connection, String sql, String parameter) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getBoolean(1);
            }
        }
    }
}
