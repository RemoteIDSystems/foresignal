package ai.foresignal.test;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Truncates Foresignal's tables before each test method.
 *
 * <p>Applied automatically by {@link IntegrationTest}, so an integration test never has to
 * remember to clean up after itself or order itself relative to other tests.
 */
public class DatabaseCleanupExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SpringExtension.getApplicationContext(context).getBean(DatabaseCleaner.class).clean();
    }
}
