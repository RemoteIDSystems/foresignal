package ai.foresignal.testsupport.notify;

import ai.foresignal.net.HostResolver;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.net.TestOutbound;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * The shared test configuration of the notification integration tests (prompt 018): a
 * {@link MutableClock}. Every notification IT imports exactly this, so they share one cached context.
 *
 * <p>Prompt 035: the outbound URL policy resolves through {@link TestOutbound#offlineResolver()}, so
 * no channel or LLM test reaches real DNS: {@code localhost} (the WireMock stand-ins) is 127.0.0.1 and
 * every other name does not resolve, which save-time validation accepts and a send reports as a
 * failure.
 */
@TestConfiguration
@Import(MutableClock.TestConfig.class)
public class NotifyTestConfig {

    /** DNS without the network. */
    @Bean
    @Primary
    public HostResolver offlineHostResolver() {
        return TestOutbound.offlineResolver();
    }
}
