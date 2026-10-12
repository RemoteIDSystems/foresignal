package ai.foresignal.briefing;

import ai.foresignal.ask.AskActionHandler;
import ai.foresignal.ask.AskCaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The briefing action of Ask (prompts 028 and 029): {@code create_briefing}, which also turns on the
 * {@code propose_briefing} tool. A proposal is validated by the same {@link BriefingValidator} as
 * {@code POST /v1/briefings}; execution goes through {@link BriefingService#create}, which validates
 * again, with the confirming token as the owner.
 */
@Configuration
public class BriefingActionHandlers {

    /** {@code create_briefing}: payload is the {@code BriefingInput}. */
    @Bean
    public AskActionHandler createBriefingActionHandler(BriefingValidator validator, BriefingService briefings,
            JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "create_briefing";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                validator.validate(payload, caller.excludedSources());
                return payload;
            }

            @Override
            public String describe(JsonNode payload) {
                String name = payload == null ? "" : payload.path("name").asString("");
                String schedule = payload == null ? "" : payload.path("schedule").asString("");
                String timezone = payload == null ? "" : payload.path("timezone").asString("UTC");
                return "Create the briefing '" + (name.isBlank() ? "unnamed" : name) + "' on schedule '" + schedule
                        + "' (" + (timezone.isBlank() ? "UTC" : timezone) + ")";
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                return json.valueToTree(briefings.create(payload, caller.mcp()));
            }
        };
    }
}
