package ai.foresignal.testsupport.stats;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stats.H3Cells;
import ai.foresignal.stats.RollupJob;
import ai.foresignal.stats.StatsService;
import ai.foresignal.stats.StatsSettingsService;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared helpers of prompt 038's integration tests, on top of prompt 019's retention fixture (every
 * scenario starts at {@link #T0}, a UTC midnight, with the default settings). Records are seeded only
 * through {@code TestRecords}.
 */
public abstract class StatsItSupport extends RetentionItSupport {

    /** One hour after {@link #T0}. */
    public static final Instant H1 = T0.plusSeconds(3600);

    @Autowired protected RollupJob rollup;
    @Autowired protected StatsService stats;
    @Autowired protected StatsSettingsService statsSettings;
    @Autowired protected H3Cells cells;

    /** Ingests {@code batch} at {@code at} and asserts nothing was rejected. */
    protected void seed(String sourceId, Instant at, NormalizedRecord... batch) {
        IngestOutcome outcome = ingestAt(sourceId, at, batch);
        assertThat(outcome.rejections()).as("rejections").isEmpty();
    }

    /** A point record with a {@code value} and a {@code name}. */
    protected static NormalizedRecord point(String externalId, String kind, double lon, double lat, Instant observedAt,
            Object value) {
        return NormalizedRecord.builder(externalId, kind).observedAt(observedAt).point(lon, lat).put("value", value)
                .put("name", "Name " + externalId).build();
    }

    /** A record without a geometry. */
    protected static NormalizedRecord plain(String externalId, String kind, Instant observedAt, Object value) {
        return NormalizedRecord.builder(externalId, kind).observedAt(observedAt).put("value", value)
                .put("name", "Name " + externalId).build();
    }

    /** Runs the rollup until it has nothing left to do at {@code now}; returns the hours rolled. */
    protected int rollUpAt(Instant now) {
        clock.setInstant(now);
        int hours = 0;
        for (int i = 0; i < 500; i++) {
            RollupJob.RunResult result = rollup.runOnce();
            hours += result.hours();
            if (result.hours() == 0) {
                return hours;
            }
        }
        throw new AssertionError("the rollup never caught up");
    }

    /** The measures of one rollup row, or an empty map. */
    protected Map<String, Object> row(int resolution, Instant bucket, String sourceId, String kind, long cell) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT versions, entities, firsts, tombstones, edges, changes,"
                + " cell_lon, cell_lat FROM activity_rollup WHERE resolution = ? AND bucket_start = ? AND source_id = ?"
                + " AND kind = ? AND cell = ?", (short) resolution, Timestamp.from(bucket), sourceId, kind, cell);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    /** {@code sum(column)} of rollup rows matching the filter ({@code cell <> -1} when {@code cells}). */
    protected long sum(String column, int resolution, String sourceId, boolean cellRows) {
        Long n = jdbc.queryForObject("SELECT coalesce(sum(" + column + "), 0) FROM activity_rollup WHERE resolution = ?"
                + " AND source_id = ? AND " + (cellRows ? "cell <> -1" : "cell = -1"), Long.class, (short) resolution,
                sourceId);
        return n == null ? 0 : n;
    }

    /** Rollup rows of {@code sourceId}. */
    protected long rows(String sourceId) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM activity_rollup WHERE source_id = ?", Long.class, sourceId);
        return n == null ? 0 : n;
    }
}
