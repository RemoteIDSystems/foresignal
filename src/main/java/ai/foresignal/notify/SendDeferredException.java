package ai.foresignal.notify;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

/**
 * A send that must wait rather than fail (prompt 084): the mesh airtime governor had no room. The delivery worker
 * defers the delivery to {@link #until()} with {@link #reason()} instead of counting an attempt, so it can be folded
 * into a digest with the others released at the same time.
 */
public class SendDeferredException extends IOException {

    private static final long serialVersionUID = 1L;

    /** When the send may be tried again. */
    private final transient Instant until;

    /** The {@code deferred_reason} to record. */
    private final String reason;

    /**
     * Creates the exception.
     *
     * @param until when the send may be tried again
     * @param reason the {@code deferred_reason} value
     * @param message why
     */
    public SendDeferredException(Instant until, String reason, String message) {
        super(message);
        this.until = Objects.requireNonNull(until, "until");
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    /** When the send may be tried again. */
    public Instant until() {
        return until;
    }

    /** The {@code deferred_reason} to record. */
    public String reason() {
        return reason;
    }
}
