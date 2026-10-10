package ai.elaro.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;

/**
 * Marks an integration test that needs the full application context backed by the shared PostGIS
 * container.
 *
 * <p>Because every test annotated this way produces the same context configuration, Spring's
 * context cache keeps one context (and one container) for the whole test run.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ImportTestcontainers(PostgisContainers.class)
@ActiveProfiles("test")
public @interface IntegrationTest {}
