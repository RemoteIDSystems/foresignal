package ai.foresignal.inbound.edp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Checks an item geometry against RFC 7946 and the stream's declared geometry
 * (docs/External-Data-Protocol.md §3.3).
 *
 * <p>Allowed types: Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon (no
 * GeometryCollection); longitude −180…180 and latitude −90…90; two or three finite ordinates;
 * closed linear rings of at least four positions; at most 10,000 vertices; and a type compatible
 * with the stream's declared {@code geometry}.
 */
public final class EdpGeometryValidator {

    private static final Map<String, Set<String>> COMPATIBLE = Map.of(
            "Point", Set.of("Point", "MultiPoint"),
            "LineString", Set.of("LineString", "MultiLineString"),
            "Polygon", Set.of("Polygon", "MultiPolygon"),
            "MultiPolygon", Set.of("Polygon", "MultiPolygon"),
            "any", Set.of("Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon"),
            "none", Set.of());

    private EdpGeometryValidator() {}

    /**
     * The problems of {@code geometry} for a stream declaring {@code declared}.
     *
     * @param geometry the item's {@code geometry}; null or JSON null means none
     * @param declared the stream's {@code geometry} ({@code any} when null)
     * @param path the JSON Pointer of the geometry in the item, for error paths
     * @return empty when acceptable
     */
    public static List<EdpSchemaError> validate(JsonNode geometry, String declared, String path) {
        List<EdpSchemaError> errors = new ArrayList<>();
        if (geometry == null || geometry.isNull()) {
            return errors;
        }
        String allowedKey = declared == null ? "any" : declared;
        Set<String> allowed = COMPATIBLE.getOrDefault(allowedKey, COMPATIBLE.get("any"));
        if (!geometry.isObject()) {
            errors.add(new EdpSchemaError(path, "must be a GeoJSON geometry object"));
            return errors;
        }
        JsonNode typeNode = geometry.get("type");
        String type = typeNode != null && typeNode.isString() ? typeNode.asString() : null;
        if (type == null || !COMPATIBLE.get("any").contains(type)) {
            errors.add(new EdpSchemaError(path + "/type", "must be Point, MultiPoint, LineString, MultiLineString, "
                    + "Polygon or MultiPolygon"));
            return errors;
        }
        if (!allowed.contains(type)) {
            errors.add(new EdpSchemaError(path + "/type", type + " does not match the stream's declared geometry "
                    + allowedKey));
            return errors;
        }
        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null || !coordinates.isArray()) {
            errors.add(new EdpSchemaError(path + "/coordinates", "must be an array"));
            return errors;
        }
        int[] vertices = {0};
        String coords = path + "/coordinates";
        switch (type) {
            case "Point" -> position(coordinates, coords, errors, vertices);
            case "MultiPoint" -> each(coordinates, coords, errors, (c, p) -> position(c, p, errors, vertices));
            case "LineString" -> line(coordinates, coords, errors, vertices);
            case "MultiLineString" -> each(coordinates, coords, errors, (c, p) -> line(c, p, errors, vertices));
            case "Polygon" -> polygon(coordinates, coords, errors, vertices);
            default -> each(coordinates, coords, errors, (c, p) -> polygon(c, p, errors, vertices));
        }
        if (vertices[0] > EdpProtocol.MAX_VERTICES) {
            errors.add(new EdpSchemaError(coords, "has " + vertices[0] + " vertices; the maximum is "
                    + EdpProtocol.MAX_VERTICES));
        }
        return errors;
    }

    private interface Part {
        void check(JsonNode node, String path);
    }

    private static void each(JsonNode array, String path, List<EdpSchemaError> errors, Part part) {
        if (array.size() == 0) {
            errors.add(new EdpSchemaError(path, "must not be empty"));
        }
        for (int i = 0; i < array.size(); i++) {
            JsonNode child = array.get(i);
            if (!child.isArray()) {
                errors.add(new EdpSchemaError(path + "/" + i, "must be an array"));
                continue;
            }
            part.check(child, path + "/" + i);
        }
    }

    private static void line(JsonNode line, String path, List<EdpSchemaError> errors, int[] vertices) {
        if (line.size() < 2) {
            errors.add(new EdpSchemaError(path, "a line needs at least two positions"));
        }
        positions(line, path, errors, vertices);
    }

    private static void polygon(JsonNode rings, String path, List<EdpSchemaError> errors, int[] vertices) {
        if (rings.size() == 0) {
            errors.add(new EdpSchemaError(path, "a polygon needs at least one ring"));
        }
        for (int r = 0; r < rings.size(); r++) {
            JsonNode ring = rings.get(r);
            String ringPath = path + "/" + r;
            if (!ring.isArray()) {
                errors.add(new EdpSchemaError(ringPath, "must be an array"));
                continue;
            }
            if (ring.size() < 4) {
                errors.add(new EdpSchemaError(ringPath, "a linear ring needs at least four positions"));
            }
            int before = errors.size();
            positions(ring, ringPath, errors, vertices);
            if (errors.size() == before && ring.size() >= 2 && !samePosition(ring.get(0), ring.get(ring.size() - 1))) {
                errors.add(new EdpSchemaError(ringPath, "a linear ring must be closed (first and last positions equal)"));
            }
        }
    }

    private static void positions(JsonNode positions, String path, List<EdpSchemaError> errors, int[] vertices) {
        for (int i = 0; i < positions.size(); i++) {
            position(positions.get(i), path + "/" + i, errors, vertices);
            if (vertices[0] > EdpProtocol.MAX_VERTICES) {
                return;
            }
        }
    }

    private static void position(JsonNode position, String path, List<EdpSchemaError> errors, int[] vertices) {
        vertices[0]++;
        if (!position.isArray() || position.size() < 2 || position.size() > 3) {
            errors.add(new EdpSchemaError(path, "a position has two or three numbers"));
            return;
        }
        for (JsonNode ordinate : position) {
            if (!ordinate.isNumber() || !Double.isFinite(ordinate.doubleValue())) {
                errors.add(new EdpSchemaError(path, "ordinates must be finite numbers"));
                return;
            }
        }
        double lon = position.get(0).doubleValue();
        double lat = position.get(1).doubleValue();
        if (lon < -180 || lon > 180) {
            errors.add(new EdpSchemaError(path + "/0", "longitude must be between -180 and 180"));
        }
        if (lat < -90 || lat > 90) {
            errors.add(new EdpSchemaError(path + "/1", "latitude must be between -90 and 90"));
        }
    }

    private static boolean samePosition(JsonNode a, JsonNode b) {
        if (!a.isArray() || !b.isArray() || a.size() < 2 || b.size() < 2) {
            return false;
        }
        return a.get(0).doubleValue() == b.get(0).doubleValue() && a.get(1).doubleValue() == b.get(1).doubleValue();
    }
}
