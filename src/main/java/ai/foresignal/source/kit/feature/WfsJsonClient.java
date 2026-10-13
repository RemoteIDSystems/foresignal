package ai.foresignal.source.kit.feature;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * WFS 2.0 {@code GetFeature} with GeoJSON output (GeoServer {@code outputFormat=application/json}, prompt 073).
 *
 * <p>Requests {@code service=WFS&version=2.0.0&request=GetFeature&typeNames=…&outputFormat=application/json&count=N
 * &startIndex=K&srsName=urn:ogc:def:crs:EPSG::4326} and pages while {@code numberReturned == count}.
 *
 * <p><strong>Axis order.</strong> GeoServer's GeoJSON writer emits longitude first even when the response {@code crs}
 * says {@code EPSG::4326} (verified 2026-10 on CWFIS), so the spec's {@code axisOrder} ({@code lonlat} by default,
 * or {@code latlon}) decides. As a sanity check, when the spec has a {@code bbox} and the first
 * {@value #SAMPLE_POINTS} points of the first page fit it only when swapped, every coordinate is swapped and a warning
 * is logged once per source.
 */
public final class WfsJsonClient {

    /** The points checked against the bounding box. */
    public static final int SAMPLE_POINTS = 20;

    /** The {@code srsName} requested. */
    public static final String SRS_NAME = "urn:ogc:def:crs:EPSG::4326";

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    /** Sources already warned about a swapped axis order, so the warning appears once. */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    /**
     * One GetFeature query.
     *
     * @param endpoint the WFS endpoint
     * @param typeName the feature type
     * @param count the page size
     * @param maxPages the page cap
     * @param maxFeatures the feature cap
     * @param latLon true when the spec says coordinates arrive latitude first
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]} for the sanity check, or empty
     * @param filters extra query parameters (vendor filters)
     * @param headers extra request headers
     * @param extraQuery extra query parameters (a query credential)
     * @param sourceId the source, for the once-only warning
     */
    public record Request(URI endpoint, String typeName, int count, int maxPages, int maxFeatures, boolean latLon,
            List<Double> bbox, Map<String, String> filters, Map<String, String> headers,
            Map<String, String> extraQuery, String sourceId) {

        /** Null collections become empty ones. */
        public Request {
            bbox = bbox == null ? List.of() : List.copyOf(bbox);
            filters = filters == null ? Map.of() : new LinkedHashMap<>(filters);
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            extraQuery = extraQuery == null ? Map.of() : new LinkedHashMap<>(extraQuery);
        }
    }

    /**
     * Fetches the feature type.
     *
     * @param ctx the fetch context
     * @param request the query
     * @return the features, longitude first
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a non-200 or truncated body or a body that is not a
     *     FeatureCollection
     */
    public FeatureFetch fetch(FetchContext ctx, Request request) throws AdapterException {
        List<JsonNode> features = new ArrayList<>();
        boolean swap = request.latLon();
        boolean checked = request.bbox().size() != 4;
        int startIndex = 0;
        for (int n = 1; n <= request.maxPages(); n++) {
            JsonNode root = FeatureHttp.get(ctx, page(request, startIndex), request.headers(), "WFS page " + n).root();
            JsonNode list = root.get("features");
            if (list == null || !list.isArray()) {
                throw new UpstreamFormatException("WFS page " + n + " has no features array", "");
            }
            if (!checked) {
                checked = true;
                if (onlyFitsSwapped(list, request.bbox(), swap)) {
                    swap = !swap;
                    if (WARNED.add(request.sourceId() == null ? "" : request.sourceId())) {
                        ctx.log().warn("{}: the first {} points fit the spec bbox only with the axes swapped; swapping"
                                + " (axisOrder {})", ctx.sourceId(), SAMPLE_POINTS, swap ? "latlon" : "lonlat");
                    }
                }
            }
            for (JsonNode feature : list) {
                if (features.size() >= request.maxFeatures()) {
                    return new FeatureFetch(features, false, false, n, "max-features");
                }
                features.add(swap ? swapped(feature) : feature);
            }
            JsonNode returned = root.get("numberReturned");
            int numberReturned = returned != null && returned.isNumber() ? returned.intValue() : list.size();
            if (numberReturned < request.count() || list.isEmpty()) {
                return new FeatureFetch(features, true, false, n, "last-page");
            }
            startIndex += numberReturned;
        }
        return new FeatureFetch(features, false, false, request.maxPages(), "max-pages");
    }

    /** The URL of the page starting at {@code startIndex}. */
    static URI page(Request request, int startIndex) {
        Map<String, Object> params = FeatureHttp.params();
        params.put("service", "WFS");
        params.put("version", "2.0.0");
        params.put("request", "GetFeature");
        params.put("typeNames", request.typeName());
        params.put("outputFormat", "application/json");
        params.put("count", request.count());
        params.put("startIndex", startIndex);
        params.put("srsName", SRS_NAME);
        params.putAll(request.filters());
        params.putAll(request.extraQuery());
        return FeatureHttp.uri(request.endpoint().toString(), params);
    }

    /**
     * True when the first {@value #SAMPLE_POINTS} points do not all fit {@code bbox} as read, but all fit swapped.
     *
     * @param features the first page's features
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}
     * @param swapped true when the points are being read latitude first
     */
    static boolean onlyFitsSwapped(JsonNode features, List<Double> bbox, boolean swapped) {
        List<double[]> points = new ArrayList<>();
        for (JsonNode feature : features) {
            collect(feature.get("geometry"), points);
            if (points.size() >= SAMPLE_POINTS) {
                break;
            }
        }
        if (points.isEmpty()) {
            return false;
        }
        List<double[]> sample = points.subList(0, Math.min(SAMPLE_POINTS, points.size()));
        boolean asRead = true;
        boolean flipped = true;
        for (double[] p : sample) {
            double x = swapped ? p[1] : p[0];
            double y = swapped ? p[0] : p[1];
            asRead &= fits(x, y, bbox);
            flipped &= fits(y, x, bbox);
        }
        return !asRead && flipped;
    }

    private static boolean fits(double lon, double lat, List<Double> bbox) {
        return lon >= bbox.get(0) && lon <= bbox.get(2) && lat >= bbox.get(1) && lat <= bbox.get(3);
    }

    private static void collect(JsonNode geometry, List<double[]> out) {
        if (geometry == null || !geometry.isObject()) {
            return;
        }
        JsonNode geometries = geometry.get("geometries");
        if (geometries != null && geometries.isArray()) {
            for (JsonNode g : geometries) {
                collect(g, out);
            }
            return;
        }
        positions(geometry.get("coordinates"), out);
    }

    private static void positions(JsonNode coordinates, List<double[]> out) {
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty() || out.size() >= SAMPLE_POINTS) {
            return;
        }
        if (coordinates.get(0).isNumber()) {
            if (coordinates.size() >= 2 && coordinates.get(1).isNumber()) {
                out.add(new double[] {coordinates.get(0).doubleValue(), coordinates.get(1).doubleValue()});
            }
            return;
        }
        for (JsonNode c : coordinates) {
            positions(c, out);
        }
    }

    /** {@code feature} with the first two values of every position swapped. */
    static JsonNode swapped(JsonNode feature) {
        if (!feature.isObject()) {
            return feature;
        }
        ObjectNode copy = (ObjectNode) feature.deepCopy();
        JsonNode geometry = copy.get("geometry");
        if (geometry != null && geometry.isObject()) {
            copy.set("geometry", swapGeometry(geometry));
        }
        return copy;
    }

    private static JsonNode swapGeometry(JsonNode geometry) {
        ObjectNode copy = (ObjectNode) geometry.deepCopy();
        JsonNode geometries = copy.get("geometries");
        if (geometries != null && geometries.isArray()) {
            ArrayNode out = NODES.arrayNode();
            for (JsonNode g : geometries) {
                out.add(g.isObject() ? swapGeometry(g) : g);
            }
            copy.set("geometries", out);
            return copy;
        }
        JsonNode coordinates = copy.get("coordinates");
        if (coordinates != null) {
            copy.set("coordinates", swapCoordinates(coordinates));
        }
        return copy;
    }

    private static JsonNode swapCoordinates(JsonNode coordinates) {
        if (!coordinates.isArray() || coordinates.isEmpty()) {
            return coordinates;
        }
        ArrayNode out = NODES.arrayNode();
        if (coordinates.get(0).isNumber()) {
            if (coordinates.size() < 2) {
                return coordinates;
            }
            out.add(coordinates.get(1));
            out.add(coordinates.get(0));
            for (int i = 2; i < coordinates.size(); i++) {
                out.add(coordinates.get(i));
            }
            return out;
        }
        for (JsonNode c : coordinates) {
            out.add(swapCoordinates(c));
        }
        return out;
    }

    /** Forgets the once-only warnings (tests). */
    static void resetWarnings() {
        WARNED.clear();
    }
}
