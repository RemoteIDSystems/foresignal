package ai.foresignal.api.mapper;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.RawJson;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps {@link RecordRow}s to the API shapes of {@code docs/OpenAPI.json}: {@code Record},
 * {@code RecordVersion}, {@code Change}, GeoJSON {@code Feature} and CSV rows.
 *
 * <p>Items are ordered maps rather than fixed records so that one mapping honors {@code fields=}
 * for every format: a projected response carries only the selected properties. Geometry, payload,
 * quality and observers are PostgreSQL JSON text written verbatim ({@link RawJson}), except that a
 * projected payload (selected {@code payload.<path>} entries, keyed by dot path in SQL) is rebuilt
 * as nested objects here.
 *
 * <p>The title is {@code record_head.title} for current rows. Versions have no stored title, so it
 * is resolved from the version's payload with the shared {@link TitleResolver} and the source's
 * {@code titleField}.
 */
@Component
public class RecordMapper {

    /** CSV columns when {@code fields=} is absent, for records, versions and history. */
    public static final List<String> DEFAULT_CSV_COLUMNS =
            List.of("id", "source", "externalId", "kind", "title", "version", "seq", "observedAt", "validUntil", "geometry");

    /** CSV columns when {@code fields=} is absent, for changes. */
    public static final List<String> DEFAULT_CHANGE_CSV_COLUMNS = List.of(
            "seq", "source", "externalId", "kind", "version", "observedAt", "tombstone", "persistReason", "validUntil",
            "geometry");

    /** Top-level properties in output order. */
    static final List<String> ORDER = List.of(
            "id", "source", "externalId", "kind", "title", "version", "seq", "observedAt", "persistedObservedAt",
            "eventTime", "validFrom", "validUntil", "tombstone", "live", "qaFlags", "quality", "geometry", "payload",
            "attribution", "distanceKm", "persistReason", "prevSeq", "historyTier", "ingestedAt", "receivedAt",
            "batchId", "downsampled", "observer", "bbox");

    private final SourceRegistry registry;
    private final JsonMapper json;

    /** Creates the mapper. */
    public RecordMapper(SourceRegistry registry, JsonMapper json) {
        this.registry = registry;
        this.json = json;
    }

    /** What a {@code Record} item carries beyond the base fields. */
    public enum Shape {
        /** Current state (head plus live). */
        CURRENT,
        /** An asOf result: a version presented as a record, with {@code ingestedAt}. */
        AS_OF,
        /** A {@code RecordVersion} (versions endpoint and history). */
        VERSION
    }

    /**
     * A {@code Record} or {@code RecordVersion} item.
     *
     * @param row the row
     * @param fields the projection
     * @param shape which fields apply
     */
    public Map<String, Object> record(RecordRow row, FieldSelection fields, Shape shape) {
        Map<String, Object> all = new LinkedHashMap<>();
        all.put("id", row.id());
        all.put("source", row.sourceId());
        all.put("externalId", row.externalId());
        all.put("kind", row.kind());
        all.put("title", title(row, shape));
        all.put("version", row.version());
        all.put("seq", row.seq());
        all.put("observedAt", row.observedAt());
        all.put("persistedObservedAt", row.persistedObservedAt());
        all.put("eventTime", row.eventTime());
        all.put("validFrom", row.validFrom());
        all.put("validUntil", row.validUntil());
        all.put("tombstone", row.tombstone());
        all.put("live", shape == Shape.CURRENT && Boolean.TRUE.equals(row.live()));
        all.put("qaFlags", row.qaFlags());
        all.put("quality", RawJson.of(row.quality()));
        all.put("geometry", RawJson.of(row.geometry()));
        all.put("payload", payload(row, fields));
        all.put("attribution", attribution(row.sourceId()));
        if (row.distanceMeters() != null) {
            all.put("distanceKm", row.distanceMeters() / 1000.0);
        }
        if (shape != Shape.CURRENT) {
            all.put("ingestedAt", row.ingestedAt());
        }
        if (shape == Shape.VERSION) {
            all.put("persistReason", persistReason(row));
            all.put("prevSeq", row.prevSeq());
            all.put("historyTier", historyTier(row));
            all.put("receivedAt", row.receivedAt());
            all.put("batchId", row.batchId());
            all.put("downsampled", false);
        }
        if (row.observer() != null) {
            all.put("observer", RawJson.of(row.observer()));
        }
        return project(all, fields);
    }

    /** A {@code Change} item; {@code payload} is present only when it was loaded. */
    public Map<String, Object> change(RecordRow row, FieldSelection fields) {
        Map<String, Object> all = new LinkedHashMap<>();
        all.put("seq", row.seq());
        all.put("source", row.sourceId());
        all.put("externalId", row.externalId());
        all.put("kind", row.kind());
        all.put("version", row.version());
        all.put("observedAt", row.observedAt());
        all.put("eventTime", row.eventTime());
        all.put("validFrom", row.validFrom());
        all.put("validUntil", row.validUntil());
        all.put("tombstone", row.tombstone());
        all.put("qaFlags", row.qaFlags());
        all.put("geometry", RawJson.of(row.geometry()));
        if (row.payload() != null) {
            all.put("payload", payload(row, fields));
        }
        all.put("prevSeq", row.prevSeq());
        all.put("historyTier", historyTier(row));
        all.put("persistReason", persistReason(row));
        all.put("bbox", row.bbox() == null ? null : List.of(row.bbox()[0], row.bbox()[1], row.bbox()[2], row.bbox()[3]));
        return project(all, fields);
    }

