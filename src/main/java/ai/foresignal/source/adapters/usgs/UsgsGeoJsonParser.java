package ai.foresignal.source.adapters.usgs;

import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses a USGS earthquake summary feed (GeoJSON, API 2.7.0) into normalized records.
 *
 * <p>A pure function of the response bytes (adapter rule 3): no clock, no network, no state, so it
 * is tested against recorded fixtures with no Spring context.
 *
 * <p><strong>Time base (Core-Data-Model §2, row "Revised events"):</strong> {@code eventTime} is
 * {@code properties.time}, the origin time, and {@code observedAt} is {@code properties.updated},
 * when USGS last revised the event. That mapping is fixed for every record (adapter rule 13): a
 * feature with no {@code updated} is skipped and counted rather than falling back to {@code time}.
 *
 * <p><strong>Geometry and altitude:</strong> the geometry is a 2D point from
 * {@code coordinates[0..1]}. {@code coordinates[2]} is the hypocenter depth in kilometers, positive
 * down, and goes to the payload field {@code depth}; it is never a Z coordinate (Adapter Guide,
 * altitude convention).
 *
 * <p><strong>Payload:</strong> an allowlist of {@code properties} keys plus {@code depth}, with
 * absent and null keys omitted (adapter rule 8). Values are coerced to their declared type so a
 * magnitude sent as {@code 2} and one sent as {@code 2.0} hash the same; a value of an unexpected
 * JSON type is kept verbatim, so the fieldSpecs contract test flags the format change instead of the
 * parser hiding it.
 */
public final class UsgsGeoJsonParser {

    /** The record kind every feature becomes. */
    public static final String KIND = "earthquake";

    /** Payload key holding the hypocenter depth from {@code coordinates[2]}. */
    public static final String DEPTH = "depth";

    /**
     * The payload allowlist and each key's declared JSON type, in emission order.
     *
     * <p>{@code tz} is left out: it is always null in API 2.7.0.
     */
    public static final Map<String, FieldType> PAYLOAD_TYPES = payloadTypes();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private UsgsGeoJsonParser() {}

    /**
     * Parses a feed and returns its records.
     *
     * @param json the response body
     * @return one record per usable feature, in feed order; tombstones for deleted events
     * @throws UpstreamFormatException when the body is not JSON, not a {@code FeatureCollection}, or
     *     has no {@code features} array
     */
    public static List<NormalizedRecord> parse(byte[] json) {
        return parseWithDiagnostics(json).records();
    }

