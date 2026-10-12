package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * The global notification pause (prompt 018), stored under {@link SettingKeys#NOTIFICATIONS}
 * (OpenAPI schema {@code NotificationSettings}).
 *
 * <p>While {@code pausedUntil} is in the future every delivery is deferred to it, except messages at
 * or above {@code pauseBypassSeverity}; deferred deliveries are released as one digest per channel.
 * The {@code chatops} (061) and {@code imageLinks} (108) members belong to later prompts and are
 * not stored yet.
 *
 * @param pausedUntil pause deliveries until this instant, or null for no pause
 * @param pauseBypassSeverity the severity wire value at or above which messages are sent anyway, or
 *     null when nothing bypasses the pause
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationSettings(Instant pausedUntil, String pauseBypassSeverity) {

    /** The shipped bypass severity. */
    public static final String DEFAULT_BYPASS = "critical";

    /** No pause; critical bypasses. */
    public static NotificationSettings defaults() {
        return new NotificationSettings(null, DEFAULT_BYPASS);
    }

    /** True when deliveries are paused at {@code now}. */
    public boolean pausedAt(Instant now) {
        return pausedUntil != null && pausedUntil.isAfter(now);
    }
}
