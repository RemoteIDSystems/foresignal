package ai.foresignal.testsupport.notify;

import ai.foresignal.test.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * The shared test configuration of the notification integration tests (prompt 018): a
 * {@link MutableClock}. Every notification IT imports exactly this, so they share one cached context.
 */
@TestConfiguration
@Import(MutableClock.TestConfig.class)
public class NotifyTestConfig {}
