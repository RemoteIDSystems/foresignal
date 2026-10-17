package ai.foresignal.evidence;

import java.util.ArrayList;
import java.util.List;

/**
 * The payload fields of a source that carry the upstream's own event id or the cited original source
 * (prompt 116).
 *
 * @param eventIds the event id fields with their namespaces
 * @param citations the fields carrying a cited original source (a URL, domain or name)
 */
public record UpstreamRefs(List<EventIdField> eventIds, List<String> citations) {

    /** No references. */
    public static final UpstreamRefs NONE = new UpstreamRefs(List.of(), List.of());

    /** The pseudo-field naming the record's external id. */
    public static final String EXTERNAL_ID = "$externalId";

    /** The prefix of a citation field. */
    public static final String CITE = "cite:";

    /** Copies the lists. */
    public UpstreamRefs {
        eventIds = eventIds == null ? List.of() : List.copyOf(eventIds);
        citations = citations == null ? List.of() : List.copyOf(citations);
    }

    /**
     * An upstream event id field: values of {@code field} are compared within {@code namespace}, so two sources
     * that carry the same identifier in differently named fields still match.
     *
     * @param field the payload field, or {@link #EXTERNAL_ID}
     * @param namespace the namespace values are compared in
     */
    public record EventIdField(String field, String namespace) {}

    /**
     * Parses the descriptor and registry notation: {@code field}, {@code field@namespace} (the namespace
     * defaults to the field name) and {@code cite:field}.
     */
    public static UpstreamRefs parse(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return NONE;
        }
        List<EventIdField> ids = new ArrayList<>();
        List<String> citations = new ArrayList<>();
        for (String raw : entries) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String entry = raw.strip();
            if (entry.startsWith(CITE)) {
                citations.add(entry.substring(CITE.length()));
                continue;
            }
            int at = entry.indexOf('@');
            String field = at < 0 ? entry : entry.substring(0, at);
            String namespace = at < 0 ? field : entry.substring(at + 1);
            ids.add(new EventIdField(field, namespace));
        }
        return new UpstreamRefs(ids, citations);
    }

    /** True when nothing is declared. */
    public boolean isEmpty() {
        return eventIds.isEmpty() && citations.isEmpty();
    }
}
