package ai.foresignal.chronicle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates the partitioned {@code chronicle_version} parent and its partitioned indexes (prompt 131,
 * {@link ChronicleDdl}). Registered by {@code SchemaBootstrap} after the PostGIS extension; it depends on no JPA
 * component, and Hibernate never maps the table. Leaves are created per chronicle by {@link ChronicleStorage}. Every
 * statement is idempotent, so a restart is a no-op.
 */
public class ChronicleSchemaInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ChronicleSchemaInitializer.class);

    private final DataSource dataSource;

    /** Creates the initializer; it does nothing until {@link #afterPropertiesSet()}. */
    public ChronicleSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /** Applies the parent DDL. Safe to call repeatedly. */
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute(ChronicleDdl.CREATE_PARENT);
                statement.execute(ChronicleDdl.ADD_MISSING_COLUMNS);
                for (String sql : ChronicleDdl.CREATE_INDEXES) {
                    statement.execute(sql);
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the partitioned chronicle_version: " + e.getMessage(), e);
        }
        log.info("Partitioned chronicle_version is present");
    }
}
