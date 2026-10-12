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
 * {@link IntegrationTest} with a real servlet container on a random port, for tests that must talk
 * HTTP over a socket (the SSE streams of prompt 015, read with {@code java.net.http.HttpClient}).
 *
 * <p>Same database, cleaner and profile as {@link IntegrationTest}; only the web environment
 * differs, which makes it one more cached context. Every test that uses it should import the same
 * configuration so they share that context.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(PostgisContainers.class)
@Import(DatabaseCleaner.TestConfig.class)
@ExtendWith(DatabaseCleanupExtension.class)
@ActiveProfiles("test")
public @interface WebIntegrationTest {}
