package ai.foresignal.trigger;

import java.util.Objects;

/**
 * One alert lifecycle transition on the {@link AlertBus} ({@code AlertEvent} schema).
 *
 * <p>The SSE streams map {@link #FIRED} to {@code alert}, {@link #RESOLVED} to
 * {@code alert.resolved} and every other type to {@code alert.updated}. Suppressed alerts (recorded
 * under a snooze) are published too, flagged, so prompt 018 does not route them and the streams hide
 * them unless asked.
 *
 * <p>{@link #ENRICHED} (prompt 030) is published when an enrichment summary is stored. The streams
 * send it as {@code alert.enriched} with the full {@code Alert} as data (an update of the same
 * alert); the notification router uses it to bring waiting deliveries forward and to edit messages
 * already sent.
 *
 * @param type {@code fired}, {@code updated}, {@code acknowledged}, {@code snoozed},
 *     {@code resolved} or {@code enriched}
 * @param alert the alert after the transition
 * @param suppressed true when the alert was suppressed by a snooze
 */
public record AlertEvent(String type, AlertView alert, boolean suppressed) {

    /** A new alert. */
    public static final String FIRED = "fired";

    /** Feedback, a note or an unsnooze changed. */
    public static final String UPDATED = "updated";

    /** Acknowledged. */
    public static final String ACKNOWLEDGED = "acknowledged";

    /** Snoozed. */
    public static final String SNOOZED = "snoozed";

    /** Resolved, by hand or automatically. */
    public static final String RESOLVED = "resolved";

    /** The enrichment summary was stored (prompt 030). */
    public static final String ENRICHED = "enriched";

    /** Requires a type and an alert. */
    public AlertEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(alert, "alert");
    }

    /** An event of {@code type} for {@code alert}, flagged as the alert is. */
    public static AlertEvent of(String type, AlertView alert) {
        return new AlertEvent(type, alert, alert.suppressed());
    }

    /**
     * The SSE event name: {@code alert}, {@code alert.updated}, {@code alert.resolved} or
     * {@code alert.enriched}.
     */
    public String sseEvent() {
        return switch (type) {
            case FIRED -> "alert";
            case RESOLVED -> "alert.resolved";
            case ENRICHED -> "alert.enriched";
            default -> "alert.updated";
        };
    }
}
