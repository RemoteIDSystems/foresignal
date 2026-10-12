package ai.foresignal.stream.sse;

import java.util.Objects;

/**
 * One server-sent event as it goes on the wire: an optional {@code id:}, an {@code event:} name and
 * one line of JSON {@code data:}.
 *
 * <p>Only FIFO events ({@code change}, later {@code alert} and {@code notification}) carry an id,
 * so {@code Last-Event-ID} always names a FIFO cursor. The live lane ({@code live}, {@code touch})
 * and the control events ({@code resync}, {@code heartbeat}, {@code reset}, {@code hello},
 * {@code filter-applied}) are built with {@link #control} and never have one.
 *
 * @param id the event id, or null for none
 * @param event the event name
 * @param data the JSON data, on one line
 */
public record SseFrame(String id, String event, String data) {

    /** Validates the frame: an event name is required and data must be one line. */
    public SseFrame {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(data, "data");
        if (data.indexOf('\n') >= 0 || data.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("SSE data must be a single line");
        }
        if (id != null && (id.indexOf('\n') >= 0 || id.indexOf('\r') >= 0)) {
            throw new IllegalArgumentException("SSE id must be a single line");
        }
    }

    /** A frame without an id. */
    public static SseFrame control(String event, String data) {
        return new SseFrame(null, event, data);
    }

    /** True when the frame carries an {@code id:} line. */
    public boolean hasId() {
        return id != null;
    }

    /** The frame in {@code text/event-stream} form, ending with the blank line. */
    public String wire() {
        StringBuilder out = new StringBuilder();
        if (id != null) {
            out.append("id:").append(id).append('\n');
        }
        out.append("event:").append(event).append('\n');
        out.append("data:").append(data).append('\n');
        out.append('\n');
        return out.toString();
    }
}
