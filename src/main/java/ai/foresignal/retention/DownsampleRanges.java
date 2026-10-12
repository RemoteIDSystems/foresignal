package ai.foresignal.retention;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.source.spi.HistoryTier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The seq ranges of history that downsampling has thinned (prompt 044): the DONE rows of
 * {@code history_downsample}, cached in memory so the read paths ({@code /versions}, asOf,
 * {@code testTrigger}, {@code /v1/system}) can ask without SQL.
 *
 * <p>Loaded once every singleton exists (Hibernate has created the table by then), reloaded after
 * every downsampling run and startup recovery, and after the test database cleaner truncates the
 * table ({@link IngestStateReset}). A failed load keeps the previous ranges.
 */
@Component
public class DownsampleRanges implements SmartInitializingSingleton, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(DownsampleRanges.class);

    static final String SELECT_DONE = "SELECT history_tier, from_seq, to_seq, coalesce(rows_after, 0)"
            + " FROM history_downsample WHERE status = 'DONE'";

    /**
     * One thinned leaf.
     *
     * @param fromSeq the inclusive lower bound
     * @param toSeq the exclusive upper bound
     * @param rows the rows the thinned copy kept
     */
    public record Range(long fromSeq, long toSeq, long rows) {}

    /**
     * What a tier's downsampling amounts to, for {@code /v1/system.history}.
     *
     * @param leaves the thinned leaves still attached
     * @param rows the rows they kept
     */
    public record Summary(int leaves, long rows) {}

    private final JdbcTemplate jdbc;
    private volatile Map<HistoryTier, NavigableMap<Long, Range>> ranges = empty();

    /** Creates the cache. */
    public DownsampleRanges(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Loads the ranges at startup. */
    @Override
    public void afterSingletonsInstantiated() {
        refresh();
    }

    /** Reloads after the test cleaner truncated the table. */
    @Override
    public void resetState() {
        refresh();
    }

    /** Reloads the DONE ranges; on failure the previous ranges stay. */
    public void refresh() {
        try {
            Map<HistoryTier, NavigableMap<Long, Range>> loaded = new EnumMap<>(HistoryTier.class);
            for (HistoryTier tier : HistoryTier.values()) {
                loaded.put(tier, new TreeMap<>());
            }
            jdbc.query(SELECT_DONE, rs -> {
                HistoryTier tier = HistoryTier.fromCode(rs.getShort(1));
                long from = rs.getLong(2);
                loaded.get(tier).put(from, new Range(from, rs.getLong(3), rs.getLong(4)));
            });
            Map<HistoryTier, NavigableMap<Long, Range>> frozen = new EnumMap<>(HistoryTier.class);
            loaded.forEach((tier, map) -> frozen.put(tier, Collections.unmodifiableNavigableMap(map)));
            ranges = frozen;
        } catch (RuntimeException e) {
            log.warn("Could not load the downsampled history ranges; keeping the previous ones", e);
        }
    }

    /** True when {@code seq} lies in a thinned leaf of {@code tier}. */
    public boolean contains(HistoryTier tier, long seq) {
        Map.Entry<Long, Range> entry = ranges.get(tier).floorEntry(seq);
        return entry != null && seq < entry.getValue().toSeq();
    }

    /** True when any seq of {@code [fromSeq, toSeq]} (inclusive) lies in a thinned leaf of {@code tier}. */
    public boolean overlaps(HistoryTier tier, long fromSeq, long toSeq) {
        if (toSeq < fromSeq) {
            return false;
        }
        NavigableMap<Long, Range> map = ranges.get(tier);
        if (contains(tier, fromSeq)) {
            return true;
        }
        Long next = map.higherKey(fromSeq);
        return next != null && next <= toSeq;
    }

    /** True when no leaf of any tier has been thinned. */
    public boolean isEmpty() {
        return ranges.values().stream().allMatch(Map::isEmpty);
    }

    /** The thinned leaves of {@code tier}, ordered by range. */
    public NavigableMap<Long, Range> ranges(HistoryTier tier) {
        return ranges.get(tier);
    }

    /** Thinned leaves and their rows for {@code tier}. */
    public Summary summary(HistoryTier tier) {
        NavigableMap<Long, Range> map = ranges.get(tier);
        long rows = 0;
        for (Range range : map.values()) {
            rows += range.rows();
        }
        return new Summary(map.size(), rows);
    }

    private static Map<HistoryTier, NavigableMap<Long, Range>> empty() {
        Map<HistoryTier, NavigableMap<Long, Range>> map = new EnumMap<>(HistoryTier.class);
        for (HistoryTier tier : HistoryTier.values()) {
            map.put(tier, Collections.emptyNavigableMap());
        }
        return map;
    }
}
