package ai.foresignal.system;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Watchdog mechanics ({@code foresignal.watchdog.*}, prompt 019). What to ping and how often are
 * operator settings ({@code app_setting['watchdog']}).
 *
 * @param scheduled whether the watchdog ticks on its own; off in tests, which call
 *     {@code WatchdogService.tick()}
 * @param timeout the connect and read timeout of a ping
 * @param canaryWait how long the canary waits for its change on the change bus
 */
@ConfigurationProperties("foresignal.watchdog")
public record WatchdogProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("5s") Duration timeout,
        @DefaultValue("30s") Duration canaryWait) {

    /** Fills missing values with the defaults. */
    public WatchdogProperties {
        timeout = timeout == null ? Duration.ofSeconds(5) : timeout;
        canaryWait = canaryWait == null ? Duration.ofSeconds(30) : canaryWait;
    }
}
