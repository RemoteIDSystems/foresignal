package ai.foresignal.ask;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.mcp.tools.AlertItems;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Snoozes;
import java.time.Clock;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The alert actions of Ask (prompt 028), validated and executed like the REST alert lifecycle of
 * prompt 017:
 *
 * <ul>
 *   <li>{@code acknowledge_alert} ({@code acknowledgeAlert}): payload {@code {alertId, note?, feedback?}};
 *   <li>{@code alert_status}: payload {@code {alertId, action, …}} with {@code action} {@code resolve}
 *       ({@code resolveAlert}, with {@code note}/{@code feedback}), {@code snooze} ({@code snoozeAlert},
 *       with {@code until} or {@code for}) or {@code unsnooze} ({@code unsnoozeAlert}).
 * </ul>
 * Prompt 033 adds {@code snooze_alert} ({@code snoozeAlert}): payload {@code {alertId, until | for}},
 * proposed by {@code propose_snooze}. An alert on a source the caller may not see is not found, as
 * on REST.
 */
@Configuration
public class AlertActionHandlers {

    /** The {@code alert_status} actions. */
    static final Set<String> STATUS_ACTIONS = Set.of("resolve", "snooze", "unsnooze");

    /** {@code acknowledge_alert}. */
    @Bean
    public AskActionHandler acknowledgeAlertActionHandler(AlertService alerts, AlertItems items, AlertMapper mapper,
            JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "acknowledge_alert";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources());
                ObjectNode body = transitionBody(json, payload);
                AlertService.transition(body, false);
                body.put("alertId", id);
                return body;
            }

            @Override
            public String describe(JsonNode payload) {
                return "Acknowledge alert " + payload.path("alertId").asString();
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources());
                AlertView alert = alerts.acknowledge(id, AlertService.transition(transitionBody(json, payload), false),
                        caller.actor());
                return json.valueToTree(mapper.alert(alert, null));
            }
        };
    }

    /** {@code alert_status}. */
    @Bean
    public AskActionHandler alertStatusActionHandler(AlertService alerts, AlertItems items, AlertMapper mapper,
            JsonMapper json, Clock clock) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "alert_status";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                String action = action(payload);
                items.visible(id, caller.excludedSources());
                check(action, payload);
                ObjectNode out = json.createObjectNode();
                out.put("alertId", id);
                out.put("action", action);
                if (action.equals("snooze")) {
                    Snoozes.expiry(snoozeBody(json, payload), clock.instant(), null);
                    copy(payload, out, "until");
                    copy(payload, out, "for");
                } else if (action.equals("resolve")) {
                    AlertService.transition(transitionBody(json, payload), false);
                    copy(payload, out, "note");
                    copy(payload, out, "feedback");
                }
                return out;
            }

            @Override
            public String describe(JsonNode payload) {
                String action = payload.path("action").asString("");
                String alert = payload.path("alertId").asString();
                return switch (action) {
                    case "resolve" -> "Resolve alert " + alert;
                    case "snooze" -> "Snooze alert " + alert + (payload.hasNonNull("until")
                            ? " until " + payload.get("until").asString()
                            : " for " + payload.path("for").asString());
                    case "unsnooze" -> "End the snooze of alert " + alert;
                    default -> "Change the status of alert " + alert;
                };
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                String action = action(payload);
                items.visible(id, caller.excludedSources());
                check(action, payload);
                AlertView alert = switch (action) {
                    case "resolve" -> alerts.resolve(id, AlertService.transition(transitionBody(json, payload), false),
                            caller.actor());
                    case "snooze" -> alerts.snooze(id, snoozeBody(json, payload));
                    default -> alerts.unsnooze(id);
                };
                return json.valueToTree(mapper.alert(alert, null));
            }
        };
    }

    /** {@code snooze_alert} (prompt 033): payload {@code {alertId, until | for}} ({@code snoozeAlert}). */
    @Bean
    public AskActionHandler snoozeAlertActionHandler(AlertService alerts, AlertItems items, AlertMapper mapper,
            JsonMapper json, Clock clock) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "snooze_alert";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources());
                Snoozes.expiry(snoozeBody(json, payload), clock.instant(), null);
                ObjectNode out = snoozeBody(json, payload);
                out.put("alertId", id);
                return out;
            }

            @Override
            public String describe(JsonNode payload) {
                String alert = payload.path("alertId").asString();
                return "Snooze alert " + alert + (payload.hasNonNull("until")
                        ? " until " + payload.get("until").asString() : " for " + payload.path("for").asString());
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                long id = alertId(payload);
                items.visible(id, caller.excludedSources());
                AlertView alert = alerts.snooze(id, snoozeBody(json, payload));
                return json.valueToTree(mapper.alert(alert, null));
            }
        };
    }

    private static long alertId(JsonNode payload) {
        JsonNode id = payload == null ? null : payload.get("alertId");
        if (id == null || !id.isIntegralNumber() || id.asLong() < 1) {
            throw ValidationException.forField("alertId", "must be an alert id");
        }
        return id.asLong();
    }

    private static String action(JsonNode payload) {
        String action = payload.path("action").asString("");
        if (!STATUS_ACTIONS.contains(action)) {
            throw ValidationException.forField("action", "must be resolve, snooze or unsnooze");
        }
        return action;
    }

    private static void check(String action, JsonNode payload) {
        boolean transition = action.equals("resolve");
        if (!transition && (payload.hasNonNull("note") || payload.hasNonNull("feedback"))) {
            throw ValidationException.forField("note", "note and feedback apply to resolve only");
        }
        if (!action.equals("snooze") && (payload.hasNonNull("until") || payload.hasNonNull("for"))) {
            throw ValidationException.forField("until", "until and for apply to snooze only");
        }
    }

    /** The {@code AlertTransitionInput} body of a payload. */
    static ObjectNode transitionBody(JsonMapper json, JsonNode payload) {
        ObjectNode body = json.createObjectNode();
        copy(payload, body, "note");
        copy(payload, body, "feedback");
        return body;
    }

    /** The {@code SnoozeInput} body of a payload. */
    static ObjectNode snoozeBody(JsonMapper json, JsonNode payload) {
        ObjectNode body = json.createObjectNode();
        copy(payload, body, "until");
        copy(payload, body, "for");
        return body;
    }

    private static void copy(JsonNode from, ObjectNode to, String member) {
        if (from != null && from.hasNonNull(member)) {
            to.set(member, from.get(member));
        }
    }
}
