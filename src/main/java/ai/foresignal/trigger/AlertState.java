package ai.foresignal.trigger;

import java.util.List;
import java.util.Locale;

/** An alert's lifecycle state. {@link #RESOLVED} is terminal. */
public enum AlertState {
    /** Fired and not yet handled. */
    FIRING,
    /** Seen by the operator or an agent; still open. */
    ACKNOWLEDGED,
    /** Closed, by hand or automatically. */
    RESOLVED;

    /** The wire values. */
    public static final List<String> WIRE_VALUES = List.of("firing", "acknowledged", "resolved");

    /** The lowercase wire value stored in {@code alert.state}. */
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }
}
