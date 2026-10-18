package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.GeometryFixer;
import tools.jackson.databind.JsonNode;

/**
 * A chronicle's area, or a layer's area override (prompt 131, OpenAPI {@code ChronicleArea}): a GeoJSON geometry,
 * 036 places and 040 region codes, all optional, with an optional buffer in kilometers around the geometry and places.
 * A record matches when it intersects the geometry or the places, or carries one of the region codes (a country code
 * also matches its {@code CC-admin1} codes). An empty area matches everything.
 *
 * @param geometry the geometry (SRID 4326), or null
 * @param placeIds the place ids
 * @param regionCodes the region codes (upper case)
 * @param bufferKm the buffer around the geometry and places, or null
 */
public record ChronicleArea(Geometry geometry, List<String> placeIds, List<String> regionCodes, Double bufferKm) {

    /** The 040 region code pattern. */
    public static final Pattern REGION_CODE = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    /** Most places or region codes. */
    public static final int MAX_ITEMS = 200;

    /** The largest buffer. */
    public static final double MAX_BUFFER_KM = 2_000;

    /** Kilometers per degree, for the buffer (as 127 does). */
    public static final double KM_PER_DEGREE = 111.32;

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    /** Copies the lists. */
    public ChronicleArea {
        placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
    }

    /** True when the area narrows nothing. */
    public boolean isEmpty() {
        return geometry == null && placeIds.isEmpty() && regionCodes.isEmpty();
    }

