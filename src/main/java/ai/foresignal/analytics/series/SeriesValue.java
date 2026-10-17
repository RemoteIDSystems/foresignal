package ai.foresignal.analytics.series;

import java.time.Instant;

/**
 * One point of a series read through the {@link SeriesReader} (prompt 119).
 *
 * @param periodStart the bucket start (event time)
 * @param periodEnd the bucket end
 * @param value the value, or null where the point is missing or its coverage failed
 * @param knownAt when Foresignal knew it: the ingest time of the supplying version for series points, the bucket close
 *     time for rollups, the bucket end for rule samples
 * @param coverageOk false when the involved sources were failing or not covering the period
 * @param period the upstream period label, or null
 * @param version the record version that supplied it (series points), or null
 * @param status the upstream status ({@code final}, {@code preliminary}, …), or null
 */
public record SeriesValue(Instant periodStart, Instant periodEnd, Double value, Instant knownAt, boolean coverageOk,
        String period, Integer version, String status) {

    /** True when the point has a usable value: present, covered and finite. */
    public boolean usable() {
        return value != null && coverageOk && Double.isFinite(value);
    }

    /** This point with its value removed (for {@code LOCAL_ONLY} callers). */
    public SeriesValue withoutValue() {
        return new SeriesValue(periodStart, periodEnd, null, knownAt, coverageOk, period, version, status);
    }
}
