package ai.foresignal.store.track;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.TimeTravelCache;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.history.VersionSql;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.store.query.SqlParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Builds the track of one entity (prompt 037): {@code GET /v1/records/{sourceId}/{externalId}/track}.
 *
 * <ol>
 *   <li>Versions with {@code source_id = ? AND external_id = ? AND observed_at BETWEEN from AND to},
 *       ordered by seq, which the {@code (source_id, external_id, seq)} index serves; at most
 *       {@value #MAX_SCAN} rows ({@code truncated} when more exist).
 *   <li>{@code POSITION_STALE} carry-forwards (qa flag 16) are skipped; non-point geometry is
 *       reduced with {@code ST_PointOnSurface}.
 *   <li>A tombstone, a time gap larger than {@code gap}, or a version outside the caller's
 *       restriction starts a new segment.
 *   <li>The current {@code record_live} position is appended with {@code live: true} when it is
 *       newer than the last version (under SAMPLED most positions live only there).
 *   <li>{@link TrackSimplifier}: unwrapped longitudes, Douglas–Peucker on {@code (lon, lat, t)},
 *       the {@code maxPoints} search, then the antimeridian split.
 * </ol>
 *
 * <p>The window must not exceed the source's retention window ({@code 400
 * TRACK_WINDOW_TOO_LARGE}).
 */
@Service
public class TrackService {

    /** The shared private-source visibility (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.visibility.SourceVisibility visibility;


    /** The most versions one track reads. */
    public static final int MAX_SCAN = 200_000;

    /** The default window ending at {@code to}. */
    public static final Duration DEFAULT_WINDOW = Duration.ofHours(6);

    /** The smallest default segment gap. */
    public static final Duration MIN_DEFAULT_GAP = Duration.ofMinutes(10);

    /** Default {@code maxPoints}. */
    public static final int DEFAULT_MAX_POINTS = 2_000;

    /** Largest {@code maxPoints}. */
    public static final int MAX_POINTS = 10_000;

    /** Most {@code fields}. */
    public static final int MAX_FIELDS = 4;

    /** A field name that may be requested. */
    public static final Pattern FIELD = VersionSql.PROPERTY;

    /** qa flag of a carried-forward position (Core-Data-Model §3). */
    static final int POSITION_STALE = 16;

    /** Persist reasons whose versions simplification always keeps. */
    static final Set<Short> KEPT_REASONS = Set.of(PersistReason.FIRST.code(), PersistReason.REAPPEAR.code(),
            PersistReason.SIGNIFICANT.code(), PersistReason.TRIGGER_EDGE.code());

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private final RetentionState retention;
    private final AsOfResolver asOf;
    private final Clock clock;

    /** Creates the service. */
    public TrackService(ReadQueryRunner runner, SeqClockQuery seqClock, SourceRegistry registry,
            RetentionState retention, AsOfResolver asOf, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.registry = registry;
        this.retention = retention;
        this.asOf = asOf;
        this.clock = clock;
    }

    /** The default {@code gap} of {@code descriptor}: {@code max(ttl, 10 min)}. */
    public static Duration defaultGap(SourceDescriptor descriptor) {
        Duration ttl = descriptor.recordTtl();
        return ttl == null || ttl.compareTo(MIN_DEFAULT_GAP) < 0 ? MIN_DEFAULT_GAP : ttl;
    }

    /**
     * Builds the track.
     *
     * @throws NotFoundException for an unknown or hidden source, or an entity with neither a head nor
     *     a version the caller may see
     * @throws ValidationException {@code TRACK_WINDOW_TOO_LARGE}, or {@code VALIDATION_FAILED} for a
     *     bad window or field
     */
    public TrackResult track(TrackQuery q) {
        return track(q, MAX_SCAN);
    }

