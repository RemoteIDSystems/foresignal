package ai.foresignal.ogc;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.ogc.cql2.Cql2;
import ai.foresignal.ogc.cql2.Cql2Queryables;
import ai.foresignal.ogc.cql2.Cql2SqlTranslator;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicencePolicy;
import ai.foresignal.provenance.LicencePolicyService;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.SqlParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * OGC API &ndash; Features over Foresignal records (prompt 112): one collection per kind for current state
 * ({@code record_head ⋈ record_live}, Core-Data-Model §12) and one {@code {kind}-history} collection per kind
 * whose items are versions ({@code record_version}).
 *
 * <p>Every query applies the caller's excluded sources (private sources, 039) and {@link ReadRestriction}
 * (035) through {@link RestrictionClamp}, prunes sources the license policy excludes before the query and
 * filters each row by its resolved terms after it (109). Paging is keyset: {@code (source_id, external_id)}
 * for current collections and {@code seq} for history, encoded with {@link CursorCodec} and bound to the
 * query's fingerprint.
 */
@Service
public class OgcFeatureService {

    /** The suffix of history collection ids. */
    public static final String HISTORY_SUFFIX = "-history";

    /** Collection extents are recomputed at most this often. */
    static final Duration EXTENT_TTL = Duration.ofMinutes(5);

    /** {@code numberMatched} is returned only up to this count. */
    public static final int MAX_COUNT = 100_000;

    static final String CURRENT_FROM = " FROM record_head h JOIN record_live l ON l.source_id = h.source_id"
            + " AND l.external_id = h.external_id";

    static final String CURRENT_PAYLOAD = "jsonb_strip_nulls(h.payload || l.kinematic)";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final LicenceResolver licenceResolver;
    private final LicencePolicyService licencePolicies;
    private final AttributionService attributions;
    private final JsonMapper json;
    private final Clock clock;

    private volatile ExtentCache extentCache;

    /** Creates the service. */
    public OgcFeatureService(NamedParameterJdbcTemplate jdbc, SourceRegistry registry, LicenceResolver licenceResolver,
            LicencePolicyService licencePolicies, AttributionService attributions, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.licenceResolver = licenceResolver;
        this.licencePolicies = licencePolicies;
        this.attributions = attributions;
        this.json = json;
        this.clock = clock;
    }

    // -------------------------------------------------------------------------------- collections

    /**
     * A feature collection as seen by one caller.
     *
     * @param id the collection id (a kind, or {@code {kind}-history})
     * @param kind the kind
     * @param history true for the history collection
     * @param sources the visible sources declaring the kind, in id order
     * @param bbox the spatial extent {@code [w, s, e, n]}, or null when nothing has a geometry
     * @param from the start of the temporal extent, or null
     * @param to the end of the temporal extent, or null (open)
     * @param attribution the aggregated attribution text of the sources (109), or null
     */
    public record FeatureCollection(String id, String kind, boolean history, List<String> sources, double[] bbox,
            Instant from, Instant to, String attribution) {}

    /**
     * The feature collections the caller may see: every declared, non-hidden kind with at least one source that
     * is neither excluded for the caller nor outside its restriction, current state first, then history.
     *
     * @param excludedSources the caller's excluded sources ({@code ReadCallerResolver.excludedSources})
     * @param restriction the caller's restriction
     */
    public List<FeatureCollection> collections(Set<String> excludedSources, ReadRestriction restriction) {
        List<FeatureCollection> current = new ArrayList<>();
        List<FeatureCollection> history = new ArrayList<>();
        ExtentCache cache = extents();
        Instant historyFrom = cache.historyFrom();
        for (String kind : new TreeSet<>(registry.kinds())) {
            List<SourceDescriptor> sources = visibleSources(kind, excludedSources, restriction);
            if (sources.isEmpty() || !restriction.allowsKind(kind)) {
                continue;
            }
            double[] bbox = null;
            Instant from = null;
            Instant to = null;
            for (SourceDescriptor d : sources) {
                Extent e = cache.extents().get(kind + "\u0000" + d.id());
                if (e == null) {
                    continue;
                }
                bbox = e.bbox() == null ? bbox : union(bbox, e.bbox());
                from = min(from, e.from());
                to = max(to, e.to());
            }
            List<String> ids = sources.stream().map(SourceDescriptor::id).toList();
            String attribution = attributions.layerText(sources);
            current.add(new FeatureCollection(kind, kind, false, ids, bbox, from, to, attribution));
            history.add(new FeatureCollection(kind + HISTORY_SUFFIX, kind, true, ids, bbox, historyFrom, null,
                    attribution));
        }
        List<FeatureCollection> out = new ArrayList<>(current);
        out.addAll(history);
        return out;
    }

