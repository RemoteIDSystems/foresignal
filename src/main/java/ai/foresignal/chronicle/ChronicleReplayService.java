package ai.foresignal.chronicle;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.tiles.TileProperties;
import ai.foresignal.store.tiles.TileQueryService;
import ai.foresignal.store.track.TrackPoint;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import ai.foresignal.store.track.TrackSimplifier;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Replays a chronicle from its own storage only (prompt 131), so replay never depends on global retention:
 *
 * <ul>
 *   <li><b>state at T</b>: per layer, the latest captured version of each entity with {@code observed_at ≤ T} and
 *       {@code ingested_at ≤ T} (no look-ahead: nothing Foresignal learned after T), kept when not a tombstone and
 *       still valid at T by its effective expiry; rollup-only layers give their region-hour counts up to T; paged by
 *       layer;</li>
 *   <li><b>tiles</b>: Mapbox Vector Tiles of the same state with the 016 machinery (tile envelope, Web Mercator
 *       clipping, extent and buffer, the feature cap); an entity is drawn only where its latest version at T is;</li>
 *   <li><b>frames</b>: a playback index of counts per layer and changed entities per step;</li>
 *   <li><b>tracks</b>: an entity's trail from captured versions, in the 037 track shape;</li>
 *   <li><b>series</b>: captured series points and rollup buckets of a series ref;</li>
 *   <li><b>summary</b>: layer counts over time, top entities, linked situations, hypothesis sets and questions,
 *       storage, and capture gaps (reused by 132 reports).</li>
 * </ul>
 *
 * <p>The caller's excluded sources (private sources without private access, restriction-hidden sources) and read
 * restriction (035) apply to every captured version, exactly as to global history.
 */
@Service
public class ChronicleReplayService {

    /** Most steps of a frame index. */
    public static final int MAX_FRAMES = 2_000;

    /** Most changed entities listed per step. */
    public static final int MAX_CHANGED = 50;

    /** The default playback step. */
    public static final Duration DEFAULT_STEP = Duration.ofMinutes(5);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ChronicleService chronicles;
    private final ChronicleStorage storage;
    private final ObjectService objects;
    private final TileProperties tiles;
    private final NamedParameterJdbcTemplate named;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public ChronicleReplayService(ChronicleService chronicles, ChronicleStorage storage, ObjectService objects,
            TileProperties tiles, NamedParameterJdbcTemplate named, JsonMapper json, Clock clock) {
        this.chronicles = chronicles;
        this.storage = storage;
        this.objects = objects;
        this.tiles = tiles;
        this.named = named;
        this.json = json;
        this.clock = clock;
    }

    /**
     * Who reads: the sources hidden from the caller and its read restriction.
     *
     * @param excludedSources sources the caller may not read
     * @param restriction the caller's read restriction
     */
    public record Reader(Set<String> excludedSources, ReadRestriction restriction) {

        /** Copies the set. */
        public Reader {
            excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }

        /** A reader of an object caller with a restriction. */
        public static Reader of(ObjectCaller caller, ReadRestriction restriction) {
            return new Reader(caller == null ? Set.of() : caller.excludedSources(), restriction);
        }
    }

    // ------------------------------------------------------------------------- state