    /**
     * Parses a feed and also reports what was skipped and why.
     *
     * @param json the response body
     * @return the records and the skip counts
     * @throws UpstreamFormatException when the document shape is not a feature collection
     */
    public static ParseResult parseWithDiagnostics(byte[] json) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("USGS response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(
                    "USGS response is not valid JSON: " + e.getOriginalMessage(), text(json), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("USGS response is not a JSON object", text(json));
        }
        JsonNode type = root.get("type");
        if (type == null || !type.isString() || !"FeatureCollection".equals(type.stringValue())) {
            throw new UpstreamFormatException("USGS response is not a GeoJSON FeatureCollection", text(json));
        }
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("USGS FeatureCollection has no 'features' array", text(json));
        }

        List<NormalizedRecord> records = new ArrayList<>(features.size());
        Counts counts = new Counts();
        for (JsonNode feature : features) {
            NormalizedRecord record = parseFeature(feature, counts);
            if (record != null) {
                records.add(record);
            }
        }
        return new ParseResult(
                Collections.unmodifiableList(records),
                new ParseDiagnostics(
                        features.size(),
                        records.size(),
                        counts.tombstones,
                        counts.malformed,
                        counts.noId,
                        counts.noCoordinates,
                        counts.badCoordinates,
                        counts.noUpdated));
    }

    private static NormalizedRecord parseFeature(JsonNode feature, Counts counts) {
        if (feature == null || !feature.isObject()) {
            counts.malformed++;
            return null;
        }
        String id = scalarText(feature.get("id"));
        if (id == null || id.isBlank()) {
            counts.noId++;
            return null;
        }
        JsonNode properties = feature.get("properties");
        if (properties == null || !properties.isObject()) {
            counts.malformed++;
            return null;
        }
        Instant updated = epochMillis(properties.get("updated"));
        if (updated == null) {
            counts.noUpdated++;
            return null;
        }

        // A deleted event needs no geometry or payload: a tombstone is stored without either.
        JsonNode status = properties.get("status");
        if (status != null && status.isString() && "deleted".equals(status.stringValue())) {
            counts.tombstones++;
            return NormalizedRecord.tombstone(id, KIND, updated);
        }

        JsonNode coordinates = feature.path("geometry").path("coordinates");
        if (!coordinates.isArray()
                || coordinates.size() < 2
                || !coordinates.get(0).isNumber()
                || !coordinates.get(1).isNumber()) {
            counts.noCoordinates++;
            return null;
        }
        double longitude = coordinates.get(0).doubleValue();
        double latitude = coordinates.get(1).doubleValue();
        if (!Double.isFinite(longitude)
                || !Double.isFinite(latitude)
                || Math.abs(longitude) > 180.0
                || Math.abs(latitude) > 90.0) {
            counts.badCoordinates++;
            return null;
        }

        NormalizedRecord.Builder builder =
                NormalizedRecord.builder(id, KIND)
                        .observedAt(updated)
                        .eventTime(epochMillis(properties.get("time")))
                        .geometry(Geo.point(longitude, latitude));

        for (Map.Entry<String, FieldType> field : PAYLOAD_TYPES.entrySet()) {
            String key = field.getKey();
            JsonNode value =
                    DEPTH.equals(key)
                            ? (coordinates.size() > 2 ? coordinates.get(2) : null)
                            : properties.get(key);
            builder.put(key, coerce(value, field.getValue()));
        }
        return builder.build();
    }

    /**
     * Converts a JSON value to its declared type, or keeps it verbatim when its JSON type is not the
     * declared one; null for an absent or null value, which the record builder then omits.
     */
    static Object coerce(JsonNode node, FieldType type) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        switch (type) {
            case NUMBER -> {
                if (node.isNumber()) {
                    double value = node.doubleValue();
                    return Double.isFinite(value) ? value : null;
                }
            }
            case INTEGER, TIMESTAMP -> {
                if (node.isIntegralNumber() && node.canConvertToLong()) {
                    return node.longValue();
                }
                if (node.isNumber()) {
                    double value = node.doubleValue();
                    if (Double.isFinite(value) && value == Math.rint(value) && Math.abs(value) < 9.0e15) {
                        return (long) value;
                    }
                }
            }
            case STRING -> {
                if (node.isString()) {
                    return node.stringValue();
                }
            }
            case BOOLEAN -> {
                if (node.isBoolean()) {
                    return node.booleanValue();
                }
            }
            default -> {
                // OBJECT and ARRAY are not used by this feed.
            }
        }
        return verbatim(node);
    }

    /** A scalar kept as its natural Java type; containers are dropped rather than guessed at. */
    private static Object verbatim(JsonNode node) {
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber() && node.canConvertToLong()) {
            return node.longValue();
        }
        if (node.isNumber()) {
            double value = node.doubleValue();
            return Double.isFinite(value) ? value : null;
        }
        return null;
    }

    /** An epoch-millisecond instant, or null when the value is absent or not a number. */
    private static Instant epochMillis(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        if (!Double.isFinite(value)) {
            return null;
        }
        return Instant.ofEpochMilli(node.isIntegralNumber() && node.canConvertToLong()
                ? node.longValue()
                : (long) value);
    }

    private static String scalarText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isObject() || node.isArray()) {
            return null;
        }
        return node.isString() ? node.stringValue() : node.asString();
    }

    private static String text(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    private static Map<String, FieldType> payloadTypes() {
        Map<String, FieldType> types = new LinkedHashMap<>();
        types.put("mag", FieldType.NUMBER);
        types.put("magType", FieldType.STRING);
        types.put("place", FieldType.STRING);
        types.put("time", FieldType.TIMESTAMP);
        types.put("updated", FieldType.TIMESTAMP);
        types.put("url", FieldType.STRING);
        types.put("detail", FieldType.STRING);
        types.put("felt", FieldType.INTEGER);
        types.put("cdi", FieldType.NUMBER);
        types.put("mmi", FieldType.NUMBER);
        types.put("alert", FieldType.STRING);
        types.put("status", FieldType.STRING);
        types.put("tsunami", FieldType.INTEGER);
        types.put("sig", FieldType.INTEGER);
        types.put("net", FieldType.STRING);
        types.put("code", FieldType.STRING);
        types.put("ids", FieldType.STRING);
        types.put("sources", FieldType.STRING);
        types.put("types", FieldType.STRING);
        types.put("nst", FieldType.INTEGER);
        types.put("dmin", FieldType.NUMBER);
        types.put("rms", FieldType.NUMBER);
        types.put("gap", FieldType.NUMBER);
        types.put("type", FieldType.STRING);
        types.put("title", FieldType.STRING);
        types.put(DEPTH, FieldType.NUMBER);
        return Collections.unmodifiableMap(types);
    }

    /** Mutable tallies for one parse. */
    private static final class Counts {
        int tombstones;
        int malformed;
        int noId;
        int noCoordinates;
        int badCoordinates;
        int noUpdated;
    }

    /**
     * The records of one parse and what was skipped.
     *
     * @param records the records, in feed order
     * @param diagnostics the counts
     */
    public record ParseResult(List<NormalizedRecord> records, ParseDiagnostics diagnostics) {}

    /**
     * What one parse kept and skipped, logged by the adapter so a feed that starts dropping features
     * is visible.
     *
     * @param features features in the document
     * @param records records returned, tombstones included
     * @param tombstones features with {@code status: deleted}
     * @param skippedMalformed features that were not objects or had no {@code properties} object
     * @param skippedNoId features without an {@code id}
     * @param skippedNoCoordinates features without a geometry or with fewer than two numeric
     *     coordinates
     * @param skippedBadCoordinates features whose longitude or latitude is out of range
     * @param skippedNoUpdated features without {@code properties.updated}, which is the time base
     */
    public record ParseDiagnostics(
            int features,
            int records,
            int tombstones,
            int skippedMalformed,
            int skippedNoId,
            int skippedNoCoordinates,
            int skippedBadCoordinates,
            int skippedNoUpdated) {

        /** Every feature that did not become a record. */
        public int skipped() {
            return skippedMalformed + skippedNoId + skippedNoCoordinates + skippedBadCoordinates + skippedNoUpdated;
        }
    }
}
