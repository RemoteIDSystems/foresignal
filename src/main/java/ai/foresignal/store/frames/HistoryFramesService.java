package ai.foresignal.store.frames;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.history.VersionSql;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.store.tiles.TileLayerResolver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * Keyframe plus delta frames on the transaction axis (prompt 037): {@code GET /v1/history/frames}.
 *
 * <p>Semantics, the same as asOf ({@link AsOfResolver}):
 *
 * <ul>
 *   <li>the keyframe is the asOf state at {@code from};
 *   <li>frame {@code i} covers {@code (t_{i−1}, t_i]} with {@code t_i = min(from + i·step, to)}; its
 *       seq range is {@code (S(t_{i−1}), S(t_i)]}. It holds {@code upserts} (the newest version of
 *       each entity that appeared or changed) and {@code removals}: a tombstone, validity expiring
 *       inside the frame, or a newest version outside the bbox or the caller's filter
 *       ({@code left}).
 * </ul>
 *
 * <p>Built in one ordered pass over {@code seq BETWEEN L(from) AND S(to)} per tier (UNION ALL,
 * ordered by seq), with an entity map in Java. With a bbox, only entities that have at least one
 * version inside it in that range are read. More than {@value #MAX_ENTITIES} entities or
 * {@value #MAX_ROWS} rows is {@code 400 FRAMES_TOO_LARGE}.
 *
 * <p><strong>Invariant:</strong> applying frames {@code 1..i} to the keyframe equals
 * {@code asOf(t_i)}. A TRACK version below {@code L(t_i)} is invalid at {@code t_i} by its TTL, so
 * reading from {@code L(from)} changes nothing.
 */
@Service
public class HistoryFramesService {

    /** The smallest step. */
    public static final Duration MIN_STEP = Duration.ofSeconds(10);

    /** The most frames. */
    public static final int MAX_FRAMES = 240;

    /** The most distinct entities one request may cover. */
    public static final int MAX_ENTITIES = 20_000;

    /** The most rows one request may scan. */
    public static final int MAX_ROWS = 2_000_000;

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final AsOfResolver asOf;
    private final Clock clock;

    /** Creates the service. */
    public HistoryFramesService(ReadQueryRunner runner, SeqClockQuery seqClock, AsOfResolver asOf, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.asOf = asOf;
        this.clock = clock;
    }

    /** The frame end instants {@code t_1..t_n} of {@code from}, {@code to} and {@code step}. */
    public static List<Instant> frameTimes(Instant from, Instant to, Duration step) {
        List<Instant> times = new ArrayList<>();
        Instant t = from;
        while (t.isBefore(to)) {
            t = t.plus(step);
            times.add(t.isAfter(to) ? to : t);
        }
        return times;
    }

    /**
     * Builds the frames.
     *
     * @throws ValidationException {@code VALIDATION_FAILED} for a bad window, step or frame count;
     *     {@code ASOF_FILTER_REQUIRED}, {@code ASOF_INVALID} or {@code ASOF_BEYOND_RETENTION} from the
     *     plan; {@code FRAMES_TOO_LARGE}
     */
    public HistoryFrames frames(FramesQuery q) {
        return frames(q, MAX_ENTITIES, MAX_ROWS);
    }

    /**
     * {@link #frames(FramesQuery)} with explicit limits ({@code FRAMES_TOO_LARGE} beyond them); tests
     * lower them instead of seeding millions of rows.
     */
    public HistoryFrames frames(FramesQuery q, int maxEntities, int maxRows) {
        if (q.from() == null || q.to() == null) {
            throw ValidationException.forField(q.from() == null ? "from" : "to", "is required");
        }
        if (!q.from().isBefore(q.to())) {
            throw ValidationException.forField("to", "must be after from");
        }
        if (q.to().isAfter(clock.instant())) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "to must not be in the future.", List.of());
        }
        if (q.step() == null || q.step().compareTo(MIN_STEP) < 0) {
            throw ValidationException.forField("step", "must be at least 10s");
        }
        int maxFrames = q.maxFrames() <= 0 ? MAX_FRAMES : Math.min(q.maxFrames(), MAX_FRAMES);
        List<Instant> times = frameTimes(q.from(), q.to(), q.step());
        if (times.size() > maxFrames) {
            throw ValidationException.forField("step", "(to - from) / step gives " + times.size()
                    + " frames, more than maxFrames " + maxFrames + "; use a larger step or a shorter window");
        }
        return runner.stream(jdbc -> {
            AsOfPlan plan = asOf.plan(jdbc, q.from(), q.sources(), q.kinds(), q.excludedSources());
            long[] bounds = new long[times.size() + 1];
            bounds[0] = plan.s();
            for (int i = 0; i < times.size(); i++) {
                bounds[i + 1] = Math.max(bounds[i], seqClock.seqAt(jdbc, times.get(i)));
            }
            long upper = bounds[bounds.length - 1];
            boolean downsampled = false;
            for (HistoryTier tier : plan.sources().keySet()) {
                downsampled |= asOf.fidelity().downsampled(tier, plan.lower(tier), upper);
            }
            Pass pass = new Pass(plan, times, bounds, maxEntities, maxRows);
            if (!plan.empty()) {
                SqlParams p = new SqlParams();
                String sql = scanSql(q, plan, upper, p);
                jdbc.query(sql, p.source(), rs -> {
                    pass.accept(row(rs, q.fullGeometry()));
                });
            }
            return new HistoryFrames(q.from(), q.step(), upper, plan.horizon(), plan.approximate() || downsampled,
                    pass.keyframe(), pass.finish());
        });
    }

    /** The ordered scan: both tiers, entities with a version in the bbox, with the "inside" verdict. */
    static String scanSql(FramesQuery q, AsOfPlan plan, long upper, SqlParams p) {
        List<String> tileProps = TileLayerResolver.tilePropertiesOf(plan.allSources());
        List<String> inside = new ArrayList<>();
        inside.add("NOT v.tombstone");
        if (q.bbox() != null) {
            inside.add(RecordQueryService.bboxClause("v.geom", q.bbox(), p));
        }
        if (!q.kinds().isEmpty()) {
            inside.add("v.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        RestrictionClamp.sqlPredicate("v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p)
                .ifPresent(inside::add);
        String select = "SELECT v.seq, v.source_id, v.external_id, v.kind, v.version, v.observed_at, v.ingested_at,"
                + " v.valid_until_explicit, v.tombstone, v.history_tier, (" + String.join(" AND ", inside) + ") AS inside,"
                + " ST_X(g.pt) AS lon, ST_Y(g.pt) AS lat,"
                + (q.fullGeometry() ? " ST_AsGeoJSON(v.geom) AS geometry," : " CAST(NULL AS text) AS geometry,")
                + " " + VersionSql.title("v", plan.allSources(), p) + " AS title,"
                + " CAST(" + VersionSql.tileProperties("v.payload", tileProps) + " AS text) AS props"
                + " FROM record_version v CROSS JOIN LATERAL (SELECT "
                + (q.fullGeometry() ? "CAST(NULL AS geometry)" : VersionSql.point("v.geom")) + " AS pt) g";
        List<String> ctes = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        String up = p.bigint(upper);
        for (Map.Entry<HistoryTier, List<SourceDescriptor>> tier : plan.sources().entrySet()) {
            String n = tier.getKey().wireValue();
            String code = "CAST(" + p.add((int) tier.getKey().code()) + " AS smallint)";
            String lower = p.bigint(plan.lower(tier.getKey()));
            String ids = p.textArray(tier.getValue().stream().map(SourceDescriptor::id).toList());
            String range = "v.history_tier = " + code + " AND v.seq BETWEEN " + lower + " AND " + up
                    + " AND v.source_id = ANY(" + ids + ")";
            if (q.bbox() != null) {
                ctes.add("cand_" + n + " AS (SELECT DISTINCT v.source_id, v.external_id FROM record_version v WHERE "
                        + range + " AND " + RecordQueryService.bboxClause("v.geom", q.bbox(), p) + ")");
                parts.add(select + " JOIN cand_" + n + " c ON c.source_id = v.source_id AND c.external_id = v.external_id"
                        + " WHERE " + range);
            } else {
                parts.add(select + " WHERE " + range);
            }
        }
        return (ctes.isEmpty() ? "" : "WITH " + String.join(", ", ctes) + " ")
                + "SELECT * FROM (" + String.join(" UNION ALL ", parts) + ") x ORDER BY x.seq";
    }

    private static Row row(ResultSet rs, boolean full) throws SQLException {
        double lon = rs.getDouble("lon");
        Double lonValue = rs.wasNull() ? null : lon;
        double lat = rs.getDouble("lat");
        Double latValue = rs.wasNull() ? null : lat;
        return new Row(rs.getLong("seq"), rs.getString("source_id") + ":" + rs.getString("external_id"),
                rs.getString("source_id"), rs.getString("kind"), rs.getInt("version"), instant(rs, "observed_at"),
                instant(rs, "ingested_at"), instant(rs, "valid_until_explicit"), rs.getBoolean("tombstone"),
                HistoryTier.fromCode(rs.getShort("history_tier")), rs.getBoolean("inside"), full ? null : lonValue,
                full ? null : latValue, rs.getString("geometry"), rs.getString("title"), rs.getString("props"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    /** One scanned version. */
    record Row(long seq, String id, String sourceId, String kind, int version, Instant observedAt, Instant ingestedAt,
            Instant validUntilExplicit, boolean tombstone, HistoryTier tier, boolean inside, Double lon, Double lat,
            String geometry, String title, String props) {

        FrameItem item() {
            return new FrameItem(id, kind, title, observedAt, version, lon, lat, geometry, props);
        }
    }

    /** The ordered pass: the entity map, the client's view and the frames. */
    static final class Pass {

        private final AsOfPlan plan;
        private final List<Instant> times;
        private final long[] bounds;
        private final Map<String, Row> latest = new HashMap<>();
        /** What a client that applied every frame so far holds: id to seq. */
        private final Map<String, Long> shown = new HashMap<>();
        private final List<HistoryFrames.Frame> frames = new ArrayList<>();
        private final TreeSet<String> touched = new TreeSet<>();
        private List<FrameItem> keyframe;
        private final int maxEntities;
        private final int maxRows;
        private int frame;
        private long rows;

        Pass(AsOfPlan plan, List<Instant> times, long[] bounds, int maxEntities, int maxRows) {
            this.plan = plan;
            this.times = times;
            this.bounds = bounds;
            this.maxEntities = maxEntities;
            this.maxRows = maxRows;
        }

        /** Applies one row (rows arrive in seq order), closing every frame it passes. */
        void accept(Row row) {
            if (++rows > maxRows) {
                throw tooLarge("more than " + maxRows + " rows");
            }
            while (row.seq() > bounds[frame]) {
                close();
            }
            if (latest.put(row.id(), row) == null && latest.size() > maxEntities) {
                throw tooLarge("more than " + maxEntities + " entities");
            }
            touched.add(row.id());
        }

        List<FrameItem> keyframe() {
            if (keyframe == null) {
                close();
            }
            return keyframe;
        }

        /** Closes the remaining frames. */
        List<HistoryFrames.Frame> finish() {
            keyframe();
            while (frame <= times.size()) {
                close();
            }
            return List.copyOf(frames);
        }

        /** Closes frame {@code frame} (0 is the keyframe). */
        private void close() {
            Instant at = frame == 0 ? plan.t() : times.get(frame - 1);
            if (frame == 0) {
                TreeMap<String, FrameItem> state = new TreeMap<>();
                for (Row row : latest.values()) {
                    if (visible(row, at)) {
                        state.put(row.id(), row.item());
                        shown.put(row.id(), row.seq());
                    }
                }
                keyframe = List.copyOf(state.values());
            } else {
                TreeSet<String> candidates = new TreeSet<>(touched);
                candidates.addAll(shown.keySet());
                List<FrameItem> upserts = new ArrayList<>();
                List<HistoryFrames.Removal> removals = new ArrayList<>();
                for (String id : candidates) {
                    Row row = latest.get(id);
                    Long held = shown.get(id);
                    if (row != null && visible(row, at)) {
                        if (held == null || held != row.seq()) {
                            upserts.add(row.item());
                            shown.put(id, row.seq());
                        }
                    } else if (held != null) {
                        removals.add(new HistoryFrames.Removal(id, reason(row)));
                        shown.remove(id);
                    }
                }
                frames.add(new HistoryFrames.Frame(at, List.copyOf(upserts), List.copyOf(removals)));
            }
            touched.clear();
            frame++;
        }

        private String reason(Row row) {
            if (row == null || row.tombstone()) {
                return HistoryFrames.REASON_TOMBSTONE;
            }
            return row.inside() ? HistoryFrames.REASON_EXPIRED : HistoryFrames.REASON_LEFT;
        }

        private boolean visible(Row row, Instant at) {
            return !row.tombstone() && row.inside()
                    && plan.valid(row.tier(), row.sourceId(), row.ingestedAt(), row.validUntilExplicit(), at);
        }
    }

    private static ValidationException tooLarge(String what) {
        return new ValidationException(ErrorCodes.FRAMES_TOO_LARGE,
                "The frames would cover " + what + "; narrow bbox or kind, or shorten the window.", List.of());
    }
}
