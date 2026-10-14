package ai.foresignal.mcp;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Keeps MCP tool output small enough for an agent's context (Architecture §18, prompt 025,
 * Weather-Imagery-Design §6.9).
 *
 * <ul>
 *   <li>{@code detail=short} (the default): compact items, a 4 KB cap for the whole result. A short
 *       record is {@code {id: "source:externalId", seq, version, kind, title, observedAt, live,
 *       lon/lat or bbox, fields}}: the geometry is reduced to its centroid (plus a bbox for
 *       non-points), {@code fields} holds up to {@value #MAX_KEY_FIELDS} scalar payload values
 *       (declared {@code fieldSpecs} first, then significant fields, then the rest), and strings
 *       are cut to {@value #MAX_TEXT_CHARS} characters.
 *   <li>{@code detail=long}: the full items, capped at 50 KB.
 *   <li>Lists carry {@code total} and {@code truncated}; when the cap cuts a list, items are
 *       dropped from the end and {@code notice} says so. An object over the cap loses its largest
 *       members first ({@code payload}, {@code geometry}, …), each replaced by a note.
 * </ul>
 *
 * <p>Nothing leaves at more than {@value #LONG_LIMIT_BYTES} bytes. Titles come from the shared
 * {@code TitleResolver} (stored {@code record_head.title}, or resolved by {@code RecordMapper}),
 * the same resolver REST GeoJSON uses.
 */
@Component
public class McpOutputShaper {

    /** The cap of a {@code short} result. */
    public static final int COMPACT_LIMIT_BYTES = 4 * 1024;

    /** The cap of a {@code long} result, and of anything else. */
    public static final int LONG_LIMIT_BYTES = 50 * 1024;

    /** Room kept for the {@code notice} member a cut adds. */
    static final int NOTICE_RESERVE_BYTES = 320;

    /** Scalar payload values in a short record. */
    public static final int MAX_KEY_FIELDS = 8;

    /** The longest string in a short item. */
    public static final int MAX_TEXT_CHARS = 200;

    /** Members dropped first when an object is over its cap, largest first among these. */
    static final List<String> BULKY_MEMBERS =
            List.of("payload", "geometry", "record", "recordSnapshot", "matches", "quality", "observer", "items");

    /** How much output a tool returns. */
    public enum Detail {
        /** Compact items, 4 KB. */
        SHORT,
        /** Full items, 50 KB. */
        LONG;

        /** The {@code detail} argument of {@code args}; short when absent. */
        public static Detail of(JsonNode args) {
            String value = McpArgs.text(args, "detail");
            if (value == null || value.equalsIgnoreCase("short")) {
                return SHORT;
            }
            if (value.equalsIgnoreCase("long")) {
                return LONG;
            }
            throw new McpToolException("The argument 'detail' must be short or long; got '" + value + "'.");
        }

        /** The byte cap of this detail level. */
        public int limit() {
            return this == SHORT ? COMPACT_LIMIT_BYTES : LONG_LIMIT_BYTES;
        }
    }

    private final JsonMapper json;
    private final SourceRegistry registry;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the shaper. */
    public McpOutputShaper(JsonMapper json, SourceRegistry registry) {
        this.json = json;
        this.registry = registry;
    }

    /** The JSON tree of a mapped API value (maps, DTOs, raw JSON), serialized as REST would. */
    public JsonNode tree(Object value) {
        return json.readTree(json.writeValueAsString(value));
    }

    /** The JSON tree of a mapped object. */
    public ObjectNode objectTree(Object value) {
        JsonNode node = tree(value);
        if (node instanceof ObjectNode object) {
            return object;
        }
        throw new IllegalArgumentException("not a JSON object: " + node.getNodeType());
    }

    /** A new empty object. */
    public ObjectNode object() {
        return json.createObjectNode();
    }

    /**
     * A record item at {@code detail}: the full {@code Record}/{@code RecordVersion}/{@code Change}
     * object for long, the compact form for short.
     *
     * @param mapped the item as {@code RecordMapper} produced it
     */
    public ObjectNode record(Map<String, Object> mapped, Detail detail) {
        ObjectNode full = objectTree(mapped);
        if (detail == Detail.LONG) {
            llmNotice(textOf(full, "source"), full);
            return full;
        }
        return shortRecord(full);
    }

    /** The compact form of a full record, version or change item. */
    public ObjectNode shortRecord(ObjectNode full) {
        ObjectNode out = json.createObjectNode();
        String source = textOf(full, "source");
        String externalId = textOf(full, "externalId");
        String id = textOf(full, "id");
        out.put("id", id != null ? id : source + ":" + externalId);
        copy(full, out, "seq");
        copy(full, out, "version");
        copy(full, out, "kind");
        if (full.hasNonNull("title")) {
            out.put("title", cut(full.get("title").asString()));
        }
        copy(full, out, "observedAt");
        boolean live = full.path("live").asBoolean(false);
        if (full.has("live")) {
            out.put("live", live);
        }
        if (live) {
            copy(full, out, "persistedObservedAt");
        }
        copyIfPresent(full, out, "eventTime");
        copyIfPresent(full, out, "validUntil");
        if (full.path("tombstone").asBoolean(false)) {
            out.put("tombstone", true);
        }
        copyIfPresent(full, out, "persistReason");
        copyIfPresent(full, out, "distanceKm");
        // Prompt 040: the near text of a tagged record ("12 km NNE of Austin, TX"; GeoNames, CC BY 4.0).
        JsonNode nearText = full.path("region").path("near").path("text");
        if (nearText.isString() && !nearText.asString().isBlank()) {
            out.put("near", nearText.asString());
        }
        centroid(full.get("geometry"), full.get("bbox"), out);
        ObjectNode fields = keyFields(source, full.get("payload"));
        if (!fields.isEmpty()) {
            out.set("fields", fields);
        }
        llmNotice(source, out);
        return out;
    }

    /** Appends the source's {@code llmNotice} (prompt 090), when it has one, to a record item. */
    void llmNotice(String sourceId, ObjectNode out) {
        if (sourceId == null || out.has("llmNotice")) {
            return;
        }
        String notice = registry.find(sourceId).map(SourceRegistry.Registration::descriptor)
                .map(SourceDescriptor::llmNotice).orElse(null);
        if (notice != null && !notice.isBlank()) {
            out.put("llmNotice", notice);
        }
    }

    /**
     * Writes {@code lon}/{@code lat} (the centroid, 5 decimals) and, for anything but a point, a
     * {@code bbox}. Falls back to the center of {@code bbox} when there is no geometry.
     */
    void centroid(JsonNode geometry, JsonNode bbox, ObjectNode out) {
        if (geometry != null && geometry.isObject()) {
            try {
                Geometry g = geoJson.read(geometry);
                if (g != null && !g.isEmpty()) {
                    Point c = g.getCentroid();
                    out.put("lon", round(c.getX()));
                    out.put("lat", round(c.getY()));
                    if (!(g instanceof Point)) {
                        Envelope e = g.getEnvelopeInternal();
                        ArrayNode box = out.putArray("bbox");
                        box.add(round(e.getMinX())).add(round(e.getMinY())).add(round(e.getMaxX()))
                                .add(round(e.getMaxY()));
                    }
                    return;
                }
            } catch (RuntimeException e) {
                // An unreadable geometry is left out of the short form rather than failing the call.
            }
        }
        if (bbox != null && bbox.isArray() && bbox.size() == 4) {
            double minX = bbox.get(0).asDouble();
            double minY = bbox.get(1).asDouble();
            double maxX = bbox.get(2).asDouble();
            double maxY = bbox.get(3).asDouble();
            out.put("lon", round((minX + maxX) / 2));
            out.put("lat", round((minY + maxY) / 2));
            if (minX != maxX || minY != maxY) {
                out.set("bbox", bbox.deepCopy());
            }
        }
    }

    /** Up to {@value #MAX_KEY_FIELDS} scalar payload values, declared fields first. */
    ObjectNode keyFields(String sourceId, JsonNode payload) {
        ObjectNode out = json.createObjectNode();
        if (payload == null || !payload.isObject()) {
            return out;
        }
        Set<String> order = new LinkedHashSet<>();
        SourceDescriptor descriptor = sourceId == null ? null
                : registry.find(sourceId).map(SourceRegistry.Registration::descriptor).orElse(null);
        if (descriptor != null) {
            if (descriptor.fieldSpecs() != null) {
                order.addAll(descriptor.fieldSpecs().keySet());
            }
            if (descriptor.significantFields() != null) {
                order.addAll(descriptor.significantFields());
            }
        }
        payload.propertyNames().forEach(order::add);
        for (String name : order) {
            if (out.size() >= MAX_KEY_FIELDS) {
                break;
            }
            JsonNode value = payload.get(name);
            if (value == null || value.isNull() || value.isContainer()) {
                continue;
            }
            if (value.isString()) {
                out.put(name, cut(value.asString()));
            } else {
                out.set(name, value);
            }
        }
        return out;
    }

    /**
     * A list result capped at {@code detail}'s limit.
     *
     * @param items the items, already in their short or long form
     * @param total the items the query produced (before capping)
     * @param more true when more matched than {@code items} (a limit was hit)
     * @param extra members to add before the items (watermarks, cursors), or null
     */
    public ObjectNode list(List<? extends JsonNode> items, long total, boolean more, Detail detail,
            Map<String, ?> extra) {
        ObjectNode out = json.createObjectNode();
        if (extra != null) {
            extra.forEach((k, v) -> out.set(k, v instanceof JsonNode node ? node : json.valueToTree(v)));
        }
        out.put("total", total);
        out.put("truncated", more);
        ArrayNode array = out.putArray("items");
        int limit = detail.limit() - NOTICE_RESERVE_BYTES;
        int used = size(out);
        int kept = 0;
        for (JsonNode item : items) {
            JsonNode fitted = item;
            int itemSize = size(item) + 1;
            if (used + itemSize > limit && kept == 0 && item instanceof ObjectNode object) {
                // A single item over the whole budget is trimmed rather than dropped.
                fitted = fit(object.deepCopy(), limit - used);
                itemSize = size(fitted) + 1;
            }
            if (used + itemSize > limit) {
                break;
            }
            array.add(fitted);
            used += itemSize;
            kept++;
        }
        if (kept < items.size()) {
            out.put("truncated", true);
            out.put("notice", "Output cut to " + kept + " of " + items.size() + " items to stay within the "
                    + (detail.limit() / 1024) + " KB limit of detail=" + detail.name().toLowerCase()
                    + ". Narrow the filters or lower limit"
                    + (detail == Detail.SHORT ? ", or ask for detail=long." : "."));
        }
        return out;
    }

    /** An object result capped at {@code detail}'s limit. */
    public ObjectNode object(ObjectNode value, Detail detail) {
        return fit(value, detail.limit() - NOTICE_RESERVE_BYTES);
    }

    /**
     * {@code value} trimmed to at most {@code budget} bytes: its bulkiest known members are replaced
     * by a note, then any remaining container members, then long strings are cut.
     */
    ObjectNode fit(ObjectNode value, int budget) {
        if (size(value) <= budget) {
            return value;
        }
        List<String> omitted = new ArrayList<>();
        for (String member : bulkyFirst(value)) {
            if (size(value) <= budget) {
                break;
            }
            JsonNode node = value.get(member);
            value.put(member, "omitted: " + size(node) + " bytes exceed the size limit");
            omitted.add(member);
        }
        if (size(value) > budget) {
            for (String member : new ArrayList<>(value.propertyNames())) {
                if (size(value) <= budget) {
                    break;
                }
                JsonNode node = value.get(member);
                if (node.isContainer()) {
                    value.put(member, "omitted: " + size(node) + " bytes exceed the size limit");
                    omitted.add(member);
                } else if (node.isString() && node.asString().length() > MAX_TEXT_CHARS) {
                    value.put(member, cut(node.asString()));
                }
            }
        }
        if (!omitted.isEmpty()) {
            value.put("notice", "Trimmed to fit the size limit; omitted " + String.join(", ", omitted)
                    + ". Ask for a narrower result to see them.");
        }
        if (size(value) > budget) {
            // Still too big (very many scalar members): keep the identity only.
            ObjectNode minimal = json.createObjectNode();
            for (String member : List.of("id", "seq", "version", "kind", "title", "observedAt")) {
                copyIfPresent(value, minimal, member);
            }
            minimal.put("notice", "The object exceeds the size limit and was reduced to its identity.");
            return minimal;
        }
        return value;
    }

    /** The bulky members present in {@code value}, largest first. */
    private List<String> bulkyFirst(ObjectNode value) {
        List<String> present = new ArrayList<>();
        for (String member : BULKY_MEMBERS) {
            if (value.has(member) && value.get(member).isContainer()) {
                present.add(member);
            }
        }
        present.sort((a, b) -> Integer.compare(size(value.get(b)), size(value.get(a))));
        return present;
    }

    /** The UTF-8 size of {@code node}'s JSON text. */
    public static int size(JsonNode node) {
        return node == null ? 4 : node.toString().getBytes(StandardCharsets.UTF_8).length;
    }

    /** {@code text} cut to {@value #MAX_TEXT_CHARS} characters with an ellipsis. */
    static String cut(String text) {
        if (text == null || text.length() <= MAX_TEXT_CHARS) {
            return text;
        }
        return text.substring(0, MAX_TEXT_CHARS - 1) + "…";
    }

    private static double round(double value) {
        return Math.round(value * 100_000d) / 100_000d;
    }

    private static String textOf(JsonNode node, String member) {
        JsonNode value = node.get(member);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static void copy(JsonNode from, ObjectNode to, String member) {
        JsonNode value = from.get(member);
        if (value == null) {
            to.putNull(member);
        } else {
            to.set(member, value);
        }
    }

    private static void copyIfPresent(JsonNode from, ObjectNode to, String member) {
        JsonNode value = from.get(member);
        if (value != null && !value.isNull()) {
            to.set(member, value);
        }
    }
}