    /**
     * {@link #track(TrackQuery)} reading at most {@code maxScan} versions ({@code truncated} beyond);
     * tests lower it instead of seeding {@value #MAX_SCAN} rows.
     */
    public TrackResult track(TrackQuery q, int maxScan) {
        SourceDescriptor descriptor = registry.descriptor(q.sourceId());
        if (q.excludedSources().contains(descriptor.id())) {
            throw NotFoundException.of("Record", q.sourceId() + ":" + q.externalId());
        }
        Instant now = clock.instant();
        Instant to = q.to() == null ? now : q.to();
        Instant from = q.from() == null ? to.minus(DEFAULT_WINDOW) : q.from();
        if (to.isAfter(now)) {
            throw ValidationException.forField("to", "must not be in the future");
        }
        if (!from.isBefore(to)) {
            throw ValidationException.forField("from", "must be before to");
        }
        Duration window = retention.window(descriptor.id());
        if (window != null && Duration.between(from, to).compareTo(window) > 0) {
            throw new ValidationException(ErrorCodes.TRACK_WINDOW_TOO_LARGE,
                    "The window " + Duration.between(from, to) + " is larger than the retention window " + window
                            + " of " + descriptor.id() + ".", List.of())
                    .withProperty("retentionWindow", window.toString());
        }
        if (q.fields().size() > MAX_FIELDS) {
            throw ValidationException.forField("fields", "at most " + MAX_FIELDS + " fields");
        }
        for (String field : q.fields()) {
            if (!FIELD.matcher(field).matches()) {
                throw ValidationException.forField("fields", "'" + field + "' is not a valid field name");
            }
        }
        if (q.maxPoints() < 2 || q.maxPoints() > MAX_POINTS) {
            throw ValidationException.forField("maxPoints", "must be between 2 and " + MAX_POINTS);
        }
        Duration gap = q.gap() == null ? defaultGap(descriptor) : q.gap();
        if (gap.isZero() || gap.isNegative()) {
            throw ValidationException.forField("gap", "must be positive");
        }
        Scan scan = runner.read(jdbc -> {
            Scan s = new Scan();
            s.watermarkSeq = seqClock.seqAt(jdbc, to);
            SqlParams hp = new SqlParams();
            jdbc.query(headSql(q, hp), hp.source(), rs -> {
                s.head = new Head(rs.getString("kind"), rs.getBoolean("tombstone"), instant(rs, "observed_at"),
                        number(rs, "lon"), number(rs, "lat"), rs.getInt("qa_flags"), rs.getBoolean("visible"),
                        values(rs, q.fields().size()));
            });
            SqlParams vp = new SqlParams();
            jdbc.query(versionsSql(q, descriptor.historyTier(), from, to, maxScan, vp), vp.source(), rs -> {
                if (s.rows.size() >= maxScan) {
                    s.truncated = true;
                    return;
                }
                s.rows.add(new Row(rs.getLong("seq"), instant(rs, "observed_at"), rs.getBoolean("tombstone"),
                        rs.getInt("qa_flags"), rs.getShort("persist_reason"), rs.getString("kind"),
                        number(rs, "lon"), number(rs, "lat"), rs.getBoolean("visible"), values(rs, q.fields().size())));
            });
            return s;
        });
        boolean anyVisible = scan.rows.stream().anyMatch(Row::visible);
        if ((scan.head == null || !scan.head.visible()) && !anyVisible) {
            throw NotFoundException.of("Record", q.sourceId() + ":" + q.externalId());
        }
        return build(q, descriptor, from, to, gap, scan, now);
    }