    /**
     * An SSE {@code change} event (prompt 015): the whole {@link #change} item plus the SSE-only
     * {@code prevPersistedBbox}, {@code prevLiveBbox} and {@code leftFilter}.
     *
     * @param row the change row; its payload is present only when it was loaded
     * @param prevPersisted the box of the previously persisted geometry, or null
     * @param prevLive the box of the live geometry before this version, or null
     * @param leftFilter true when only a previous box matched the client's spatial filter
     */
    public Map<String, Object> streamChange(RecordRow row, Bbox prevPersisted, Bbox prevLive, boolean leftFilter) {
        Map<String, Object> item = change(row, FieldSelection.ALL);
        item.put("prevPersistedBbox", box(prevPersisted));
        item.put("prevLiveBbox", box(prevLive));
        item.put("leftFilter", leftFilter);
        return item;
    }

    /**
     * An SSE {@code change} event built from the bus summary alone, for a version that could not be
     * loaded (retention removed it between commit and delivery): no geometry and no payload.
     *
     * @param summary the bus summary
     * @param leftFilter true when only a previous box matched the client's spatial filter
     */
    public Map<String, Object> streamChange(ChangeSummary summary, boolean leftFilter) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("seq", summary.seq());
        item.put("source", summary.sourceId());
        item.put("externalId", summary.externalId());
        item.put("kind", summary.kind());
        item.put("version", summary.version());
        item.put("observedAt", summary.observedAt());
        item.put("eventTime", summary.eventTime());
        item.put("validUntil", summary.validUntil());
        item.put("tombstone", summary.tombstone());
        item.put("qaFlags", summary.qaFlags());
        item.put("geometry", null);
        item.put("prevSeq", summary.prevSeq());
        item.put("historyTier", summary.historyTier() == null ? null : summary.historyTier().name());
        item.put("persistReason", summary.persistReason() == null ? null : summary.persistReason().name());
        item.put("bbox", box(summary.bbox()));
        item.put("prevPersistedBbox", box(summary.prevPersistedBbox()));
        item.put("prevLiveBbox", box(summary.prevLiveBbox()));
        item.put("leftFilter", leftFilter);
        return item;
    }

    /** {@code [minX, minY, maxX, maxY]}, or null. */
    public static List<Double> box(Bbox bbox) {
        return bbox == null ? null : List.of(bbox.minX(), bbox.minY(), bbox.maxX(), bbox.maxY());
    }

    /** {@code [minX, minY, maxX, maxY]} of a selected box array, or null. */
    public static Bbox bbox(double[] values) {
        return values == null ? null : new Bbox(values[0], values[1], values[2], values[3]);
    }

    /**
     * A GeoJSON {@code Feature}: {@code properties = {source, externalId, kind, title, version, seq,
     * observedAt, validUntil, live, payload}} plus {@code distanceKm} under {@code sort=distance}.
     * Null geometry is allowed (RFC 7946).
     */
    public Map<String, Object> feature(RecordRow row, FieldSelection fields, Shape shape) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("source", row.sourceId());
        properties.put("externalId", row.externalId());
        properties.put("kind", row.kind());
        properties.put("title", title(row, shape));
        properties.put("version", row.version());
        properties.put("seq", row.seq());
        properties.put("observedAt", row.observedAt());
        properties.put("validUntil", row.validUntil());
        properties.put("live", shape == Shape.CURRENT && Boolean.TRUE.equals(row.live()));
        properties.put("payload", payload(row, fields));
        if (row.distanceMeters() != null) {
            properties.put("distanceKm", row.distanceMeters() / 1000.0);
        }
        if (shape != Shape.CURRENT) {
            properties.put("ingestedAt", row.ingestedAt());
        }
        if (shape == Shape.VERSION) {
            properties.put("persistReason", persistReason(row));
        }
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", row.id());
        feature.put("geometry", fields.includesGeometry() ? RawJson.of(row.geometry()) : null);
        feature.put("properties", project(properties, fields));
        return feature;
    }

    /** A GeoJSON {@code Feature} for a change (used by {@code geojsonseq} on the change feed). */
    public Map<String, Object> changeFeature(RecordRow row, FieldSelection fields) {
        Map<String, Object> properties = change(row, fields);
        properties.remove("geometry");
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", row.id());
        feature.put("geometry", fields.includesGeometry() ? RawJson.of(row.geometry()) : null);
        feature.put("properties", properties);
        return feature;
    }

    /**
     * The CSV header for {@code fields}: the selected top-level properties in output order, then one
     * {@code payload.<path>} column per selected path; {@code defaults} when nothing was selected.
     */
    public static List<String> csvColumns(FieldSelection fields, List<String> defaults) {
        if (fields.all()) {
            return defaults;
        }
        List<String> columns = new ArrayList<>();
        for (String name : ORDER) {
            if (fields.topLevel().contains(name)) {
                columns.add(name);
            }
        }
        for (String path : fields.payloadPaths()) {
            columns.add("payload." + path);
        }
        return columns;
    }

    /**
     * One CSV row over {@code columns} from an item built by {@link #record} or {@link #change}:
     * scalars as text, geometry as the WKT the query produced, objects as JSON text.
     */
    public Map<String, Object> csvRow(Map<String, Object> item, List<String> columns) {
        Map<String, Object> out = new LinkedHashMap<>();
        Object payload = item.get("payload");
        JsonNode payloadNode = null;
        for (String column : columns) {
            Object value;
            if (column.startsWith("payload.")) {
                if (payloadNode == null && payload != null) {
                    payloadNode = toNode(payload);
                }
                value = payloadNode == null ? null : at(payloadNode, column.substring("payload.".length()));
            } else {
                value = item.get(column);
            }
            out.put(column, csvValue(value));
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private Object payload(RecordRow row, FieldSelection fields) {
        if (row.payload() == null) {
            return null;
        }
        if (!fields.projectsPayload()) {
            return RawJson.of(row.payload());
        }
        JsonNode flat = parse(row.payload());
        ObjectNode nested = json.createObjectNode();
        if (flat != null && flat.isObject()) {
            for (String path : fields.payloadPaths()) {
                JsonNode value = flat.get(path);
                if (value == null || value.isNull()) {
                    continue;
                }
                String[] segments = path.split("\\.");
                ObjectNode parent = nested;
                for (int i = 0; i < segments.length - 1; i++) {
                    JsonNode child = parent.get(segments[i]);
                    ObjectNode childObject;
                    if (child instanceof ObjectNode existing) {
                        childObject = existing;
                    } else {
                        childObject = json.createObjectNode();
                        parent.set(segments[i], childObject);
                    }
                    parent = childObject;
                }
                parent.set(segments[segments.length - 1], value);
            }
        }
        return nested;
    }

    private String title(RecordRow row, Shape shape) {
        if (shape == Shape.CURRENT) {
            return row.title();
        }
        String text = row.titlePayload() != null ? row.titlePayload() : row.payload();
        if (text == null) {
            return null;
        }
        Map<String, Object> payload = parseMap(text);
        if (payload == null) {
            return null;
        }
        return TitleResolver.resolve(descriptor(row.sourceId()) == null ? null : descriptor(row.sourceId()).titleField(), payload);
    }

    private String attribution(String sourceId) {
        SourceDescriptor descriptor = descriptor(sourceId);
        return descriptor == null ? null : descriptor.attribution();
    }

    private SourceDescriptor descriptor(String sourceId) {
        return registry.find(sourceId).map(SourceRegistry.Registration::descriptor).orElse(null);
    }

    private static String persistReason(RecordRow row) {
        return row.persistReason() == null ? null : PersistReason.fromCode(row.persistReason()).name();
    }

    private static String historyTier(RecordRow row) {
        return row.historyTier() == null ? null : HistoryTier.fromCode(row.historyTier()).name();
    }

    private static Map<String, Object> project(Map<String, Object> all, FieldSelection fields) {
        if (fields.all()) {
            return all;
        }
        Map<String, Object> projected = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            if (fields.includes(entry.getKey())) {
                projected.put(entry.getKey(), entry.getValue());
            }
        }
        return projected;
    }

    private JsonNode toNode(Object payload) {
        if (payload instanceof JsonNode node) {
            return node;
        }
        if (payload instanceof RawJson raw) {
            return parse(raw.json());
        }
        return json.valueToTree(payload);
    }

    private static Object at(JsonNode root, String path) {
        JsonNode node = root;
        for (String segment : path.split("\\.")) {
            if (node == null || !node.isObject()) {
                return null;
            }
            node = node.get(segment);
        }
        return node;
    }

    private Object csvValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof RawJson raw) {
            String text = raw.json();
            return text.startsWith("\"") ? parse(text).asString() : text;
        }
        if (value instanceof JsonNode node) {
            if (node.isNull()) {
                return null;
            }
            if (node.isValueNode()) {
                return node.isString() ? node.asString() : node.toString();
            }
            return node.toString();
        }
        if (value instanceof List<?> || value instanceof Map<?, ?>) {
            return json.writeValueAsString(value);
        }
        return value.toString();
    }

    private JsonNode parse(String text) {
        try {
            return json.readTree(text);
        } catch (JacksonException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(String text) {
        try {
            Object value = json.readValue(text, Object.class);
            return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
        } catch (JacksonException e) {
            return null;
        }
    }
}
