package ai.foresignal.config;

import ai.foresignal.search.SemanticCapability;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Phase 1 of {@link SchemaBootstrap}, right after PostGIS: creates the pgvector extension
 * (Architecture §7.3, prompt 027).
 *
 * <p>Unlike PostGIS, pgvector is optional. A failure (the package is not installed in the server, or
 * the database user may not create extensions) is logged once as a warning and recorded in
 * {@link SemanticCapability}; the application starts, text search works and
 * {@code GET /v1/search/status} explains why semantic search is off. The supported image is
 * {@code docker/db} ({@code foresignal-db:17-3.5-pgvector}).
 */
public class VectorExtensionInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(VectorExtensionInitializer.class);

    static final String CREATE_EXTENSION = "CREATE EXTENSION IF NOT EXISTS vector";

    private final DataSource dataSource;
    private final SemanticCapability capability;

    /** Creates the initializer; it does nothing until {@link #afterPropertiesSet()}. */
    public VectorExtensionInitializer(DataSource dataSource, SemanticCapability capability) {
        this.dataSource = dataSource;
        this.capability = capability;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /** Creates the extension, or records why it could not. Never throws. */
    public void initialize() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(CREATE_EXTENSION);
            capability.markAvailable();
            log.info("pgvector extension is present; semantic search can be enabled");
        } catch (SQLException e) {
            String reason = "The pgvector extension is not available in the database (" + e.getMessage().strip()
                    + "). Run the foresignal-db image from docker/db, or have a superuser run"
                    + " CREATE EXTENSION vector; then restart. Text search keeps working.";
            capability.markUnavailable(reason);
            log.warn("Semantic search is disabled: {}", reason);
        }
    }
}
