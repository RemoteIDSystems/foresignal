package ai.foresignal.security.restrict;

import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * The effective read restriction of a principal (OpenAPI {@code ReadRestriction}, prompt 035): a
 * restricted token, a feed key or the anonymous profile.
 *
 * <p>Immutable. {@code null} sets mean "any"; an empty set means "none" (nothing passes). The
 * spatial part is the {@linkplain #region() region}: the {@code bbox} rectangle, the {@code area}
 * multipolygon, or their intersection when both are set. A record without a geometry never passes a
 * spatial restriction (fail closed).
 *
 * <p>{@link RestrictionClamp} is the one place that turns a restriction into query predicates and
 * in-memory tests; nothing else interprets these members.
 *
 * <p>Prompt 036 adds two members that are never stored with a token:
 *
 * <ul>
 *   <li>{@link #filter()}: a request-scoped {@link RecordFilter} ({@code place=}, {@code view=},
 *       {@code inWatchlist=}) intersected with everything else ({@link #withFilter});
 *   <li>{@link #excludeSensitive()}: sensitive alerts (from rules referencing a watchlist) are hidden,
 *       set for anonymous callers and feed keys.
 * </ul>
 *
 * <p>A stored restriction's {@code placeIds} are resolved into {@link #area()} by {@link #of}
 * through {@link PlaceAreas}.
 */
public final class ReadRestriction {

    /** The most vertices an {@code area} may have. */
    public static final int MAX_AREA_VERTICES = 2_000;

    /** No restriction at all: every record passes, private sources are not excluded here. */
    public static final ReadRestriction NONE = new ReadRestriction(null, null, null, null, null, false, null);

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Set<String> sources;
    private final Set<String> kinds;
    private final List<Double> bbox;
    private final Geometry area;
    private final List<String> endpoints;
    private final boolean excludePrivate;
    private final Integer maxRequestsPerMinute;
    private final Geometry region;
    private final PreparedGeometry preparedRegion;
    private final RecordFilter filter;
    private final boolean excludeSensitive;

    /**
     * A restriction.
     *
     * @param sources allowed source ids, or null for any
     * @param kinds allowed kinds, or null for any
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
     * @param area a WGS84 (multi)polygon, or null
     * @param endpoints allowed operations ({@code METHOD /v1/pattern}) or canonical path prefixes, or
     *     null for any
     * @param excludePrivate true to hide private sources
     * @param maxRequestsPerMinute the {@code read} rate-limit override, or null
     */
    public ReadRestriction(Collection<String> sources, Collection<String> kinds, List<Double> bbox, Geometry area,
            List<String> endpoints, boolean excludePrivate, Integer maxRequestsPerMinute) {
        this(sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute, null, false);
    }

    private ReadRestriction(Collection<String> sources, Collection<String> kinds, List<Double> bbox, Geometry area,
            List<String> endpoints, boolean excludePrivate, Integer maxRequestsPerMinute, RecordFilter filter,
            boolean excludeSensitive) {
        this.filter = filter;
        this.excludeSensitive = excludeSensitive;
        this.sources = sources == null ? null : Set.copyOf(new TreeSet<>(sources));
        this.kinds = kinds == null ? null : Set.copyOf(new TreeSet<>(kinds));
        this.bbox = bbox == null ? null : List.copyOf(bbox);
        this.area = area;
        this.endpoints = endpoints == null ? null : List.copyOf(endpoints);
        this.excludePrivate = excludePrivate;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.region = region(this.bbox, area);
        this.preparedRegion = region == null ? null : PreparedGeometryFactory.prepare(region);
    }

    /**
     * The effective restriction of a stored token restriction.
     *
     * @param r the stored restriction (null for none)
     * @param defaultExcludePrivate the value of {@code excludePrivate} when {@code r} leaves it out
     */
    public static ReadRestriction of(TokenRestrictions r, boolean defaultExcludePrivate) {
        TokenRestrictions t = TokenRestrictions.orNone(r);
        boolean exclude = t.excludePrivate() == null ? defaultExcludePrivate : t.excludePrivate();
        Geometry area = area(t.area());
        if (t.placeIds() != null) {
            // Prompt 036: the places' areas bound the restriction; unknown places fail closed (empty).
            Geometry places = PlaceAreas.resolve(t.placeIds());
            if (area == null) {
                area = places;
            } else {
                area = area.intersection(places);
                area.setSRID(4326);
            }
        }
        return new ReadRestriction(t.sources(), t.kinds(), t.bbox(), area, t.endpoints(), exclude,
                t.maxRequestsPerMinute());
    }

    /** Parses a GeoJSON area map (as stored in {@link TokenRestrictions#area()}), or null. */
    public static Geometry area(Map<String, Object> geoJson) {
        if (geoJson == null) {
            return null;
        }
        Geometry g = new GeoJsonReader(MAX_AREA_VERTICES, 2).read(JSON.valueToTree(geoJson));
        g.setSRID(4326);
        return g;
    }

    /**
     * The stored form of this restriction's data members ({@code briefing.restriction_snapshot}),
     * or null when it restricts no data.
     */
    public TokenRestrictions toStored() {
        if (!hasDataRestrictions() && !excludePrivate) {
            return null;
        }
        Map<String, Object> areaJson = area == null ? null
                : new ai.foresignal.source.formats.geojson.GeoJsonWriter().write(area);
        return new TokenRestrictions(sources == null ? null : List.copyOf(new TreeSet<>(sources)),
                kinds == null ? null : List.copyOf(new TreeSet<>(kinds)), bbox, null, areaJson, excludePrivate, null);
    }

    /** A rectangle polygon for {@code [minLon, minLat, maxLon, maxLat]}. */
    public static Geometry rectangle(List<Double> bbox) {
        double minLon = bbox.get(0);
        double minLat = bbox.get(1);
        double maxLon = bbox.get(2);
        double maxLat = bbox.get(3);
        return WGS84.createPolygon(new Coordinate[] {
            new Coordinate(minLon, minLat), new Coordinate(maxLon, minLat), new Coordinate(maxLon, maxLat),
            new Coordinate(minLon, maxLat), new Coordinate(minLon, minLat)
        });
    }

    private static Geometry region(List<Double> bbox, Geometry area) {
        if (bbox == null && area == null) {
            return null;
        }
        if (bbox == null) {
            return area;
        }
        Geometry box = rectangle(bbox);
        if (area == null) {
            return box;
        }
        Geometry both = area.intersection(box);
        both.setSRID(4326);
        return both;
    }

    /** Allowed source ids, or null for any. */
    public Set<String> sources() {
        return sources;
    }

    /** Allowed kinds, or null for any. */
    public Set<String> kinds() {
        return kinds;
    }

    /** The bbox member, or null. */
    public List<Double> bbox() {
        return bbox;
    }

    /** The area member, or null. */
    public Geometry area() {
        return area;
    }

    /** Allowed endpoints or path prefixes, or null for any. */
    public List<String> endpoints() {
        return endpoints;
    }

    /** True when private sources are hidden from this principal. */
    public boolean excludePrivate() {
        return excludePrivate;
    }

    /** The {@code read} rate-limit override, or null. */
    public Integer maxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    /** The spatial region (bbox, area or their intersection), or null for anywhere. */
    public Geometry region() {
        return region;
    }

    /** The prepared {@link #region()}, for repeated in-memory tests; null for anywhere. */
    PreparedGeometry preparedRegion() {
        return preparedRegion;
    }

    /** The region's envelope (the {@code &&} prefilter), or null. */
    public Envelope regionEnvelope() {
        return region == null ? null : region.getEnvelopeInternal();
    }

    /** True when a source, kind, spatial or request filter narrows what is read. */
    public boolean hasDataRestrictions() {
        return sources != null || kinds != null || region != null || filter != null;
    }

    /** The request-scoped filter (prompt 036), or null. */
    public RecordFilter filter() {
        return filter;
    }

    /** True when sensitive alerts are hidden from this principal (anonymous callers and feed keys, 036). */
    public boolean excludeSensitive() {
        return excludeSensitive;
    }

    /**
     * This restriction further narrowed by {@code value} (prompt 036): an existing filter and
     * {@code value} must both pass. A null {@code value} returns this restriction.
     */
    public ReadRestriction withFilter(RecordFilter value) {
        if (value == null) {
            return this;
        }
        RecordFilter combined = filter == null ? value : RecordFilters.and(filter, value);
        return new ReadRestriction(sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute,
                combined, excludeSensitive);
    }

    /** This restriction with {@code excludeSensitive} replaced (prompt 036). */
    public ReadRestriction withExcludeSensitive(boolean value) {
        return value == excludeSensitive ? this
                : new ReadRestriction(sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute,
                        filter, value);
    }

    /** True when nothing at all is restricted, private exclusion included. */
    public boolean isUnrestricted() {
        return !hasDataRestrictions() && endpoints == null && !excludePrivate && maxRequestsPerMinute == null;
    }

    /** True when {@code sourceId} passes the source member (always, when it is null). */
    public boolean allowsSource(String sourceId) {
        return sources == null || (sourceId != null && sources.contains(sourceId));
    }

    /** True when {@code kind} passes the kind member (always, when it is null). */
    public boolean allowsKind(String kind) {
        return kinds == null || (kind != null && kinds.contains(kind));
    }

    /**
     * True when everything this restriction lets through is inside {@code outer}: its sources and
     * kinds are subsets and its region is covered by {@code outer}'s. A briefing made under this
     * restriction may then be shown to a caller restricted by {@code outer} (prompt 035).
     */
    public boolean within(ReadRestriction outer) {
        if (outer == null || !outer.hasDataRestrictions()) {
            return true;
        }
        if (outer.sources != null && (sources == null || !outer.sources.containsAll(sources))) {
            return false;
        }
        if (outer.kinds != null && (kinds == null || !outer.kinds.containsAll(kinds))) {
            return false;
        }
        if (outer.region != null && (region == null || !outer.region.covers(region))) {
            return false;
        }
        return true;
    }

    /** This restriction with {@code excludePrivate} replaced. */
    public ReadRestriction withExcludePrivate(boolean value) {
        return value == excludePrivate ? this
                : new ReadRestriction(sources, kinds, bbox, area, endpoints, value, maxRequestsPerMinute, filter,
                        excludeSensitive);
    }

    /** This restriction with {@code endpoints} replaced. */
    public ReadRestriction withEndpoints(List<String> value) {
        return new ReadRestriction(sources, kinds, bbox, area, value, excludePrivate, maxRequestsPerMinute, filter,
                excludeSensitive);
    }

    /**
     * A stable text form of the data members, for entity tags, cursor fingerprints and cache keys,
     * so two principals with different restrictions never share a cached response.
     */
    public String canonical() {
        if (!hasDataRestrictions() && !excludePrivate) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        parts.add("s=" + (sources == null ? "*" : String.join(",", new TreeSet<>(sources))));
        parts.add("k=" + (kinds == null ? "*" : String.join(",", new TreeSet<>(kinds))));
        parts.add("r=" + (region == null ? "*" : Integer.toHexString(region.toText().hashCode())));
        parts.add("p=" + excludePrivate);
        if (filter != null) {
            parts.add("f=" + Integer.toHexString(filter.canonical().hashCode()));
        }
        if (excludeSensitive) {
            parts.add("x=true");
        }
        return String.join(";", parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadRestriction other)) {
            return false;
        }
        return excludePrivate == other.excludePrivate
                && excludeSensitive == other.excludeSensitive
                && Objects.equals(filter == null ? null : filter.canonical(),
                        other.filter == null ? null : other.filter.canonical())
                && Objects.equals(sources, other.sources)
                && Objects.equals(kinds, other.kinds)
                && Objects.equals(bbox, other.bbox)
                && Objects.equals(area == null ? null : area.toText(), other.area == null ? null : other.area.toText())
                && Objects.equals(endpoints, other.endpoints)
                && Objects.equals(maxRequestsPerMinute, other.maxRequestsPerMinute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sources, kinds, bbox, area == null ? null : area.toText(), endpoints, excludePrivate,
                maxRequestsPerMinute, filter == null ? null : filter.canonical(), excludeSensitive);
    }

    @Override
    public String toString() {
        return "ReadRestriction[" + canonical() + (endpoints == null ? "" : ";e=" + endpoints)
                + (maxRequestsPerMinute == null ? "" : ";rpm=" + maxRequestsPerMinute) + "]";
    }
}
