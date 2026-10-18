package ai.foresignal.analytics.series;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Series backed by external (EDP) observation streams bound with target {@code series} (prompt 138):
 * {@code series:ext-{id}/{kind}/{seriesKey}}. The binding package provides the bean; {@link SeriesReader} asks it for
 * every {@code series:} ref of an {@code ext-} source, and the 119 registrar lists its candidates nightly.
 */
public interface ExternalSeriesSource {

    /** True when {@code sourceId} is an external provider this source answers for. */
    boolean handles(String sourceId);

    /** The metadata of a bound series, or empty when no enabled binding backs it or it has no point. */
    Optional<SeriesReader.Meta> meta(SeriesRef ref);

    /**
     * The points of a bound series whose period starts in {@code [from, to)}: the latest version of each period,
     * {@code knownAt} being the item's effective availability ({@code max(knownAt, receipt)}); with {@code asOf}, only
     * versions stored and known by then.
     */
    List<SeriesValue> points(SeriesRef ref, SeriesReader.Meta meta, Instant from, Instant to, Instant asOf);

    /**
     * The registry candidates of bound series (prompt 119 auto-registration).
     *
     * @param ref the series ref
     * @param frequency {@code hour}, {@code day} or {@code week}
     * @param family the signal family, or null
     * @param regionCode the region, or null
     * @param volume the ranking volume (points in the last 90 days)
     */
    record Candidate(String ref, String frequency, String family, String regionCode, double volume) {}

    /** Every bound series that the 119 registry can analyze (hour, day or week periods). */
    List<Candidate> candidates(Instant now);
}
