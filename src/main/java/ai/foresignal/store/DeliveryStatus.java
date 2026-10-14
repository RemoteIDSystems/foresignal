package ai.foresignal.store;

import java.util.Locale;

/** Lifecycle of one notification delivery (prompt 018; stored by name). */
public enum DeliveryStatus {
    /** Queued, deferred, or waiting for its next retry. */
    PENDING,
    /** The destination accepted it, or it was folded into a digest that carries it. */
    DELIVERED,
    /** Retries are exhausted, or its channel was deleted. */
    FAILED,
    /**
     * Not sent on purpose (prompt 033): a presence-deferred send whose alert was acknowledged,
     * snoozed or resolved, or whose notification was read on an active desktop, before release; or a
     * pending escalation delivery canceled because the escalation stopped.
     */
    SUPPRESSED,
    /**
     * Sent, but the recipient never confirmed it (prompt 084): a {@code meshcore} direct message with
     * {@code requireAck} whose acknowledgement did not arrive after one retry. Terminal; unlike {@link #FAILED} it
     * raises no delivery-failure event.
     */
    UNACKED;

    /** The lowercase wire value of the {@code NotificationDelivery.status} schema. */
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a wire value.
     *
     * @throws IllegalArgumentException when it is not {@code pending}, {@code delivered}, {@code failed},
     *     {@code suppressed} or {@code unacked}
     */
    public static DeliveryStatus fromWire(String value) {
        for (DeliveryStatus status : values()) {
            if (status.wire().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("must be one of pending, delivered, failed, suppressed, unacked");
    }
}
