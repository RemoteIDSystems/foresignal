package ai.foresignal.view;

import ai.foresignal.ask.AskActionHandler;
import ai.foresignal.ask.AskCaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The saved-view action of Ask (prompt 036): {@code create_view}, which turns on the
 * {@code propose_view} tool. A proposal is validated by the same {@link SavedViewService} parser as
 * {@code POST /v1/views} (slug uniqueness is checked again on execution).
 */
@Configuration
public class ViewActionHandlers {

    /** {@code create_view}: payload is the {@code SavedViewInput}. */
    @Bean
    public AskActionHandler createViewActionHandler(SavedViewService views, SavedViewMapper mapper, JsonMapper json) {
        return new AskActionHandler() {
            @Override
            public String type() {
                return "create_view";
            }

            @Override
            public JsonNode validate(JsonNode payload, AskCaller caller) {
                views.parse(payload);
                return payload;
            }

            @Override
            public String describe(JsonNode payload) {
                String name = payload == null ? "" : payload.path("name").asString("");
                String slug = payload == null ? "" : payload.path("slug").asString("");
                return "Create the saved view '" + (name.isBlank() ? slug : name) + "'";
            }

            @Override
            public JsonNode execute(JsonNode payload, AskCaller caller) {
                return json.valueToTree(mapper.view(views.create(payload)));
            }
        };
    }
}
