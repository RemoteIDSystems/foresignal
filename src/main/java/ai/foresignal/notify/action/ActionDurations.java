package ai.foresignal.notify.action;

import ai.foresignal.notify.NotificationAction;
import ai.foresignal.settings.Durations;
import java.time.Duration;

/**
 * The durations of snooze and mute capabilities (prompt 033): snooze defaults to 1 h and is allowed
 * from 15 min to 24 h; mute defaults to 4 h and is allowed from 1 h to 7 d. Capabilities default to
 * 12 h of validity and at most 24 h.
 */
public final class ActionDurations {

    /** The default snooze. */
    public static final Duration SNOOZE_DEFAULT = Duration.ofHours(1);

    /** The shortest snooze. */
    public static final Duration SNOOZE_MIN = Duration.ofMinutes(15);

    /** The longest snooze. */
    public static final Duration SNOOZE_MAX = Duration.ofHours(24);

    /** The default mute. */
    public static final Duration MUTE_DEFAULT = Duration.ofHours(4);

    /** The shortest mute. */
    public static final Duration MUTE_MIN = Duration.ofHours(1);

    /** The longest mute. */
    public static final Duration MUTE_MAX = Duration.ofDays(7);

    /** The default validity of a capability. */
    public static final Duration TTL_DEFAULT = Duration.ofHours(12);

    /** The longest validity of a capability. */
    public static final Duration TTL_MAX = Duration.ofHours(24);

    private ActionDurations() {}

    /** The default duration of {@code action}, or null for ack. */
    public static Duration defaultFor(String action) {
        return switch (action) {
            case NotificationAction.SNOOZE -> SNOOZE_DEFAULT;
            case NotificationAction.MUTE -> MUTE_DEFAULT;
            default -> null;
        };
    }

    /**
     * {@code requested} clamped to the allowed range of {@code action}; the default when null or
     * unparsable; null for ack.
     */
    public static Duration clamp(String action, Duration requested) {
        Duration def = defaultFor(action);
        if (def == null) {
            return null;
        }
        if (requested == null) {
            return def;
        }
        Duration min = NotificationAction.SNOOZE.equals(action) ? SNOOZE_MIN : MUTE_MIN;
        Duration max = NotificationAction.SNOOZE.equals(action) ? SNOOZE_MAX : MUTE_MAX;
        if (requested.compareTo(min) < 0) {
            return min;
        }
        return requested.compareTo(max) > 0 ? max : requested;
    }

    /** {@code text} (shorthand or ISO-8601) clamped for {@code action}. */
    public static Duration parse(String action, Object text) {
        if (text == null) {
            return clamp(action, null);
        }
        try {
            return clamp(action, Durations.parsePositive(String.valueOf(text)));
        } catch (RuntimeException e) {
            return clamp(action, null);
        }
    }

    /** The button label of {@code action} with {@code duration}: "Acknowledge", "Snooze 1h", "Mute 4h". */
    public static String label(String action, Duration duration) {
        return switch (action) {
            case NotificationAction.SNOOZE -> "Snooze " + Durations.format(duration);
            case NotificationAction.MUTE -> "Mute " + Durations.format(duration);
            default -> "Acknowledge";
        };
    }
}
