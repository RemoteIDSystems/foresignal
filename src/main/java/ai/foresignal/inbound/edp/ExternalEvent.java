package ai.foresignal.inbound.edp;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One event of the SSE topic {@code external} (prompt 138): {@value #SOURCE_STATE} ({@code ExternalSourceStateEvent})
 * or {@value #BINDING_DISABLED} ({@code ExternalBindingEvent}).
 *
 * @param type the event name
 * @param sourceId the provider the event is about
 * @param payload the body
 */
public record ExternalEvent(String type, String sourceId, Map<String, Object> payload) {

    /** A provider was registered, approved, suspended, deleted or its manifest changed. */
    public static final String SOURCE_STATE = "external.source.state";

    /** A binding was disabled (a breaking manifest update changed its stream). */
    public static final String BINDING_DISABLED = "external.binding.disabled";

    /** Copies the payload. */
    public ExternalEvent {
        payload = payload == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }

    /** The {@code external.source.state} event of a lifecycle change ({@code state} {@code deleted} once deleted). */
    public static ExternalEvent sourceState(ExternalSourceChangedEvent change) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sourceId", change.sourceId());
        body.put("state", change.state() != null ? change.state() : ExternalSourceChangedEvent.DELETED);
        body.put("change", change.change());
        body.put("at", change.at().toString());
        return new ExternalEvent(SOURCE_STATE, change.sourceId(), body);
    }

    /** The {@code external.binding.disabled} event. */
    public static ExternalEvent bindingDisabled(String bindingId, String sourceId, String reason, Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bindingId", bindingId);
        body.put("sourceId", sourceId);
        body.put("reason", reason);
        body.put("at", at.toString());
        return new ExternalEvent(BINDING_DISABLED, sourceId, body);
    }
}