    private TrackResult build(TrackQuery q, SourceDescriptor descriptor, Instant from, Instant to, Duration gap,
            Scan scan, Instant now) {
        List<List<TrackPoint>> segments = new ArrayList<>();
        List<TrackPoint> current = new ArrayList<>();
        long gapMillis = gap.toMillis();
        Instant lastObserved = null;
        String kind = null;
        long minSeq = Long.MAX_VALUE;
        long maxSeq = Long.MIN_VALUE;
        for (Row row : scan.rows) {
            minSeq = Math.min(minSeq, row.seq());
            maxSeq = Math.max(maxSeq, row.seq());
            lastObserved = row.observedAt();
            if (row.tombstone() || !row.visible()) {
                current = close(segments, current);
                continue;
            }
            kind = row.kind();
            if ((row.qaFlags() & POSITION_STALE) != 0 || row.lon() == null || row.lat() == null) {
                continue;
            }
            long t = row.observedAt().toEpochMilli();
            if (!current.isEmpty() && t - current.getLast().time() > gapMillis) {
                current = close(segments, current);
            }
            current.add(new TrackPoint(row.lon(), row.lat(), t, KEPT_REASONS.contains(row.persistReason()),
                    row.values()));
        }
        boolean live = false;
        Head head = scan.head;
        if (q.includeLive() && head != null && head.visible() && !head.tombstone() && head.observedAt() != null
                && head.lon() != null && head.lat() != null && (head.qaFlags() & POSITION_STALE) == 0
                && (lastObserved == null || head.observedAt().isAfter(lastObserved))
                && !head.observedAt().isBefore(from) && !head.observedAt().isAfter(to)) {
            long t = head.observedAt().toEpochMilli();
            if (!current.isEmpty() && t - current.getLast().time() > gapMillis) {
                current = close(segments, current);
            }
            current.add(new TrackPoint(head.lon(), head.lat(), t, true, head.values()));
            live = true;
        }
        close(segments, current);
        if (head != null && head.kind() != null) {
            kind = head.kind();
        }

        List<List<TrackPoint>> unwrapped = new ArrayList<>();
        int crossings = 0;
        for (List<TrackPoint> segment : segments) {
            List<TrackPoint> u = TrackSimplifier.unwrap(segment);
            crossings += TrackSimplifier.crossings(u);
            unwrapped.add(u);
        }
        double tolerance = q.toleranceMeters() != null ? q.toleranceMeters() : TrackSimplifier.autoTolerance(unwrapped);
        // Every antimeridian crossing adds two points after simplification; reserve them.
        int budget = Math.max(2, q.maxPoints() - 2 * crossings);
        TrackSimplifier.Result simplified = TrackSimplifier.simplify(unwrapped, tolerance, budget);
        List<List<TrackPoint>> split = TrackSimplifier.splitAntimeridian(simplified.segments());

        List<List<double[]>> coordinates = new ArrayList<>();
        List<List<Long>> times = new ArrayList<>();
        Map<String, List<List<Double>>> values = new LinkedHashMap<>();
        for (String field : q.fields()) {
            values.put(field, new ArrayList<>());
        }
        int returned = 0;
        for (List<TrackPoint> segment : split) {
            List<TrackPoint> points = segment.size() == 1 ? List.of(segment.getFirst(), segment.getFirst()) : segment;
            List<double[]> coords = new ArrayList<>(points.size());
            List<Long> segmentTimes = new ArrayList<>(points.size());
            List<List<Double>> segmentValues = new ArrayList<>();
            for (int k = 0; k < q.fields().size(); k++) {
                segmentValues.add(new ArrayList<>(points.size()));
            }
            for (TrackPoint point : points) {
                coords.add(new double[] {point.lon(), point.lat()});
                segmentTimes.add(point.time());
                for (int k = 0; k < q.fields().size(); k++) {
                    segmentValues.get(k).add(k < point.values().length ? point.values()[k] : null);
                }
            }
            coordinates.add(coords);
            times.add(segmentTimes);
            for (int k = 0; k < q.fields().size(); k++) {
                values.get(q.fields().get(k)).add(segmentValues.get(k));
            }
            returned += segment.size();
        }

        HistoryTier tier = descriptor.historyTier();
        boolean downsampled = !scan.rows.isEmpty() && asOf.fidelity().downsampled(tier, minSeq, maxSeq);
        boolean approximate = asOf.sampled(descriptor.id()) || downsampled;
        Instant horizon = asOf.horizon(descriptor.id(), now);
        boolean settled = asOf.settled(tier, to, scan.rows.isEmpty() ? 0L : minSeq, scan.watermarkSeq);
        boolean immutable = !live
                && TimeTravelCache.immutable(to, now, null, settled)
                && !from.isBefore(horizon.plus(TimeTravelCache.HORIZON_GRACE));
        return new TrackResult(descriptor.id(), q.externalId(), kind, from, to, coordinates, times, values, live,
                scan.rows.size(), returned, simplified.toleranceMeters(), coordinates.size(), approximate, downsampled,
                scan.truncated, scan.watermarkSeq, immutable,
                !(visibility == null ? descriptor.privateSource() : visibility.isPrivate(descriptor.id())));
    }

