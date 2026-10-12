package ai.foresignal.test;

import java.nio.file.Path;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The PostGIS container shared by every integration test.
 *
 * <p>The field is static, so the container is started once per JVM and reused across test classes
 * rather than restarted per class. Foresignal needs PostGIS, not plain Postgres, so the image is
 * declared as a compatible substitute for {@code postgres} to satisfy Testcontainers' image check.
 *
 * <p>Since prompt 027 the image is Foresignal's own database image, built from {@code docker/db}
 * (PostGIS plus pgvector, the image {@code docker-compose.yml} runs) once per JVM through
 * {@link ImageFromDockerfile}. The build is tagged {@value #PGVECTOR_TAG} and kept after the run
 * ({@code deleteOnExit=false}), so later runs reuse Docker's layer cache. The plain PostGIS image
 * stays available as {@link #POSTGIS_IMAGE} for tests of the path without pgvector.
 *
 * <p>Imported by {@link IntegrationTest} via {@code @ImportTestcontainers}, which also wires the
 * datasource from the {@code @ServiceConnection} annotation below.
 *
 * <h2>One container for the whole run</h2>
 *
 * <p>{@code @ImportTestcontainers} registers the container as a bean of every context that imports
 * it, and Spring closes an {@code AutoCloseable} bean when its context closes. A context that fails
 * to start, or one the context cache evicts, would therefore stop the container out from under every
 * other cached context, and every later test would fail with "connection refused" (checkpoint 012,
 * run 1). {@link SharedPostgisContainer#stop()} ignores those calls; Testcontainers' Ryuk sidecar
 * removes the container when the test JVM exits.
 *
 * <p>Each cached context holds its own Hikari pool (10 connections by default), so PostgreSQL's
 * default {@code max_connections=100} was exhausted by the eleventh context ("sorry, too many
 * clients already"). The server is started with room for the context cache's full 32 contexts.
 * {@code max_locks_per_transaction} matches {@code docker-compose.yml}.
 */
public final class PostgisContainers {

    /** The plain PostGIS image pinned by Architecture §2 (no pgvector), the base of {@code docker/db}. */
    public static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres");

    /** The tag the {@code docker/db} build gets, the same one {@code docker-compose.yml} uses. */
    public static final String PGVECTOR_TAG = "foresignal-db:17-3.5-pgvector";

    /** Foresignal's database image: {@code docker/db} (PostGIS plus pgvector), built once per JVM. */
    public static final DockerImageName PGVECTOR_IMAGE = buildDatabaseImage();

    @ServiceConnection
    public static final PostgreSQLContainer POSTGIS = new SharedPostgisContainer(PGVECTOR_IMAGE)
            .withDatabaseName("foresignal")
            .withUsername("foresignal")
            .withPassword("foresignal")
            .withCommand(
                    "postgres",
                    "-c", "fsync=off",
                    "-c", "max_connections=500",
                    "-c", "max_locks_per_transaction=256");

    private PostgisContainers() {}

    /**
     * Builds {@code docker/db} (relative to the project directory, Gradle's test working directory)
     * and returns its name as a compatible substitute for {@code postgres}.
     */
    private static DockerImageName buildDatabaseImage() {
        ImageFromDockerfile image = new ImageFromDockerfile(PGVECTOR_TAG, false)
                .withFileFromPath(".", Path.of("docker", "db"));
        // LazyFuture.get() builds the image (or reuses Docker's cache) and rethrows failures unchecked.
        return DockerImageName.parse(image.get()).asCompatibleSubstituteFor("postgres");
    }

    /** A PostGIS container that outlives every Spring context that imports it. */
    static final class SharedPostgisContainer extends PostgreSQLContainer {

        SharedPostgisContainer(DockerImageName image) {
            super(image);
        }

        /**
         * Ignored, so that closing one test context cannot stop the container the others still use.
         * Ryuk removes the container at JVM exit.
         */
        @Override
        public void stop() {
            // Deliberately empty: see the class comment on PostgisContainers.
        }
    }
}
