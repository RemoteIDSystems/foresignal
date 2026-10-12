package ai.foresignal.stream.sse;

import java.util.Map;
import java.util.Objects;

/**
 * One id-bearing event for a client's FIFO lane ({@code change}; later {@code alert},
 * {@code notification}).
 *
 * <p>{@code data} is this client's own copy, so the sender may add a loaded payload to it.
 *
 * @param topic the topic that produced it
 * @param cursor the topic cursor of the event (the change seq for {@code changes}); strictly
 *     increasing per topic, so a client drops anything at or below the cursor it already has
 * @param event the SSE event name
 * @param data the JSON object to send
 * @param payloadSeq when non-null, the sender loads this seq's payload with
 *     {@link EventTopic#loadPayloads} (batched {@code WHERE seq = ANY(?)}) and sets {@code payload}
 */
public record FifoEvent(String topic, long cursor, String event, Map<String, Object> data, Long payloadSeq) {

    /** Requires topic, event and data. */
    public FifoEvent {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(data, "data");
    }
}
