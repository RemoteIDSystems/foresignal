package ai.foresignal.security.restrict;

import ai.foresignal.store.query.PgArrays;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.stream.Bbox;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The one place that turns a {@link ReadRestriction} into query predicates and in-memory tests
 * (prompt 035). Every {@code @RestrictedRead} path goes through it.
 *
 * <p><b>Intersect, never union.</b> A request filter and the restriction are ANDed: an absent request
 * filter becomes the restriction, and an explicit filter disjoint from it yields nothing. A
 * single-record read outside the restriction is answered {@code 404} by its caller, never
 * {@code 403}, so existence does not leak.
 *
 * <p>Spatial restrictions use the {@linkplain ReadRestriction#region() region}: the SQL form is a
 * bbox {@code &&} prefilter followed by {@code ST_Intersects}; a row without a geometry fails.
 *
 * <p>Prompt 036 adds the request filter ({@link ReadRestriction#filter()}: places, saved views and
 * watchlists), rendered with the payload expression of the path when the caller passes one (a
 * filter with payload conditions fails closed without it), and {@code NOT a.sensitive} on alert
 * rows ({@link #ALERT_GEOMETRY_SQL}) for principals that exclude sensitive alerts.
 */
public final class RestrictionClamp {

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * The SQL geometry of an alert row aliased {@code a}: its {@code record_snapshot} geometry, or
     * null when the snapshot has none (alerts carry no geometry column of their own).
     */
    public static final String ALERT_GEOMETRY_SQL = "(CASE WHEN jsonb_typeof(a.record_snapshot -> 'geometry') = 'object'"
            + " THEN ST_SetSRID(ST_GeomFromGeoJSON((a.record_snapshot -> 'geometry')::text), 4326) END)";

    private RestrictionClamp() {}

    // ------------------------------------------------------------------------- request filters

    /**
     * The source filter a request may use under {@code r}: the requested ids that the restriction
     * allows, or, when the request names none, the restriction's own ids (null = no filter at all).
     * An empty result means the request and the restriction are disjoint: nothing matches.
     *
     * @param requested the request's source ids (empty or null for all)
     */
    public static List<String> clampSources(Collection<String> requested, ReadRestriction r) {
        return clampSet(requested, r.sources());
    }

    /** {@link #clampSources} for kinds. */
    public static List<String> clampKinds(Collection<String> requested, ReadRestriction r) {
        return clampSet(requested, r.kinds());
    }

    private static List<String> clampSet(Collection<String> requested, Set<String> allowed) {
        boolean asked = requested != null && !requested.isEmpty();
        if (allowed == null) {
            return asked ? List.copyOf(new TreeSet<>(requested)) : null;
        }
        if (!asked) {
            return List.copyOf(new TreeSet<>(allowed));
        }
        List<String> out = new ArrayList<>();
        for (String id : new TreeSet<>(requested)) {
            if (allowed.contains(id)) {
                out.add(id);
            }
        }
        return List.copyOf(out);
    }

    /**
     * True when a request filtering on {@code sources} and {@code kinds} (empty for all) is disjoint
     * from {@code r}, so the answer is empty without asking the database.
     */
    public static boolean disjoint(Collection<String> sources, Collection<String> kinds, ReadRestriction r) {
        List<String> s = clampSources(sources, r);
        List<String> k = clampKinds(kinds, r);
        return (s != null && s.isEmpty()) || (k != null && k.isEmpty());
    }

    // ------------------------------------------------------------------------- SQL

    /**
     * The SQL predicate for rows aliased {@code alias} (columns {@code source_id}, {@code kind} and
     * {@code geom}), or empty when {@code r} restricts no data.
     */
    public static Optional<String> sqlPredicate(String alias, ReadRestriction r, SqlParams p) {
        return sqlPredicate(alias + ".source_id", alias + ".kind", alias + ".geom", null, r, p::add);
    }

    /**
     * The SQL predicate over explicit columns, binding through {@code p}; a null column skips that
     * member (a null {@code geomColumn} with a spatial restriction fails closed: {@code FALSE}).
     */
    public static Optional<String> sqlPredicate(String sourceColumn, String kindColumn, String geomColumn,
            ReadRestriction r, SqlParams p) {
        return sqlPredicate(sourceColumn, kindColumn, geomColumn, null, r, p::add);
    }

    /**
     * The SQL predicate over explicit columns and the row's payload expression (prompt 036), so a
     * request filter with payload conditions can be rendered; binding through {@code p}.
     */
    public static Optional<String> sqlPredicate(String sourceColumn, String kindColumn, String geomColumn,
            String payloadExpression, ReadRestriction r, SqlParams p) {
        return sqlPredicate(sourceColumn, kindColumn, geomColumn, payloadExpression, r, p::add);
    }

    /**
     * The SQL predicate over explicit columns, binding named parameters {@code <prefix>0},
     * {@code <prefix>1}, … into {@code params}, for query builders that do not use {@link SqlParams}.
     */
    public static Optional<String> sqlPredicate(String sourceColumn, String kindColumn, String geomColumn,
            ReadRestriction r, MapSqlParameterSource params, String prefix) {
        int[] n = {0};
        return sqlPredicate(sourceColumn, kindColumn, geomColumn, null, r, value -> {
            String name = prefix + n[0]++;
            params.addValue(name, value);
            return ":" + name;
        });
    }

    private static Optional<String> sqlPredicate(String sourceColumn, String kindColumn, String geomColumn,
            String payloadExpression, ReadRestriction r, Function<Object, String> bind) {
        boolean sensitive = r != null && r.excludeSensitive() && ALERT_GEOMETRY_SQL.equals(geomColumn);
        if (r == null || (!r.hasDataRestrictions() && !sensitive)) {
            return Optional.empty();
        }
        List<String> and = new ArrayList<>();
        if (sensitive) {
            and.add("NOT a.sensitive");
        }
        if (r.sources() != null && sourceColumn != null) {
            and.add(sourceColumn + " = ANY(CAST(" + bind.apply(PgArrays.text(new TreeSet<>(r.sources())))
                    + " AS text[]))");
        }
        if (r.kinds() != null && kindColumn != null) {
            and.add(kindColumn + " = ANY(CAST(" + bind.apply(PgArrays.text(new TreeSet<>(r.kinds())))
                    + " AS text[]))");
        }
        if (r.region() != null) {
            if (geomColumn == null || r.region().isEmpty()) {
                and.add("FALSE");
            } else {
                Envelope e = r.regionEnvelope();
                String envelope = "ST_MakeEnvelope(CAST(" + bind.apply(e.getMinX()) + " AS double precision), CAST("
                        + bind.apply(e.getMinY()) + " AS double precision), CAST(" + bind.apply(e.getMaxX())
                        + " AS double precision), CAST(" + bind.apply(e.getMaxY()) + " AS double precision), 4326)";
                String area = "ST_GeomFromWKB(CAST(" + bind.apply(new WKBWriter().write(r.region()))
                        + " AS bytea), 4326)";
                and.add("(" + geomColumn + " && " + envelope + " AND ST_Intersects(" + geomColumn + ", " + area + "))");
            }
        }
        if (r.filter() != null) {
            r.filter().sql(sourceColumn, kindColumn, geomColumn, payloadExpression, bind).ifPresent(and::add);
        }
        return and.isEmpty() ? Optional.empty() : Optional.of("(" + String.join(" AND ", and) + ")");
    }

    /** {@link #sqlPredicate(String, ReadRestriction, SqlParams)} added to {@code where} when present. */
    public static void addTo(List<String> where, String alias, ReadRestriction r, SqlParams p) {
        sqlPredicate(alias, r, p).ifPresent(where::add);
    }

    /** {@link #sqlPredicate(String, String, String, ReadRestriction, SqlParams)} added to {@code where}. */
    public static void addTo(List<String> where, String sourceColumn, String kindColumn, String geomColumn,
            ReadRestriction r, SqlParams p) {
        sqlPredicate(sourceColumn, kindColumn, geomColumn, r, p).ifPresent(where::add);
    }

    /**
     * {@link #sqlPredicate(String, String, String, String, ReadRestriction, SqlParams)} added to
     * {@code where} (prompt 036: with the row's payload expression).
     */
    public static void addTo(List<String> where, String sourceColumn, String kindColumn, String geomColumn,
            String payloadExpression, ReadRestriction r, SqlParams p) {
        sqlPredicate(sourceColumn, kindColumn, geomColumn, payloadExpression, r, p).ifPresent(where::add);
    }

    // ------------------------------------------------------------------------- in memory

    /**
     * True when a record of {@code sourceId} and {@code kind} with {@code geometry} passes {@code r}
     * (streams, push fan-out, citations). A null geometry fails a spatial restriction.
     */
    public static boolean test(ReadRestriction r, String sourceId, String kind, Geometry geometry) {
        if (r == null || !r.hasDataRestrictions()) {
            return true;
        }
        if (!r.allowsSource(sourceId) || !r.allowsKind(kind)) {
            return false;
        }
        if (r.filter() != null && !r.filter().testShape(sourceId, kind, geometry)) {
            return false;
        }
        if (r.region() == null) {
            return true;
        }
        return geometry != null && !geometry.isEmpty() && r.preparedRegion().intersects(geometry);
    }

    /**
     * {@link #test(ReadRestriction, String, String, Geometry)} for a record known only by its
     * geometry's box (change summaries). A point box is exact; a larger box passes when it touches
     * the region, so line and polygon geometries near the edge are judged by their box.
     */
    public static boolean test(ReadRestriction r, String sourceId, String kind, Bbox box) {
        if (r == null || !r.hasDataRestrictions()) {
            return true;
        }
        if (!r.allowsSource(sourceId) || !r.allowsKind(kind)) {
            return false;
        }
        if (r.filter() != null && !r.filter().testShape(sourceId, kind, box == null ? null : toGeometry(box))) {
            return false;
        }
        if (r.region() == null) {
            return true;
        }
        return box != null && r.preparedRegion().intersects(toGeometry(box));
    }

    /** The point or rectangle of {@code box}. */
    static Geometry toGeometry(Bbox box) {
        if (box.minX() == box.maxX() && box.minY() == box.maxY()) {
            return WGS84.createPoint(new Coordinate(box.minX(), box.minY()));
        }
        if (box.minX() == box.maxX() || box.minY() == box.maxY()) {
            return WGS84.createLineString(new Coordinate[] {
                new Coordinate(box.minX(), box.minY()), new Coordinate(box.maxX(), box.maxY())});
        }
        return ReadRestriction.rectangle(List.of(box.minX(), box.minY(), box.maxX(), box.maxY()));
    }

    /**
     * {@link #test(ReadRestriction, String, String, Geometry)} for an alert, whose geometry is the
     * one in its record snapshot (JSON text, may be null); a {@code sensitive} alert (prompt 036)
     * fails for a principal that excludes sensitive alerts.
     */
    public static boolean testSnapshot(ReadRestriction r, String sourceId, String kind, String snapshotJson,
            boolean sensitive) {
        if (sensitive && r != null && r.excludeSensitive()) {
            return false;
        }
        return testSnapshot(r, sourceId, kind, snapshotJson);
    }

    /**
     * {@link #test(ReadRestriction, String, String, Geometry)} for an alert, whose geometry is the
     * one in its record snapshot (JSON text, may be null), ignoring its sensitive flag; use the
     * overload with {@code sensitive} for alerts read by a principal (prompt 036).
     */
    public static boolean testSnapshot(ReadRestriction r, String sourceId, String kind, String snapshotJson) {
        if (r == null || !r.hasDataRestrictions()) {
            return true;
        }
        Geometry geometry = r.region() == null ? null : snapshotGeometry(snapshotJson);
        return test(r, sourceId, kind, geometry);
    }

    /**
     * {@link #test(ReadRestriction, String, String, Geometry)} for a record whose geometry is GeoJSON
     * text (as {@code RecordRow.geometry()} carries it), or null.
     */
    public static boolean testGeoJson(ReadRestriction r, String sourceId, String kind, String geoJson) {
        if (r == null || !r.hasDataRestrictions()) {
            return true;
        }
        Geometry geometry = null;
        if (r.region() != null && geoJson != null && !geoJson.isBlank()) {
            try {
                geometry = new GeoJsonReader().read(geoJson);
            } catch (RuntimeException e) {
                geometry = null;
            }
        }
        return test(r, sourceId, kind, geometry);
    }

    /** The geometry of a record snapshot (JSON text with a GeoJSON {@code geometry}), or null. */
    public static Geometry snapshotGeometry(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        try {
            JsonNode geometry = JSON.readTree(snapshotJson).get("geometry");
            if (geometry == null || !geometry.isObject()) {
                return null;
            }
            return new GeoJsonReader().read(geometry);
        } catch (RuntimeException e) {
            // Unreadable geometry: treated as none, so a spatial restriction fails closed.
            return null;
        }
    }

    /**
     * Sources hidden by {@code r} among {@code allSources}: those outside its source member. Callers
     * that already thread an excluded-source set add these to it.
     */
    public static Set<String> excludedBy(ReadRestriction r, Collection<String> allSources) {
        if (r == null || r.sources() == null) {
            return Set.of();
        }
        Set<String> out = new TreeSet<>();
        for (String id : allSources) {
            if (!r.sources().contains(id)) {
                out.add(id);
            }
        }
        return Set.copyOf(out);
    }
}
