package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;

/**
 * The off-box dead-man watchdog and the optional ingest canary (prompt 019), stored under
 * {@link SettingKeys#WATCHDOG} (OpenAPI schema {@code WatchdogSettings}).
 *
 * <p>While readiness is UP (and the canary passes, when enabled) {@code heartbeatUrl} is pinged every
 * {@code interval}; otherwise {@code failUrl} is, when set. Healthchecks.io and Uptime Kuma push
 * monitors alert when the pings stop.
 *
 * @param heartbeatUrl pinged while ready, or null for no watchdog
 * @param failUrl pinged instead while not ready, or null
 * @param interval the ping interval, at least 30 s
 * @param method {@code GET} or {@code POST}
 * @param canary the ingest canary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WatchdogSettings(String heartbeatUrl, String failUrl, Duration interval, String method, Canary canary) {

    /** The shipped interval. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(60);

    /** The shortest interval. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(30);

    /** The shipped canary interval. */
    public static final Duration DEFAULT_CANARY_INTERVAL = Duration.ofHours(1);

    /** Fills missing members with their defaults. */
    public WatchdogSettings {
        interval = interval == null ? DEFAULT_INTERVAL : interval;
        method = method == null ? "GET" : method;
        canary = canary == null ? Canary.defaults() : canary;
    }

    /** No watchdog, canary off. */
    public static WatchdogSettings defaults() {
        return new WatchdogSettings(null, null, null, null, null);
    }

    /** True when there is a URL to ping. */
    public boolean configured() {
        return (heartbeatUrl != null && !heartbeatUrl.isBlank()) || (failUrl != null && !failUrl.isBlank());
    }

    /**
     * The ingest canary: a synthetic {@code system-canary} record whose change must arrive on the
     * change bus within 30 s.
     *
     * @param enabled whether the canary runs
     * @param interval how often it runs
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Canary(boolean enabled, Duration interval) {

        /** Fills a missing interval. */
        public Canary {
            interval = interval == null ? DEFAULT_CANARY_INTERVAL : interval;
        }

        /** Off, hourly. */
        public static Canary defaults() {
            return new Canary(false, DEFAULT_CANARY_INTERVAL);
        }
    }
}