    /**
     * The state of every layer (or one) at {@code at}.
     *
     * @param c the chronicle
     * @param at the replay time
     * @param layerId one layer to page through, or null for the first page of every layer
     * @param bbox envelopes the state is narrowed to, or null
     * @param cursor the opaque cursor of a layer page, or null
     * @param limit the page size per layer
     * @param reader the reader
     * @return the {@code ChronicleState}
     */
    public Map<String, Object> state(ChronicleEntity c, Instant at, String layerId, List<Envelope> bbox, String cursor,
            int limit, Reader reader) {
        String pagedLayer = layerId;
        int offset = 0;
        if (cursor != null && !cursor.isBlank()) {
            String[] parsed = stateCursor(cursor);
            if (pagedLayer != null && !pagedLayer.equals(parsed[0])) {
                throw ValidationException.forField("cursor", "belongs to another layer");
            }
            pagedLayer = parsed[0];
            offset = Integer.parseInt(parsed[1]);
        }
        List<ChronicleLayerEntity> layers = chronicles.liveLayers(c.getId());
        if (pagedLayer != null) {
            ChronicleLayerEntity one = chronicles.layer(c.getId(), pagedLayer);
            layers = List.of(one);
        }
        boolean leaf = storage.leafExists(c.getId());
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChronicleLayerEntity l : layers) {
            CaptureMode mode = CaptureMode.fromMap(l.getCapture());
            Map<String, Object> page = new LinkedHashMap<>();
            page.put("layerId", l.getId());
            page.put("name", l.getName());
            page.put("capture", mode.toMap());
            if (CaptureMode.ROLLUP_ONLY.equals(mode.mode())) {
                List<Map<String, Object>> rollups = rollups(c.getId(), l.getId(), at);
                page.put("count", rollups.size());
                page.put("items", List.of());
                page.put("rollups", rollups);
                page.put("nextCursor", null);
            } else if (!leaf) {
                page.put("count", 0);
                page.put("items", List.of());
                page.put("nextCursor", null);
            } else {
                int skip = l.getId().equals(pagedLayer) ? offset : 0;
                List<Map<String, Object>> items = latest(c.getId(), l.getId(), at, bbox, reader, skip, limit + 1);
                boolean more = items.size() > limit;
                page.put("count", count(c.getId(), l.getId(), at, bbox, reader));
                page.put("items", more ? items.subList(0, limit) : items);
                page.put("nextCursor", more ? stateCursor(l.getId(), skip + limit) : null);
            }
            out.add(page);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chronicleId", c.getId());
        result.put("at", at.toString());
        result.put("layers", out);
        result.put("caveats", caveats(c, at));
        return result;
    }

    /** The SQL of the latest captured version per entity at {@code at} (the {@code picked} rows). */
    private String latestSql(String chronicleId, String layerId, Instant at, List<Envelope> bbox, Reader reader,
            SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("v.chronicle_id = " + p.text(chronicleId));
        where.add("v.layer_id = " + p.text(layerId));
        String t = p.instant(at);
        where.add("v.observed_at <= " + t);
        where.add("v.ingested_at <= " + t);
        readerWhere(where, reader, p);
        List<String> outer = new ArrayList<>();
        outer.add("NOT s.tombstone");
        outer.add("(s.valid_until IS NULL OR s.valid_until > " + t + ")");
        if (bbox != null && !bbox.isEmpty()) {
            List<String> boxes = new ArrayList<>();
            for (Envelope e : bbox) {
                boxes.add("s.geom && ST_MakeEnvelope(" + p.dbl(e.getMinX()) + ", " + p.dbl(e.getMinY()) + ", "
                        + p.dbl(e.getMaxX()) + ", " + p.dbl(e.getMaxY()) + ", 4326)");
            }
            outer.add("(" + String.join(" OR ", boxes) + ")");
        }
        return "SELECT * FROM (SELECT DISTINCT ON (v.source_id, v.external_id) v.* FROM " + ChronicleDdl.TABLE + " v"
                + " WHERE " + String.join(" AND ", where)
                + " ORDER BY v.source_id, v.external_id, v.observed_at DESC, v.seq DESC) s WHERE "
                + String.join(" AND ", outer);
    }

    private void readerWhere(List<String> where, Reader reader, SqlParams p) {
        if (!reader.excludedSources().isEmpty()) {
            where.add("NOT (v.source_id = ANY(" + p.textArray(reader.excludedSources()) + "))");
        }
        RestrictionClamp.sqlPredicate("v.source_id", "v.kind", "v.geom", "v.payload", reader.restriction(), p)
                .ifPresent(where::add);
    }

