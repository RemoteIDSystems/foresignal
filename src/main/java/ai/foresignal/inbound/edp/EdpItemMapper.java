package ai.foresignal.inbound.edp;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps a validated EDP item to a {@link NormalizedRecord} (docs/External-Data-Protocol.md §3).
 *
 * <p>The record's external id is {@code {stream}:{id}} and its kind the stream's kind. The payload
 * holds the item's {@code properties} as top-level keys, then the item type's own fields
 * (overriding a property of the same name): an observation's {@code seriesKey}, {@code t},
 * {@code value}, {@code unit} and {@code knownAt}; a document's {@code documentType}, {@code text},
 * {@code title}, {@code url}, {@code author}, {@code language}, {@code publishedAt}, {@code about},
 * {@code media} and {@code engagement}; a forecast's {@code question} or {@code questionRef},
 * {@code probability} or {@code quantiles} and {@code madeAt}; an event's {@code upstreamEventId}.
 *
 * <p>Two more keys follow. {@value #PAYLOAD_EDP} carries provenance that is stable across repeats
 * ({@code stream}, {@code mode}, {@code itemType}, {@code originator}, {@code originatorClass},
 * {@code upstreamId}, {@code derivedFrom}, {@code aggregates}) for prompt 116.
 * {@value #PAYLOAD_TIMES} carries the receipt and the effective availability times
 * ({@code max(provider time, receipt)}, invariant EXT-4); it is volatile on the descriptor, so an
 * identical repeat stays a touch and keeps the first receipt's times.
 */
@Component
public class EdpItemMapper {

    /** The payload key of the provenance object. */
    public static final String PAYLOAD_EDP = "edp";

    /** The payload key of the receipt and effective times (volatile). */
    public static final String PAYLOAD_TIMES = "edpTimes";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final List<String> DOCUMENT_FIELDS = List.of(
            "documentType", "text", "title", "url", "author", "language", "publishedAt", "about", "media", "engagement");

    private final JsonMapper json;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the mapper. */
    public EdpItemMapper(JsonMapper json) {
        this.json = json;
    }

    /**
     * Maps {@code valid}.
     *
     * @param valid an item that passed {@link EdpItemValidator}
     * @param manifest the registered manifest
     * @param receivedAt when Foresignal received the item
     * @return the record, or null for a {@code status} item (health only)
     */
    public NormalizedRecord map(EdpValidItem valid, EdpManifestModel manifest, Instant receivedAt) {
        String type = valid.type();
        if ("status".equals(type) || valid.stream() == null) {
            return null;
        }
        JsonNode item = valid.item();
        EdpManifestModel.Stream stream = valid.stream();
        String externalId = valid.externalId();
        Instant observedAt = observedAt(item, type, stream, receivedAt);

        if ("delete".equals(type)) {
            return NormalizedRecord.tombstone(externalId, stream.kind(), observedAt == null ? receivedAt : observedAt);
        }

        NormalizedRecord.Builder builder = NormalizedRecord.builder(externalId, stream.kind()).observedAt(observedAt);
        JsonNode properties = item.get("properties");
        if (properties != null && properties.isObject()) {
            builder.putAll(json.convertValue(properties, MAP));
        }

        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", receivedAt.toString());
        switch (type) {
            case "feature" -> {
                builder.geometry(geometry(item));
                builder.eventTime(EdpTimes.parse(item, "eventTime"));
                builder.validUntil(EdpTimes.parse(item, "validUntil"));
            }
            case "event" -> {
                builder.geometry(geometry(item));
                builder.eventTime(EdpTimes.parse(item, "eventTime"));
                put(builder, item, "upstreamEventId");
            }
            case "observation" -> {
                put(builder, item, "seriesKey");
                put(builder, item, "t");
                put(builder, item, "value");
                JsonNode unit = item.get("unit");
                builder.put("unit", unit != null && !unit.isNull() ? unit.asString() : stream.unit());
                put(builder, item, "knownAt");
                Instant knownAt = EdpTimes.parse(item, "knownAt");
                times.put("effectiveKnownAt",
                        EdpTimes.effective(knownAt != null ? knownAt : observedAt, receivedAt).toString());
            }
            case "document" -> {
                builder.geometry(geometry(item));
                DOCUMENT_FIELDS.forEach(field -> put(builder, item, field));
                Instant publishedAt = EdpTimes.parse(item, "publishedAt");
                if (publishedAt != null) {
                    times.put("effectivePublishedAt", EdpTimes.effective(publishedAt, receivedAt).toString());
                }
            }
            case "forecast" -> {
                put(builder, item, "question");
                put(builder, item, "questionRef");
                put(builder, item, "probability");
                put(builder, item, "quantiles");
                put(builder, item, "madeAt");
                times.put("effectiveMadeAt",
                        EdpTimes.effective(EdpTimes.parse(item, "madeAt"), receivedAt).toString());
            }
            default -> {
                // Validated types only; nothing else reaches here.
            }
        }
        builder.put(PAYLOAD_EDP, provenance(item, type, stream, manifest));
        builder.put(PAYLOAD_TIMES, times);
        return builder.build();
    }

    /** {@code observedAt}: as given, receipt for a received-mode stream, {@code madeAt} for a forecast. */
    private static Instant observedAt(JsonNode item, String type, EdpManifestModel.Stream stream, Instant receivedAt) {
        if (stream.observedAtReceived()) {
            return receivedAt;
        }
        Instant observedAt = EdpTimes.parse(item, "observedAt");
        if (observedAt == null && "forecast".equals(type)) {
            observedAt = EdpTimes.parse(item, "madeAt");
        }
        return observedAt;
    }

    private Map<String, Object> provenance(JsonNode item, String type, EdpManifestModel.Stream stream,
            EdpManifestModel manifest) {
        JsonNode origin = item.get("origin");
        boolean hasOrigin = origin != null && origin.isObject();
        Map<String, Object> edp = new LinkedHashMap<>();
        edp.put("stream", stream.id());
        edp.put("mode", stream.mode());
        edp.put("itemType", type);
        putIfNotNull(edp, "originator", firstText(hasOrigin ? origin.get("originator") : null, manifest.originatorId()));
        putIfNotNull(edp, "originatorClass", firstText(hasOrigin ? origin.get("class") : null, manifest.originatorClass()));
        putIfNotNull(edp, "upstreamId", firstText(hasOrigin ? origin.get("upstreamId") : null,
                textOrNull(item.get("upstreamEventId"))));
        JsonNode derivedFrom = hasOrigin ? origin.get("derivedFrom") : null;
        if (derivedFrom != null && derivedFrom.isArray() && derivedFrom.size() > 0) {
            edp.put("derivedFrom", json.convertValue(derivedFrom, List.class));
        }
        if (!manifest.aggregates().isEmpty()) {
            edp.put("aggregates", manifest.aggregates());
        }
        return edp;
    }

    private org.locationtech.jts.geom.Geometry geometry(JsonNode item) {
        JsonNode geometry = item.get("geometry");
        return geometry == null || geometry.isNull() ? null : geoJson.read(geometry);
    }

    private void put(NormalizedRecord.Builder builder, JsonNode item, String field) {
        JsonNode value = item.get(field);
        if (value == null || value.isNull()) {
            return;
        }
        builder.put(field, json.convertValue(value, Object.class));
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static String firstText(JsonNode preferred, String fallback) {
        String value = textOrNull(preferred);
        return value != null ? value : fallback;
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() || !node.isValueNode() ? null : node.asString();
    }
}
