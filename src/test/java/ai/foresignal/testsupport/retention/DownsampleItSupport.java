package ai.foresignal.testsupport.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;

import ai.foresignal.retention.DownsampleRanges;
import ai.foresignal.retention.HistoryDownsampler;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared steps of prompt 044's downsampling ITs, on top of prompt 019's retention fixture (same cached
 * context). The TRACK source is {@code test-track}: SAMPLED, minInterval 60 s, keyframe 240 s,
 * minDeviation 1,000 m, TTL 5 min.
 *
 * <p>Instead of a small {@code foresignal.history.trackBlock} (another cached context), the scenarios
 * write into the default first track leaf {@code rv_t_0}, then move the allocator into the next leaf
 * ({@link #pushPastRawWindow()}) so {@code rv_t_0} becomes eligible once the clock passes the raw window.
 */
public abstract class DownsampleItSupport extends RetentionItSupport {

    /** The first TRACK leaf, where every scenario seeds its versions. */
    public static final String LEAF = "rv_t_0";

    /** The default TRACK block (Core-Data-Model §9). */
    public static final long BLOCK = 20_000_000L;

    /** When {@link #pushPastRawWindow()} ingests its marker, in the next leaf. */
    public static final Instant MARKER = T0.plus(Duration.ofHours(2));

    /** A run at which {@code rv_t_0} is past the 24 h raw window and far from the 3 d drop horizon. */
    public static final Instant DUE = MARKER.plus(Duration.ofHours(24)).plus(Duration.ofMinutes(5));

    @Autowired protected HistoryDownsampler downsampler;
    @Autowired protected DownsampleRanges ranges;
    @Autowired protected SourceService sources;

    /** {@code T0 + minutes}. */
    protected static Instant minute(int minutes) {
        return T0.plus(Duration.ofMinutes(minutes));
    }

    /**
     * An aircraft at {@code (lon, 50)} with a callsign and an altitude, and no speed or course, so the
     * deviation is plain distance.
     */
    protected static NormalizedRecord aircraft(String id, Instant at, double lon, String callsign) {
        return NormalizedRecord.builder(id, TRACK_KIND).observedAt(at).point(lon, 50.0)
                .put("callsign", callsign).put("alt", 30_000).build();
    }

    /**
     * Ingests one observation of {@code id} per minute for minutes {@code 0..last}, moving 0.05° of
     * longitude (about 3.6 km) each time, so every one persists (FIRST, then SAMPLE or KEYFRAME).
     */
    protected void flyEveryMinute(String id, double lon0, int last) {
        for (int k = 0; k <= last; k++) {
            ingestAt(TRACK, minute(k), aircraft(id, minute(k), lon0 + 0.05 * k, id.toUpperCase()));
        }
    }

    /** Moves the allocator into the next leaf and ingests a marker there at {@link #MARKER}. */
    protected void pushPastRawWindow() {
        jumpSeq(BLOCK);
        ingestAt(TRACK, MARKER, aircraft("marker", MARKER, 100.0, "MARKER"));
    }

    /** Runs retention at {@link #DUE}. */
    protected RetentionRunResult runDue() {
        return runAt(DUE);
    }

    /** The seqs of one entity's versions, ascending. */
    protected List<Long> seqs(String sourceId, String externalId) {
        return jdbc.queryForList("SELECT seq FROM record_version WHERE source_id = ? AND external_id = ? ORDER BY seq",
                Long.class, sourceId, externalId);
    }

    /** The persist reasons of one entity's versions, in seq order. */
    protected List<Integer> reasons(String sourceId, String externalId) {
        return jdbc.queryForList("SELECT persist_reason FROM record_version WHERE source_id = ? AND external_id = ?"
                + " ORDER BY seq", Integer.class, sourceId, externalId);
    }

    /** The {@code history_downsample} row of {@code leaf}, or an empty map. */
    protected Map<String, Object> bookkeeping(String leaf) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM history_downsample WHERE leaf_name = ?", leaf);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    /** True when a table of that name exists in the current schema. */
    protected boolean tableExists(String name) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, name));
    }

    /** The index names of {@code table}. */
    protected List<String> indexes(String table) {
        return jdbc.queryForList("SELECT indexname FROM pg_indexes WHERE schemaname = current_schema()"
                + " AND tablename = ? ORDER BY indexname", String.class, table);
    }
}