    /** One feature collection by id, if the caller may see it. */
    public Optional<FeatureCollection> collection(String id, Set<String> excludedSources, ReadRestriction restriction) {
        if (id == null || !isFeatureCollectionId(id)) {
            return Optional.empty();
        }
        return collections(excludedSources, restriction).stream().filter(c -> c.id().equals(id)).findFirst();
    }

    /** True when {@code id} names a declared kind or its history collection. */
    public boolean isFeatureCollectionId(String id) {
        if (id == null) {
            return false;
        }
        String kind = id.endsWith(HISTORY_SUFFIX) && !registry.kinds().contains(id)
                ? id.substring(0, id.length() - HISTORY_SUFFIX.length()) : id;
        return registry.kinds().contains(kind);
    }

    /** The collection, or a 404 {@code NOT_FOUND}. */
    public FeatureCollection require(String id, Set<String> excludedSources, ReadRestriction restriction) {
        return collection(id, excludedSources, restriction).orElseThrow(() -> new NotFoundException(
                ErrorCodes.NOT_FOUND, "Unknown collection " + id + "."));
    }

    // ------------------------------------------------------------------------------- queryables

    /**
     * The queryables of a kind: the built-ins plus every top-level payload key a source of the kind declares in
     * {@code fieldSpecs} for it (the first declaration's type wins when sources disagree).
     */
    public Cql2Queryables queryables(String kind, Collection<String> sourceIds) {
        Map<String, FieldType> payload = new LinkedHashMap<>();
        for (String sourceId : sourceIds) {
            SourceDescriptor d = registry.descriptor(sourceId);
            for (Map.Entry<String, FieldSpec> e : d.fieldSpecs().entrySet()) {
                if (e.getValue().appliesTo(kind) && !Cql2Queryables.isBuiltIn(e.getKey())) {
                    payload.putIfAbsent(e.getKey(), e.getValue().type());
                }
            }
        }
        return new Cql2Queryables(payload);
    }

