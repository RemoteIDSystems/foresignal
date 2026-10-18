package ai.foresignal.cli.sse;

/**
 * One dispatched Server-Sent Event.
 *
 * @param event the {@code event:} name ({@code message} when the server sent none)
 * @param id    the {@code id:} of this event, or {@code null} when the event carried none (Foresignal's {@code live},
 *              {@code touch}, {@code resync}, {@code reset} and {@code heartbeat} events have no id)
 * @param data  the {@code data:} lines joined with {@code \n}
 * @param retry the last {@code retry:} value seen on the stream in milliseconds, or {@code null}
 */
public record SseEvent(String event, String id, String data, Long retry) {
}
