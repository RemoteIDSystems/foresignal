package ai.foresignal.store.query;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ApiProperties;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.stream.ChangeBus;
import java.sql.Array;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Envelope;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The read side of the record store (Core-Data-Model §12): current state, versions, history and the
 * change feed, as native SQL through {@link NamedParameterJdbcTemplate}.
 *
 * <p><strong>Current reads touch only {@code record_head h JOIN record_live l}</strong> (invariant
 * I5, HIST-2): the payload is {@code jsonb_strip_nulls(h.payload || l.kinematic)}, the geometry
 * {@code l.geom}. The one exception is {@code include=observer}, which probes the head's own
 * version ({@code WHERE seq = h.seq}) because observers are stored on versions only (§14).
 *
 * <p><strong>There is no index on {@code record_live.observed_at}, and none may be added</strong>:
 * live touches must stay HOT updates. {@code order=observed} therefore sorts the matched set per
 * page, by design.
 *
 * <p>Every query runs through {@link ReadQueryRunner} (read-only transaction, statement timeout).
 * The snapshot watermark ({@link ChangeBus#lastPublishedSeq()}) is read <em>before</em> the query, so
 * a client that opens the change stream at it misses nothing.
 */
@Service
public class RecordQueryService {

    /** Payload paths per {@code jsonb_build_object} call (the function takes at most 100 arguments). */
    static final int PATHS_PER_OBJECT = 40;

    static final String CURRENT_FROM =
            " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id";

    static final String CURRENT_PAYLOAD = "jsonb_strip_nulls(h.payload || l.kinematic)";

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final ChangeBus changeBus;
    private final RetentionState retention;
    private final SourceRegistry registry;
    private final ApiProperties properties;
    private final Clock clock;

    /** Creates the service. */
    public RecordQueryService(
            ReadQueryRunner runner,
            SeqClockQuery seqClock,
            ChangeBus changeBus,
            RetentionState retention,
            SourceRegistry registry,
            ApiProperties properties,
            Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.changeBus = changeBus;
        this.retention = retention;
        this.registry = registry;
        this.properties = properties;
        this.clock = clock;
    }

    /** Receives streamed rows; called inside the read transaction. */
    public interface RowSink {

        /** Called once, before the first row, with the watermark read before the query. */
        void begin(long watermarkSeq);

        /** Called for each row, in order. */
        void accept(RecordRow row);
    }

    // =====================================================================================
    // Current state
    // =====================================================================================

    /**
     * One page of current records.
     *
     * @throws ValidationException {@code INVALID_CURSOR} for a cursor of another query or ordering
     */
    public Slices.RecordSlice list(CurrentQuery q) {
        long watermark = changeBus.lastPublishedSeq();
        String fingerprint = CursorCodec.fingerprint(q.canonical());
        Cursor cursor = decodeCurrentCursor(q, fingerprint);
        Long pin = pinFor(q, cursor, watermark);
        Instant now = clock.instant();
        List<RecordRow> rows = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = currentSql(jdbc, q, p, pin, cursor, now, false) + " LIMIT " + (q.limit() + 1);
            return jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
        });
        String next = null;
        if (rows.size() > q.limit()) {
            rows = new ArrayList<>(rows.subList(0, q.limit()));
            next = CursorCodec.encode(currentCursor(q, rows.getLast(), pin), fingerprint);
        }
        return new Slices.RecordSlice(rows, next, watermark, pin);
    }

    /**
     * Streams every matching current record (no paging), refusing more than {@code cap} rows with
     * {@code 400 RESULT_TOO_LARGE} before anything is written.
     */
    public void streamCurrent(CurrentQuery q, int cap, RowSink sink) {
        long watermark = changeBus.lastPublishedSeq();
        Long pin = q.pinned() ? (q.asOfSeq() != null ? q.asOfSeq() : watermark) : null;
        Instant now = clock.instant();
        runner.stream(jdbc -> {
            SqlParams countParams = new SqlParams();
            String countSql = "SELECT count(*) FROM (SELECT 1" + CURRENT_FROM
                    + where(currentWhere(jdbc, q, countParams, pin, null, now)) + " LIMIT " + (cap + 1L) + ") c";
            checkCap(jdbc.queryForObject(countSql, countParams.source(), Long.class), cap);
            SqlParams p = new SqlParams();
            String sql = currentSql(jdbc, q, p, pin, null, now, true) + " LIMIT " + cap;
            sink.begin(watermark);
            jdbc.query(sql, p.source(), rowCallback(sink));
            return null;
        });
    }

    /**
     * {@code count=true} on {@code listRecords} (prompt 038): the current records matching {@code q}'s
     * filters (cursor and order ignored), counted with {@code LIMIT 1,000,001}.
     */
    public CountResult countCurrent(CurrentQuery q) {
        return countCurrent(q, CountResult.CAP);
    }

    /** {@link #countCurrent(CurrentQuery)} with another cap (a test hook). */
    CountResult countCurrent(CurrentQuery q, long cap) {
        long watermark = changeBus.lastPublishedSeq();
        Long pin = q.pinned() ? (q.asOfSeq() != null ? q.asOfSeq() : watermark) : null;
        Instant now = clock.instant();
        long counted = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = CountResult.sql("SELECT 1" + CURRENT_FROM + where(currentWhere(jdbc, q, p, pin, null, now)),
                    cap);
            Long n = jdbc.queryForObject(sql, p.source(), Long.class);
            return n == null ? 0L : n;
        });
        return CountResult.of(counted, watermark, cap);
    }

    /**
     * {@code count=true} on {@code queryHistory} (prompt 038): the versions matching {@code q}'s filters
     * (cursor ignored), counted with {@code LIMIT 1,000,001}.
     */
    public CountResult countHistory(HistoryQuery q) {
        return countHistory(q, CountResult.CAP);
    }

    /** {@link #countHistory(HistoryQuery)} with another cap (a test hook). */
    CountResult countHistory(HistoryQuery q, long cap) {
        long watermark = changeBus.lastPublishedSeq();
        long counted = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = CountResult.sql("SELECT 1 FROM record_version v" + where(historyWhere(jdbc, q, p, null)), cap);
            Long n = jdbc.queryForObject(sql, p.source(), Long.class);
            return n == null ? 0L : n;
        });
        return CountResult.of(counted, watermark, cap);
    }

    /**
     * The current state of one record (head plus live), whatever its expiry or tombstone.
     *
     * @return empty when no such record exists or its source is excluded
     */
    public Optional<RecordRow> get(
            String sourceId,
            String externalId,
            boolean includeObserver,
            FieldSelection fields,
            GeometryOptions geometry,
            Set<String> excludedSources) {
        return get(sourceId, externalId, includeObserver, fields, geometry, excludedSources, ReadRestriction.NONE);
    }

    /**
     * {@link #get(String, String, boolean, FieldSelection, GeometryOptions, Set)} under
     * {@code restriction}: a record outside it is absent (its caller answers 404, prompt 035).
     */
    public Optional<RecordRow> get(
            String sourceId,
            String externalId,
            boolean includeObserver,
            FieldSelection fields,
            GeometryOptions geometry,
            Set<String> excludedSources,
            ReadRestriction restriction) {
        if (excludedSources.contains(sourceId) || !restriction.allowsSource(sourceId)) {
            return Optional.empty();
        }
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            where.add("h.source_id = " + p.text(sourceId));
            where.add("h.external_id = " + p.text(externalId));
            RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, restriction, p);
            String sql = "SELECT " + currentSelect(fields, geometry, GeometryEncoding.GEOJSON, includeObserver, null, p)
                    + CURRENT_FROM + where(where);
            List<RecordRow> rows = jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
            return rows.stream().findFirst();
        });
    }

    /**
     * The current state of several records (head plus live), whatever their expiry or tombstone,
     * keyed by {@code <source>:<externalId>}. Search results join it (prompt 027). Records that no
     * longer exist, or whose source is excluded, are absent.
     *
     * @param keys pairs of source id and external id
     */
    public Map<String, RecordRow> currentByKeys(List<Map.Entry<String, String>> keys, Set<String> excludedSources) {
        return currentByKeys(keys, excludedSources, ReadRestriction.NONE);
    }

    /** {@link #currentByKeys(List, Set)} keeping only records inside {@code restriction} (prompt 035). */
    public Map<String, RecordRow> currentByKeys(List<Map.Entry<String, String>> keys, Set<String> excludedSources,
            ReadRestriction restriction) {
        List<String> sources = new ArrayList<>();
        List<String> externalIds = new ArrayList<>();
        for (Map.Entry<String, String> key : keys) {
            if (!excludedSources.contains(key.getKey())) {
                sources.add(key.getKey());
                externalIds.add(key.getValue());
            }
        }
        if (sources.isEmpty()) {
            return Map.of();
        }
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = "SELECT " + currentSelect(FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                    false, null, p) + CURRENT_FROM + " JOIN unnest(" + p.textArray(sources) + ", "
                    + p.textArray(externalIds) + ") AS k(source_id, external_id)"
                    + " ON k.source_id = h.source_id AND k.external_id = h.external_id"
                    + RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, restriction, p)
                            .map(w -> " WHERE " + w).orElse("");
            Map<String, RecordRow> rows = new HashMap<>();
            for (RecordRow row : jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE)) {
                rows.put(row.sourceId() + ":" + row.externalId(), row);
            }
            return rows;
        });
    }

    /** Kinds currently present (head ⋈ live, current only), with counts and sources. */
    public List<Slices.KindRow> kinds(Set<String> excludedSources) {
        return kinds(excludedSources, ReadRestriction.NONE);
    }

    /** {@link #kinds(Set)} counting only records inside {@code restriction} (prompt 035). */
    public List<Slices.KindRow> kinds(Set<String> excludedSources, ReadRestriction restriction) {
        Instant now = clock.instant();
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            StringBuilder sql = new StringBuilder(
                    "SELECT h.kind, count(*) AS n, array_agg(DISTINCT h.source_id ORDER BY h.source_id) AS sources")
                    .append(CURRENT_FROM)
                    .append(" WHERE NOT h.tombstone AND (l.valid_until IS NULL OR l.valid_until > ")
                    .append(p.instant(now)).append(')');
            if (!excludedSources.isEmpty()) {
                sql.append(" AND NOT (h.source_id = ANY(").append(p.textArray(excludedSources)).append("))");
            }
            RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, restriction, p)
                    .ifPresent(w -> sql.append(" AND ").append(w));
            sql.append(" GROUP BY h.kind ORDER BY h.kind");
            return jdbc.query(sql.toString(), p.source(), (rs, i) -> {
                Array array = rs.getArray("sources");
                List<String> sources = array == null ? List.of() : Arrays.asList((String[]) array.getArray());
                return new Slices.KindRow(rs.getString("kind"), rs.getLong("n"), List.copyOf(sources));
            });
        });
    }

    // =====================================================================================
    // Versions and history
    // =====================================================================================

    /**
     * Versions of one record ordered by seq, with {@code since}/{@code until} on {@code observed_at}.
     *
     * @return empty when the record has neither a head nor any version
     */
    public Optional<Slices.VersionSlice> versions(
            String sourceId,
            String externalId,
            Instant since,
            Instant until,
            String cursorToken,
            int limit,
            boolean includeObserver,
            Set<String> excludedSources) {
        return versions(sourceId, externalId, since, until, cursorToken, limit, includeObserver, excludedSources,
                ReadRestriction.NONE);
    }

    /**
     * {@link #versions(String, String, Instant, Instant, String, int, boolean, Set)} under
     * {@code restriction}: only versions inside it, and empty (404) when the record is outside it
     * entirely (prompt 035).
     */
    public Optional<Slices.VersionSlice> versions(
            String sourceId,
            String externalId,
            Instant since,
            Instant until,
            String cursorToken,
            int limit,
            boolean includeObserver,
            Set<String> excludedSources,
            ReadRestriction restriction) {
        if (excludedSources.contains(sourceId) || !restriction.allowsSource(sourceId)) {
            return Optional.empty();
        }
        String fingerprint = CursorCodec.fingerprint("versions|" + sourceId + "|" + externalId + "|" + since + "|" + until);
        Cursor.Seq cursor = CursorCodec.decode(cursorToken, Cursor.Seq.class, fingerprint);
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            where.add("v.source_id = " + p.text(sourceId));
            where.add("v.external_id = " + p.text(externalId));
            if (since != null) {
                where.add("v.observed_at >= " + p.instant(since));
            }
            if (until != null) {
                where.add("v.observed_at < " + p.instant(until));
            }
            if (cursor != null) {
                where.add("v.seq > " + p.bigint(cursor.q()));
            }
            RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", restriction, p);
            String sql = "SELECT " + versionSelect("v", FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                    includeObserver, p) + " FROM record_version v" + where(where)
                    + " ORDER BY v.seq LIMIT " + (limit + 1);
            List<RecordRow> rows = jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
            if (rows.isEmpty() && cursor == null && !recordExists(jdbc, sourceId, externalId, restriction)) {
                return Optional.<Slices.VersionSlice>empty();
            }
            String next = null;
            if (rows.size() > limit) {
                rows = new ArrayList<>(rows.subList(0, limit));
                next = CursorCodec.encode(new Cursor.Seq(rows.getLast().seq(), null), fingerprint);
            }
            return Optional.of(new Slices.VersionSlice(rows, next));
        });
    }

    /** One page of history. */
    public Slices.VersionSlice history(HistoryQuery q) {
        String fingerprint = CursorCodec.fingerprint(q.canonical());
        Cursor.History cursor = CursorCodec.decode(q.cursor(), Cursor.History.class, fingerprint);
        List<RecordRow> rows = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = historySql(jdbc, q, p, cursor) + " LIMIT " + (q.limit() + 1);
            return jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
        });
        String next = null;
        if (rows.size() > q.limit()) {
            rows = new ArrayList<>(rows.subList(0, q.limit()));
            RecordRow last = rows.getLast();
            next = CursorCodec.encode(new Cursor.History(historyAxisValue(q.timeField(), last), last.seq(), null), fingerprint);
        }
        return new Slices.VersionSlice(rows, next);
    }

    /** Streams a history range (no paging), refusing more than {@code cap} rows. */
    public void streamHistory(HistoryQuery q, int cap, RowSink sink) {
        long watermark = changeBus.lastPublishedSeq();
        runner.stream(jdbc -> {
            SqlParams countParams = new SqlParams();
            String countSql = "SELECT count(*) FROM (SELECT 1 FROM record_version v"
                    + where(historyWhere(jdbc, q, countParams, null)) + " LIMIT " + (cap + 1L) + ") c";
            checkCap(jdbc.queryForObject(countSql, countParams.source(), Long.class), cap);
            SqlParams p = new SqlParams();
            String sql = historySql(jdbc, q, p, null) + " LIMIT " + cap;
            sink.begin(watermark);
            jdbc.query(sql, p.source(), rowCallback(sink));
            return null;
        });
    }

    // =====================================================================================
    // Changes
    // =====================================================================================

    /**
     * One page of the change feed.
     *
     * @throws ForesignalException {@code 410 RESYNC_REQUIRED} when {@code after} is below the purge
     *     horizon of the filtered sources
     */
    public Slices.ChangeSlice changes(ChangesQuery q) {
        long watermark = changeBus.lastPublishedSeq();
        long horizon = purgeHorizon(q.sources());
        checkResync(q.after(), horizon, watermark);
        Set<String> inline = Set.copyOf(properties.changes().inlinePayloadKinds());
        List<RecordRow> rows = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = "SELECT " + changeSelect(q, p, false, inline) + changesFrom(q)
                    + where(changesWhere(q, p)) + " ORDER BY v.seq LIMIT " + (q.limit() + 1);
            List<RecordRow> found = jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
            boolean wantsPayload = q.fields().includes("payload");
            List<Long> payloadSeqs = new ArrayList<>();
            for (int i = 0; i < Math.min(found.size(), q.limit()); i++) {
                RecordRow row = found.get(i);
                if (wantsPayload && (q.includePayload() || inline.contains(row.kind()))) {
                    payloadSeqs.add(row.seq());
                }
            }
            if (payloadSeqs.isEmpty()) {
                return found;
            }
            Map<Long, String> payloads = loadPayloads(jdbc, payloadSeqs, q.fields());
            List<RecordRow> withPayload = new ArrayList<>(found.size());
            for (RecordRow row : found) {
                String payload = payloads.get(row.seq());
                withPayload.add(payload == null ? row : row.withPayload(payload));
            }
            return withPayload;
        });
        boolean hasMore = rows.size() > q.limit();
        if (hasMore) {
            rows = new ArrayList<>(rows.subList(0, q.limit()));
        }
        long lastSeq = rows.isEmpty() ? q.after() : rows.getLast().seq();
        if (!hasMore) {
            // Every seq at or below the watermark read before the query was committed and scanned,
            // so a filtered client may skip past the gaps up to it.
            lastSeq = Math.max(lastSeq, Math.max(q.after(), watermark));
        }
        return new Slices.ChangeSlice(rows, lastSeq, hasMore, watermark, horizon);
    }

    /** Streams every change with {@code seq > after} (no paging), refusing more than {@code cap} rows. */
    public void streamChanges(ChangesQuery q, int cap, RowSink sink) {
        long watermark = changeBus.lastPublishedSeq();
        long horizon = purgeHorizon(q.sources());
        checkResync(q.after(), horizon, watermark);
        Set<String> inline = Set.copyOf(properties.changes().inlinePayloadKinds());
        runner.stream(jdbc -> {
            SqlParams countParams = new SqlParams();
            String countSql = "SELECT count(*) FROM (SELECT 1" + changesFrom(q)
                    + where(changesWhere(q, countParams)) + " LIMIT " + (cap + 1L) + ") c";
            checkCap(jdbc.queryForObject(countSql, countParams.source(), Long.class), cap);
            SqlParams p = new SqlParams();
            String sql = "SELECT " + changeSelect(q, p, true, inline) + changesFrom(q)
                    + where(changesWhere(q, p)) + " ORDER BY v.seq LIMIT " + cap;
            sink.begin(watermark);
            jdbc.query(sql, p.source(), rowCallback(sink));
            return null;
        });
    }

    /** The purge horizon: the largest {@code purgeSeq} over {@code sources}, or over every source when empty. */
    public long purgeHorizon(Collection<String> sources) {
        Collection<String> ids = sources.isEmpty() ? registry.ids() : sources;
        long horizon = 0L;
        for (String id : ids) {
            horizon = Math.max(horizon, retention.purgeSeq(id));
        }
        return horizon;
    }

    /**
     * The change rows of {@code seqs} ({@code WHERE seq = ANY(?)}, runtime pruning to one leaf per
     * tier), with GeoJSON geometry and box but no payload, keyed by seq. The SSE hub loads each
     * published batch once with it (prompt 015). Seqs no longer stored are absent.
     */
    public Map<Long, RecordRow> changesBySeq(Collection<Long> seqs) {
        if (seqs.isEmpty()) {
            return Map.of();
        }
        ChangesQuery shape = new ChangesQuery(0L, List.of(), List.of(), List.of(), null, false, 1,
                FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, Set.of());
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = "SELECT " + changeSelect(shape, p, false, Set.of()) + " FROM record_version v WHERE v.seq = ANY("
                    + p.bigintArray(new ArrayList<>(seqs)) + ")";
            Map<Long, RecordRow> rows = new HashMap<>();
            for (RecordRow row : jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE)) {
                rows.put(row.seq(), row);
            }
            return rows;
        });
    }

    /**
     * Whole versions of {@code seqs} ({@code WHERE seq = ANY(?)}), with GeoJSON geometry and payload,
     * keyed by seq; seqs no longer stored are absent. The alert API loads an alert's version with it
     * and {@code testTrigger} its matches (prompt 017).
     */
    public Map<Long, RecordRow> versionsBySeq(Collection<Long> seqs) {
        if (seqs.isEmpty()) {
            return Map.of();
        }
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = "SELECT " + versionSelect("v", FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                    false, p) + " FROM record_version v WHERE v.seq = ANY(" + p.bigintArray(new ArrayList<>(seqs)) + ")";
            Map<Long, RecordRow> rows = new HashMap<>();
            for (RecordRow row : jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE)) {
                rows.put(row.seq(), row);
            }
            return rows;
        });
    }

    /**
     * The whole payloads of {@code seqs} as JSON text ({@code WHERE seq = ANY(?)}), keyed by seq.
     * The SSE sender threads load {@code payload=true} payloads in small batches with it (prompt 015).
     */
    public Map<Long, String> payloadsBySeq(Collection<Long> seqs) {
        if (seqs.isEmpty()) {
            return Map.of();
        }
        return runner.read(jdbc -> loadPayloads(jdbc, new ArrayList<>(seqs), FieldSelection.ALL));
    }

    // =====================================================================================
    // SQL assembly: current
    // =====================================================================================

    private String currentSql(
            NamedParameterJdbcTemplate jdbc,
            CurrentQuery q,
            SqlParams p,
            Long pin,
            Cursor cursor,
            Instant now,
            boolean streaming) {
        String point = q.near() == null ? null : point(q.near(), p);
        String select = currentSelect(q.fields(), q.geometry(), q.encoding(), q.includeObserver(),
                q.order() == ListOrder.DISTANCE ? point : null, p);
        List<String> where = currentWhere(jdbc, q, p, pin, cursor, now);
        String order = switch (q.order()) {
            case OBSERVED -> " ORDER BY l.observed_at DESC, h.source_id DESC, h.external_id DESC";
            case ID -> " ORDER BY h.source_id, h.external_id";
            case DISTANCE -> " ORDER BY dist_m, h.source_id, h.external_id";
        };
        return "SELECT " + select + CURRENT_FROM + where(where) + order;
    }

    private String currentSelect(
            FieldSelection fields,
            GeometryOptions geometry,
            GeometryEncoding encoding,
            boolean includeObserver,
            String distancePoint,
            SqlParams p) {
        SelectList select = new SelectList()
                .put("source_id", "h.source_id")
                .put("external_id", "h.external_id")
                .put("kind", "h.kind")
                .put("version", "h.version")
                .put("seq", "h.seq")
                .put("observed_at", "l.observed_at")
                .put("persisted_observed_at", "h.persisted_observed_at")
                .put("event_time", "h.event_time")
                .put("valid_from", "h.valid_from")
                .put("valid_until", "l.valid_until")
                .put("tombstone", "h.tombstone")
                .put("title", "h.title")
                .put("quality_out", "COALESCE(l.quality, h.quality)::text")
                .put("live", "l.dirty")
                .put("qa_flags", "l.qa_flags::integer");
        if (fields.includesGeometry()) {
            select.put("geom_out", geometryExpression("l.geom", geometry, encoding, p));
        }
        String payload = payloadExpression(CURRENT_PAYLOAD, fields, p);
        if (payload != null) {
            select.put("payload_out", payload);
        }
        if (includeObserver) {
            select.put("observer_out",
                    "(SELECT v.observer::text FROM record_version v WHERE v.seq = h.seq AND v.source_id = h.source_id"
                            + " AND v.external_id = h.external_id)");
        }
        if (distancePoint != null) {
            select.put("dist_m", "ST_Distance(l.geom::geography, " + distancePoint + "::geography)");
        }
        return select.sql();
    }

    private List<String> currentWhere(
            NamedParameterJdbcTemplate jdbc, CurrentQuery q, SqlParams p, Long pin, Cursor cursor, Instant now) {
        List<String> where = new ArrayList<>();
        addIn(where, "h.source_id", q.sources(), p);
        addNotIn(where, "h.source_id", q.excludedSources(), p);
        RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, q.restriction(), p);
        if (q.restriction().regionFilter() != null) {
            // country=, admin1=, county= (prompt 040), through record_region.
            where.add(q.restriction().regionFilter().sql("h.source_id", "h.external_id", p::textArray));
        }
        addIn(where, "h.kind", q.kinds(), p);
        addIn(where, "h.external_id", q.externalIds(), p);
        where.add("NOT h.tombstone");
        if (!q.includeExpired()) {
            where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(now) + ")");
        }
        if (q.text() != null && !q.text().isBlank()) {
            where.add("h.search_text ILIKE " + p.text("%" + escapeLike(q.text().strip()) + "%") + " ESCAPE '\\'");
        }
        if (q.bbox() != null) {
            where.add(bboxClause("l.geom", q.bbox(), p));
        }
        if (q.near() != null) {
            where.add(nearClause("l.geom", q.near(), p));
        }
        addTimeBounds(jdbc, where, q.timeField(), q.since(), q.until(), p,
                "COALESCE(h.event_time, l.observed_at)", "l.observed_at", "h.event_time", "h.seq");
        if (pin != null) {
            where.add("h.seq <= " + p.bigint(pin));
        }
        if (cursor != null) {
            switch (cursor) {
                case Cursor.Observed c -> where.add("(l.observed_at, h.source_id, h.external_id) < ("
                        + p.instant(c.o()) + ", " + p.text(c.s()) + ", " + p.text(c.e()) + ")");
                case Cursor.ById c -> where.add("(h.source_id, h.external_id) > (" + p.text(c.s()) + ", "
                        + p.text(c.e()) + ")");
                case Cursor.Distance c -> where.add("(ST_Distance(l.geom::geography, " + point(q.near(), p)
                        + "::geography), h.source_id, h.external_id) > (" + p.dbl(c.d()) + ", " + p.text(c.s())
                        + ", " + p.text(c.e()) + ")");
                default -> throw new IllegalStateException("Unexpected cursor " + cursor);
            }
        }
        return where;
    }

    private static Cursor decodeCurrentCursor(CurrentQuery q, String fingerprint) {
        Class<? extends Cursor> type = switch (q.order()) {
            case OBSERVED -> Cursor.Observed.class;
            case ID -> Cursor.ById.class;
            case DISTANCE -> Cursor.Distance.class;
        };
        Cursor cursor = CursorCodec.decode(q.cursor(), type, fingerprint);
        if (cursor != null && q.pinned() && cursor.w() == null) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor carries no snapshot seq.", List.of());
        }
        return cursor;
    }

    private static Long pinFor(CurrentQuery q, Cursor cursor, long watermark) {
        if (!q.pinned()) {
            return null;
        }
        if (cursor != null) {
            return cursor.w();
        }
        return q.asOfSeq() != null ? q.asOfSeq() : watermark;
    }

    private static Cursor currentCursor(CurrentQuery q, RecordRow last, Long pin) {
        return switch (q.order()) {
            case OBSERVED -> new Cursor.Observed(last.observedAt(), last.sourceId(), last.externalId(), pin);
            case ID -> new Cursor.ById(last.sourceId(), last.externalId(), pin);
            case DISTANCE -> new Cursor.Distance(
                    last.distanceMeters() == null ? 0.0 : last.distanceMeters(), last.sourceId(), last.externalId(), pin);
        };
    }

    // =====================================================================================
    // SQL assembly: versions, history, changes
    // =====================================================================================

    /** The select list of version-family rows over {@code record_version} aliased {@code a}. */
    static String versionSelect(
            String a,
            FieldSelection fields,
            GeometryOptions geometry,
            GeometryEncoding encoding,
            boolean includeObserver,
            SqlParams p) {
        SelectList select = new SelectList()
                .put("source_id", a + ".source_id")
                .put("external_id", a + ".external_id")
                .put("kind", a + ".kind")
                .put("version", a + ".version")
                .put("seq", a + ".seq")
                .put("observed_at", a + ".observed_at")
                .put("persisted_observed_at", a + ".observed_at")
                .put("event_time", a + ".event_time")
                .put("valid_from", a + ".valid_from")
                .put("valid_until", a + ".valid_until")
                .put("tombstone", a + ".tombstone")
                .put("quality_out", a + ".quality::text")
                .put("qa_flags", a + ".qa_flags::integer")
                .put("ingested_at", a + ".ingested_at")
                .put("received_at", a + ".received_at")
                .put("persist_reason", a + ".persist_reason")
                .put("prev_seq", a + ".prev_seq")
                .put("history_tier", a + ".history_tier")
                .put("batch_id", a + ".batch_id");
        if (fields.includesGeometry()) {
            select.put("geom_out", geometryExpression(a + ".geom", geometry, encoding, p));
        }
        String payload = payloadExpression(a + ".payload", fields, p);
        if (payload != null) {
            select.put("payload_out", payload);
        }
        if (fields.includes("title") && (payload == null || fields.projectsPayload())) {
            select.put("title_payload", a + ".payload::text");
        }
        if (includeObserver) {
            select.put("observer_out", a + ".observer::text");
        }
        return select.sql();
    }

    private String historySql(NamedParameterJdbcTemplate jdbc, HistoryQuery q, SqlParams p, Cursor.History cursor) {
        String select = versionSelect("v", q.fields(), q.geometry(), q.encoding(), q.includeObserver(), p);
        List<String> where = historyWhere(jdbc, q, p, cursor);
        String axis = historyAxis(q.timeField());
        String order = axis == null ? " ORDER BY v.seq" : " ORDER BY " + axis + ", v.seq";
        return "SELECT " + select + " FROM record_version v" + where(where) + order;
    }

    private List<String> historyWhere(NamedParameterJdbcTemplate jdbc, HistoryQuery q, SqlParams p, Cursor.History cursor) {
        List<String> where = new ArrayList<>();
        addIn(where, "v.source_id", q.sources(), p);
        addNotIn(where, "v.source_id", q.excludedSources(), p);
        RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p);
        addIn(where, "v.kind", q.kinds(), p);
        addIn(where, "v.external_id", q.externalIds(), p);
        if (q.bbox() != null) {
            where.add(bboxClause("v.geom", q.bbox(), p));
        }
        if (q.near() != null) {
            where.add(nearClause("v.geom", q.near(), p));
        }
        addTimeBounds(jdbc, where, q.timeField(), q.since(), q.until(), p,
                "COALESCE(v.event_time, v.observed_at)", "v.observed_at", "v.event_time", "v.seq");
        if (cursor != null) {
            String axis = historyAxis(q.timeField());
            if (axis == null || cursor.o() == null) {
                where.add("v.seq > " + p.bigint(cursor.q()));
            } else {
                where.add("(" + axis + ", v.seq) > (" + p.instant(cursor.o()) + ", " + p.bigint(cursor.q()) + ")");
            }
        }
        return where;
    }

    /** The history order axis; null on the ingested axis, which orders by seq. */
    static String historyAxis(TimeField field) {
        return switch (field) {
            case OBSERVED -> "v.observed_at";
            case EFFECTIVE -> "COALESCE(v.event_time, v.observed_at)";
            case EVENT -> "v.event_time";
            case INGESTED -> null;
        };
    }

    private static Instant historyAxisValue(TimeField field, RecordRow row) {
        return switch (field) {
            case OBSERVED -> row.observedAt();
            case EFFECTIVE -> row.eventTime() != null ? row.eventTime() : row.observedAt();
            case EVENT -> row.eventTime();
            case INGESTED -> null;
        };
    }

    private String changeSelect(ChangesQuery q, SqlParams p, boolean inlinePayload, Set<String> inlineKinds) {
        SelectList select = new SelectList()
                .put("source_id", "v.source_id")
                .put("external_id", "v.external_id")
                .put("kind", "v.kind")
                .put("version", "v.version")
                .put("seq", "v.seq")
                .put("observed_at", "v.observed_at")
                .put("persisted_observed_at", "v.observed_at")
                .put("event_time", "v.event_time")
                .put("valid_from", "v.valid_from")
                .put("valid_until", "v.valid_until")
                .put("tombstone", "v.tombstone")
                .put("qa_flags", "v.qa_flags::integer")
                .put("persist_reason", "v.persist_reason")
                .put("prev_seq", "v.prev_seq")
                .put("history_tier", "v.history_tier")
                .put("bx1", "ST_XMin(v.geom)")
                .put("by1", "ST_YMin(v.geom)")
                .put("bx2", "ST_XMax(v.geom)")
                .put("by2", "ST_YMax(v.geom)");
        if (q.prevBoxes()) {
            select.put("pbx1", "ST_XMin(p.geom)")
                    .put("pby1", "ST_YMin(p.geom)")
                    .put("pbx2", "ST_XMax(p.geom)")
                    .put("pby2", "ST_YMax(p.geom)");
        }
        if (q.fields().includesGeometry()) {
            select.put("geom_out", geometryExpression("v.geom", q.geometry(), q.encoding(), p));
        }
        if (inlinePayload && q.fields().includes("payload")) {
            String payload = payloadExpression("v.payload", q.fields(), p);
            if (q.includePayload()) {
                select.put("payload_out", payload);
            } else if (!inlineKinds.isEmpty()) {
                select.put("payload_out", "CASE WHEN v.kind = ANY(" + p.textArray(inlineKinds) + ") THEN "
                        + payload + " END");
            }
        }
        return select.sql();
    }

    private List<String> changesWhere(ChangesQuery q, SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("v.seq > " + p.bigint(q.after()));
        addIn(where, "v.source_id", q.sources(), p);
        addNotIn(where, "v.source_id", q.excludedSources(), p);
        RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p);
        addIn(where, "v.kind", q.kinds(), p);
        addIn(where, "v.external_id", q.externalIds(), p);
        if (q.bbox() != null) {
            where.add(q.prevBoxes()
                    ? "(" + bboxClause("v.geom", q.bbox(), p) + " OR " + bboxClause("p.geom", q.bbox(), p) + ")"
                    : bboxClause("v.geom", q.bbox(), p));
        }
        if (q.near() != null) {
            where.add(q.prevBoxes()
                    ? "(" + nearClause("v.geom", q.near(), p) + " OR " + nearClause("p.geom", q.near(), p) + ")"
                    : nearClause("v.geom", q.near(), p));
        }
        return where;
    }

    /**
     * The FROM clause of a change query: {@code record_version v}, joined to the previous version by
     * seq alone (one leaf per tier after runtime pruning) when the query wants previous boxes.
     */
    private static String changesFrom(ChangesQuery q) {
        return q.prevBoxes()
                ? " FROM record_version v LEFT JOIN record_version p ON p.seq = v.prev_seq"
                : " FROM record_version v";
    }

    private Map<Long, String> loadPayloads(NamedParameterJdbcTemplate jdbc, List<Long> seqs, FieldSelection fields) {
        SqlParams p = new SqlParams();
        String sql = "SELECT v.seq, " + payloadExpression("v.payload", fields, p)
                + " AS payload_out FROM record_version v WHERE v.seq = ANY(" + p.bigintArray(seqs) + ")";
        Map<Long, String> result = new HashMap<>();
        jdbc.query(sql, p.source(), (RowCallbackHandler) rs -> result.put(rs.getLong(1), rs.getString(2)));
        return result;
    }

    private static void checkResync(long after, long horizon, long watermark) {
        if (after < horizon) {
            throw new ForesignalException(
                            HttpStatus.GONE,
                            ErrorCodes.RESYNC_REQUIRED,
                            "Changes after seq " + after + " are no longer complete: retention purged entities at seq "
                                    + horizon + ". Re-snapshot with GET /v1/records?order=id, then resume from its watermarkSeq.")
                    .withProperty("purgeHorizon", horizon)
                    .withProperty("watermarkSeq", watermark);
        }
    }

    private boolean recordExists(NamedParameterJdbcTemplate jdbc, String sourceId, String externalId,
            ReadRestriction restriction) {
        if (restriction.hasDataRestrictions()) {
            // Only a version inside the restriction proves the record exists for this caller.
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            where.add("v.source_id = " + p.text(sourceId));
            where.add("v.external_id = " + p.text(externalId));
            RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", restriction, p);
            Boolean exists = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM record_version v" + where(where) + ")",
                    p.source(), Boolean.class);
            return Boolean.TRUE.equals(exists);
        }
        SqlParams p = new SqlParams();
        String sql = "SELECT EXISTS (SELECT 1 FROM record_head WHERE source_id = " + p.text(sourceId)
                + " AND external_id = " + p.text(externalId) + ")"
                + " OR EXISTS (SELECT 1 FROM record_version WHERE source_id = " + p.text(sourceId)
                + " AND external_id = " + p.text(externalId) + ")";
        Boolean exists = jdbc.queryForObject(sql, p.source(), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    // =====================================================================================
    // Shared pieces
    // =====================================================================================

    /**
     * Adds {@code since}/{@code until} on the chosen axis. {@code ingested} becomes seq bounds:
     * {@code seq > S(since − 1 µs)} and {@code seq <= S(until − 1 µs)}, exact to the batch.
     */
    private void addTimeBounds(
            NamedParameterJdbcTemplate jdbc,
            List<String> where,
            TimeField field,
            Instant since,
            Instant until,
            SqlParams p,
            String effective,
            String observed,
            String event,
            String seqColumn) {
        if (since == null && until == null) {
            return;
        }
        if (field == TimeField.INGESTED) {
            if (since != null) {
                where.add(seqColumn + " > " + p.bigint(seqClock.lastSeqBefore(jdbc, since)));
            }
            if (until != null) {
                where.add(seqColumn + " <= " + p.bigint(seqClock.lastSeqBefore(jdbc, until)));
            }
            return;
        }
        String column = switch (field) {
            case EFFECTIVE -> effective;
            case OBSERVED -> observed;
            case EVENT -> event;
            case INGESTED -> throw new IllegalStateException();
        };
        if (since != null) {
            where.add(column + " >= " + p.instant(since));
        }
        if (until != null) {
            where.add(column + " < " + p.instant(until));
        }
    }

    /** The geometry output expression: precision, optional simplification, GeoJSON or WKT. */
    static String geometryExpression(String column, GeometryOptions options, GeometryEncoding encoding, SqlParams p) {
        String geometry = options.simplifies() ? options.simplified(column, p.dbl(options.simplifyMeters())) : column;
        String precision = "CAST(" + p.add(options.precision()) + " AS integer)";
        return encoding == GeometryEncoding.WKT
                ? "ST_AsText(" + geometry + ", " + precision + ")"
                : "ST_AsGeoJSON(" + geometry + ", " + precision + ")";
    }

    /**
     * The payload output expression: null when the payload is not selected, the projected paths
     * ({@code jsonb_build_object} of {@code #>} lookups, keyed by dot path) when projected, else the
     * whole payload.
     */
    static String payloadExpression(String base, FieldSelection fields, SqlParams p) {
        if (!fields.includes("payload")) {
            return null;
        }
        if (!fields.projectsPayload()) {
            return "(" + base + ")::text";
        }
        StringJoiner objects = new StringJoiner(" || ");
        List<String> paths = fields.payloadPaths();
        for (int start = 0; start < paths.size(); start += PATHS_PER_OBJECT) {
            StringJoiner args = new StringJoiner(", ", "jsonb_build_object(", ")");
            for (String path : paths.subList(start, Math.min(paths.size(), start + PATHS_PER_OBJECT))) {
                args.add(p.text(path));
                args.add("(" + base + ") #> " + p.textArray(List.of(path.split("\\."))));
            }
            objects.add(args.toString());
        }
        return "jsonb_strip_nulls(" + objects + ")::text";
    }

    /**
     * {@code (col && env AND ST_Intersects(col, env)) OR …} over the bbox envelopes; public for the
     * search queries (prompt 027).
     */
    public static String bboxClause(String column, BboxFilter bbox, SqlParams p) {
        StringJoiner or = new StringJoiner(" OR ", "(", ")");
        for (Envelope e : bbox.envelopes()) {
            String envelope = envelope(e, p);
            or.add("(" + column + " && " + envelope + " AND ST_Intersects(" + column + ", " + envelope + "))");
        }
        return or.toString();
    }

    /** The degree-envelope prefilter ORed, then {@code ST_DWithin} on geography. */
    static String nearClause(String column, NearFilter near, SqlParams p) {
        StringJoiner or = new StringJoiner(" OR ", "(", ")");
        for (Envelope e : near.prefilter()) {
            or.add(column + " && " + envelope(e, p));
        }
        return "(" + or + " AND ST_DWithin(" + column + "::geography, " + point(near, p) + "::geography, "
                + p.dbl(near.radiusMeters()) + "))";
    }

    static String point(NearFilter near, SqlParams p) {
        return "ST_SetSRID(ST_MakePoint(" + p.dbl(near.lon()) + ", " + p.dbl(near.lat()) + "), 4326)";
    }

    private static String envelope(Envelope e, SqlParams p) {
        return "ST_MakeEnvelope(" + p.dbl(e.getMinX()) + ", " + p.dbl(e.getMinY()) + ", " + p.dbl(e.getMaxX()) + ", "
                + p.dbl(e.getMaxY()) + ", 4326)";
    }

    static void addIn(List<String> where, String column, Collection<String> values, SqlParams p) {
        if (!values.isEmpty()) {
            where.add(column + " = ANY(" + p.textArray(new LinkedHashSet<>(values)) + ")");
        }
    }

    static void addNotIn(List<String> where, String column, Collection<String> values, SqlParams p) {
        if (!values.isEmpty()) {
            where.add("NOT (" + column + " = ANY(" + p.textArray(new LinkedHashSet<>(values)) + "))");
        }
    }

    /** Escapes {@code %}, {@code _} and the escape character itself for {@code ILIKE … ESCAPE '\'}. */
    static String escapeLike(String text) {
        StringBuilder out = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '%' || c == '_' || c == '\\') {
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }

    static String where(List<String> clauses) {
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    static void checkCap(Long count, int cap) {
        if (count != null && count > cap) {
            throw new ValidationException(
                    ErrorCodes.RESULT_TOO_LARGE,
                    "The result has more than " + cap + " rows (foresignal.api.stream.maxRows). Narrow the filters or"
                            + " use the paged JSON format.",
                    List.of());
        }
    }

    static RowCallbackHandler rowCallback(RowSink sink) {
        return rs -> sink.accept(RecordRowMapper.INSTANCE.mapRow(rs, 0));
    }
}
