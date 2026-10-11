package ai.foresignal.test;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The PostGIS container shared by every integration test.
 *
 * <p>The field is static, so the container is started once per JVM and reused across test classes
 * rather than restarted per class. Foresignal needs PostGIS, not plain Postgres, so the image is
 * declared as a compatible substitute for {@code postgres} to satisfy Testcontainers' image check.
 *
 * <p>Imported by {@link IntegrationTest} via {@code @ImportTestcontainers}, which also wires the
 * datasource from the {@code @ServiceConnection} annotation below.
 */
public final class PostgisContainers {

    /** The PostGIS image pinned by Architecture §2. */
    public static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres");

    @ServiceConnection
    public static final PostgreSQLContainer POSTGIS = new PostgreSQLContainer(POSTGIS_IMAGE)
            .withDatabaseName("foresignal")
            .withUsername("foresignal")
            .withPassword("foresignal");

    private PostgisContainers() {}
}
