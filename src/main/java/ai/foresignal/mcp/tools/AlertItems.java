package ai.foresignal.mcp.tools;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** Shared alert handling of the alert tools: visibility and the short and long alert forms. */
@Component
public class AlertItems {

    /** Members of a short alert, in order. */
    static final List<String> SHORT_MEMBERS = List.of("id", "ruleId", "ruleName", "severity", "state", "firedAt",
            "observedAt", "seq", "kind", "acknowledgedAt", "acknowledgedBy", "resolvedAt", "resolvedBy", "feedback",
            "snoozedUntil", "suppressed");

    private final AlertService alerts;
    private final AlertMapper mapper;
    private final RecordMapper records;
    private final RecordQueryService queries;
    private final McpOutputShaper shaper;

    /** Creates the helper. */
    public AlertItems(AlertService alerts, AlertMapper mapper, RecordMapper records, RecordQueryService queries,
            McpOutputShaper shaper) {
        this.alerts = alerts;
        this.mapper = mapper;
        this.records = records;
        this.queries = queries;
        this.shaper = shaper;
    }

    /**
     * The alert, when its source is visible to the caller.
     *
     * @throws NotFoundException when it does not exist or its source is hidden
     */
    public AlertView visible(long id, Set<String> excluded) {
        AlertView alert = alerts.get(id);
        if (excluded.contains(alert.sourceId())) {
            throw NotFoundException.of("Alert", id);
        }
        return alert;
    }

    /** An alert as a list item (no record). */
    public ObjectNode item(AlertView alert, Detail detail) {
        ObjectNode full = shaper.objectTree(mapper.alert(alert, null));
        if (detail == Detail.LONG) {
            return full;
        }
        return compact(full, alert);
    }

    /**
     * An alert with its record: the matching version loaded by seq while it is retained, otherwise
     * the alert's {@code recordSnapshot} ({@code recordFrom} says which). Both forms carry the
     * enrichment {@code summary}, {@code enrichedAt} and {@code summaryCitations} (prompt 030).
     */
    public ObjectNode withRecord(AlertView alert, Detail detail) {
        RecordRow row = queries.versionsBySeq(List.of(alert.seq())).get(alert.seq());
        ObjectNode full = shaper.objectTree(mapper.alert(alert, null));
        ObjectNode out = detail == Detail.LONG ? full : compact(full, alert);
        JsonNode snapshot = full.get("recordSnapshot");
        if (row != null) {
            Map<String, Object> record = records.record(row, FieldSelection.ALL, RecordMapper.Shape.VERSION);
            out.set("record", shaper.record(record, detail));
            out.put("recordFrom", "version");
        } else if (snapshot != null && snapshot.isObject()) {
            out.set("record", detail == Detail.LONG ? snapshot : shaper.shortRecord((ObjectNode) snapshot.deepCopy()));
            out.put("recordFrom", "snapshot");
        } else {
            out.putNull("record");
            out.put("recordFrom", "none");
        }
        if (detail == Detail.SHORT) {
            out.remove("recordSnapshot");
            // The enrichment summary and its citations (prompt 030) are part of get_alert's short form.
            for (String member : List.of("summary", "enrichedAt", "summaryCitations")) {
                JsonNode value = full.get(member);
                if (value != null && !value.isNull()) {
                    out.set(member, value);
                }
            }
        }
        return out;
    }

    private ObjectNode compact(ObjectNode full, AlertView alert) {
        ObjectNode out = shaper.object();
        for (String member : SHORT_MEMBERS) {
            JsonNode value = full.get(member);
            if (value != null && !value.isNull()) {
                out.set(member, value);
            }
        }
        out.put("record", alert.sourceId() + ":" + alert.externalId());
        String message = alert.message() == null ? "" : alert.message();
        out.put("message", message.length() > McpOutputShaper.MAX_TEXT_CHARS
                ? message.substring(0, McpOutputShaper.MAX_TEXT_CHARS - 1) + "…" : message);
        return out;
    }
}
