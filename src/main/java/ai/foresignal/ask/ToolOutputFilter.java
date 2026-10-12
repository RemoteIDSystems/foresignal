package ai.foresignal.ask;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.source.SourceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Filters a tool result before it is sent to the provider (prompt 028): every object that names a
 * record source the destination may not receive ({@code EgressPolicy}'s LLM-context path) or a source
 * outside the question's scope is removed, and so is every object whose position lies outside the
 * scope's area. This is the second line after the tools' own source exclusion: what a tool returns
 * for any reason, the model only sees when it may.
 *
 * <p>An object's source is its {@code source} member, else the source part of a
 * {@code source:externalId} in {@code docId} (search hits of type {@code record}), {@code id} or
 * {@code record}, when that part is a registered source. Its position is {@code lon}/{@code lat}, or a
 * GeoJSON {@code Point} in {@code geometry}. Removed array items are dropped; a removed object member
 * becomes null; a removed top-level object becomes a {@code withheld} notice.
 */
@Component
public class ToolOutputFilter {

    private final SourceRegistry registry;
    private final JsonMapper json;

    /** Creates the filter. */
    public ToolOutputFilter(SourceRegistry registry, JsonMapper json) {
        this.registry = registry;
        this.json = json;
    }

    /**
     * The filtered result.
     *
     * @param output the tool's output (not modified)
     * @param sourceAllowed whether records of a source may be shown to the model
     * @param area the scope's area, or null
     * @return the filtered copy and how many objects were removed
     */
    public Filtered filter(JsonNode output, Predicate<String> sourceAllowed, BboxFilter area) {
        if (output == null || !output.isContainer()) {
            return new Filtered(output, 0);
        }
        AtomicInteger removed = new AtomicInteger();
        JsonNode copy = output.deepCopy();
        if (copy.isObject() && removed(copy, sourceAllowed, area)) {
            ObjectNode notice = json.createObjectNode();
            notice.put("withheld", true);
            notice.put("notice", "The result is outside the question's scope or may not be shared with this model.");
            return new Filtered(notice, 1);
        }
        walk(copy, sourceAllowed, area, removed);
        return new Filtered(copy, removed.get());
    }

    private void walk(JsonNode node, Predicate<String> sourceAllowed, BboxFilter area, AtomicInteger removed) {
        if (node instanceof ArrayNode array) {
            for (int i = array.size() - 1; i >= 0; i--) {
                JsonNode item = array.get(i);
                if (item.isObject() && removed(item, sourceAllowed, area)) {
                    array.remove(i);
                    removed.incrementAndGet();
                } else {
                    walk(item, sourceAllowed, area, removed);
                }
            }
        } else if (node instanceof ObjectNode object) {
            List<String> names = new ArrayList<>(object.propertyNames());
            for (String name : names) {
                JsonNode child = object.get(name);
                if (child.isObject() && !"geometry".equals(name) && removed(child, sourceAllowed, area)) {
                    object.putNull(name);
                    removed.incrementAndGet();
                } else {
                    walk(child, sourceAllowed, area, removed);
                }
            }
        }
    }

    private boolean removed(JsonNode object, Predicate<String> sourceAllowed, BboxFilter area) {
        String source = sourceOf(object);
        if (source != null && !sourceAllowed.test(source)) {
            return true;
        }
        if (area != null) {
            double[] point = pointOf(object);
            return point != null && !area.contains(point[0], point[1]);
        }
        return false;
    }

    /** The record source an object names, or null. */
    String sourceOf(JsonNode object) {
        JsonNode source = object.get("source");
        if (source != null && source.isString()) {
            return source.asString();
        }
        if (object.has("docType") && !"record".equals(object.path("docType").asString())) {
            return null;
        }
        for (String member : List.of("docId", "id", "record")) {
            JsonNode value = object.get(member);
            if (value != null && value.isString()) {
                String text = value.asString();
                int colon = text.indexOf(':');
                if (colon > 0 && registry.contains(text.substring(0, colon))) {
                    return text.substring(0, colon);
                }
            }
        }
        return null;
    }

    private static double[] pointOf(JsonNode object) {
        JsonNode lon = object.get("lon");
        JsonNode lat = object.get("lat");
        if (lon != null && lat != null && lon.isNumber() && lat.isNumber()) {
            return new double[] {lon.asDouble(), lat.asDouble()};
        }
        JsonNode geometry = object.get("geometry");
        if (geometry != null && geometry.isObject() && "Point".equals(geometry.path("type").asString())) {
            JsonNode coordinates = geometry.get("coordinates");
            if (coordinates != null && coordinates.isArray() && coordinates.size() >= 2
                    && coordinates.get(0).isNumber() && coordinates.get(1).isNumber()) {
                return new double[] {coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
            }
        }
        return null;
    }

    /**
     * A filtered result.
     *
     * @param output the filtered output
     * @param removed how many objects were removed
     */
    public record Filtered(JsonNode output, int removed) {}
}