    /**
     * Parses a {@code ChronicleArea}; null or JSON null gives null.
     *
     * @param node the body
     * @param field the field name for errors
     * @param placeExists tells whether a place id exists
     * @throws ValidationException {@code 400 CHRONICLE_LAYER_INVALID}
     */
    public static ChronicleArea parse(JsonNode node, String field, Function<String, Boolean> placeExists) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            throw invalid(field, "must be an object with geometry, placeIds, regionCodes or bufferKm");
        }
        for (String member : node.propertyNames()) {
            if (!Set.of("geometry", "placeIds", "regionCodes", "bufferKm").contains(member)) {
                throw invalid(field + "." + member, "is not a ChronicleArea member");
            }
        }
        Geometry geometry = null;
        JsonNode g = node.get("geometry");
        if (g != null && !g.isNull()) {
            try {
                geometry = GeometryFixer.fix(new GeoJsonReader().read(g));
                geometry.setSRID(4326);
            } catch (RuntimeException e) {
                throw invalid(field + ".geometry", "must be a valid GeoJSON geometry: " + e.getMessage());
            }
        }
        List<String> places = strings(node.get("placeIds"), field + ".placeIds");
        for (String id : places) {
            if (placeExists != null && !Boolean.TRUE.equals(placeExists.apply(id))) {
                throw invalid(field + ".placeIds", "unknown place '" + id + "'");
            }
        }
        List<String> regions = new ArrayList<>();
        for (String code : strings(node.get("regionCodes"), field + ".regionCodes")) {
            String upper = code.toUpperCase(Locale.ROOT);
            if (!REGION_CODE.matcher(upper).matches()) {
                throw invalid(field + ".regionCodes", "'" + code + "' is not a 040 region code (CC or CC-admin1)");
            }
            if (!regions.contains(upper)) {
                regions.add(upper);
            }
        }
        Double buffer = null;
        JsonNode b = node.get("bufferKm");
        if (b != null && !b.isNull()) {
            if (!b.isNumber() || b.asDouble() < 0 || b.asDouble() > MAX_BUFFER_KM) {
                throw invalid(field + ".bufferKm", "must be a number from 0 to " + (int) MAX_BUFFER_KM);
            }
            buffer = b.asDouble();
        }
        return new ChronicleArea(geometry, places, regions, buffer);
    }

    private static List<String> strings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw invalid(field, "must be an array of strings");
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw invalid(field, "must hold non-empty strings");
            }
            if (!out.contains(item.asString().strip())) {
                out.add(item.asString().strip());
            }
        }
        if (out.size() > MAX_ITEMS) {
            throw invalid(field, "holds more than " + MAX_ITEMS + " items");
        }
        return out;
    }

    /** The {@code ChronicleArea} wire shape (also stored as jsonb). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("geometry", geometry == null ? null : new GeoJsonWriter().write(geometry));
        out.put("placeIds", placeIds);
        out.put("regionCodes", regionCodes);
        out.put("bufferKm", bufferKm);
        return out;
    }

    /** An area from its stored map, or null. */
    public static ChronicleArea fromMap(Map<String, Object> map, tools.jackson.databind.json.JsonMapper json) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return parse(json.valueToTree(map), "area", null);
        } catch (ValidationException e) {
            return null;
        }
    }

    /**
     * Resolves the area for matching: the geometry and the places' areas united, buffered, and prepared.
     *
     * @param placeArea the area of a place id, or null when it is gone (a deleted place matches nothing there)
     * @return the resolved area
     */
    public Resolved resolve(Function<String, Geometry> placeArea) {
        List<Geometry> parts = new ArrayList<>();
        if (geometry != null && !geometry.isEmpty()) {
            parts.add(geometry);
        }
        for (String id : placeIds) {
            Geometry a = placeArea == null ? null : placeArea.apply(id);
            if (a != null && !a.isEmpty()) {
                parts.add(a);
            }
        }
        Geometry union = null;
        if (!parts.isEmpty()) {
            union = parts.size() == 1 ? parts.getFirst() : GeometryFixer.fix(WGS84.buildGeometry(parts).union());
            if (bufferKm != null && bufferKm > 0) {
                union = union.buffer(bufferKm / KM_PER_DEGREE);
            }
            union.setSRID(4326);
        }
        boolean spatial = geometry != null || !placeIds.isEmpty();
        return new Resolved(union, spatial, Set.copyOf(regionCodes));
    }

    /**
     * An area ready for matching: the united, buffered geometry (prepared once, JTS prepared geometries), whether the
     * area named a geometry or places (a missing union then matches nothing spatially), and the region codes.
     */
    public static final class Resolved {

        /** Matches everything. */
        public static final Resolved ANYWHERE = new Resolved(null, false, Set.of());

        private final Geometry geometry;
        private final boolean spatial;
        private final Set<String> regionCodes;
        private volatile PreparedGeometry prepared;

        /**
         * Creates a resolved area.
         *
         * @param geometry the united, buffered geometry, or null
         * @param spatial true when the area named a geometry or places
         * @param regionCodes the region codes
         */
        public Resolved(Geometry geometry, boolean spatial, Set<String> regionCodes) {
            this.geometry = geometry;
            this.spatial = spatial;
            this.regionCodes = regionCodes == null ? Set.of() : Set.copyOf(regionCodes);
        }

        /** The united, buffered geometry, or null. */
        public Geometry geometry() {
            return geometry;
        }

        /** True when the area named a geometry or places. */
        public boolean spatial() {
            return spatial;
        }

        /** The region codes. */
        public Set<String> regionCodes() {
            return regionCodes;
        }

        private PreparedGeometry prepared() {
            PreparedGeometry p = prepared;
            if (p == null && geometry != null) {
                p = PreparedGeometryFactory.prepare(geometry);
                prepared = p;
            }
            return p;
        }

        /** True when the area narrows nothing. */
        public boolean anywhere() {
            return !spatial && regionCodes.isEmpty();
        }

        /**
         * True when a record with {@code recordGeometry} and {@code region} is inside: it intersects the geometry, or
         * its region is one of the codes (a country code covers its admin-1 codes).
         */
        public boolean contains(Geometry recordGeometry, String region) {
            if (anywhere()) {
                return true;
            }
            if (spatial && geometry != null && recordGeometry != null && !recordGeometry.isEmpty()
                    && prepared().intersects(recordGeometry)) {
                return true;
            }
            return region != null && regionMatches(region);
        }

        boolean regionMatches(String region) {
            if (regionCodes.contains(region)) {
                return true;
            }
            int dash = region.indexOf('-');
            return dash == 2 && regionCodes.contains(region.substring(0, 2));
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.CHRONICLE_LAYER_INVALID, "The area is not valid.",
                List.of(new FieldError(field, message)));
    }
}
