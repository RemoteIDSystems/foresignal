package ai.foresignal.api.mapper;

import ai.foresignal.api.support.RawJson;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps alerts to the {@code Alert} and {@code AlertEvent} schemas (prompt 017). Prompt 030 adds the
 * enrichment members {@code summary}, {@code enrichedAt} and {@code summaryCitations} (an empty
 * array until the alert is enriched). Prompt 040 adds {@code region}. Prompt 036 adds {@code sensitive} and {@code context}
 * ({@code near} place versions, {@code anchor} distance). Prompt 056 adds {@code context.weather}; list items
 * ({@link #listItem}) carry only {@code context.weather.summary} unless {@code include=context}.
 */
@Component
public class AlertMapper {

    private static final tools.jackson.databind.json.JsonMapper JSON = tools.jackson.databind.json.JsonMapper.builder()
            .build();

    /**
     * The {@code Alert} object of a list item (prompt 056): with {@code fullContext} the whole {@code context};
     * otherwise {@code context} is {@code {"weather": {"summary": …}}} when the alert has a weather context, else
     * null.
     *
     * @param alert the alert
     * @param fullContext true for {@code include=context}
     */
    public Map<String, Object> listItem(AlertView alert, boolean fullContext) {
        Map<String, Object> out = alert(alert, null);
        if (!fullContext) {
            out.put("context", summaryOnly(alert.context()));
        }
        return out;
    }

    /** {@code {"weather": {"summary": …}}} of a stored context, or null without a weather summary. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> summaryOnly(String context) {
        if (context == null || context.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> map = JSON.readValue(context, Map.class);
            if (map != null && map.get("weather") instanceof Map<?, ?> weather && weather.get("summary") != null) {
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("summary", weather.get("summary"));
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("weather", w);
                return out;
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    /**
     * The {@code Alert} object of {@code alert}.
     *
     * @param alert the alert
     * @param record the {@code Record} of its version when still retained (getAlert only), or null
     */
    public Map<String, Object> alert(AlertView alert, Map<String, Object> record) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", alert.id());
        out.put("ruleId", alert.ruleId());
        out.put("ruleName", alert.ruleName());
        out.put("seq", alert.seq());
        out.put("source", alert.sourceId());
        out.put("externalId", alert.externalId());
        out.put("kind", alert.kind());
        out.put("firedAt", alert.firedAt());
        out.put("observedAt", alert.observedAt());
        out.put("message", alert.message() == null ? "" : alert.message());
        out.put("severity", alert.severity());
        out.put("state", alert.state());
        out.put("rev", alert.rev());
        out.put("acknowledgedAt", alert.acknowledgedAt());
        out.put("acknowledgedBy", alert.acknowledgedBy());
        out.put("resolvedAt", alert.resolvedAt());
        out.put("resolvedBy", alert.resolvedBy());
        out.put("resolvedSeq", alert.resolvedSeq());
        out.put("feedback", alert.feedback());
        out.put("feedbackAt", alert.feedbackAt());
        out.put("note", alert.note());
        out.put("snoozedUntil", alert.snoozedUntil());
        out.put("suppressed", alert.suppressed());
        out.put("suppressedReason", alert.suppressedReason());
        out.put("tags", alert.tags());
        out.put("recordSnapshot", RawJson.of(alert.recordSnapshot()));
        out.put("summary", alert.summary());
        out.put("enrichedAt", alert.enrichedAt());
        out.put("summaryCitations", alert.summaryCitations() == null ? List.of() : RawJson.of(alert.summaryCitations()));
        out.put("sensitive", alert.sensitive());
        out.put("context", alert.context() == null ? null : RawJson.of(alert.context()));
        // The record's region tags at fire time (prompt 040), with the GeoNames credit when near is set.
        out.put("region", alert.region() == null ? null : RawJson.of(alert.region()));
        // Exercise replay alerts (prompt 115).
        out.put("exercise", alert.exercise());
        out.put("exerciseRunId", alert.exerciseRunId());
        if (record != null) {
            out.put("record", record);
        }
        return out;
    }

    /** The {@code AlertEvent} object of {@code event}. */
    public Map<String, Object> event(AlertEvent event) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", event.type());
        out.put("alert", alert(event.alert(), null));
        out.put("suppressed", event.suppressed());
        out.put("exercise", event.alert().exercise());
        return out;
    }
}
