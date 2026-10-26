package ai.foresignal.retention;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleRetentionJob;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.media.MediaQueries;
import ai.foresignal.media.MediaRetentionStep;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterRetentionStep;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * {@code previewRetention} (prompt R2-015): what the next retention run would prune, by subsystem, computed with the
 * same plans and selects the run uses, as counts and sizes only. Nothing is deleted or written.
 *
 * <ul>
 *   <li>{@code records}: STANDARD history below each scope's cutoff, from {@link RetentionPlanner#standard} (the
 *       consumer clamp included), one item per kind (null for whole-source scopes); bytes are estimated from the
 *       average row size of {@code record_version}.
 *   <li>{@code track}: the TRACK leaves {@link RetentionPlanner#track} would drop, with their estimated rows and
 *       on-disk size.
 *   <li>{@code frames}: {@link RasterRetentionStep#preview} (ladders and the cap-history age cut).
 *   <li>{@code media}: {@link MediaRetentionStep#preview}.
 *   <li>{@code chronicles}: stopped or archived chronicles whose history {@code ChronicleRetentionJob} would remove,
 *       with the bytes they hold.
 * </ul>
 *
 * <p>The settings are those the next run applies ({@link RetentionDbCap#tighten}). With cap history off this is
 * exactly today's per-subsystem windows. The database-size cap is reported as found; a further step-down the run
 * might take is not previewed.
 */
@Service
public class RetentionPreviewService {

    static final String COUNT_BELOW = """
            SELECT count(*) FROM record_version WHERE history_tier = 0 AND source_id = :source%s AND seq < :cutoff
            """;

    static final String AVERAGE_ROW = """
            SELECT coalesce(sum(pg_total_relation_size(t.relid)), 0) AS bytes,
                   coalesce(sum(greatest(c.reltuples, 0)), 0) AS rows
              FROM pg_partition_tree('record_version') t JOIN pg_class c ON c.oid = t.relid
             WHERE t.isleaf
            """;

    static final String LEAF = """
            SELECT greatest(c.reltuples, 0)::bigint AS rows, pg_total_relation_size(c.oid) AS bytes
              FROM pg_class c WHERE c.oid = to_regclass(:name)
            """;

    private final RetentionResolver resolver;
    private final RetentionPlanner planner;
    private final RetentionDbCap dbCap;
    private final RetentionJob job;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<RasterRetentionStep> rasters;
    private final ObjectProvider<MediaRetentionStep> media;
    private final ObjectProvider<ChronicleRepository> chronicles;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param resolver the retention resolver
     * @param planner the retention planner
     * @param dbCap the database-size cap
     * @param job the retention job (its consumers)
     * @param seqClock the seq clock
     * @param jdbc the database
     * @param rasters the frame retention step, when present
     * @param media the media retention step, when present
     * @param chronicles the chronicles, when present
     * @param clock the clock
     */
    public RetentionPreviewService(RetentionResolver resolver, RetentionPlanner planner, RetentionDbCap dbCap,
            RetentionJob job, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc,
            ObjectProvider<RasterRetentionStep> rasters, ObjectProvider<MediaRetentionStep> media,
            ObjectProvider<ChronicleRepository> chronicles, Clock clock) {
        this.resolver = resolver;
        this.planner = planner;
        this.dbCap = dbCap;
        this.job = job;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.rasters = rasters;
        this.media = media;
        this.chronicles = chronicles;
        this.clock = clock;
    }

    /** What the next run would prune now. */
    public RetentionPreview preview() {
        Instant now = clock.instant();
        RetentionSettings configured = resolver.settings();
        RetentionSettings settings = dbCap.tighten(configured);
        RetentionRun run = new RetentionRun(now, settings, jdbc, seqClock, job.consumers());
        List<RetentionPreview.Item> items = new ArrayList<>();
        items.addAll(records(run, now));
        items.add(track(run, now));
        RasterRetentionStep raster = rasters.getIfAvailable();
        if (raster != null) {
            RasterFrameQueries.Totals t = raster.preview(now, settings);
            items.add(new RetentionPreview.Item("frames", null,
                    settings.capWindow().map(now::minus).orElse(null), t.frames(), t.bytes()));
        }
        MediaRetentionStep mediaStep = media.getIfAvailable();
        if (mediaStep != null) {
            MediaQueries.Totals t = mediaStep.preview(now, settings);
            items.add(new RetentionPreview.Item("media", null, null, t.count(), t.bytes()));
        }
        ChronicleRepository repository = chronicles.getIfAvailable();
        if (repository != null) {
            items.add(chronicles(repository, settings, now));
        }
        long total = 0;
        Long bytes = null;
        for (RetentionPreview.Item item : items) {
            total += item.affectedCount();
            if (item.bytesEstimate() != null) {
                bytes = (bytes == null ? 0 : bytes) + item.bytesEstimate();
            }
        }
        return new RetentionPreview(items, total, bytes, dbCap(configured), now);
    }

    private List<RetentionPreview.Item> records(RetentionRun run, Instant now) {
        RetentionPlanner.StandardPlan plan = planner.standard(run);
        double perRow = averageRowBytes();
        Map<String, long[]> counts = new TreeMap<>(java.util.Comparator.nullsFirst(String::compareTo));
        Map<String, Duration> longest = new TreeMap<>(java.util.Comparator.nullsFirst(String::compareTo));
        for (List<RetentionPlanner.StandardScope> scopes : plan.scopes().values()) {
            for (RetentionPlanner.StandardScope scope : scopes) {
                long n = 0;
                if (scope.cutoff() > 0) {
                    MapSqlParameterSource params = scope.kindParams().addValue("source", scope.sourceId())
                            .addValue("cutoff", scope.cutoff());
                    Long found = jdbc.queryForObject(COUNT_BELOW.formatted(scope.kindClause(null)), params, Long.class);
                    n = found == null ? 0 : found;
                }
                counts.computeIfAbsent(scope.kind(), k -> new long[1])[0] += n;
                longest.merge(scope.kind(), scope.window(), (a, b) -> a.compareTo(b) >= 0 ? a : b);
            }
        }
        List<RetentionPreview.Item> out = new ArrayList<>();
        counts.forEach((kind, n) -> out.add(new RetentionPreview.Item("records", kind,
                now.minus(longest.get(kind)), n[0], Math.round(n[0] * perRow))));
        return out;
    }

    private RetentionPreview.Item track(RetentionRun run, Instant now) {
        RetentionPlanner.TrackPlan plan = planner.track(run);
        long rows = 0;
        long bytes = 0;
        for (LeafInfo leaf : plan.droppable()) {
            List<long[]> found = jdbc.query(LEAF, new MapSqlParameterSource("name", leaf.name()),
                    (rs, i) -> new long[] {rs.getLong("rows"), rs.getLong("bytes")});
            if (!found.isEmpty()) {
                rows += found.getFirst()[0];
                bytes += found.getFirst()[1];
            }
        }
        return new RetentionPreview.Item("track", null, now.minus(run.settings().effectiveTrackWindow()), rows, bytes);
    }

    private RetentionPreview.Item chronicles(ChronicleRepository repository, RetentionSettings settings, Instant now) {
        Duration cap = settings.capWindow().orElse(null);
        long count = 0;
        long bytes = 0;
        for (ChronicleEntity c : repository.findByStatusInOrderByCreatedAtAscIdAsc(
                List.of(ChronicleVocabulary.STOPPED, ChronicleVocabulary.ARCHIVED))) {
            if (c.getHistoryExpiredAt() != null || c.getStoppedAt() == null) {
                continue;
            }
            Duration keep = ChronicleRetentionJob.keep(c.getRetention(), cap);
            if (keep != null && !c.getStoppedAt().plus(keep).isAfter(now)) {
                count++;
                bytes += c.getBytesUsed();
            }
        }
        return new RetentionPreview.Item("chronicles", null, null, count, bytes);
    }

    private RetentionPreview.DbCap dbCap(RetentionSettings configured) {
        OptionalLong cap = configured.capBytes();
        if (cap.isEmpty()) {
            return null;
        }
        Long size = jdbc.getJdbcTemplate().queryForObject("SELECT pg_database_size(current_database())", Long.class);
        long now = size == null ? 0 : size;
        RetentionDbCap.State state = dbCap.state();
        Integer window = state.windowDays() != null && state.windowDays() < dbCap.baseDays(configured)
                ? state.windowDays() : null;
        return new RetentionPreview.DbCap(cap.getAsLong(), now, now > cap.getAsLong(), window,
                Boolean.TRUE.equals(configured.overwriteOldest()));
    }

    private double averageRowBytes() {
        Map<String, Object> row = jdbc.getJdbcTemplate().queryForMap(AVERAGE_ROW);
        double bytes = ((Number) row.get("bytes")).doubleValue();
        double rows = ((Number) row.get("rows")).doubleValue();
        return rows <= 0 ? 0 : bytes / rows;
    }
}