    private List<Map<String, Object>> latest(String chronicleId, String layerId, Instant at, List<Envelope> bbox,
            Reader reader, int offset, int limit) {
        SqlParams p = new SqlParams();
        String sql = "SELECT s.layer_id, s.source_id, s.external_id, s.kind, s.seq, s.observed_at, s.event_time,"
                + " s.ingested_at, ST_AsGeoJSON(s.geom) AS geojson, s.payload::text AS payload, s.persist_reason,"
                + " s.backfilled FROM (" + latestSql(chronicleId, layerId, at, bbox, reader, p) + ") s"
                + " ORDER BY s.source_id, s.external_id OFFSET " + p.add(offset) + " LIMIT " + p.add(limit);
        return named.query(sql, p.source(), (rs, n) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            String source = rs.getString("source_id");
            String external = rs.getString("external_id");
            long seq = rs.getLong("seq");
            item.put("ref", ObjectRef.record(source, external, seq).format());
            item.put("sourceId", source);
            item.put("externalId", external);
            item.put("kind", rs.getString("kind"));
            item.put("seq", seq);
            item.put("observedAt", instant(rs.getObject("observed_at", OffsetDateTime.class)));
            item.put("eventTime", instant(rs.getObject("event_time", OffsetDateTime.class)));
            item.put("ingestedAt", instant(rs.getObject("ingested_at", OffsetDateTime.class)));
            String geo = rs.getString("geojson");
            item.put("geometry", geo == null ? null : json.readValue(geo, MAP));
            item.put("payload", parse(rs.getString("payload")));
            item.put("persistReason", reasonName(rs.getShort("persist_reason")));
            item.put("layerId", rs.getString("layer_id"));
            item.put("backfilled", rs.getBoolean("backfilled"));
            return item;
        });
    }

    private long count(String chronicleId, String layerId, Instant at, List<Envelope> bbox, Reader reader) {
        SqlParams p = new SqlParams();
        Long n = named.queryForObject("SELECT count(*) FROM (" + latestSql(chronicleId, layerId, at, bbox, reader, p)
                + ") c", p.source(), Long.class);
        return n == null ? 0 : n;
    }

    private List<Map<String, Object>> rollups(String chronicleId, String layerId, Instant at) {
        SqlParams p = new SqlParams();
        return named.query("SELECT region, hour_start, versions FROM chronicle_rollup WHERE chronicle_id = "
                + p.text(chronicleId) + " AND layer_id = " + p.text(layerId) + " AND hour_start <= " + p.instant(at)
                + " ORDER BY hour_start, region LIMIT 10000", p.source(), (rs, n) -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("region", rs.getString("region"));
                    r.put("hour", instant(rs.getObject("hour_start", OffsetDateTime.class)));
                    r.put("count", rs.getLong("versions"));
                    return r;
                });
    }

    private List<Map<String, Object>> caveats(ChronicleEntity c, Instant at) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (c.getStartedAt() == null || at.isBefore(c.getStartedAt())) {
            out.add(Map.of("code", "BEFORE_CAPTURE", "text", "The time is before the chronicle started capturing;"
                    + " only backfilled history, if any, is shown."));
        }
        if (at.isAfter(clock.instant())) {
            out.add(Map.of("code", "FUTURE", "text", "The time is in the future; the state is the latest captured."));
        }
        for (Map<String, Object> gap : gaps(c.getId())) {
            Instant from = Instant.parse(String.valueOf(gap.get("from")));
            Instant to = Instant.parse(String.valueOf(gap.get("to")));
            if (!at.isBefore(from) && !at.isAfter(to)) {
                out.add(Map.of("code", "CAPTURE_GAP", "text", "The time falls in a capture gap; versions of the"
                        + " interval were lost to global retention before capture.", "data", gap));
            }
        }
        if (ChronicleVocabulary.ARCHIVED.equals(c.getStatus())) {
            out.add(Map.of("code", "ARCHIVED", "text", "The chronicle is archived; restore it to replay its captured"
                    + " versions."));
        }
        return out;
    }

    /** The capture gaps of a chronicle ({@code from}, {@code to}, {@code reason}), oldest first. */
    public List<Map<String, Object>> gaps(String chronicleId) {
        SqlParams p = new SqlParams();
        return named.query("SELECT gap::text FROM chronicle_event WHERE chronicle_id = " + p.text(chronicleId)
                + " AND type = 'capture-gap' AND gap IS NOT NULL ORDER BY id", p.source(),
                (rs, n) -> parse(rs.getString(1)));
    }

    static String stateCursor(String layerId, int offset) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("s:" + layerId + ":" + offset)
                .getBytes(StandardCharsets.UTF_8));
    }

    static String[] stateCursor(String cursor) {
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor.strip()), StandardCharsets.UTF_8);
            String[] parts = s.split(":", 3);
            if (parts.length != 3 || !"s".equals(parts[0]) || Integer.parseInt(parts[2]) < 0) {
                throw new IllegalArgumentException(s);
            }
            return new String[] {parts[1], parts[2]};
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor this endpoint issued")));
        }
    }

    // ------------------------------------------------------------------------- tiles

    /**
     * A vector tile of the chronicle's state at {@code at} (default: the latest captured).
     *
     * @param c the chronicle
     * @param coordinates the tile
     * @param at the replay time, or null for now
     * @param layerIds the layers, or empty for all
     * @param reader the reader
     * @return the MVT bytes (empty for an empty tile) and whether the feature cap cut it
     */
    public TileBytes tile(ChronicleEntity c, TileCoordinates coordinates, Instant at, Collection<String> layerIds,
            Reader reader) {
        if (!storage.leafExists(c.getId())) {
            return new TileBytes(new byte[0], false);
        }
        List<String> layers = new ArrayList<>();
        for (ChronicleLayerEntity l : chronicles.liveLayers(c.getId())) {
            if (layerIds == null || layerIds.isEmpty() || layerIds.contains(l.getId())) {
                layers.add(l.getId());
            }
        }
        if (layers.isEmpty()) {
            return new TileBytes(new byte[0], false);
        }
        Instant time = at == null ? clock.instant() : at;
        int cap = tiles.maxFeatures();
        SqlParams p = new SqlParams();
        String env = TileQueryService.envelope(coordinates, p);
        String t = p.instant(time);
        String chronicle = p.text(c.getId());
        String layerArray = p.textArray(layers);
        List<String> where = new ArrayList<>();
        readerWhere(where, reader, p);
        String extra = where.isEmpty() ? "" : " AND " + String.join(" AND ", where);
        String sql = "WITH bounds AS (SELECT " + env + " AS env),"
                + " cand AS (SELECT DISTINCT v.layer_id, v.source_id, v.external_id FROM " + ChronicleDdl.TABLE + " v"
                + " CROSS JOIN bounds WHERE v.chronicle_id = " + chronicle + " AND v.layer_id = ANY(" + layerArray + ")"
                + " AND v.observed_at <= " + t + " AND v.ingested_at <= " + t
                + " AND v.geom && ST_Transform(bounds.env, 4326)" + extra + "),"
                + " pick AS (SELECT x.* FROM cand c CROSS JOIN LATERAL (SELECT * FROM " + ChronicleDdl.TABLE + " v"
                + " WHERE v.chronicle_id = " + chronicle + " AND v.layer_id = c.layer_id AND v.source_id = c.source_id"
                + " AND v.external_id = c.external_id AND v.observed_at <= " + t + " AND v.ingested_at <= " + t
                + extra + " ORDER BY v.observed_at DESC, v.seq DESC LIMIT 1) x),"
                + " matched AS (SELECT h.* FROM pick h CROSS JOIN bounds WHERE NOT h.tombstone"
                + " AND (h.valid_until IS NULL OR h.valid_until > " + t + ")"
                + " AND h.geom && ST_Transform(bounds.env, 4326)),"
                + " features AS (SELECT ST_AsMVTGeom(ST_Transform(" + TileQueryService.clipped("h.geom") + ", 3857),"
                + " bounds.env, " + TileQueryService.EXTENT + ", " + TileQueryService.BUFFER + ", true) AS geom,"
                + " jsonb_strip_nulls(jsonb_build_object('source', h.source_id, 'externalId', h.external_id,"
                + " 'kind', h.kind, 'layerId', h.layer_id, 'seq', h.seq, 'version', h.version,"
                + " 'observedAt', CAST(extract(epoch FROM h.observed_at) AS bigint),"
                + " 'title', left(COALESCE(NULLIF(h.payload ->> 'title', ''), NULLIF(h.payload ->> 'name', ''),"
                + " NULLIF(h.payload ->> 'callsign', ''), NULLIF(h.payload ->> 'place', '')), 256),"
                + " 'backfilled', h.backfilled)) AS props, h.observed_at AS observed_at, h.source_id AS sid,"
                + " h.external_id AS eid FROM matched h CROSS JOIN bounds"
                + " ORDER BY h.observed_at DESC, h.source_id, h.external_id LIMIT " + p.bigint(cap + 1L) + ")"
                + " SELECT (SELECT ST_AsMVT(f.*, 'chronicle', " + TileQueryService.EXTENT + ", 'geom') FROM"
                + " (SELECT geom, props FROM features WHERE geom IS NOT NULL ORDER BY observed_at DESC, sid, eid"
                + " LIMIT " + p.bigint(cap) + ") f) AS mvt, (SELECT count(*) FROM features) AS matched";
        return named.queryForObject(sql, p.source(), (rs, n) -> new TileBytes(
                rs.getBytes("mvt") == null ? new byte[0] : rs.getBytes("mvt"), rs.getLong("matched") > cap));
    }

    /**
     * A rendered tile.
     *
     * @param bytes the MVT bytes
     * @param truncated true when the feature cap cut it
     */
    public record TileBytes(byte[] bytes, boolean truncated) {}

    // ------------------------------------------------------------------------- frames

    /**
     * The playback frame index.
     *
     * @param c the chronicle
     * @param from the first step, or null for the start of capture
     * @param to the end, or null for the end of capture (or now)
     * @param step the step, or null for 5 minutes
     * @param layerIds the layers, or empty for all
     * @param reader the reader
     * @return the {@code ChronicleFrameIndex}
     */
    public Map<String, Object> frames(ChronicleEntity c, Instant from, Instant to, Duration step,
            Collection<String> layerIds, Reader reader) {
        Duration width = step == null ? DEFAULT_STEP : step;
        if (width.compareTo(Duration.ofSeconds(1)) < 0) {
            throw ValidationException.forField("step", "must be at least PT1S");
        }
        Instant start = from != null ? from : c.getStartedAt() != null ? c.getStartedAt() : c.getCreatedAt();
        Instant end = to != null ? to : c.getStoppedAt() != null ? c.getStoppedAt() : clock.instant();
        if (!start.isBefore(end)) {
            throw ValidationException.forField("to", "must be after from");
        }
        long steps = Math.max(1, (long) Math.ceil(Duration.between(start, end).toMillis() / (double) width.toMillis()));
        boolean truncated = steps > MAX_FRAMES;
        if (truncated) {
            end = start.plus(width.multipliedBy(MAX_FRAMES));
            steps = MAX_FRAMES;
        }
        List<String> layers = new ArrayList<>();
        for (ChronicleLayerEntity l : chronicles.liveLayers(c.getId())) {
            if (layerIds == null || layerIds.isEmpty() || layerIds.contains(l.getId())) {
                layers.add(l.getId());
            }
        }
        TreeMap<Long, Map<String, Object>> frames = new TreeMap<>();
        for (long i = 0; i < steps; i++) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("t", start.plus(width.multipliedBy(i)).toString());
            f.put("counts", new LinkedHashMap<String, Long>());
            f.put("changed", new ArrayList<String>());
            f.put("changedTotal", 0L);
            frames.put(i, f);
        }
        if (!layers.isEmpty() && storage.leafExists(c.getId())) {
            SqlParams p = new SqlParams();
            String bucket = "CAST(floor(extract(epoch FROM (v.observed_at - " + p.instant(start) + ")) / "
                    + p.dbl(width.toMillis() / 1000.0) + ") AS bigint)";
            List<String> where = new ArrayList<>();
            where.add("v.chronicle_id = " + p.text(c.getId()));
            where.add("v.layer_id = ANY(" + p.textArray(layers) + ")");
            where.add("v.observed_at >= " + p.instant(start));
            where.add("v.observed_at < " + p.instant(end));
            readerWhere(where, reader, p);
            String w = String.join(" AND ", where);
            named.query("SELECT " + bucket + " AS b, v.layer_id, count(*) AS n FROM " + ChronicleDdl.TABLE + " v WHERE "
                    + w + " GROUP BY 1, 2", p.source(), rs -> {
                        Map<String, Object> f = frames.get(rs.getLong("b"));
                        if (f != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Long> counts = (Map<String, Long>) f.get("counts");
                            counts.put(rs.getString("layer_id"), rs.getLong("n"));
                        }
                    });
            named.query("SELECT b, count(*) AS total FROM (SELECT DISTINCT " + bucket + " AS b, v.source_id,"
                    + " v.external_id FROM " + ChronicleDdl.TABLE + " v WHERE " + w + ") d GROUP BY b", p.source(),
                    rs -> {
                        Map<String, Object> f = frames.get(rs.getLong("b"));
                        if (f != null) {
                            f.put("changedTotal", rs.getLong("total"));
                        }
                    });
            named.query("SELECT b, source_id, external_id FROM (SELECT b, source_id, external_id, row_number() OVER"
                    + " (PARTITION BY b ORDER BY first_seq) AS rn FROM (SELECT " + bucket + " AS b, v.source_id,"
                    + " v.external_id, min(v.seq) AS first_seq FROM " + ChronicleDdl.TABLE + " v WHERE " + w
                    + " GROUP BY 1, 2, 3) g) r WHERE rn <= " + MAX_CHANGED + " ORDER BY b, rn", p.source(), rs -> {
                        Map<String, Object> f = frames.get(rs.getLong("b"));
                        if (f != null) {
                            @SuppressWarnings("unchecked")
                            List<String> changed = (List<String>) f.get("changed");
                            changed.add(ObjectRef.record(rs.getString("source_id"), rs.getString("external_id"), null)
                                    .format());
                        }
                    });
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chronicleId", c.getId());
        out.put("from", start.toString());
        out.put("to", end.toString());
        out.put("step", width.toString());
        out.put("layers", layers);
        out.put("frames", new ArrayList<>(frames.values()));
        out.put("truncated", truncated);
        return out;
    }

    // ------------------------------------------------------------------------- tracks

    /**
     * An entity's trail from captured versions (037 shape).
     *
     * @param c the chronicle
     * @param sourceId the source
     * @param externalId the entity
     * @param from the start, or null for the start of capture
     * @param to the end, or null for the end of capture (or now)
     * @param maxPoints the most points returned
     * @param reader the reader
     * @return the track; {@code 404 UNKNOWN_RECORD} when the chronicle holds no version of the entity
     */
    public TrackResult track(ChronicleEntity c, String sourceId, String externalId, Instant from, Instant to,
            int maxPoints, Reader reader) {
        if (reader.excludedSources().contains(sourceId) || !storage.leafExists(c.getId())) {
            throw new ai.foresignal.api.error.NotFoundException(ErrorCodes.UNKNOWN_RECORD,
                    "The chronicle holds no version of " + sourceId + "/" + externalId + ".");
        }
        Instant start = from != null ? from : c.getStartedAt() != null ? c.getStartedAt().minus(Duration.ofDays(3650))
                : Instant.EPOCH;
        Instant end = to != null ? to : c.getStoppedAt() != null ? c.getStoppedAt() : clock.instant();
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        where.add("v.chronicle_id = " + p.text(c.getId()));
        where.add("v.source_id = " + p.text(sourceId));
        where.add("v.external_id = " + p.text(externalId));
        where.add("v.observed_at >= " + p.instant(start));
        where.add("v.observed_at <= " + p.instant(end));
        readerWhere(where, reader, p);
        String[] kind = {null};
        int[] scanned = {0};
        List<List<TrackPoint>> segments = new ArrayList<>();
        List<TrackPoint> current = new ArrayList<>();
        long[] lastTime = {Long.MIN_VALUE};
        named.query("SELECT DISTINCT ON (v.seq) v.seq, v.kind, v.observed_at, v.tombstone,"
                + " ST_X(ST_PointOnSurface(v.geom)) AS lon, ST_Y(ST_PointOnSurface(v.geom)) AS lat FROM "
                + ChronicleDdl.TABLE + " v WHERE " + String.join(" AND ", where) + " ORDER BY v.seq LIMIT "
                + p.add(TrackService.MAX_SCAN), p.source(), rs -> {
                    scanned[0]++;
                    kind[0] = rs.getString("kind");
                    Object lon = rs.getObject("lon");
                    long time = rs.getObject("observed_at", OffsetDateTime.class).toInstant().toEpochMilli();
                    boolean tombstone = rs.getBoolean("tombstone");
                    boolean gap = lastTime[0] != Long.MIN_VALUE
                            && time - lastTime[0] > TrackService.MIN_DEFAULT_GAP.toMillis();
                    if ((tombstone || gap || lon == null) && !current.isEmpty()) {
                        segments.add(new ArrayList<>(current));
                        current.clear();
                    }
                    if (!tombstone && lon != null) {
                        current.add(new TrackPoint(rs.getDouble("lon"), rs.getDouble("lat"), time, false,
                                new Double[0]));
                        lastTime[0] = time;
                    }
                });
        if (!current.isEmpty()) {
            segments.add(current);
        }
        if (scanned[0] == 0) {
            throw new ai.foresignal.api.error.NotFoundException(ErrorCodes.UNKNOWN_RECORD,
                    "The chronicle holds no version of " + sourceId + "/" + externalId + ".");
        }
        List<List<TrackPoint>> split = TrackSimplifier.splitAntimeridian(segments);
        TrackSimplifier.Result simplified = TrackSimplifier.simplify(split, TrackSimplifier.autoTolerance(split),
                maxPoints);
        List<List<double[]>> coordinates = new ArrayList<>();
        List<List<Long>> times = new ArrayList<>();
        for (List<TrackPoint> segment : simplified.segments()) {
            List<double[]> coords = new ArrayList<>();
            List<Long> ts = new ArrayList<>();
            for (TrackPoint pt : segment) {
                coords.add(new double[] {pt.lon(), pt.lat()});
                ts.add(pt.time());
            }
            if (coords.size() == 1) {
                coords.add(coords.getFirst());
                ts.add(ts.getFirst());
            }
            coordinates.add(coords);
            times.add(ts);
        }
        return new TrackResult(sourceId, externalId, kind[0], start, end, coordinates, times, Map.of(), false,
                scanned[0], simplified.points(), simplified.toleranceMeters(), simplified.segments().size(), false,
                false, scanned[0] >= TrackService.MAX_SCAN, c.getCaptureToSeq() == null ? 0 : c.getCaptureToSeq(),
                false, reader.excludedSources().isEmpty());
    }

    // ------------------------------------------------------------------------- series

    /**
     * The captured points of a series ref.
     *
     * @param c the chronicle
     * @param refText the series ref
     * @param from the first period start, or null
     * @param to the end (exclusive), or null
     * @return the {@code ChronicleSeries}
     */
    public Map<String, Object> series(ChronicleEntity c, String refText, Instant from, Instant to) {
        SeriesRef ref = SeriesRef.parse(refText);
        String text = ref.format();
        SqlParams p = new SqlParams();
        StringBuilder sql = new StringBuilder("SELECT period, period_start, period_end, value, unit, status,"
                + " point_version, known_at, coverage_ok, backfilled FROM chronicle_series_point WHERE chronicle_id = "
                + p.text(c.getId()) + " AND ref = " + p.text(text));
        if (from != null) {
            sql.append(" AND period_start >= ").append(p.instant(from));
        }
        if (to != null) {
            sql.append(" AND period_start < ").append(p.instant(to));
        }
        sql.append(" ORDER BY period_start LIMIT 20000");
        boolean[] backfilled = {false};
        List<Map<String, Object>> points = named.query(sql.toString(), p.source(), (rs, n) -> {
            Map<String, Object> pt = new LinkedHashMap<>();
            OffsetDateTime ps = rs.getObject("period_start", OffsetDateTime.class);
            Instant start = ps == null ? null : ps.toInstant();
            pt.put("period", rs.getString("period") == null ? String.valueOf(start) : rs.getString("period"));
            pt.put("periodStart", start == null ? null : start.toString());
            pt.put("periodEnd", instant(rs.getObject("period_end", OffsetDateTime.class)));
            Object value = rs.getObject("value");
            pt.put("value", value instanceof Number num ? num.doubleValue() : null);
            pt.put("unit", rs.getString("unit") == null ? "" : rs.getString("unit"));
            pt.put("status", rs.getString("status") == null ? "final" : rs.getString("status"));
            Object version = rs.getObject("point_version");
            if (version instanceof Number num) {
                pt.put("version", num.intValue());
            }
            String known = instant(rs.getObject("known_at", OffsetDateTime.class));
            if (known != null) {
                pt.put("knownAt", known);
            }
            Object coverage = rs.getObject("coverage_ok");
            if (coverage instanceof Boolean b) {
                pt.put("coverageOk", b);
            }
            backfilled[0] |= rs.getBoolean("backfilled");
            return pt;
        });
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chronicleId", c.getId());
        out.put("ref", text);
        out.put("from", from == null ? null : from.toString());
        out.put("to", to == null ? null : to.toString());
        out.put("points", points);
        out.put("backfilled", backfilled[0]);
        return out;
    }

    // ------------------------------------------------------------------------- summary

    /**
     * The chronicle's summary ({@code ChronicleSummary}).
     *
     * @param c the chronicle
     * @param caller the caller (envelopes are resolved as it may see them)
     * @param reader the reader
     * @return the summary
     */
    public Map<String, Object> summary(ChronicleEntity c, ObjectCaller caller, Reader reader) {
        boolean leaf = storage.leafExists(c.getId());
        List<Map<String, Object>> layerRows = new ArrayList<>();
        long totalRows = 0;
        for (ChronicleLayerEntity l : chronicles.liveLayers(c.getId())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("layerId", l.getId());
            row.put("name", l.getName());
            row.put("rows", l.getRowsCaptured());
            totalRows += l.getRowsCaptured();
            List<Map<String, Object>> series = new ArrayList<>();
            SqlParams p = new SqlParams();
            String sql = CaptureMode.ROLLUP_ONLY.equals(CaptureMode.fromMap(l.getCapture()).mode())
                    ? "SELECT date_trunc('day', hour_start) AS t, sum(versions) AS n FROM chronicle_rollup WHERE"
                            + " chronicle_id = " + p.text(c.getId()) + " AND layer_id = " + p.text(l.getId())
                            + " GROUP BY 1 ORDER BY 1"
                    : leaf ? "SELECT date_trunc('day', v.observed_at) AS t, count(*) AS n FROM " + ChronicleDdl.TABLE
                            + " v WHERE v.chronicle_id = " + p.text(c.getId()) + " AND v.layer_id = " + p.text(l.getId())
                            + excluded(reader, p) + " GROUP BY 1 ORDER BY 1" : null;
            if (sql != null) {
                named.query(sql, p.source(), rs -> {
                    Map<String, Object> b = new LinkedHashMap<>();
                    b.put("t", instant(rs.getObject("t", OffsetDateTime.class)));
                    b.put("count", rs.getLong("n"));
                    series.add(b);
                });
            }
            row.put("series", series);
            layerRows.add(row);
        }
        List<Map<String, Object>> top = new ArrayList<>();
        if (leaf) {
            SqlParams p = new SqlParams();
            named.query("SELECT v.source_id, v.external_id, count(*) AS n, (array_agg(COALESCE(v.payload ->> 'title',"
                    + " v.payload ->> 'name', v.payload ->> 'callsign', v.payload ->> 'place') ORDER BY v.seq DESC))[1]"
                    + " AS title FROM " + ChronicleDdl.TABLE + " v WHERE v.chronicle_id = " + p.text(c.getId())
                    + excluded(reader, p) + " GROUP BY v.source_id, v.external_id ORDER BY n DESC, v.source_id,"
                    + " v.external_id LIMIT 10", p.source(), rs -> {
                        Map<String, Object> e = new LinkedHashMap<>();
                        e.put("ref", ObjectRef.record(rs.getString("source_id"), rs.getString("external_id"), null)
                                .format());
                        e.put("title", rs.getString("title") == null ? rs.getString("external_id")
                                : rs.getString("title"));
                        e.put("count", rs.getLong("n"));
                        top.add(e);
                    });
        }
        ResolveContext ctx = new ResolveContext(caller, null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chronicleId", c.getId());
        Map<String, Object> span = new LinkedHashMap<>();
        span.put("start", (c.getStartedAt() != null ? c.getStartedAt() : c.getCreatedAt()).toString());
        span.put("end", c.getStoppedAt() == null ? null : c.getStoppedAt().toString());
        out.put("span", span);
        out.put("layers", layerRows);
        out.put("topEntities", top);
        out.put("situations", envelopes(c.getSituationIds().stream().map(id -> "situation:" + id).toList(), ctx));
        out.put("hypothesisSets", envelopes(c.getHypothesisSetIds().stream().map(id -> "hypothesis-set:" + id)
                .toList(), ctx));
        out.put("forecasts", envelopes(questions(c), ctx));
        long[] usage = storage.usage(c.getId());
        Map<String, Object> storageView = new LinkedHashMap<>();
        storageView.put("bytesUsed", usage == null ? c.getBytesUsed() : usage[0]);
        storageView.put("sizeCapBytes", usage == null ? c.getSizeCapBytes() : usage[1]);
        storageView.put("rows", Math.max(totalRows, c.getRowsCaptured()));
        out.put("storage", storageView);
        out.put("gaps", gaps(c.getId()));
        return out;
    }

    private String excluded(Reader reader, SqlParams p) {
        List<String> where = new ArrayList<>();
        readerWhere(where, reader, p);
        return where.isEmpty() ? "" : " AND " + String.join(" AND ", where);
    }

    private List<String> questions(ChronicleEntity c) {
        if (c.getSituationIds().isEmpty() && c.getHypothesisSetIds().isEmpty()) {
            return List.of();
        }
        SqlParams p = new SqlParams();
        return named.queryForList("SELECT 'question:' || id FROM forecast_question WHERE situation_id = ANY("
                + p.textArray(c.getSituationIds()) + ") OR hypothesis_set_id = ANY("
                + p.textArray(c.getHypothesisSetIds()) + ") ORDER BY created_at DESC LIMIT 50", p.source(),
                String.class);
    }

    private List<ObjectEnvelope> envelopes(List<String> refs, ResolveContext ctx) {
        List<ObjectRef> parsed = new ArrayList<>();
        for (String r : refs) {
            if (ObjectRef.isValid(r)) {
                parsed.add(ObjectRef.parse(r));
            }
        }
        if (parsed.isEmpty()) {
            return List.of();
        }
        try {
            return new ArrayList<>(objects.resolveRefs(parsed, ctx).values());
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    // ------------------------------------------------------------------------- helpers

    private Map<String, Object> parse(String text) {
        if (text == null) {
            return new LinkedHashMap<>();
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return new LinkedHashMap<>();
        }
    }

    private static String instant(OffsetDateTime t) {
        return t == null ? null : t.toInstant().toString();
    }

    private static String reasonName(short code) {
        try {
            return ai.foresignal.ingest.PersistReason.fromCode(code).name();
        } catch (IllegalArgumentException e) {
            return String.valueOf(code);
        }
    }
}
