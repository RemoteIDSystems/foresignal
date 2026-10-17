package ai.foresignal.quality;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An implicit alert-quality signal (prompt 115), stored in {@code alert_signal} for non-exercise alerts only and
 * never as feedback. The weights are the one documented table of {@code signalScore}:
 *
 * <table>
 *   <caption>Signal weights</caption>
 *   <tr><th>Signal</th><th>Meaning</th><th>Weight</th></tr>
 *   <tr><td>{@code ACK_FAST}</td><td>acknowledged within 15 min of firing</td><td>+0.5</td></tr>
 *   <tr><td>{@code SNOOZED}</td><td>the alert, or its rule while it was open, was snoozed</td><td>−0.5</td></tr>
 *   <tr><td>{@code FLAP}</td><td>resolved by the inverse edge within 10 min of firing</td><td>−0.5</td></tr>
 *   <tr><td>{@code UNOPENED}</td><td>its notification was not read within 24 h (only where 033 read cursors
 *       exist)</td><td>−0.25</td></tr>
 * </table>
 *
 * <p>{@code signalScore} is the weighted sum of a rule's signal counts divided by its alerts in the window
 * ({@link #score}); it is shown next to, never mixed into, the feedback-based precision.
 */
public enum AlertSignalType {
    /** Acknowledged within {@link #ACK_FAST_WITHIN}. */
    ACK_FAST(0.5),
    /** The alert or its rule was snoozed. */
    SNOOZED(-0.5),
    /** Resolved by the inverse edge within {@link #FLAP_WITHIN}. */
    FLAP(-0.5),
    /** The notification was not read within {@link #UNOPENED_AFTER}. */
    UNOPENED(-0.25);

    /** The acknowledgement time that counts as fast. */
    public static final Duration ACK_FAST_WITHIN = Duration.ofMinutes(15);

    /** The resolution time that counts as a flap. */
    public static final Duration FLAP_WITHIN = Duration.ofMinutes(10);

    /** How long a notification may stay unread before it counts as unopened. */
    public static final Duration UNOPENED_AFTER = Duration.ofHours(24);

    /** The wire names, in declaration order. */
    public static final List<String> WIRE_VALUES = List.of("ACK_FAST", "SNOOZED", "FLAP", "UNOPENED");

    private final double weight;

    AlertSignalType(double weight) {
        this.weight = weight;
    }

    /** The weight of the signal in {@code signalScore}. */
    public double weight() {
        return weight;
    }

    /** The weight table, by wire name. */
    public static Map<String, Double> weights() {
        Map<String, Double> out = new LinkedHashMap<>();
        for (AlertSignalType type : values()) {
            out.put(type.name(), type.weight);
        }
        return out;
    }

    /** Counts per wire name with every signal present (zero when absent). */
    public static Map<String, Long> complete(Map<String, Long> counts) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (AlertSignalType type : values()) {
            out.put(type.name(), counts == null ? 0L : counts.getOrDefault(type.name(), 0L));
        }
        return out;
    }

    /**
     * The weighted signal score: {@code Σ weight × count ÷ alerts}, rounded to four decimals; null without alerts.
     *
     * @param counts signal counts by wire name
     * @param alerts the rule's (non-exercise) alerts in the same window
     */
    public static Double score(Map<String, Long> counts, long alerts) {
        if (alerts <= 0) {
            return null;
        }
        double sum = 0;
        for (AlertSignalType type : values()) {
            sum += type.weight * (counts == null ? 0L : counts.getOrDefault(type.name(), 0L));
        }
        return Math.round(sum / alerts * 10_000d) / 10_000d;
    }
}
