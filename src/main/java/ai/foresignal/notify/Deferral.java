package ai.foresignal.notify;

import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.trigger.Severity;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Decides whether a delivery must wait for the global pause or the channel's quiet hours (prompt
 * 018). The pause wins over quiet hours; each has its own bypass severity.
 */
public final class Deferral {

    /**
     * Why and until when a delivery waits.
     *
     * @param until when it may be sent
     * @param reason {@link DeferredReason#PAUSED} or {@link DeferredReason#QUIET_HOURS}
     */
    public record Decision(Instant until, String reason) {}

    private Deferral() {}

    /**
     * The deferral of a message of {@code severity} on a channel at {@code now}, if any.
     *
     * @param quietHours the channel's quiet hours, or null
     * @param zone the channel's effective timezone
     * @param pause the global pause setting
     */
    public static Optional<Decision> check(
            String severity, QuietHours quietHours, ZoneId zone, NotificationSettings pause, Instant now) {
        if (pause != null && pause.pausedAt(now) && !atOrAbove(severity, pause.pauseBypassSeverity())) {
            return Optional.of(new Decision(pause.pausedUntil(), DeferredReason.PAUSED));
        }
        if (quietHours != null && !quietHours.bypasses(severity)) {
            return quietHours.windowEnd(now, zone).map(end -> new Decision(end, DeferredReason.QUIET_HOURS));
        }
        return Optional.empty();
    }

    private static boolean atOrAbove(String severity, String threshold) {
        if (threshold == null || severity == null) {
            return false;
        }
        return Severity.fromWire(severity).compareTo(Severity.fromWire(threshold)) >= 0;
    }
}
