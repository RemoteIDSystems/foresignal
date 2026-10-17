package ai.foresignal.quality;

import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Detects the implicit signal of an alert lifecycle event (prompt 115). Exercise and suppressed alerts never
 * produce a signal.
 *
 * <ul>
 *   <li>{@code acknowledged} within {@link AlertSignalType#ACK_FAST_WITHIN} of firing: {@code ACK_FAST};
 *   <li>{@code snoozed}: {@code SNOOZED};
 *   <li>{@code resolved} by {@code inverse_edge} within {@link AlertSignalType#FLAP_WITHIN} of firing:
 *       {@code FLAP}.
 * </ul>
 * {@code UNOPENED} is found by the nightly job from the 033 read cursors, not from events.
 */
public final class AlertSignals {

    /** The {@code resolved_by} of an inverse-edge resolution. */
    public static final String INVERSE_EDGE = "inverse_edge";

    private AlertSignals() {}

    /**
     * The signal of {@code event}, if any.
     *
     * @param event the lifecycle event
     * @return the signal and when it happened
     */
    public static Optional<Signal> of(AlertEvent event) {
        AlertView alert = event.alert();
        if (alert == null || alert.exercise() || event.suppressed() || alert.firedAt() == null) {
            return Optional.empty();
        }
        return switch (event.type()) {
            case AlertEvent.ACKNOWLEDGED -> within(alert.firedAt(), alert.acknowledgedAt(),
                    AlertSignalType.ACK_FAST_WITHIN) ? Optional.of(new Signal(AlertSignalType.ACK_FAST,
                    alert.acknowledgedAt())) : Optional.empty();
            case AlertEvent.SNOOZED -> Optional.of(new Signal(AlertSignalType.SNOOZED, null));
            case AlertEvent.RESOLVED -> INVERSE_EDGE.equals(alert.resolvedBy())
                    && within(alert.firedAt(), alert.resolvedAt(), AlertSignalType.FLAP_WITHIN)
                    ? Optional.of(new Signal(AlertSignalType.FLAP, alert.resolvedAt())) : Optional.empty();
            default -> Optional.empty();
        };
    }

    /** True when {@code end} is at most {@code limit} after {@code start}. */
    static boolean within(Instant start, Instant end, Duration limit) {
        return start != null && end != null && !end.isBefore(start)
                && Duration.between(start, end).compareTo(limit) <= 0;
    }

    /**
     * A detected signal.
     *
     * @param type the signal
     * @param at when it happened, or null for "now"
     */
    public record Signal(AlertSignalType type, Instant at) {}
}
