package ai.foresignal.stats;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.ReadRestriction;
import java.time.Instant;
import java.util.List;

/**
 * One validated stats request (prompt 038).
 *
 * @param sources the involved source ids (visible to the caller), sorted
 * @param kinds the kind filter, empty for all
 * @param bbox spatial filter, or null
 * @param restriction the caller's restriction with any {@code place=}/{@code view=} filter folded in
 * @param from the first bucket start (inclusive)
 * @param to the end (exclusive)
 * @param bucket the bucket resolution
 * @param measure the measure
 * @param groupBy {@code none}, {@code source}, {@code kind} or {@value StatsService#GROUP_PAIR} (internal:
 *     source and kind, keys joined by {@link StatsService#PAIR_SEPARATOR})
 * @param timeField the time axis
 */
public record StatsQuery(List<String> sources, List<String> kinds, BboxFilter bbox, ReadRestriction restriction,
        Instant from, Instant to, RollupResolution bucket, StatsMeasure measure, String groupBy, TimeField timeField) {

    /** Normalizes nulls. */
    public StatsQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
        groupBy = groupBy == null ? "none" : groupBy;
        timeField = timeField == null ? TimeField.INGESTED : timeField;
    }

    /** This query over other sources. */
    public StatsQuery withSources(List<String> value) {
        return new StatsQuery(value, kinds, bbox, restriction, from, to, bucket, measure, groupBy, timeField);
    }

    /**
     * True when the rollup can answer it: no filter needing the payload (view conditions, watchlists) and no
     * region filter (prompt 040: the rollup is not keyed by region until prompt 119).
     */
    public boolean rollupUsable() {
        return (restriction.filter() == null || !restriction.filter().needsPayload())
                && restriction.regionFilter() == null;
    }

    /**
     * True when the query filters by area (bbox, restricted region or a place filter): rollup rows are
     * then read per cell (approximate at cell resolution); otherwise the exact per-{@code (source, kind)}
     * totals rows are read.
     */
    public boolean spatial() {
        return bbox != null || restriction.region() != null || restriction.filter() != null;
    }
}