    private static List<TrackPoint> close(List<List<TrackPoint>> segments, List<TrackPoint> current) {
        if (!current.isEmpty()) {
            segments.add(current);
            return new ArrayList<>();
        }
        return current;
    }

    /** The head and live row: kind, tombstone, live position and values, and the restriction verdict. */
    static String headSql(TrackQuery q, SqlParams p) {
        String merged = "jsonb_strip_nulls(h.payload || l.kinematic)";
        String visible = RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", merged, q.restriction(), p)
                .orElse("TRUE");
        return "SELECT h.kind, h.tombstone, l.observed_at, l.qa_flags::integer AS qa_flags,"
                + " ST_X(g.pt) AS lon, ST_Y(g.pt) AS lat, (" + visible + ") AS visible"
                + fieldColumns(merged, q.fields(), p)
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " CROSS JOIN LATERAL (SELECT " + VersionSql.point("l.geom") + " AS pt) g"
                + " WHERE h.source_id = " + p.text(q.sourceId()) + " AND h.external_id = " + p.text(q.externalId());
    }

    /**
     * The version scan: one entity, its tier, {@code observed_at} in the window, ordered by seq,
     * {@code maxScan + 1} rows at most.
     */
    static String versionsSql(TrackQuery q, HistoryTier tier, Instant from, Instant to, int maxScan, SqlParams p) {
        String visible = RestrictionClamp.sqlPredicate("v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p)
                .map(w -> "(v.tombstone OR " + w + ")")
                .orElse("TRUE");
        return "SELECT v.seq, v.observed_at, v.tombstone, v.qa_flags::integer AS qa_flags, v.persist_reason, v.kind,"
                + " ST_X(g.pt) AS lon, ST_Y(g.pt) AS lat, " + visible + " AS visible"
                + fieldColumns("v.payload", q.fields(), p)
                + " FROM record_version v CROSS JOIN LATERAL (SELECT " + VersionSql.point("v.geom") + " AS pt) g"
                + " WHERE v.history_tier = CAST(" + p.add((int) tier.code()) + " AS smallint)"
                + " AND v.source_id = " + p.text(q.sourceId()) + " AND v.external_id = " + p.text(q.externalId())
                + " AND v.observed_at BETWEEN " + p.instant(from) + " AND " + p.instant(to)
                + " ORDER BY v.seq LIMIT " + (maxScan + 1);
    }

    /** {@code , <number or null> AS f0, …} for the requested fields of {@code payload}. */
    static String fieldColumns(String payload, List<String> fields, SqlParams p) {
        StringBuilder out = new StringBuilder();
        for (int k = 0; k < fields.size(); k++) {
            String name = p.text(fields.get(k));
            out.append(", CASE WHEN jsonb_typeof(").append(payload).append(" -> ").append(name)
                    .append(") = 'number' THEN (").append(payload).append(" ->> ").append(name)
                    .append(")::double precision END AS f").append(k);
        }
        return out.toString();
    }

    private static Double[] values(ResultSet rs, int n) throws SQLException {
        Double[] values = new Double[n];
        for (int k = 0; k < n; k++) {
            values[k] = number(rs, "f" + k);
        }
        return values;
    }

    private static Double number(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    /** What the scan read. */
    private static final class Scan {
        Head head;
        final List<Row> rows = new ArrayList<>();
        boolean truncated;
        long watermarkSeq;
    }

    /** The head and live row. */
    private record Head(String kind, boolean tombstone, Instant observedAt, Double lon, Double lat, int qaFlags,
            boolean visible, Double[] values) {}

    /** One version. */
    private record Row(long seq, Instant observedAt, boolean tombstone, int qaFlags, short persistReason, String kind,
            Double lon, Double lat, boolean visible, Double[] values) {}
}
