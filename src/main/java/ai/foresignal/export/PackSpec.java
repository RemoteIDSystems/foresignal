package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * A PMTiles offline pack request (prompt 111; OpenAPI {@code PackSpec}).
 *
 * @param name the pack name (1–200 characters)
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param placeId a place whose bounds the pack covers, or null
 * @param minZoom the minimum zoom (0–14)
 * @param maxZoom the maximum zoom (minZoom–14)
 * @param vector the vector (MVT) pack, or null
 * @param raster the raster (PNG) pack, or null
 * @param includePrivate include private sources (admin only)
 * @param personalUse include FORBIDDEN and LOCAL_ONLY sources
 */
public record PackSpec(String name, double[] bbox, String placeId, int minZoom, int maxZoom, Vector vector,
        Raster raster, boolean includePrivate, boolean personalUse) {

    /** The highest zoom a pack may reach. */
    public static final int MAX_ZOOM = 14;

    /**
     * The vector part.
     *
     * @param sources source ids (empty: every permitted source)
     * @param kinds kinds (empty: all)
     * @param asOf render the state at this instant (037), or null for current
     */
    public record Vector(List<String> sources, List<String> kinds, Instant asOf) {

        /** Copies the lists. */
        public Vector {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
        }
    }

    /**
     * The raster part.
     *
     * @param productId the product
     * @param frame {@code latest} or a frame key
     */
    public record Raster(String productId, String frame) {}

    /**
     * Parses a {@code PackSpec} body.
     *
     * @throws ValidationException {@code 400 EXPORT_SPEC_INVALID} with every field error
     */
    public static PackSpec parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "The request body must be a PackSpec object.",
                    List.of());
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body.get("name"));
        if (name == null || name.isBlank() || name.length() > 200) {
            errors.add(new FieldError("name", "is required, 1–200 characters"));
        }
        double[] bbox = box(body.get("bbox"), errors);
        String placeId = text(body.get("placeId"));
        Integer minZoom = zoom(body.get("minZoom"), "minZoom", errors);
        Integer maxZoom = zoom(body.get("maxZoom"), "maxZoom", errors);
        if (minZoom != null && maxZoom != null && maxZoom < minZoom) {
            errors.add(new FieldError("maxZoom", "must be at least minZoom"));
        }
        Vector vector = null;
        JsonNode vectorNode = body.get("vector");
        if (vectorNode != null && !vectorNode.isNull()) {
            if (!vectorNode.isObject()) {
                errors.add(new FieldError("vector", "must be an object"));
            } else {
                Instant asOf = null;
                if (vectorNode.hasNonNull("asOf")) {
                    try {
                        asOf = Instant.parse(vectorNode.get("asOf").asString());
                    } catch (RuntimeException e) {
                        errors.add(new FieldError("vector.asOf", "must be an ISO-8601 date-time"));
                    }
                }
                vector = new Vector(strings(vectorNode.get("sources")), strings(vectorNode.get("kinds")), asOf);
            }
        }
        Raster raster = null;
        JsonNode rasterNode = body.get("raster");
        if (rasterNode != null && !rasterNode.isNull()) {
            String productId = rasterNode.isObject() ? text(rasterNode.get("productId")) : null;
            if (productId == null || productId.isBlank()) {
                errors.add(new FieldError("raster.productId", "is required"));
            }
            String frame = rasterNode.isObject() ? text(rasterNode.get("frame")) : null;
            raster = new Raster(productId, frame == null || frame.isBlank() ? "latest" : frame);
        }
        if (vector == null && raster == null) {
            errors.add(new FieldError("vector", "a pack needs a vector or a raster part"));
        }
        boolean includePrivate = body.has("includePrivate") && body.get("includePrivate").asBoolean(false);
        boolean personalUse = body.has("personalUse") && body.get("personalUse").asBoolean(false);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "The pack spec is not valid.", errors);
        }
        return new PackSpec(name, bbox, placeId, minZoom, maxZoom, vector, raster, includePrivate, personalUse);
    }

    /** The stored form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", name);
        out.put("bbox", bbox == null ? null : List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        out.put("placeId", placeId);
        out.put("minZoom", minZoom);
        out.put("maxZoom", maxZoom);
        if (vector != null) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("sources", vector.sources());
            v.put("kinds", vector.kinds());
            v.put("asOf", vector.asOf() == null ? null : vector.asOf().toString());
            out.put("vector", v);
        } else {
            out.put("vector", null);
        }
        if (raster != null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("productId", raster.productId());
            r.put("frame", raster.frame());
            out.put("raster", r);
        } else {
            out.put("raster", null);
        }
        out.put("includePrivate", includePrivate);
        out.put("personalUse", personalUse);
        return out;
    }

    /** The spec back from {@link #toMap}. */
    public static PackSpec fromMap(Map<String, Object> map) {
        double[] bbox = null;
        if (map.get("bbox") instanceof List<?> raw && raw.size() == 4) {
            bbox = new double[] {((Number) raw.get(0)).doubleValue(), ((Number) raw.get(1)).doubleValue(),
                ((Number) raw.get(2)).doubleValue(), ((Number) raw.get(3)).doubleValue()};
        }
        Vector vector = null;
        if (map.get("vector") instanceof Map<?, ?> v) {
            vector = new Vector(ExportJobService.strings(v.get("sources")), ExportJobService.strings(v.get("kinds")),
                    v.get("asOf") == null ? null : Instant.parse(String.valueOf(v.get("asOf"))));
        }
        Raster raster = null;
        if (map.get("raster") instanceof Map<?, ?> r) {
            raster = new Raster(String.valueOf(r.get("productId")), r.get("frame") == null ? "latest"
                    : String.valueOf(r.get("frame")));
        }
        return new PackSpec((String) map.get("name"), bbox, (String) map.get("placeId"),
                ((Number) map.get("minZoom")).intValue(), ((Number) map.get("maxZoom")).intValue(), vector, raster,
                Boolean.TRUE.equals(map.get("includePrivate")), Boolean.TRUE.equals(map.get("personalUse")));
    }

    private static Integer zoom(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull() || !node.isIntegralNumber()) {
            errors.add(new FieldError(field, "is required, an integer 0–" + MAX_ZOOM));
            return null;
        }
        int value = node.asInt();
        if (value < 0 || value > MAX_ZOOM) {
            errors.add(new FieldError(field, "must be 0–" + MAX_ZOOM));
            return null;
        }
        return value;
    }

    private static List<String> strings(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }

    private static double[] box(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            errors.add(new FieldError("bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            return null;
        }
        double[] out = {node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble(), node.get(3).asDouble()};
        if (out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90 || out[0] > out[2] || out[1] > out[3]) {
            errors.add(new FieldError("bbox", "must be [minLon, minLat, maxLon, maxLat] within WGS84 bounds"));
            return null;
        }
        return out;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
