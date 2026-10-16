package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Streams the rows of a bulk export (prompt 111) as the column map the {@link ExportWriter} layer
 * expects, straight from {@code record_head ⋈ record_live} (current), {@code record_version}
 * (history) or the Core-Data-Model §12 asOf/atSeq query. Geometry is emitted as WKB
 * ({@code ST_AsBinary}) with its envelope; keyset reads never load a full result into memory.
 *
 * <p>The row map keys are the {@link ExportSchema} columns plus {@code geometry} (WKB, byte[]) and
 * {@code bbox} (double[4]).
 */
@Service
public class ExportQueryService {

    /** Receives streamed export rows. */
    public interface Sink {

        /** Called once, before the first row, with the watermark seq read before the query. */
        void begin(long watermarkSeq);

        /** Called for each row, in keyset order. */
        void row(Map<String, Object> row);
    }

    private static final String CURRENT_FROM =
            " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id";

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final AsOfResolver asOf;
    private final SourceRegistry registry;
    private final ai.foresignal.stream.ChangeBus changeBus;
    private final Clock clock;

    /** Creates the service. */
    public ExportQueryService(ReadQueryRunner runner, SeqClockQuery seqClock, AsOfResolver asOf,
            SourceRegistry registry, ai.foresignal.stream.ChangeBus changeBus, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.asOf = asOf;
        this.registry = registry;
        this.changeBus = changeBus;
        this.clock = clock;
    }

    /**
     * Streams the export's rows. {@code sources} is the already licence- and privacy-filtered source
     * set (empty means "every visible source").
     */
    public void stream(ExportSpec spec, Collection<String> sources, ReadRestriction restriction, Sink sink) {
        switch (spec.mode()) {
            case "history" -> streamHistory(spec, sources, restriction, sink);
            case "asOf" -> streamAsOf(spec, sources, restriction, sink);
            case "atSeq" -> streamAtSeq(spec, sources, restriction, sink);
            default -> streamCurrent(spec, sources, restriction, sink);
        }
    }

    // =====================================================================================
    // Current
    // =====================================================================================

