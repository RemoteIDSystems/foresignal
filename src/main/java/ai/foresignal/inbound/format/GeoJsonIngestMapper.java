package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps the {@code native} format's two body shapes to records (prompt 024): a GeoJSON
 * FeatureCollection, or {@code {"records": [...]}} in the {@code NormalizedRecordPreview} shape.
 *
 * <p>{@code observedAt} is never defaulted here: under {@code UPSTREAM} the writer rejects a record
 * without it ({@code MISSING_OBSERVED_AT}), and under {@code RECEIVED} the writer stamps it. Doing
 * either here would make the time base depend on the format.
 */
@Component
public class GeoJsonIngestMapper {

    /** The feature properties that are record fields rather than payload. */
    static final Set<String> RESERVED_PROPERTIES =
            Set.of("kind", "observedAt", "eventTime", "validUntil", "validFrom", "tombstone", "quality");

    private final JsonMapper json;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the mapper. */
    public GeoJsonIngestMapper(JsonMapper json) {
        this.json = json;
    }

    /**
     * Maps a parsed body.
     *
     * @throws InvalidInputException when the body is neither a FeatureCollection nor a records object
     */
    public ParsedIngest map(JsonNode body, SourceDescriptor descriptor) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be a GeoJSON FeatureCollection or {\"records\": [...]}.");
        }
        if ("FeatureCollection".equals(text(body.get("type")))) {
            JsonNode features = body.get("features");
            if (features == null || !features.isArray()) {
                throw new InvalidInputException("A FeatureCollection needs a features array.");
            }
            return features(features, descriptor);
        }
        JsonNode records = body.get("records");
        if (records != null && records.isArray()) {
            return records(records, descriptor);
        }
        throw new InvalidInputException("The body must be a GeoJSON FeatureCollection or {\"records\": [...]}.");
    }

    private ParsedIngest features(JsonNode features, SourceDescriptor descriptor) {
        ParsedIngest.Builder out = ParsedIngest.builder();
        int index = 0;
        for (JsonNode feature : features) {
            mapFeature(index++, feature, descriptor, out);
        }
        out.items(index);
        return out.build();
    }

    private void mapFeature(int index, JsonNode feature, SourceDescriptor descriptor, ParsedIngest.Builder out) {
        if (feature == null || !feature.isObject()) {
            out.reject(index, null, "INVALID_ITEM", "a feature must be an object");
            return;
        }
        String externalId = idText(feature.get("id"));
        if (externalId == null) {
            out.reject(index, null, "EXTERNAL_ID_INVALID", "the feature has no id");
            return;
        }
        JsonNode properties = feature.path("properties");
        String kind = resolveKind(text(properties.get("kind")), descriptor);
        if (kind == null) {
            out.reject(index, externalId, "UNKNOWN_KIND", "kind is missing or not one of the source's kinds");
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind);
        try {
            record.observedAt(IngestTimes.parse(properties.get("observedAt")))
                    .eventTime(IngestTimes.parse(properties.get("eventTime")))
                    .validUntil(IngestTimes.parse(properties.get("validUntil")))
                    .validFrom(IngestTimes.parse(properties.get("validFrom")))
                    .tombstone(properties.path("tombstone").asBoolean(false))
                    .quality(quality(properties.get("quality")));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "INVALID_ITEM", e.getMessage());
            return;
        }
        Geometry geometry;
        try {
            geometry = geometry(feature.get("geometry"));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "GEOMETRY_INVALID", e.getMessage());
            return;
        }
        record.geometry(geometry);
        if (properties.isObject()) {
            for (Map.Entry<String, JsonNode> entry : properties.properties()) {
                if (!RESERVED_PROPERTIES.contains(entry.getKey())) {
                    record.put(entry.getKey(), toJava(entry.getValue()));
                }
            }
        }
        out.add(index, record.build());
    }

    private ParsedIngest records(JsonNode records, SourceDescriptor descriptor) {
        ParsedIngest.Builder out = ParsedIngest.builder();
        int index = 0;
        for (JsonNode node : records) {
            mapRecord(index++, node, descriptor, out);
        }
        out.items(index);
        return out.build();
    }

    private void mapRecord(int index, JsonNode node, SourceDescriptor descriptor, ParsedIngest.Builder out) {
        if (node == null || !node.isObject()) {
            out.reject(index, null, "INVALID_ITEM", "a record must be an object");
            return;
        }
        String externalId = idText(node.get("externalId"));
        if (externalId == null) {
            out.reject(index, null, "EXTERNAL_ID_INVALID", "the record has no externalId");
            return;
        }
        String kind = resolveKind(text(node.get("kind")), descriptor);
        if (kind == null) {
            out.reject(index, externalId, "UNKNOWN_KIND", "kind is missing or not one of the source's kinds");
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind);
        try {
            record.observedAt(IngestTimes.parse(node.get("observedAt")))
                    .eventTime(IngestTimes.parse(node.get("eventTime")))
                    .validUntil(IngestTimes.parse(node.get("validUntil")))
                    .validFrom(IngestTimes.parse(node.get("validFrom")))
                    .tombstone(node.path("tombstone").asBoolean(false))
                    .quality(quality(node.get("quality")));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "INVALID_ITEM", e.getMessage());
            return;
        }
        try {
            record.geometry(geometry(node.get("geometry")));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "GEOMETRY_INVALID", e.getMessage());
            return;
        }
        JsonNode payload = node.get("payload");
        if (payload != null && !payload.isNull()) {
            if (!payload.isObject()) {
                out.reject(index, externalId, "INVALID_ITEM", "payload must be an object");
                return;
            }
            for (Map.Entry<String, JsonNode> entry : payload.properties()) {
                record.put(entry.getKey(), toJava(entry.getValue()));
            }
        }
        out.add(index, record.build());
    }

    /** A GeoJSON geometry, or null for a null or absent node. */
    Geometry geometry(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        Geometry geometry = geoJson.read(node);
        if (geometry == null || geometry.isEmpty()) {
            throw new InvalidInputException("the geometry has no coordinates");
        }
        geometry.setSRID(GeoJsonReader.SRID);
        return geometry;
    }

    /** Quality from {@code accuracyMeters} and {@code confidence}; other members are ignored. */
    static Quality quality(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return Quality.none();
        }
        if (!node.isObject()) {
            throw new InvalidInputException("quality must be an object");
        }
        Double accuracy = number(node.get("accuracyMeters"));
        Double confidence = number(node.get("confidence"));
        return new Quality(accuracy, confidence, null, Map.of());
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber()) {
            throw new InvalidInputException("quality values must be numbers");
        }
        return node.doubleValue();
    }

    /** {@code node} as plain Java values (maps, lists, strings, numbers, booleans). */
    public Object toJava(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return json.convertValue(node, Object.class);
    }

    /** {@code node} as a plain Java map; empty for anything but an object. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return new LinkedHashMap<>();
        }
        Object value = json.convertValue(node, Object.class);
        return value instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
    }

    /**
     * The kind a record gets: {@code requested} when it is one of the source's kinds, the single
     * kind when none was requested and the source has exactly one, else null.
     */
    public static String resolveKind(String requested, SourceDescriptor descriptor) {
        if (requested != null && !requested.isBlank()) {
            return descriptor.kinds().contains(requested) ? requested : null;
        }
        return descriptor.kinds().size() == 1 ? descriptor.kinds().iterator().next() : null;
    }

    /** A string or number id as text; null when absent or blank. */
    static String idText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isString()) {
            String value = node.asString();
            return value.isBlank() ? null : value;
        }
        if (node.isNumber()) {
            return node.asString();
        }
        return null;
    }

    /** The text of a value node, or null. */
    static String text(JsonNode node) {
        return node == null || node.isNull() || !node.isValueNode() ? null : node.asString();
    }
}
