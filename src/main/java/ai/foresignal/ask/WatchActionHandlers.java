package ai.foresignal.ask;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.trigger.RuleValidator;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The watch actions of Ask (prompt 028): {@code create_watch} ({@code createTrigger}) and
 * {@code update_watch} ({@code updateTrigger}). A proposal is validated by the same
 * {@link RuleValidator} as {@code POST /v1/triggers}; execution goes through
 * {@link TriggerRuleService}, which validates again.
 */
@Configuration
public class WatchActionHandlers {

    /** {@code create_watch}: payload {@code {rule}}. */
    @Bean
    public AskActionHandler createWatchActionHandler(RuleValidator validator, TriggerRuleService rules,
            TriggerMapper mapper, JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "create_watch";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                JsonNode rule = rule(payload);
                validator.validate(rule);
                ObjectNode out = json.createObjectNode();
                out.set("rule", rule);
                return out;
            }

            @Override
            public String describe(JsonNode payload) {
                return "Create the watch '" + name(payload) + "'";
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                return json.valueToTree(mapper.rule(rules.create(rule(payload))));
            }
        };
    }

    /** {@code update_watch}: payload {@code {watchId, rule}}; replaces the whole definition. */
    @Bean
    public AskActionHandler updateWatchActionHandler(RuleValidator validator, TriggerRuleService rules,
            TriggerMapper mapper, JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "update_watch";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                UUID id = watchId(payload);
                rules.load(id);
                JsonNode rule = rule(payload);
                validator.validate(rule);
                ObjectNode out = json.createObjectNode();
                out.put("watchId", id.toString());
                out.set("rule", rule);
                return out;
            }

            @Override
            public String describe(JsonNode payload) {
                return "Replace the definition of watch " + payload.path("watchId").asString() + " with '"
                        + name(payload) + "'";
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                return json.valueToTree(mapper.rule(rules.update(watchId(payload), rule(payload))));
            }
        };
    }

    private static JsonNode rule(JsonNode payload) {
        JsonNode rule = payload == null ? null : payload.get("rule");
        if (rule == null || !rule.isObject()) {
            throw ValidationException.forField("rule", "is required and must be a watch definition object");
        }
        return rule;
    }

    private static UUID watchId(JsonNode payload) {
        JsonNode id = payload == null ? null : payload.get("watchId");
        try {
            return UUID.fromString(id == null || !id.isString() ? "" : id.asString());
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("watchId", "must be a watch id (UUID)");
        }
    }

    private static String name(JsonNode payload) {
        String name = payload.path("rule").path("name").asString("");
        return name.isBlank() ? "unnamed" : name;
    }
}
