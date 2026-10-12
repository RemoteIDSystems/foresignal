package ai.foresignal.ask;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.mcp.McpToolException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.locationtech.jts.geom.Envelope;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Injects an {@link AskScope} into a read tool call as default filters (prompt 028). The model can
 * narrow the scope but never widen it.
 *
 * <p>Only parameters the tool declares in its input schema are touched:
 * <ul>
 *   <li>{@code source}/{@code kind} as arrays: absent → the scope's list; present → intersected with
 *       it (an empty intersection is refused).
 *   <li>{@code source}/{@code kind} as one string: a value outside the scope is refused.
 *   <li>{@code bbox}: absent → the scope's area; present → the intersection of both boxes (disjoint
 *       boxes are refused; when either crosses the antimeridian, the scope's own box is used).
 *   <li>{@code since}/{@code until}: clamped to the scope's window.
 * </ul>
 * A refusal is a {@link McpToolException}, returned to the model as a tool error naming the scope.
 * Tools without these parameters are filtered on their output by {@link ToolOutputFilter}.
 */
public final class ScopeInjector {

    private ScopeInjector() {}

    /**
     * The arguments to run with.
     *
     * @param scope the question's scope
     * @param schema the tool's input schema
     * @param args the model's arguments (not modified)
     * @return a copy with the scope applied
     * @throws McpToolException when the model asked for something outside the scope
     */
    public static ObjectNode apply(AskScope scope, JsonNode schema, ObjectNode args) {
        ObjectNode out = args.deepCopy();
        if (scope == null || scope.isEmpty()) {
            return out;
        }
        JsonNode properties = schema.path("properties");
        list(properties, out, "source", scope.sources(), "sources");
        list(properties, out, "kind", scope.kinds(), "kinds");
        if (scope.bbox() != null && properties.has("bbox")) {
            bbox(scope, out);
        }
        window(properties, out, scope.since(), scope.until());
        return out;
    }

    private static void list(JsonNode properties, ObjectNode out, String name, List<String> allowed, String label) {
        if (allowed.isEmpty() || !properties.has(name)) {
            return;
        }
        String type = properties.get(name).path("type").asString("");
        JsonNode given = out.get(name);
        if ("array".equals(type)) {
            ArrayNode values = out.putArray(name);
            if (given == null || given.isNull() || !given.isArray() || given.isEmpty()) {
                allowed.forEach(values::add);
                return;
            }
            List<String> kept = new ArrayList<>();
            for (JsonNode value : given) {
                if (allowed.contains(value.asString()) && !kept.contains(value.asString())) {
                    kept.add(value.asString());
                }
            }
            if (kept.isEmpty()) {
                throw new McpToolException("The question is limited to " + label + " " + allowed + "; " + name
                        + " " + given + " is outside that scope.");
            }
            kept.forEach(values::add);
        } else if (given != null && !given.isNull() && !allowed.contains(given.asString())) {
            throw new McpToolException("The question is limited to " + label + " " + allowed + "; " + name + " '"
                    + given.asString() + "' is outside that scope.");
        }
    }

    private static void bbox(AskScope scope, ObjectNode out) {
        BboxFilter allowed = scope.bboxFilter();
        JsonNode given = out.get("bbox");
        if (given == null || given.isNull() || given.asString().isBlank()) {
            out.put("bbox", scope.bbox());
            return;
        }
        BboxFilter requested;
        try {
            requested = BboxParser.parse(given.asString());
        } catch (ValidationException e) {
            // The tool reports the malformed box itself.
            return;
        }
        if (allowed.crossesAntimeridian() || requested.crossesAntimeridian()) {
            out.put("bbox", scope.bbox());
            return;
        }
        Envelope a = allowed.envelopes().getFirst();
        Envelope r = requested.envelopes().getFirst();
        Envelope both = a.intersection(r);
        if (both.isNull() || both.getWidth() <= 0 || both.getHeight() <= 0) {
            throw new McpToolException("The question is limited to the area " + scope.bbox() + "; bbox "
                    + given.asString() + " lies outside it.");
        }
        out.put("bbox", format(both));
    }

    private static void window(JsonNode properties, ObjectNode out, Instant since, Instant until) {
        Instant effectiveSince = null;
        Instant effectiveUntil = null;
        if (since != null && properties.has("since")) {
            Instant given = instant(out.get("since"));
            effectiveSince = given == null || given.isBefore(since) ? since : given;
            out.put("since", effectiveSince.toString());
        }
        if (until != null && properties.has("until")) {
            Instant given = instant(out.get("until"));
            effectiveUntil = given == null || given.isAfter(until) ? until : given;
            out.put("until", effectiveUntil.toString());
        }
        if (effectiveSince == null && properties.has("since")) {
            effectiveSince = instant(out.get("since"));
        }
        if (effectiveUntil == null && properties.has("until")) {
            effectiveUntil = instant(out.get("until"));
        }
        if (effectiveSince != null && effectiveUntil != null && !effectiveSince.isBefore(effectiveUntil)) {
            throw new McpToolException("The requested time window lies outside the question's scope (since "
                    + since + ", until " + until + ").");
        }
    }

    private static Instant instant(JsonNode node) {
        if (node == null || node.isNull() || !node.isString()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(node.asString()).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** {@code minLon,minLat,maxLon,maxLat} with up to 6 decimals. */
    static String format(Envelope e) {
        return number(e.getMinX()) + "," + number(e.getMinY()) + "," + number(e.getMaxX()) + "," + number(e.getMaxY());
    }

    private static String number(double value) {
        String text = String.format(Locale.ROOT, "%.6f", value);
        text = text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
        return text.equals("-0") ? "0" : text;
    }
}