    private void streamCurrent(ExportSpec spec, Collection<String> sources, ReadRestriction restriction, Sink sink) {
        long watermark = changeBus.lastPublishedSeq();
        Instant now = clock.instant();
        runner.stream(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            addIn(where, "h.source_id", sources, p);
            addIn(where, "h.kind", spec.kinds(), p);
            RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, restriction, p);
            where.add("NOT h.tombstone");
            where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(now) + ")");
            addBbox(where, "l.geom", spec.bbox(), p);
            addTimeBounds(jdbc, where, spec.timeField(), spec.from(), spec.to(), p,
                    "COALESCE(h.event_time, l.observed_at)", "l.observed_at", "h.event_time", "h.seq");
            String sql = "SELECT h.source_id, h.external_id, h.kind, h.seq, h.version, l.observed_at,"
                    + " h.event_time, l.valid_until, NULL::timestamptz AS ingested_at, h.title,"
                    + " " + CURRENT_PAYLOAD + "::text AS payload, ST_AsBinary(l.geom) AS geom,"
                    + " ST_XMin(l.geom) AS xmin, ST_YMin(l.geom) AS ymin, ST_XMax(l.geom) AS xmax,"
                    + " ST_YMax(l.geom) AS ymax"
                    + CURRENT_FROM + where(where) + " ORDER BY h.kind, h.source_id, h.external_id";
            sink.begin(watermark);
            jdbc.query(sql, p.source(), mapper(sink));
            return null;
        });
    }

    // =====================================================================================
    // History
    // =====================================================================================

    private void streamHistory(ExportSpec spec, Collection<String> sources, ReadRestriction restriction, Sink sink) {
        long watermark = changeBus.lastPublishedSeq();
        runner.stream(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            addIn(where, "v.source_id", sources, p);
            addIn(where, "v.kind", spec.kinds(), p);
            RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", restriction, p);
            addBbox(where, "v.geom", spec.bbox(), p);
            addTimeBounds(jdbc, where, spec.timeField(), spec.from(), spec.to(), p,
                    "COALESCE(v.event_time, v.observed_at)", "v.observed_at", "v.event_time", "v.seq");
            String sql = "SELECT v.source_id, v.external_id, v.kind, v.seq, v.version, v.observed_at,"
                    + " v.event_time, v.valid_until, v.ingested_at, NULL::varchar AS title, v.payload::text AS payload,"
                    + " ST_AsBinary(v.geom) AS geom, ST_XMin(v.geom) AS xmin, ST_YMin(v.geom) AS ymin,"
                    + " ST_XMax(v.geom) AS xmax, ST_YMax(v.geom) AS ymax"
                    + " FROM record_version v" + where(where)
                    + " ORDER BY v.kind, v.source_id, v.external_id, v.seq";
            sink.begin(watermark);
            jdbc.query(sql, p.source(), mapper(sink));
            return null;
        });
    }

    // =====================================================================================
    // asOf and atSeq
    // =====================================================================================

    private void streamAsOf(ExportSpec spec, Collection<String> sources, ReadRestriction restriction, Sink sink) {
        if (spec.asOf() == null) {
            throw ValidationException.forField("asOf", "is required when mode=asOf");
        }
        if ((sources == null || sources.isEmpty()) && spec.kinds().isEmpty()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "mode=asOf needs a source or kind filter.", List.of());
        }
        AsOfPlan plan = asOf.plan(spec.asOf(), sources, spec.kinds(), Set.of());
        streamDistinctOn(plan.t(), plan.s(), plan, sources, spec, restriction, sink);
    }

    private void streamAtSeq(ExportSpec spec, Collection<String> sources, ReadRestriction restriction, Sink sink) {
        if (spec.atSeq() == null) {
            throw ValidationException.forField("atSeq", "is required when mode=atSeq");
        }
        Instant t = clock.instant();
        streamDistinctOn(t, spec.atSeq(), null, sources, spec, restriction, sink);
    }

    private void streamDistinctOn(Instant t, long s, AsOfPlan plan, Collection<String> sources, ExportSpec spec,
            ReadRestriction restriction, Sink sink) {
        long watermark = changeBus.lastPublishedSeq();
        runner.stream(jdbc -> {
            sink.begin(Math.max(s, 0));
            for (HistoryTier tier : HistoryTier.values()) {
                long l = plan != null ? plan.lower(tier) : 0L;
                SqlParams p = new SqlParams();
                List<String> where = new ArrayList<>();
                where.add("v.history_tier = " + p.add(tier.code()));
                where.add("v.seq BETWEEN " + p.bigint(l) + " AND " + p.bigint(s));
                addIn(where, "v.source_id", sources, p);
                addIn(where, "v.kind", spec.kinds(), p);
                RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", restriction, p);
                addBbox(where, "v.geom", spec.bbox(), p);
                addTimeBounds(jdbc, where, spec.timeField(), spec.from(), spec.to(), p,
                        "COALESCE(v.event_time, v.observed_at)", "v.observed_at", "v.event_time", "v.seq");
                String cols = "v.source_id, v.external_id, v.kind, v.seq, v.version, v.observed_at, v.event_time,"
                        + " v.valid_until, v.valid_until_explicit, v.ingested_at, v.tombstone, v.payload, v.geom";
                String union = "SELECT " + cols + " FROM record_version v" + where(where)
                        + " UNION ALL SELECT " + cols + " FROM " + ExportSchemaInitializer.HELD_TABLE + " v"
                        + where(where);
                String picked = "SELECT DISTINCT ON (source_id, external_id) * FROM (" + union + ") u"
                        + " ORDER BY source_id, external_id, seq DESC";
                String sql = "SELECT source_id, external_id, kind, seq, version, observed_at, event_time, valid_until,"
                        + " ingested_at, NULL::varchar AS title, payload::text AS payload, ST_AsBinary(geom) AS geom,"
                        + " ST_XMin(geom) AS xmin, ST_YMin(geom) AS ymin, ST_XMax(geom) AS xmax, ST_YMax(geom) AS ymax,"
                        + " tombstone AS tombstone, valid_until_explicit AS valid_until_explicit"
                        + " FROM (" + picked + ") c"
                        + " ORDER BY kind, source_id, external_id";
                jdbc.query(sql, p.source(), (RowCallbackHandler) rs -> {
                    if (rs.getBoolean("tombstone")) {
                        return;
                    }
                    if (!valid(tier, plan, rs, t)) {
                        return;
                    }
                    sink.row(map(rs));
                });
            }
            return null;
        });
    }

    private boolean valid(HistoryTier tier, AsOfPlan plan, ResultSet rs, Instant t) throws SQLException {
        String sourceId = rs.getString("source_id");
        Instant ingestedAt = instant(rs.getObject("ingested_at"));
        Instant explicit = instant(rs.getObject("valid_until_explicit"));
        if (plan != null) {
            return plan.valid(tier, sourceId, ingestedAt, explicit, t);
        }
        Duration ttl = registry.find(sourceId).map(r -> r.descriptor().recordTtl()).orElse(null);
        if (tier == HistoryTier.STANDARD && explicit != null) {
            return explicit.isAfter(t);
        }
        return ttl == null || (ingestedAt != null && ingestedAt.plus(ttl).isAfter(t));
    }

    // =====================================================================================
    // Shared SQL assembly
    // =====================================================================================

    private static final String CURRENT_PAYLOAD = "jsonb_strip_nulls(h.payload || l.kinematic)";

    private RowCallbackHandler mapper(Sink sink) {
        return rs -> sink.row(map(rs));
    }

    private static Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source_id", rs.getString("source_id"));
        row.put("external_id", rs.getString("external_id"));
        row.put("kind", rs.getString("kind"));
        row.put("seq", rs.getLong("seq"));
        row.put("version", rs.getInt("version"));
        row.put("observed_at", instant(rs.getObject("observed_at")));
        row.put("event_time", instant(rs.getObject("event_time")));
        row.put("valid_until", instant(rs.getObject("valid_until")));
        row.put("ingested_at", instant(rs.getObject("ingested_at")));
        row.put("title", rs.getString("title"));
        row.put("payload", rs.getString("payload"));
        row.put("geometry", rs.getBytes("geom"));
        Double xmin = dbl(rs.getObject("xmin"));
        if (xmin != null) {
            row.put("bbox", new double[] {xmin, dbl(rs.getObject("ymin")), dbl(rs.getObject("xmax")),
                dbl(rs.getObject("ymax"))});
        }
        return row;
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof Instant i) {
            return i;
        }
        return null;
    }

    private static Double dbl(Object value) {
        return value instanceof Number n ? n.doubleValue() : null;
    }

    private static void addIn(List<String> where, String column, Collection<String> values, SqlParams p) {
        if (values != null && !values.isEmpty()) {
            where.add(column + " = ANY(" + p.textArray(new java.util.LinkedHashSet<>(values)) + ")");
        }
    }

    private static void addBbox(List<String> where, String column, double[] bbox, SqlParams p) {
        if (bbox == null) {
            return;
        }
        String envelope = "ST_MakeEnvelope(" + p.dbl(bbox[0]) + ", " + p.dbl(bbox[1]) + ", " + p.dbl(bbox[2]) + ", "
                + p.dbl(bbox[3]) + ", 4326)";
        where.add(column + " && " + envelope + " AND ST_Intersects(" + column + ", " + envelope + ")");
    }

    private void addTimeBounds(NamedParameterJdbcTemplate jdbc, List<String> where, String field, Instant since,
            Instant until, SqlParams p, String effective, String observed, String event, String seqColumn) {
        if (since == null && until == null || field == null) {
            return;
        }
        String axis = field.toLowerCase(Locale.ROOT);
        if ("ingested".equals(axis)) {
            if (since != null) {
                where.add(seqColumn + " > " + p.bigint(seqClock.lastSeqBefore(jdbc, since)));
            }
            if (until != null) {
                where.add(seqColumn + " <= " + p.bigint(seqClock.lastSeqBefore(jdbc, until)));
            }
            return;
        }
        String column = switch (axis) {
            case "observed" -> observed;
            case "event" -> event;
            default -> effective;
        };
        if (since != null) {
            where.add(column + " >= " + p.instant(since));
        }
        if (until != null) {
            where.add(column + " < " + p.instant(until));
        }
    }

    private static String where(List<String> clauses) {
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    /** The envelope of {@code placeId}, or null. Used by the job service to resolve a place to a box. */
    public static double[] envelopeOf(org.locationtech.jts.geom.Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Envelope e = geometry.getEnvelopeInternal();
        return new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
    }

    /** Exposes the involved descriptors for the licence summary. */
    public List<SourceDescriptor> descriptors(Collection<String> sources) {
        List<SourceDescriptor> out = new ArrayList<>();
        for (String id : sources) {
            registry.find(id).ifPresent(r -> out.add(r.descriptor()));
        }
        return out;
    }
}
