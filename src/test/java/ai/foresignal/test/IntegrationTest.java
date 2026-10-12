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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
 * clean up after itself. The one exception is state the reset beans re-seed after the truncation:
 * the trigger engine writes its durable cursor ({@code app_setting 'trigger.cursor'}) when it
 * reloads, and so does the region tag worker ({@code 'region.cursor'}, prompt 040).
 *
 * <p>The <em>schema</em>, however, persists for the whole run. The test profile uses
 * {@code ddl-auto: update}, not {@code create-drop}: one container is shared by every context, and
 * dropping the schema when one context closed would take the tables and the GiST indexes created by
 * {@code SchemaBootstrap} away from contexts still in use. Cleaning rows while leaving the schema
 * alone is what keeps that safe.
 *
 * <p>Every integration test gets MockMvc (checkpoint 041). Ten shared test configurations were used both with and
 * without {@code @AutoConfigureMockMvc}, and each combination was a separate cached context: 48 contexts in all,
 * more than Spring's cache of 32, so contexts were evicted and rebuilt during the run. The per-class annotations
 * that remain are now redundant and harmless.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers(PostgisContainers.class)
@Import(DatabaseCleaner.TestConfig.class)
@ExtendWith(DatabaseCleanupExtension.class)
@ActiveProfiles("test")
public @interface IntegrationTest {}