    /** The field specs behind a kind's payload queryables (for labels and units in the JSON Schema). */
    public Map<String, FieldSpec> fieldSpecs(String kind, Collection<String> sourceIds) {
        Map<String, FieldSpec> out = new LinkedHashMap<>();
        for (String sourceId : sourceIds) {
            for (Map.Entry<String, FieldSpec> e : registry.descriptor(sourceId).fieldSpecs().entrySet()) {
                if (e.getValue().appliesTo(kind) && !Cql2Queryables.isBuiltIn(e.getKey())) {
                    out.putIfAbsent(e.getKey(), e.getValue());
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------------------- items

    /**
     * An items query.
     *
     * @param collection the collection
     * @param limit the page size (1 to 10,000)
     * @param bbox the bbox filter, or null
     * @param datetime the datetime filter, or null
     * @param filter the parsed CQL2 filter, or null
     * @param sources a {@code source} filter, or null
     * @param cursor the opaque cursor, or null
     * @param count true to compute {@code numberMatched}
     * @param excludedSources the caller's excluded sources
     * @param restriction the caller's restriction
     * @param policy the caller's license policy
     * @param canonical the canonical form of the query (for the cursor fingerprint)
     */
    public record ItemsQuery(FeatureCollection collection, int limit, BboxFilter bbox, OgcDatetime datetime,
            Cql2.Expr filter, List<String> sources, String cursor, boolean count, Set<String> excludedSources,
            ReadRestriction restriction, LicencePolicy policy, String canonical) {}

    /**
     * A feature as read.
     *
     * @param id the feature id
     * @param row the row (for license resolution)
     * @param geometry the GeoJSON geometry text, or null
     * @param properties the properties: merged payload plus the built-ins
     */
    public record Feature(String id, RecordRow row, String geometry, Map<String, Object> properties) {}

    /**
     * A page of features.
     *
     * @param features the features, license-filtered
     * @param nextCursor the next page's cursor, or null on the last page
     * @param numberMatched the matched count when requested and at most {@link #MAX_COUNT}, else null
     * @param attributions the aggregated attributions of the returned features
     * @param licenceFiltered how many rows the license policy removed, or null when none
     */
    public record ItemsPage(List<Feature> features, String nextCursor, Long numberMatched,
            List<AttributionService.Attribution> attributions, Integer licenceFiltered) {}

    /** Runs an items query. */
    public ItemsPage items(ItemsQuery q) {
        FeatureCollection c = q.collection();
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        String fingerprint = CursorCodec.fingerprint(q.canonical() + "|" + q.restriction().canonical());
        String sql;
        if (c.history()) {
            Cursor.Seq cursor = CursorCodec.decode(q.cursor(), Cursor.Seq.class, fingerprint);
            historyWhere(where, q, p);
            if (cursor != null) {
                where.add("v.seq > " + p.bigint(cursor.q()));
            }
            sql = historySelect() + " WHERE " + String.join(" AND ", where) + " ORDER BY v.seq LIMIT "
                    + p.add(q.limit() + 1);
        } else {
            Cursor.ById cursor = CursorCodec.decode(q.cursor(), Cursor.ById.class, fingerprint);
            currentWhere(where, q, p);
            if (cursor != null) {
                where.add("(h.source_id, h.external_id) > (" + p.text(cursor.s()) + ", " + p.text(cursor.e()) + ")");
            }
            sql = currentSelect() + " WHERE " + String.join(" AND ", where)
                    + " ORDER BY h.source_id, h.external_id LIMIT " + p.add(q.limit() + 1);
        }
        List<RecordRow> rows = jdbc.query(sql, p.source(), (rs, i) -> row(rs, c.history()));
        String next = null;
        if (rows.size() > q.limit()) {
            rows = new ArrayList<>(rows.subList(0, q.limit()));
            RecordRow last = rows.getLast();
            next = c.history() ? CursorCodec.encode(new Cursor.Seq(last.seq(), null), fingerprint)
                    : CursorCodec.encode(new Cursor.ById(last.sourceId(), last.externalId(), null), fingerprint);
        }
        Long matched = q.count() ? count(q) : null;
        return page(rows, c, q.policy(), next, matched);
    }

    /**
     * One feature of a collection, if it exists, is visible and passes the license policy.
     *
     * @param featureId {@code {sourceId}:{externalId}} or, for history, {@code {sourceId}:{externalId}:{version}}
     */
    public Optional<ItemsPage> item(FeatureCollection c, String featureId, Set<String> excludedSources,
            ReadRestriction restriction, LicencePolicy policy) {
        int colon = featureId == null ? -1 : featureId.indexOf(':');
        if (colon <= 0 || colon == featureId.length() - 1) {
            return Optional.empty();
        }
        String sourceId = featureId.substring(0, colon);
        String rest = featureId.substring(colon + 1);
        if (!c.sources().contains(sourceId) || excludedSources.contains(sourceId)) {
            return Optional.empty();
        }
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        String sql;
        if (c.history()) {
            int last = rest.lastIndexOf(':');
            if (last <= 0) {
                return Optional.empty();
            }
            int version;
            try {
                version = Integer.parseInt(rest.substring(last + 1));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
            where.add("v.source_id = " + p.text(sourceId));
            where.add("v.external_id = " + p.text(rest.substring(0, last)));
            where.add("v.version = CAST(" + p.add(version) + " AS integer)");
            where.add("v.kind = " + p.text(c.kind()));
            where.add("NOT v.tombstone");
            RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", restriction, p);
            sql = historySelect() + " WHERE " + String.join(" AND ", where) + " ORDER BY v.seq DESC LIMIT 1";
        } else {
            where.add("h.source_id = " + p.text(sourceId));
            where.add("h.external_id = " + p.text(rest));
            where.add("h.kind = " + p.text(c.kind()));
            where.add("NOT h.tombstone");
            where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(clock.instant()) + ")");
            RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, restriction, p);
            sql = currentSelect() + " WHERE " + String.join(" AND ", where);
        }
        List<RecordRow> rows = jdbc.query(sql, p.source(), (rs, i) -> row(rs, c.history()));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        ItemsPage page = page(rows, c, policy, null, null);
        return page.features().isEmpty() ? Optional.empty() : Optional.of(page);
    }

    private ItemsPage page(List<RecordRow> rows, FeatureCollection c, LicencePolicy policy, String next,
            Long matched) {
        Map<Long, LicenceResolver.Resolution> resolutions = rows.isEmpty() ? Map.of() : licenceResolver.resolveAll(rows);
        List<RecordRow> kept = new ArrayList<>();
        int filtered = 0;
        for (RecordRow r : rows) {
            LicenceResolver.Resolution resolution = resolutions.get(r.seq());
            if (policy != null && !policy.isEmpty() && resolution != null && policy.excludes(resolution.terms())) {
                filtered++;
            } else {
                kept.add(r);
            }
        }
        List<Feature> features = new ArrayList<>();
        for (RecordRow r : kept) {
            features.add(feature(r, c));
        }
        List<AttributionService.Attribution> attrs = attributions.aggregate(kept, resolutions);
        return new ItemsPage(features, next, matched, attrs, filtered == 0 ? null : filtered);
    }

    private Feature feature(RecordRow r, FeatureCollection c) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (r.payload() != null) {
            properties.putAll(json.readValue(r.payload(), MAP));
        }
        String title = r.title();
        if (c.history()) {
            SourceDescriptor d = registry.contains(r.sourceId()) ? registry.descriptor(r.sourceId()) : null;
            title = TitleResolver.resolve(d == null ? null : d.titleField(), properties);
        }
        properties.put("title", title);
        properties.put("sourceId", r.sourceId());
        properties.put("observedAt", r.observedAt() == null ? null : r.observedAt().toString());
        properties.put("eventTime", r.eventTime() == null ? null : r.eventTime().toString());
        properties.put("version", r.version());
        String id = r.sourceId() + ":" + r.externalId() + (c.history() ? ":" + r.version() : "");
        return new Feature(id, r, r.geometry(), properties);
    }

    private Long count(ItemsQuery q) {
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        String from;
        if (q.collection().history()) {
            historyWhere(where, q, p);
            from = " FROM record_version v";
        } else {
            currentWhere(where, q, p);
            from = CURRENT_FROM;
        }
        String sql = "SELECT count(*) FROM (SELECT 1" + from + " WHERE " + String.join(" AND ", where) + " LIMIT "
                + p.add(MAX_COUNT + 1) + ") x";
        Long n = jdbc.queryForObject(sql, p.source(), Long.class);
        return n == null || n > MAX_COUNT ? null : n;
    }

    private void currentWhere(List<String> where, ItemsQuery q, SqlParams p) {
        FeatureCollection c = q.collection();
        where.add("h.kind = " + p.text(c.kind()));
        where.add("NOT h.tombstone");
        where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(clock.instant()) + ")");
        where.add("h.source_id = ANY(" + p.textArray(allowedSources(q)) + ")");
        RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", CURRENT_PAYLOAD, q.restriction(), p);
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause("l.geom", q.bbox(), p));
        }
        if (q.datetime() != null) {
            timeBounds(where, "COALESCE(h.event_time, l.observed_at)", q.datetime(), p);
        }
        if (q.filter() != null) {
            where.add(Cql2SqlTranslator.translate(q.filter(), queryables(c.kind(), c.sources()),
                    new Cql2SqlTranslator.Columns("h.source_id", "h.title", "l.observed_at", "h.event_time",
                            "h.version", CURRENT_PAYLOAD, "l.geom"), p));
        }
    }

    private void historyWhere(List<String> where, ItemsQuery q, SqlParams p) {
        FeatureCollection c = q.collection();
        where.add("v.kind = " + p.text(c.kind()));
        where.add("NOT v.tombstone");
        where.add("v.source_id = ANY(" + p.textArray(allowedSources(q)) + ")");
        RestrictionClamp.addTo(where, "v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p);
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause("v.geom", q.bbox(), p));
        }
        if (q.datetime() != null) {
            timeBounds(where, "v.observed_at", q.datetime(), p);
        }
        if (q.filter() != null) {
            String title = versionTitle(allowedSources(q), p);
            where.add(Cql2SqlTranslator.translate(q.filter(), queryables(c.kind(), c.sources()),
                    new Cql2SqlTranslator.Columns("v.source_id", title, "v.observed_at", "v.event_time", "v.version",
                            "v.payload", "v.geom"), p));
        }
    }

    /** The collection's sources narrowed by {@code source=} and pruned by the license policy. */
    private List<String> allowedSources(ItemsQuery q) {
        Set<String> excluded = licencePolicies.withExcluded(q.excludedSources(), q.policy());
        List<String> out = new ArrayList<>();
        for (String s : q.collection().sources()) {
            if (!excluded.contains(s) && (q.sources() == null || q.sources().contains(s))) {
                out.add(s);
            }
        }
        return out;
    }

    private static void timeBounds(List<String> where, String column, OgcDatetime dt, SqlParams p) {
        if (dt.from() != null) {
            where.add(column + " >= " + p.instant(dt.from()));
        }
        if (dt.to() != null) {
            where.add(column + " <= " + p.instant(dt.to()));
        }
    }

    /**
     * The SQL title of a history version: each source's own title field (versions have no title column),
     * {@code payload.title} for a source without one. One shared field is a plain expression, several a
     * {@code CASE} on the source.
     */
    private String versionTitle(List<String> sources, SqlParams p) {
        String shared = sharedTitleField(sources);
        if (shared != null || sources.size() <= 1) {
            return "(v.payload ->> " + p.text(shared == null ? "title" : shared) + ")";
        }
        StringBuilder sql = new StringBuilder("(CASE");
        for (String s : sources) {
            String field = registry.descriptor(s).titleField();
            sql.append(" WHEN v.source_id = ").append(p.text(s)).append(" THEN v.payload ->> ")
                    .append(p.text(field == null ? "title" : field));
        }
        return sql.append(" ELSE v.payload ->> ").append(p.text("title")).append(" END)").toString();
    }

    private String sharedTitleField(List<String> sources) {
        Set<String> fields = new LinkedHashSet<>();
        for (String s : sources) {
            fields.add(registry.descriptor(s).titleField());
        }
        return fields.size() == 1 ? fields.iterator().next() : null;
    }

    private static String currentSelect() {
        return "SELECT h.source_id, h.external_id, h.kind, h.version, h.seq, l.observed_at, h.event_time, h.title,"
                + " ST_AsGeoJSON(l.geom, 7) AS geom_out, (" + CURRENT_PAYLOAD + ")::text AS payload_out"
                + CURRENT_FROM;
    }

    private static String historySelect() {
        return "SELECT v.source_id, v.external_id, v.kind, v.version, v.seq, v.observed_at, v.event_time,"
                + " NULL::text AS title, ST_AsGeoJSON(v.geom, 7) AS geom_out, v.payload::text AS payload_out"
                + " FROM record_version v";
    }

    private static RecordRow row(ResultSet rs, boolean history) throws SQLException {
        return new RecordRow(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                rs.getInt("version"), rs.getLong("seq"), instant(rs.getTimestamp("observed_at")), null,
                instant(rs.getTimestamp("event_time")), null, null, false, rs.getString("title"),
                rs.getString("geom_out"), rs.getString("payload_out"), null, null, null, 0, null, null, null, null,
                null, null, null, null, null, null);
    }

    private static Instant instant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    // ------------------------------------------------------------------------------------ extents

    private List<SourceDescriptor> visibleSources(String kind, Set<String> excludedSources, ReadRestriction r) {
        List<SourceDescriptor> out = new ArrayList<>();
        for (SourceDescriptor d : registry.descriptors()) {
            if (d.kinds().contains(kind) && !d.hiddenKinds().contains(kind) && !excludedSources.contains(d.id())
                    && r.allowsSource(d.id())) {
                out.add(d);
            }
        }
        return out;
    }

    /** The extent of one kind of one source. */
    record Extent(double[] bbox, Instant from, Instant to) {}

    /** All extents, computed together and kept for {@link #EXTENT_TTL}. */
    record ExtentCache(Instant at, Map<String, Extent> extents, Instant historyFrom) {}

    private ExtentCache extents() {
        ExtentCache cache = extentCache;
        Instant now = clock.instant();
        if (cache != null && Duration.between(cache.at(), now).compareTo(EXTENT_TTL) < 0) {
            return cache;
        }
        Map<String, Extent> map = new LinkedHashMap<>();
        jdbc.query("SELECT h.kind, h.source_id, ST_XMin(e) AS xmin, ST_YMin(e) AS ymin, ST_XMax(e) AS xmax,"
                + " ST_YMax(e) AS ymax, lo, hi FROM (SELECT h.kind, h.source_id, ST_Extent(l.geom) AS e,"
                + " min(l.observed_at) AS lo, max(l.observed_at) AS hi" + CURRENT_FROM
                + " WHERE NOT h.tombstone GROUP BY h.kind, h.source_id) h", Map.of(), rs -> {
                    double[] bbox = rs.getObject("xmin") == null ? null : new double[] {rs.getDouble("xmin"),
                        rs.getDouble("ymin"), rs.getDouble("xmax"), rs.getDouble("ymax")};
                    map.put(rs.getString("kind") + "\u0000" + rs.getString("source_id"),
                            new Extent(bbox, instant(rs.getTimestamp("lo")), instant(rs.getTimestamp("hi"))));
                });
        Timestamp first = jdbc.queryForObject("SELECT min(minute) FROM seq_clock", Map.of(), Timestamp.class);
        ExtentCache fresh = new ExtentCache(now, Map.copyOf(map), instant(first));
        extentCache = fresh;
        return fresh;
    }

    /** Drops the cached extents (tests). */
    void clearExtents() {
        extentCache = null;
    }

    private static double[] union(double[] a, double[] b) {
        if (a == null) {
            return b.clone();
        }
        return new double[] {Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.max(a[2], b[2]), Math.max(a[3], b[3])};
    }

    private static Instant min(Instant a, Instant b) {
        return a == null ? b : b == null ? a : a.isBefore(b) ? a : b;
    }

    private static Instant max(Instant a, Instant b) {
        return a == null ? b : b == null ? a : a.isAfter(b) ? a : b;
    }
}
