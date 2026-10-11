package ai.foresignal.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Marks an integration test that needs the full application context backed by the shared PostGIS
 * container.
 *
 * <p>Because every test annotated this way produces the same context configuration, Spring's
 * context cache keeps one context (and one container) for the whole test run.
 *
 * <h2>Database state</h2>
 *
 * <p><strong>Every test method starts with empty tables.</strong> {@link DatabaseCleanupExtension}
 * runs {@link DatabaseCleaner} before each method, truncating every Foresignal table and restarting
 * {@code record_seq} at 1. A test may therefore assume it is alone in the database, and need not
 * clean up after itself.
 *
 * <p>The <em>schema</em>, however, persists for the whole run. The test profile uses
 * {@code ddl-auto: update}, not {@code create-drop}: one container is shared by every context, and
 * dropping the schema when one context closed would take the tables and the GiST indexes created by
 * {@code SchemaBootstrap} away from contexts still in use. Cleaning rows while leaving the schema
 * alone is what keeps that safe.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ImportTestcontainers(PostgisContainers.class)
@Import(DatabaseCleaner.TestConfig.class)
@ExtendWith(DatabaseCleanupExtension.class)
@ActiveProfiles("test")
public @interface IntegrationTest {}
