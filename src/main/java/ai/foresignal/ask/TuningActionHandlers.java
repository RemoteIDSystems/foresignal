package ai.foresignal.ask;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.mcp.tools.AlertItems;
import ai.foresignal.quality.TuningSuggestionEntity;
import ai.foresignal.quality.TuningWorkflowService;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The alert-quality actions of Ask (prompt 115), proposed actions (FR-46, EGR-3) executed only on confirmation or
 * for an agent caller that set {@code allowActions}:
 *
 * <ul>
 *   <li>{@code set_alert_feedback} ({@code setAlertFeedback}): payload {@code {alertId, feedback, note?}};
 *   <li>{@code apply_tuning_suggestion} ({@code applyTuningSuggestion}): payload {@code {ruleId, suggestionId,
 *       note?}}; the stored proposal carries the suggestion's patch verbatim, so the person confirming sees exactly
 *       what changes.
 * </ul>
 */
@Configuration
public class TuningActionHandlers {

    /** {@code set_alert_feedback}. */
    @Bean
    public AskActionHandler setAlertFeedbackActionHandler(AlertService alerts, AlertItems items, AlertMapper mapper,
            JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "set_alert_feedback";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources(), caller.restriction());
                ObjectNode body = AlertActionHandlers.transitionBody(json, payload);
                AlertService.transition(body, true);
                body.put("alertId", id);
                return body;
            }

            @Override
            public String describe(JsonNode payload) {
                return "Mark alert " + payload.path("alertId").asString() + " as "
                        + payload.path("feedback").asString("");
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources(), caller.restriction());
                AlertView alert = alerts.setFeedback(id,
                        AlertService.transition(AlertActionHandlers.transitionBody(json, payload), true));
                return json.valueToTree(mapper.alert(alert, null));
            }
        };
    }

    /** {@code apply_tuning_suggestion}. */
    @Bean
    public AskActionHandler applyTuningSuggestionActionHandler(TuningWorkflowService tuning, JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "apply_tuning_suggestion";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                UUID ruleId = ruleId(payload);
                String suggestionId = text(payload, "suggestionId", true);
                TuningSuggestionEntity suggestion = tuning.suggestion(ruleId, suggestionId);
                if (!TuningSuggestionEntity.OPEN.equals(suggestion.getStatus())) {
                    throw ValidationException.forField("suggestionId", "the suggestion is "
                            + suggestion.getStatus() + ", not OPEN");
                }
                ObjectNode out = json.createObjectNode();
                out.put("ruleId", ruleId.toString());
                out.put("suggestionId", suggestionId);
                String note = text(payload, "note", false);
                if (note != null) {
                    out.put("note", note);
                }
                // The patch verbatim, for the person who confirms.
                out.put("kind", suggestion.getKind());
                out.set("patch", json.valueToTree(suggestion.getPatch()));
                out.put("ruleRev", suggestion.getRuleRev());
                return out;
            }

            @Override
            public String describe(JsonNode payload) {
                return "Apply tuning suggestion " + payload.path("suggestionId").asString() + " ("
                        + payload.path("kind").asString("") + ") to rule " + payload.path("ruleId").asString()
                        + ": patch " + payload.path("patch");
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                return json.valueToTree(TuningWorkflowService.view(tuning.apply(ruleId(payload),
                        text(payload, "suggestionId", true), text(payload, "note", false), caller.actor())));
            }
        };
    }

    private static long alertId(JsonNode payload) {
        JsonNode id = payload == null ? null : payload.get("alertId");
        if (id == null || !id.canConvertToLong() || id.asLong() < 1) {
            throw ValidationException.forField("alertId", "must be an alert id");
        }
        return id.asLong();
    }

    private static UUID ruleId(JsonNode payload) {
        try {
            return UUID.fromString(text(payload, "ruleId", true));
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("ruleId", "must be a rule id (UUID)");
        }
    }

    private static String text(JsonNode payload, String field, boolean required) {
        JsonNode node = payload == null ? null : payload.get(field);
        if (node == null || node.isNull()) {
            if (required) {
                throw ValidationException.forField(field, "is required");
            }
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        return node.asString();
    }
}
