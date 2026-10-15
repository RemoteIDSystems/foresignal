package ai.foresignal.derive;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Coverage;
import java.time.Duration;

/**
 * The coverage picture a {@link DerivedGenerator} consults before asserting any absence (prompt
 * 099): per input source its health, the time of its last data and its reported {@link Coverage},
 * plus {@code densityNear}, the number of distinct entities a source reported near a point over a
 * window.
 *
 * <p>"Absence of evidence is not evidence of absence": a generator opens an AIS gap or clears a
 * squawk only when the source is healthy and the window is not degraded. {@code densityNear} is the
 * second test — a vessel that stops reporting while three neighbours keep reporting is a gap; a
 * vessel that stops while the whole feed is quiet is a coverage hole.
 */
public interface CoverageService {

    /** The source's derived health. */
    HealthStatus health(String sourceId);

    /** The last success that returned records for {@code sourceId}, or null. */
    java.time.Instant lastDataAt(String sourceId);

    /** The source's latest reported coverage, or {@link Coverage#none()}. */
    Coverage coverage(String sourceId);

    /** True when the source is {@code healthy}: fresh data and no recent failures. */
    boolean healthy(String sourceId);

    /**
     * The layer status for a set of input sources over a window that starts at {@code windowStart}:
     * {@link LayerStatus#DEGRADED_INPUT} when a gap touched the window, {@link LayerStatus#NO_COVERAGE}
     * when no input is healthy, otherwise {@link LayerStatus#OK}.
     *
     * @param sources the input sources
     * @param windowStart the start of the rolling window
     */
    LayerStatus statusFor(Iterable<String> sources, java.time.Instant windowStart);

    /**
     * The number of distinct entities {@code sourceId} reported within {@code radiusKm} of the point
     * during the last {@code window}, from an H3 res-5 count ring.
     */
    int densityNear(String sourceId, double lon, double lat, double radiusKm, Duration window);
}
