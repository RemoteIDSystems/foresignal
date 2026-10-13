package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * The upstream answered in a way its usage policy says a machine client must not retry on its own
 * (prompt 064): CelesTrak requires a client to stop on any unexpected status and alert a human.
 *
 * <p>{@code FetchExecutor} maps it: {@code BackoffRegistry.halt} holds the source for {@link #hold()}
 * (24 hours by default, beyond the usual one-hour backoff ceiling), the source is {@code failing} with
 * {@link #getMessage()} as the reason, a {@code system.source_halted} system notification is published,
 * and only the operator's fetch-now ({@code POST /v1/sources/{id}/fetch}) clears the hold early.
 */
public class UpstreamHaltException extends AdapterException {

    private static final long serialVersionUID = 1L;

    /** The default hold: one day. */
    public static final Duration DEFAULT_HOLD = Duration.ofHours(24);

    /**
     * The phrase every halt reason contains; {@code HealthEvaluator} recognizes a halt by it in
     * {@code last_error}.
     */
    public static final String MARKER = "automatic retries stopped";

    private final int status;
    private final Duration hold;

    /**
     * Creates the exception with the default hold.
     *
     * @param status the HTTP status that caused the halt, or 0 when the body (not the status) was wrong
     * @param reason the health reason shown to the operator; must contain {@link #MARKER}
     */
    public UpstreamHaltException(int status, String reason) {
        this(status, reason, DEFAULT_HOLD);
    }

    /**
     * Creates the exception.
     *
     * @param status the HTTP status that caused the halt, or 0
     * @param reason the health reason shown to the operator; must contain {@link #MARKER}
     * @param hold how long the source is held
     */
    public UpstreamHaltException(int status, String reason, Duration hold) {
        super(Objects.requireNonNull(reason, "reason").contains(MARKER) ? reason : reason + " (" + MARKER + ")");
        this.status = status;
        this.hold = hold == null || hold.isNegative() || hold.isZero() ? DEFAULT_HOLD : hold;
    }

    /** How long the source is held. */
    public Duration hold() {
        return hold;
    }

    /** The HTTP status, or 0 when the halt was caused by the body. */
    public int status() {
        return status;
    }

    @Override
    public OptionalInt httpStatus() {
        return status > 0 ? OptionalInt.of(status) : OptionalInt.empty();
    }
}
