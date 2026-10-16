package ai.foresignal.analyst;

import ai.foresignal.ask.CitableExtractor;
import ai.foresignal.ask.CitationSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.JsonNode;

/**
 * What one analyst run may cite (prompt 102, principle 1): exactly the items returned by a tool in that
 * run, never a substring of tool output. Records and alerts are collected by the 028
 * {@link CitableExtractor}s into a {@link CitationSet}; event and anomaly ids by their {@code id}
 * members. A citation is valid only when it names one of these exactly:
 *
 * <ul>
 *   <li>{@code source:externalId@seq} — that record version was returned;
 *   <li>{@code source:externalId} — some version of that record was returned;
 *   <li>{@code alert:<id>} — that alert was returned;
 *   <li>{@code event:<id>} or {@code anomaly:<id>} — an object with that {@code id} was returned;
 *   <li>a bare id — an object with exactly that {@code id} was returned.
 * </ul>
 *
 * <p>Safe for the parallel tool calls of one loop turn.
 */
public final class AnalystCitations {

    private final CitationSet citable = new CitationSet();
    private final Set<String> ids = ConcurrentHashMap.newKeySet();
    private final List<CitableExtractor> extractors;

    /** A citation set fed by {@code extractors}. */
    public AnalystCitations(List<CitableExtractor> extractors) {
        this.extractors = List.copyOf(extractors);
    }

    /** Records what tool {@code tool} returned (after egress filtering). */
    public void accept(String tool, JsonNode shown) {
        if (shown == null) {
            return;
        }
        for (CitableExtractor extractor : extractors) {
            extractor.extract(tool, shown, citable);
        }
        collect(shown);
    }

    /** True when {@code citation} names, exactly, an item a tool returned in this run. */
    public boolean valid(String citation) {
        if (citation == null) {
            return false;
        }
        String id = citation.strip();
        if (id.startsWith("[") && id.endsWith("]")) {
            id = id.substring(1, id.length() - 1).strip();
        }
        if (id.isEmpty()) {
            return false;
        }
        String lower = id.toLowerCase(Locale.ROOT);
        if (lower.startsWith("alert:")) {
            return citable.hasAlert(id.substring("alert:".length()));
        }
        if (lower.startsWith("event:")) {
            return ids.contains(id.substring("event:".length()));
        }
        if (lower.startsWith("anomaly:")) {
            return ids.contains(id.substring("anomaly:".length()));
        }
        int at = id.lastIndexOf('@');
        if (at > 0 && id.indexOf(':') > 0 && id.indexOf(':') < at) {
            String record = id.substring(0, at);
            try {
                return citable.hasRecord(record, Long.parseLong(id.substring(at + 1)));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (id.indexOf(':') > 0 && citable.latestSeq(id).isPresent()) {
            return true;
        }
        return ids.contains(id);
    }

    private void collect(JsonNode node) {
        if (node.isArray()) {
            for (JsonNode child : node) {
                collect(child);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        for (String member : List.of("id", "eventId")) {
            JsonNode value = node.get(member);
            if (value != null && (value.isString() || value.isIntegralNumber())) {
                ids.add(value.asString());
            }
        }
        for (JsonNode child : node) {
            if (child.isContainer()) {
                collect(child);
            }
        }
    }
}
