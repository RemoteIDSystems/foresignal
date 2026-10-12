package ai.foresignal.ask;

import ai.foresignal.source.SourceRegistry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The records and alerts in a tool result (prompt 028), walked recursively:
 *
 * <ul>
 *   <li>a search hit: {@code docType} + {@code docId} (+ {@code seq} for a record);
 *   <li>an alert: a numeric {@code id} with a {@code ruleId}; its {@code seq} also cites the record
 *       version that fired it ({@code record} or {@code source}/{@code externalId});
 *   <li>a record: {@code id} = {@code source:externalId} of a registered source, or
 *       {@code source} + {@code externalId}, with an integer {@code seq} (the persisted version seq,
 *       Core-Data-Model §12).
 * </ul>
 */
@Component
public class RecordAlertExtractor implements CitableExtractor {

    private final SourceRegistry registry;

    /** Creates the extractor. */
    public RecordAlertExtractor(SourceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void extract(String tool, JsonNode output, CitationSet citable) {
        walk(output, citable);
    }

    private void walk(JsonNode node, CitationSet citable) {
        if (node == null) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                walk(child, citable);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        visit(node, citable);
        for (JsonNode child : node) {
            if (child.isContainer()) {
                walk(child, citable);
            }
        }
    }

    private void visit(JsonNode object, CitationSet citable) {
        String title = text(object, "title");
        JsonNode docType = object.get("docType");
        JsonNode docId = object.get("docId");
        if (docType != null && docType.isString() && docId != null && docId.isString()) {
            switch (docType.asString()) {
                case AskCitation.RECORD -> {
                    if (isSeq(object.get("seq"))) {
                        citable.addRecord(docId.asString(), object.get("seq").asLong(), title);
                    }
                }
                case AskCitation.ALERT -> citable.addAlert(docId.asString(), title);
                case AskCitation.BRIEFING -> citable.addBriefing(docId.asString(), title);
                default -> {
                    // Other document types become citable through their own extractor.
                }
            }
            return;
        }
        JsonNode id = object.get("id");
        if (id != null && id.isIntegralNumber() && object.has("ruleId")) {
            String alertTitle = text(object, "ruleName");
            citable.addAlert(id.asString(), alertTitle != null ? alertTitle : text(object, "message"));
            String record = recordId(object);
            if (record != null && isSeq(object.get("seq"))) {
                citable.addRecord(record, object.get("seq").asLong(), null);
            }
            return;
        }
        if (!isSeq(object.get("seq"))) {
            return;
        }
        String record = recordId(object);
        if (record != null) {
            citable.addRecord(record, object.get("seq").asLong(), title);
        }
    }

    /** {@code source:externalId} of an object, when it names a registered source. */
    private String recordId(JsonNode object) {
        String source = text(object, "source");
        String externalId = text(object, "externalId");
        if (source != null && externalId != null && registry.contains(source)) {
            return source + ":" + externalId;
        }
        for (String member : new String[] {"id", "record"}) {
            String value = text(object, member);
            if (value != null) {
                int colon = value.indexOf(':');
                if (colon > 0 && colon < value.length() - 1 && registry.contains(value.substring(0, colon))) {
                    return value;
                }
            }
        }
        return null;
    }

    private static boolean isSeq(JsonNode node) {
        return node != null && node.isIntegralNumber() && node.asLong() > 0;
    }

    private static String text(JsonNode object, String member) {
        JsonNode value = object.get(member);
        return value != null && value.isString() && !value.asString().isBlank() ? value.asString() : null;
    }
}
