package ai.foresignal.export;

import ai.foresignal.store.history.HistoryDdl;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates {@code record_version_held}, the plain archive-then-delete copy table for legal holds
 * (prompt 111, Core-Data-Model §13). It mirrors {@code record_version}'s columns plus {@code hold_id},
 * with {@code (hold_id, seq)} as the primary key and GiST and {@code (source_id, external_id, seq)}
 * indexes. Hibernate does not map it; every access is native SQL. The DDL is idempotent.
 */
public class ExportSchemaInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ExportSchemaInitializer.class);

    /** The held-versions table. */
    public static final String HELD_TABLE = "record_version_held";

    private final DataSource dataSource;

    /** Creates the initializer. */
    public ExportSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /** Applies the held-table DDL. Safe to call repeatedly. */
    public void initialize() {
        String columns = HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::definition)
                .collect(Collectors.joining(", "));
        String create = "CREATE TABLE IF NOT EXISTS " + HELD_TABLE + " (hold_id varchar(26) NOT NULL, " + columns
                + ", PRIMARY KEY (hold_id, seq))";
        String sourceIndex = "CREATE INDEX IF NOT EXISTS record_version_held_source_idx ON " + HELD_TABLE
                + " (source_id, external_id, seq)";
        String gistIndex = "CREATE INDEX IF NOT EXISTS record_version_held_geom_gist ON " + HELD_TABLE
                + " USING GIST (geom)";
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(create);
            statement.execute(sourceIndex);
            statement.execute(gistIndex);
            log.info("Held-versions table {} is present", HELD_TABLE);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create " + HELD_TABLE + ": " + e.getMessage(), e);
        }
    }
}
