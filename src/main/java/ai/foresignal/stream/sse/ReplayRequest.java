package ai.foresignal.stream.sse;

/**
 * A request to replay one topic's FIFO events from the database.
 *
 * @param after replay events with a cursor greater than this
 * @param checkFloor check the resume floor first (a resume or a gap); false when the stream starts
 *     live from now, where {@code after} is the head read just before the client registered
 */
public record ReplayRequest(long after, boolean checkFloor) {}
