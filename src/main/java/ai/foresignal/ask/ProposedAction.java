package ai.foresignal.ask;

import java.util.Objects;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * An action a model proposed during one Ask, validated but not yet stored (prompt 028). It is stored
 * {@code pending} with the answer, so a failed Ask leaves no actions behind.
 *
 * @param id the action id the model was told
 * @param type the {@code AskAction.type}
 * @param description what it would do
 * @param payload the validated payload
 */
public record ProposedAction(UUID id, String type, String description, JsonNode payload) {

    /** Requires every member. */
    public ProposedAction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(payload, "payload");
    }
}
