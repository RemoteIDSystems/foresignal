package ai.foresignal.ask;

import tools.jackson.databind.JsonNode;

/**
 * One kind of action a model may propose in Ask (prompt 028; invariant EGR-3). Declare an
 * implementation as a Spring bean: prompt 029 adds {@code create_briefing} (which also turns on the
 * {@code propose_briefing} tool), 033 {@code snooze_alert}, 036 {@code create_view}.
 *
 * <p>A handler validates a payload exactly as the REST operation it stands for would, when the model
 * proposes it and again when it is executed, and executes it through the same services as REST.
 */
public interface AskActionHandler {

    /** The {@code AskAction.type}. */
    String type();

    /**
     * Validates a proposed payload.
     *
     * @param payload the payload the model gave
     * @param caller who asked
     * @return the payload to store (normalized where useful)
     * @throws ai.foresignal.api.error.ForesignalException when it is not valid (validation, not found)
     */
    JsonNode validate(JsonNode payload, AskCaller caller);

    /** A one-line description of what executing {@code payload} would do, for people. */
    String describe(JsonNode payload);

    /**
     * Executes a confirmed payload.
     *
     * @param payload the stored payload
     * @param caller who confirmed (an {@code agent} or {@code admin} token)
     * @return the result, as the REST operation returns it
     * @throws ai.foresignal.api.error.ForesignalException when it cannot be executed
     */
    JsonNode execute(JsonNode payload, AskCaller caller);
}
