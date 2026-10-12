package ai.foresignal.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Phase 1 of {@link SchemaBootstrap}: creates the PostGIS extension before Hibernate looks at the
 * schema.
 *
 * <p>This has to run first because Hibernate's DDL declares {@code geometry} columns, and those
 * types do not exist until the extension is installed. Ordering is enforced by
 * {@link SchemaBootstrap}, which makes the {@code EntityManagerFactory} depend on this bean.
 */
public class PostgisExtensionInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PostgisExtensionInitializer.class);

    static final String CREATE_EXTENSION = "CREATE EXTENSION IF NOT EXISTS postgis";

    private final DataSource dataSource;

    public PostgisExtensionInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(CREATE_EXTENSION);
            log.info("PostGIS extension is present");
        } catch (SQLException e) {
            // Almost always one of: the postgis package is not installed in the server, or the
            // application's database user is not a superuser and the extension was never created
            // for it. Both need an operator, so say which and stop rather than failing later with
            // an unhelpful "type geometry does not exist".
            throw new IllegalStateException(
                    """
                    Could not create the PostGIS extension, which Foresignal requires.

                    Fix one of the following and restart:
                      * Install PostGIS in the database server. The supported image is \
                    foresignal-db:17-3.5-pgvector (postgis/postgis:17-3.5 plus pgvector, built from \
                    docker/db), which docker-compose.yml already uses.
                      * Or have a superuser run: CREATE EXTENSION IF NOT EXISTS postgis;
                        in the 'foresignal' database, then restart Foresignal.

                    Underlying error: """
                            + e.getMessage(),
                    e);
        }
    }
}
