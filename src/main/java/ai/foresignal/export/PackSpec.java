package ai.foresignal.export;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * A PMTiles offline pack request (prompt 111; OpenAPI {@code PackSpec}).
 *
 * @param name the pack name
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param placeId a place whose bounds the pack covers, or null
 * @param minZoom the minimum zoom (≥ 0)
 * @param maxZoom the maximum zoom (≤ 14)
 * @param vector the vector (MVT) spec, or null
 * @param raster the raster spec, or null
 * @param includePrivate include private sources (admin only)
 * @param personalUse include FORBIDDEN/LOCAL_ONLY records
 */
public record PackSpec(String name, double[] bbox, String placeId, int minZoom, int maxZoom, Vector vector,
        Raster raster, boolean includePrivate, boolean personalUse) {

    /** A vector pack. */
    public record Vector(List<String> sources, List<String> kinds, Instant asOf) {}

    /** A raster pack. */
    public record Raster(String productId, String frame) {}

    /** Parses a {@code PackSpec} body. */
    public static PackSpec parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PackSpec object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body.get("name"));
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        }
        double[] bbox = box(body.get("bbox"), errors);
        String placeId = text(body.get("placeId"));
        int minZoom = intOr(body.get("minZoom"), 0);
        int maxZoom = intOr(body.get("maxZoom"), 0);
        if (minZoom < 0) {
            errors.add(new FieldError("minZoom", "must be ≥ 0"));
        }
        if (maxZoom > 14) {
            errors.add(new FieldError("maxZoom", "must be ≤ 14"));
        }
        if (maxZoom < minZoom) {
            errors.add(new FieldError("maxZoom", "must be ≥ minZoom"));
        }
        Vector vector = null;
        if (body.has("vector") && body.get("vector").isObject()) {
            JsonNode node = body.get("vector");
            vector = new Vector(strings(node.get("sources")), strings(node.get("kinds")),
                    node.hasNonNull("asOf") ? Instant.parse(node.get("asOf").asString()) : null);
        }
        Raster raster = null;
        if (body.has("raster") && body.get("raster").isObject()) {
            JsonNode node = body.get("raster");
            String productId = text(node.get("productId"));
            if (productId == null || productId.isBlank()) {
                errors.add(new FieldError("raster.productId", "is required"));
            }
            raster = new Raster(productId, text(node.get("frame")));
        }
        boolean includePrivate = body.has("includePrivate") && body.get("includePrivate").asBoolean(false);
        boolean personalUse = body.has("personalUse") && body.get("personalUse").asBoolean(false);
        if (vector == null && raster == null) {
            errors.add(new FieldError("vector", "a pack needs a vector or raster spec"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The pack spec is not valid.", errors);
        }
        return new PackSpec(name, bbox, placeId, minZoom, maxZoom, vector, raster, includePrivate, personalUse);
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
        return new double[] {node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble(),
            node.get(3).asDouble()};
    }

    private static int intOr(JsonNode node, int fallback) {
        return node == null || node.isNull() ? fallback : node.asInt();
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
