package ai.foresignal.export;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Streams the rows of an export or snapshot (prompt 111) in keyset pages of
 * {@code exports.pageSize} (10,000) rows, each page its own read transaction, so no result is ever
 * held whole and a cancellation takes effect between pages ({@link Sink#page}).
 *
 * <ul>
 *   <li><b>current</b>: {@code record_head ⋈ record_live} with the merged payload (HIST-2), per kind,
 *       keyset on {@code (source_id, external_id)};
 *   <li><b>history</b>: {@code record_version}, per kind and source, keyset on
 *       {@code (external_id, seq)} (the {@code (source_id, external_id, seq)} index); an
 *       {@code ingested} time window becomes a seq range through {@code seq_clock};
 *   <li><b>asOf / atSeq</b>: the Core-Data-Model §12 query per tier and source, {@code DISTINCT ON
 *       (external_id) … seq BETWEEN L AND S ORDER BY external_id, seq DESC}, unioned with the copies
 *       of covering holds when the state lies below the source's retention floor; tombstones dropped,
 *       validity judged on the transaction axis, and kind, bbox, time and restriction applied to the
 *       chosen version; keyset on {@code external_id}.
 * </ul>
 *
 * <p>Rows come out ordered by {@code (kind, source_id, external_id, seq)} for current and history
 * (kinds a source does not declare come last), and by {@code (tier, source_id, external_id)} for a
 * point in time. The caller's {@link ReadRestriction} applies to every mode. The row maps are those
 * of {@link ExportRows#map}.
 */
@Service
public class ExportQueryService {

    /** Receives streamed rows. */
    public interface Sink {

        /** Called once, before the first row, with the watermark of the export. */
        void begin(long watermarkSeq);

        /** Called for each row, in order. */
        void row(Map<String, Object> row);

        /** Called between pages; throwing here stops the export (cancellation). */
        default void page() {}
    }

    private static final String CURRENT_PAYLOAD = "jsonb_strip_nulls(h.payload || l.kinematic)";

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private final ChangeBus changeBus;
    private final ExportProperties properties;
    private final Clock clock;

    /** Creates the service. */
    public ExportQueryService(ReadQueryRunner runner, SeqClockQuery seqClock, SourceRegistry registry,
            ChangeBus changeBus, ExportProperties properties, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.registry = registry;
        this.changeBus = changeBus;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Streams the rows of {@code spec} over {@code sources} (already license- and privacy-filtered;
     * never empty).
     *
     * @param pointInTime the state to read for {@code asOf} and {@code atSeq}; ignored otherwise
     */
    public void stream(ExportSpec spec, List<String> sources, ReadRestriction restriction, PointInTime pointInTime,
            Sink sink) {
        ReadRestriction r = restriction == null ? ReadRestriction.NONE : restriction;
        List<String> ordered = new ArrayList<>(new TreeSet<>(sources));
        switch (spec.mode()) {
            case "history" -> streamHistory(spec, ordered, r, sink);
            case "asOf", "atSeq" -> streamPointInTime(spec, ordered, r, pointInTime, sink);
            default -> streamCurrent(spec, ordered, r, sink);
        }
    }

    // =====================================================================================
    // Current
    // =====================================================================================

    private void streamCurrent(ExportSpec spec, List<String> sources, ReadRestriction r, Sink sink) {
        sink.begin(changeBus.lastPublishedSeq());
        Instant now = clock.instant();
        List<String> kinds = spec.kinds().isEmpty() ? currentKinds(sources) : new ArrayList<>(new TreeSet<>(spec.kinds()));
        for (String kind : kinds) {
            String lastSource = "";
            String lastExternal = "";
            while (true) {
                String fromSource = lastSource;
                String fromExternal = lastExternal;
                List<Map<String, Object>> page = runner.read(properties.queryTimeout(), jdbc -> {
                    SqlParams p = new SqlParams();
                    List<String> where = new ArrayList<>();
                    where.add("h.kind = " + p.text(kind));
                    where.add("h.source_id = ANY(" + p.textArray(sources) + ")");
                    where.add("NOT h.tombstone");
                    where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(now) + ")");
                    where.add("(h.source_id, h.external_id) > (" + p.text(fromSource) + ", " + p.text(fromExternal) + ")");
                    addBbox(where, "l.geom", spec.bbox(), p);
                    addTime(jdbc, where, spec, p, "COALESCE(h.event_time, l.observed_at)", "l.observed_at", "h.event_time",
                            "h.seq");
                    RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, r, p);
                    String sql = "SELECT h.source_id, h.external_id, h.kind, h.seq, h.version, l.observed_at,"
                            + " h.event_time, l.valid_until, NULL::timestamptz AS ingested_at, h.title,"
                            + " " + CURRENT_PAYLOAD + "::text AS payload, ST_AsBinary(l.geom) AS geom_wkb,"
                            + " ST_XMin(l.geom) AS xmin, ST_YMin(l.geom) AS ymin, ST_XMax(l.geom) AS xmax,"
                            + " ST_YMax(l.geom) AS ymax"
                            + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id"
                            + " AND l.external_id = h.external_id"
                            + " WHERE " + String.join(" AND ", where)
                            + " ORDER BY h.source_id, h.external_id LIMIT " + properties.pageSize();
                    return jdbc.query(sql, p.source(), (rs, i) -> ExportRows.map(rs));
                });
                page.forEach(sink::row);
                sink.page();
                if (page.size() < properties.pageSize()) {
                    break;
                }
                Map<String, Object> last = page.getLast();
                lastSource = (String) last.get("source_id");
                lastExternal = (String) last.get("external_id");
            }
        }
    }

    /** The distinct current kinds of {@code sources}, sorted (the {@code record_head(kind)} index). */
    private List<String> currentKinds(List<String> sources) {
        return runner.read(properties.queryTimeout(), jdbc -> {
            SqlParams p = new SqlParams();
            return jdbc.queryForList("SELECT DISTINCT kind FROM record_head WHERE source_id = ANY("
                    + p.textArray(sources) + ") ORDER BY kind", p.source(), String.class);
        });
    }

    // =====================================================================================
    // History
    // =====================================================================================

    private void streamHistory(ExportSpec spec, List<String> sources, ReadRestriction r, Sink sink) {
        sink.begin(changeBus.lastPublishedSeq());
        Map<String, List<String>> kindsBySource = new LinkedHashMap<>();
        TreeMap<String, List<String>> sourcesByKind = new TreeMap<>();
        for (String source : sources) {
            List<String> kinds = spec.kinds().isEmpty() ? knownKinds(source) : new ArrayList<>(new TreeSet<>(spec.kinds()));
            kindsBySource.put(source, kinds);
            for (String kind : kinds) {
                sourcesByKind.computeIfAbsent(kind, k -> new ArrayList<>()).add(source);
            }
        }
        for (Map.Entry<String, List<String>> kind : sourcesByKind.entrySet()) {
            for (String source : kind.getValue()) {
                historyPages(spec, source, " AND v.kind = :kind", Map.of("kind", kind.getKey()), r, sink);
            }
        }
        if (spec.kinds().isEmpty()) {
            for (String source : sources) {
                List<String> known = kindsBySource.get(source);
                if (known.isEmpty()) {
                    historyPages(spec, source, "", Map.of(), r, sink);
                } else {
                    historyPages(spec, source, " AND v.kind <> ALL(CAST(:known AS text[]))",
                            Map.of("known", ai.foresignal.store.query.PgArrays.text(known)), r, sink);
                }
            }
        }
    }

    /** The kinds {@code source} declares or has heads of, sorted. */
    private List<String> knownKinds(String source) {
        TreeSet<String> kinds = new TreeSet<>();
        registry.find(source).ifPresent(registered -> kinds.addAll(registered.descriptor().kinds()));
        kinds.addAll(runner.read(properties.queryTimeout(), jdbc -> jdbc.queryForList(
                "SELECT DISTINCT kind FROM record_head WHERE source_id = :source",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("source", source), String.class)));
        return new ArrayList<>(kinds);
    }

    private void historyPages(ExportSpec spec, String source, String kindClause, Map<String, Object> kindParams,
            ReadRestriction r, Sink sink) {
        String lastExternal = "";
        long lastSeq = -1;
        while (true) {
            String fromExternal = lastExternal;
            long fromSeq = lastSeq;
            List<Map<String, Object>> page = runner.read(properties.queryTimeout(), jdbc -> {
                SqlParams p = new SqlParams();
                kindParams.forEach((name, value) -> p.source().addValue(name, value));
                List<String> where = new ArrayList<>();
                where.add("v.source_id = " + p.text(source));
                where.add("(v.external_id, v.seq) > (" + p.text(fromExternal) + ", " + p.bigint(fromSeq) + ")");
                addBbox(where, "v.geom", spec.bbox(), p);
                addTime(jdbc, where, spec, p, "COALESCE(v.event_time, v.observed_at)", "v.observed_at", "v.event_time",
                        "v.seq");
                RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", r, p);
                String sql = "SELECT " + ExportRows.versionColumns("v") + " FROM record_version v WHERE "
                        + String.join(" AND ", where) + kindClause
                        + " ORDER BY v.external_id, v.seq LIMIT " + properties.pageSize();
                return jdbc.query(sql, p.source(), (rs, i) -> ExportRows.map(rs));
            });
            page.forEach(sink::row);
            sink.page();
            if (page.size() < properties.pageSize()) {
                return;
            }
            Map<String, Object> last = page.getLast();
            lastExternal = (String) last.get("external_id");
            lastSeq = (Long) last.get("seq");
        }
    }

    // =====================================================================================
    // asOf and atSeq
    // =====================================================================================

    private void streamPointInTime(ExportSpec spec, List<String> sources, ReadRestriction r, PointInTime plan,
            Sink sink) {
        if (plan == null) {
            throw new IllegalStateException("An asOf or atSeq export needs a point-in-time plan");
        }
        sink.begin(plan.s());
        for (HistoryTier tier : HistoryTier.values()) {
            for (String source : sources) {
                SourceDescriptor descriptor = registry.find(source).map(reg -> reg.descriptor()).orElse(null);
                if (descriptor == null || descriptor.historyTier() != tier) {
                    continue;
                }
                pointInTimePages(spec, tier, source, r, plan, sink);
            }
        }
    }

    private void pointInTimePages(ExportSpec spec, HistoryTier tier, String source, ReadRestriction r, PointInTime plan,
            Sink sink) {
        List<String> holds = plan.holdsFor(source);
        String lastExternal = "";
        while (true) {
            String fromExternal = lastExternal;
            List<Map<String, Object>> page = runner.read(properties.queryTimeout(), jdbc -> {
                SqlParams p = new SqlParams();
                String inner = "SELECT v.source_id, v.external_id, v.kind, v.seq, v.version, v.observed_at, v.event_time,"
                        + " v.valid_until, v.valid_until_explicit, v.ingested_at, v.tombstone, v.payload, v.geom FROM %s v"
                        + " WHERE v.history_tier = " + p.add(tier.code()) + " AND v.source_id = " + p.text(source)
                        + " AND v.seq BETWEEN " + p.bigint(plan.lower(tier)) + " AND " + p.bigint(plan.s())
                        + " AND v.external_id > " + p.text(fromExternal);
                String union = inner.formatted("record_version");
                if (!holds.isEmpty()) {
                    union += " UNION ALL " + inner.formatted(ExportSchemaInitializer.HELD_TABLE)
                            + " AND v.hold_id = ANY(" + p.textArray(holds) + ")";
                }
                List<String> keep = new ArrayList<>();
                if (!spec.kinds().isEmpty()) {
                    keep.add("c.kind = ANY(" + p.textArray(spec.kinds()) + ")");
                }
                addBbox(keep, "c.geom", spec.bbox(), p);
                addTime(jdbc, keep, spec, p, "COALESCE(c.event_time, c.observed_at)", "c.observed_at", "c.event_time",
                        "c.seq");
                RestrictionClamp.addTo(keep, "c.source_id", "c.kind", "c.geom", "c.payload", r, p);
                String keepExpression = keep.isEmpty() ? "TRUE" : "(" + String.join(" AND ", keep) + ")";
                String sql = "SELECT " + ExportRows.versionColumns("c") + ", c.tombstone, c.valid_until_explicit,"
                        + " " + keepExpression + " AS keep_row"
                        + " FROM (SELECT DISTINCT ON (u.external_id) u.* FROM (" + union + ") u"
                        + " ORDER BY u.external_id, u.seq DESC LIMIT " + properties.pageSize() + ") c"
                        + " ORDER BY c.external_id";
                return jdbc.query(sql, p.source(), (rs, i) -> {
                    Map<String, Object> row = ExportRows.map(rs);
                    boolean emit = rs.getBoolean("keep_row") && !rs.getBoolean("tombstone")
                            && plan.valid(tier, source, (Instant) row.get("ingested_at"),
                                    ExportRows.instant(rs.getObject("valid_until_explicit")));
                    row.put("__emit", emit);
                    return row;
                });
            });
            for (Map<String, Object> row : page) {
                if (Boolean.TRUE.equals(row.remove("__emit"))) {
                    sink.row(row);
                }
            }
            sink.page();
            if (page.size() < properties.pageSize()) {
                return;
            }
            lastExternal = (String) page.getLast().get("external_id");
        }
    }

    // =====================================================================================
    // Shared SQL assembly
    // =====================================================================================

    private static void addBbox(List<String> where, String column, double[] bbox, SqlParams p) {
        if (bbox == null) {
            return;
        }
        String envelope = "ST_MakeEnvelope(" + p.dbl(bbox[0]) + ", " + p.dbl(bbox[1]) + ", " + p.dbl(bbox[2]) + ", "
                + p.dbl(bbox[3]) + ", 4326)";
        where.add("(" + column + " && " + envelope + " AND ST_Intersects(" + column + ", " + envelope + "))");
    }

    /**
     * Adds the {@code from}/{@code to} window on {@code timeField} (014 semantics): {@code ingested}
     * becomes a seq range through {@code seq_clock}; the others compare the chosen column.
     */
    private void addTime(NamedParameterJdbcTemplate jdbc, List<String> where, ExportSpec spec, SqlParams p,
            String effective, String observed, String event, String seqColumn) {
        Instant since = spec.from();
        Instant until = spec.to();
        if (since == null && until == null) {
            return;
        }
        String axis = spec.timeField() == null ? "effective" : spec.timeField();
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

    /** The envelope {@code [minX, minY, maxX, maxY]} of {@code geometry}, or null when empty. */
    public static double[] envelopeOf(org.locationtech.jts.geom.Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Envelope e = geometry.getEnvelopeInternal();
        return new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
    }

    /** The descriptors of {@code sources} that are registered, in order. */
    public List<SourceDescriptor> descriptors(Collection<String> sources) {
        List<SourceDescriptor> out = new ArrayList<>();
        for (String id : sources) {
            registry.find(id).ifPresent(reg -> out.add(reg.descriptor()));
        }
        return out;
    }
}
