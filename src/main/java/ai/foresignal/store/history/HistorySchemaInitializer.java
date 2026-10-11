package ai.foresignal.store.history;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates the partitioned {@code record_version} before Hibernate looks at the schema
 * (Core-Data-Model §9, phase 1).
 *
 * <p>Registered by {@code SchemaBootstrap} after the PostGIS extension and before the
 * {@code EntityManagerFactory}. On one connection it:
 *
 * <ol>
 *   <li>creates {@code record_seq};
 *   <li>replaces the prompt-002 plain {@code record_version} (relkind {@code r}) when it and
 *       {@code record_head} are empty, dropping the head too so Hibernate recreates it without the
 *       old NOT NULL columns, and otherwise stops with {@value HistoryDdl#LEGACY_DATA_MESSAGE};
 *   <li>creates the parent and both tier parents, then adds any missing column in place;
 *   <li>creates the three partitioned indexes.
 * </ol>
 *
 * <p>Then {@link PartitionManager#ensureAhead()} attaches at least 1.5 blocks of leaves per tier.
 * Every step is idempotent, so a restart is a no-op. Depends on no JPA component.
 */
public class HistorySchemaInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(HistorySchemaInitializer.class);

    private final DataSource dataSource;
    private final PartitionManager partitionManager;

    /** Creates the initializer; it does nothing until {@link #afterPropertiesSet()}. */
    public HistorySchemaInitializer(DataSource dataSource, PartitionManager partitionManager) {
        this.dataSource = dataSource;
        this.partitionManager = partitionManager;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /**
     * Applies the bootstrap DDL and ensures partition coverage. Safe to call repeatedly.
     *
     * @throws IllegalStateException when a populated pre-v1.2 {@code record_version} is found, or
     *     the DDL fails
     */
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute(HistoryDdl.CREATE_SEQUENCE);
                replaceLegacyTable(statement);
                statement.execute(HistoryDdl.CREATE_PARENT);
                statement.execute(HistoryDdl.CREATE_STANDARD_PARENT);
                statement.execute(HistoryDdl.CREATE_TRACK_PARENT);
                statement.execute(HistoryDdl.ADD_MISSING_COLUMNS);
                for (String sql : HistoryDdl.CREATE_INDEXES) {
                    statement.execute(sql);
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not create the partitioned record_version: " + e.getMessage(), e);
        }
        partitionManager.ensureAhead();
        log.info("Partitioned record_version is present");
    }

    private static void replaceLegacyTable(Statement statement) throws SQLException {
        String relkind = null;
        try (ResultSet rows = statement.executeQuery(HistoryDdl.SELECT_RELKIND)) {
            if (rows.next()) {
                relkind = rows.getString(1);
            }
        }
        if (!"r".equals(relkind)) {
            return;
        }
        boolean empty = queryBoolean(statement, HistoryDdl.LEGACY_VERSION_EMPTY)
                && (!queryBoolean(statement, HistoryDdl.HEAD_EXISTS)
                        || queryBoolean(statement, HistoryDdl.HEAD_EMPTY));
        if (!empty) {
            throw new IllegalStateException(HistoryDdl.LEGACY_DATA_MESSAGE);
        }
        statement.execute(HistoryDdl.DROP_LEGACY);
        log.info("Replaced the empty pre-v1.2 record_version and record_head tables");
    }

    private static boolean queryBoolean(Statement statement, String sql) throws SQLException {
        try (ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getBoolean(1);
        }
    }
}
