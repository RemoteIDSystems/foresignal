package ai.foresignal.notify;

import java.util.Map;
import java.util.Set;

/**
 * What quiet hours do to a fan-out channel's sends (prompt 031), set in the channel's
 * {@code config.quietHoursMode}:
 *
 * <ul>
 *   <li>{@value #DEFER} (default): wait for the window end, as every 018 channel does;
 *   <li>{@value #DOWNGRADE}: send now with {@code Urgency: very-low} and {@code silent: true};
 *   <li>{@value #DROP}: do not send; the delivery is settled with a note.
 * </ul>
 * Messages at or above the quiet hours' {@code bypassSeverity} (default critical) bypass quiet hours
 * whatever the mode. Channel types without fan-out always defer.
 */
public final class QuietHoursMode {

    /** The config key. */
    public static final String KEY = "quietHoursMode";

    /** Wait for the window end. */
    public static final String DEFER = "defer";

    /** Send now, very-low urgency and silent. */
    public static final String DOWNGRADE = "downgrade";

    /** Do not send. */
    public static final String DROP = "drop";

    /** Every mode. */
    public static final Set<String> ALL = Set.of(DEFER, DOWNGRADE, DROP);

    private QuietHoursMode() {}

    /** The mode of a channel config ({@value #DEFER} when absent or unknown). */
    public static String of(Map<String, Object> config) {
        Object value = config == null ? null : config.get(KEY);
        String mode = value == null ? DEFER : String.valueOf(value);
        return ALL.contains(mode) ? mode : DEFER;
    }
}
