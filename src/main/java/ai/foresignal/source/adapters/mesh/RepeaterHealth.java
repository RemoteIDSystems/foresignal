package ai.foresignal.source.adapters.mesh;

import java.time.Duration;
import java.time.Instant;

/**
 * Repeater health from adverts alone (prompt 083): a MeshCore repeater is {@value #OK} while its last advert is younger
 * than {@code lateAfter}, {@value #LATE} until {@code silentAfter}, then {@value #SILENT}. Receive-only: Foresignal
 * never sends a login, status, telemetry or path request to a repeater. Pure.
 *
 * @param lateAfter the age at which a repeater is late (default 13 h, a little over the firmware's 12 h advert)
 * @param silentAfter the age at which it is silent (default 26 h)
 */
public record RepeaterHealth(Duration lateAfter, Duration silentAfter) {

    /** A repeater heard recently. */
    public static final String OK = "ok";

    /** A repeater whose advert is overdue. */
    public static final String LATE = "late";

    /** A repeater not heard for a long time. */
    public static final String SILENT = "silent";

    /** The default late age. */
    public static final Duration DEFAULT_LATE = Duration.ofHours(13);

    /** The default silent age. */
    public static final Duration DEFAULT_SILENT = Duration.ofHours(26);

    /** How often the adapter re-evaluates every repeater. */
    public static final Duration REEVALUATE_EVERY = Duration.ofMinutes(15);

    /** Applies the defaults and checks the order. */
    public RepeaterHealth {
        lateAfter = lateAfter == null ? DEFAULT_LATE : lateAfter;
        silentAfter = silentAfter == null ? DEFAULT_SILENT : silentAfter;
        if (lateAfter.isNegative() || lateAfter.isZero() || silentAfter.compareTo(lateAfter) <= 0) {
            throw new IllegalArgumentException("repeaterLateAfter must be positive and below repeaterSilentAfter");
        }
    }

    /** The defaults. */
    public static RepeaterHealth defaults() {
        return new RepeaterHealth(DEFAULT_LATE, DEFAULT_SILENT);
    }

    /**
     * The status of a repeater whose last advert was at {@code lastAdvert}.
     *
     * @param lastAdvert when the last advert was heard (or, without a live advert, the advert's own timestamp); null
     *     counts as silent
     * @param now the evaluation time
     */
    public String status(Instant lastAdvert, Instant now) {
        if (lastAdvert == null) {
            return SILENT;
        }
        Duration age = Duration.between(lastAdvert, now);
        if (age.compareTo(lateAfter) < 0) {
            return OK;
        }
        return age.compareTo(silentAfter) < 0 ? LATE : SILENT;
    }
}
