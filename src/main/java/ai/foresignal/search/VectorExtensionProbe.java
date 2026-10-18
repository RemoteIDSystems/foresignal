package ai.foresignal.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Records in {@link SemanticCapability} whether the pgvector extension exists (prompt 027; read-only since 144).
 *
 * <p>The Flyway baseline creates the extension, so a database migrated from it always has it. A development database
 * adopted with {@code baseline-on-migrate} skipped V1 and may not; then semantic search is reported off with the
 * reason, and text search keeps working. The probe runs no DDL and never fails the start.
 */
public class VectorExtensionProbe implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(VectorExtensionProbe.class);

    static final String SELECT_EXTENSION = "SELECT 1 FROM pg_extension WHERE extname = 'vector'";

    private final DataSource dataSource;
    private final SemanticCapability capability;

    /**
     * Creates the probe; it does nothing until {@link #afterPropertiesSet()}.
     *
     * @param dataSource the application database
     * @param capability where the outcome is recorded
     */
    public VectorExtensionProbe(DataSource dataSource, SemanticCapability capability) {
        this.dataSource = dataSource;
        this.capability = capability;
    }

    @Override
    public void afterPropertiesSet() {
        probe();
    }

    /** Checks for the extension and records the outcome. Never throws. */
    public void probe() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(SELECT_EXTENSION)) {
            if (rows.next()) {
                capability.markAvailable();
                log.info("pgvector extension is present; semantic search can be enabled");
            } else {
                markMissing("the vector extension is not installed in this database");
            }
        } catch (SQLException e) {
            markMissing(e.getMessage().strip());
        }
    }

    private void markMissing(String detail) {
        String reason = "The pgvector extension is not available in the database (" + detail
                + "). Run the foresignal-db image from docker/db, or have a superuser run"
                + " CREATE EXTENSION vector; then restart. Text search keeps working.";
        capability.markUnavailable(reason);
        log.warn("Semantic search is disabled: {}", reason);
    }
}
